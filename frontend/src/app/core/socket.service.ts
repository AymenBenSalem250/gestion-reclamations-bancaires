import { Injectable } from '@angular/core';
import { Client, StompSubscription, Message } from '@stomp/stompjs';
import { Observable, Subject, filter, map } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class SocketService {
  private client: Client;
  private messageSubject = new Subject<{ topic: string, body: any }>();
  private connected = false;

  constructor(private authService: AuthService) {
    this.client = new Client({
      brokerURL: 'ws://localhost:8081/ws',
      reconnectDelay: 0,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      // No connectHeaders needed
      onStompError: (frame) => {
        console.warn('Broker reported error: ' + (frame.headers ? frame.headers['message'] : ''));
      },
      onWebSocketError: (event) => {
        console.warn('WebSocket error encountered');
      },
      onWebSocketClose: () => {
        this.connected = false;
      }
    });
  }

  private connectCallbacks: (() => void)[] = [];

  connect(onConnected?: () => void): void {
    if (onConnected) {
      if (this.connected) {
        onConnected();
        return;
      }
      this.connectCallbacks.push(onConnected);
    }

    if (this.connected || this.client.active) {
      return;
    }

    this.client.onConnect = () => {
      this.connected = true;
      while (this.connectCallbacks.length > 0) {
        const cb = this.connectCallbacks.shift();
        if (cb) cb();
      }
    };

    try {
      this.client.activate();
    } catch (err) {
      console.warn('Failed to activate WebSocket');
    }
  }

  disconnect(): void {
    if (this.client.active) {
      this.client.deactivate();
      this.connected = false;
    }
  }

  subscribeToTopic(topic: string): Observable<any> {
    return new Observable(observer => {
      let subscription: StompSubscription | null = null;
      
      const trySubscribe = () => {
        if (this.connected && this.client.active) {
          try {
            subscription = this.client.subscribe(topic, (message: Message) => {
              try {
                observer.next(JSON.parse(message.body));
              } catch (e) {
                // Fallback for non-JSON
                observer.next(message.body);
              }
            });
          } catch (err) {
            console.warn('Subscription failed');
          }
        }
      };

      if (this.connected) {
        trySubscribe();
      } else {
        // Try once after a short delay instead of looping infinitely
        setTimeout(trySubscribe, 1000);
      }

      return () => {
        if (subscription) {
          subscription.unsubscribe();
        }
      };
    });
  }
}
