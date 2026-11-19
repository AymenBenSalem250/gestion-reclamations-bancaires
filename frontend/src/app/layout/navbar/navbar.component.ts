import { Component, EventEmitter, OnInit, OnDestroy, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { AuthService } from '../../core/auth.service';
import { SocketService } from '../../core/socket.service';
import { NotificationService, AppNotification } from '../../core/notification.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {

  @Output() menuToggle = new EventEmitter<void>();

  pageTitle = 'TSB Complaint Management System';
  isDarkMode = false;
  
  notifications: AppNotification[] = [];
  unreadCount = 0;
  showDropdown = false;
  pulse = false;
  
  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private socketService: SocketService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const savedTheme = localStorage.getItem('theme');
    this.isDarkMode = savedTheme === 'dark';
    
    if (this.isDarkMode) {
      document.body.classList.add('dark-mode');
    } else {
      document.body.classList.remove('dark-mode');
    }
    
    const userId = this.authService.getUserId();
    if (userId) {
      console.log("Header userId =", userId);
      this.loadNotifications();
      this.setupRealTimeNotifications(userId);
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadNotifications(): void {
    this.notificationService.getNotifications()
      .subscribe({
        next: (notifs) => {
          this.notifications = notifs;
          this.updateUnreadCount();
        },
        error: () => {
          // Gérer silencieusement (ex: 404)
        }
      });
  }

  private setupRealTimeNotifications(userId: string): void {
    try {
      this.socketService.connect(() => {
        console.log("Subscribed to topic:", `/topic/notifications/${userId}`);
        this.socketService.subscribeToTopic(`/topic/notifications/${userId}`)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: (notification: AppNotification) => {
              if (notification && !this.notifications.some(n => n.id === notification.id)) {
                this.notifications = [notification, ...this.notifications];
                this.updateUnreadCount();
                this.triggerPulse();
              }
            },
            error: () => {}
          });
      });
    } catch (err) {}
  }

  private updateUnreadCount(): void {
    this.unreadCount = this.notifications.filter(n => !n.isRead).length;
  }

  private triggerPulse(): void {
    this.pulse = true;
    setTimeout(() => this.pulse = false, 1000);
  }

  toggleDropdown(): void {
    this.showDropdown = !this.showDropdown;
  }

  handleNotificationClick(notification: AppNotification): void {
    if (!notification.isRead) {
      this.notificationService.markAsRead(notification.id).subscribe(() => {
        notification.isRead = true;
        this.updateUnreadCount();
      });
    }
    this.showDropdown = false;
    this.router.navigate(['/reclamation/messages', notification.refRec]);
  }

  onMenuToggle(): void {
    this.menuToggle.emit();
  }

  toggleDarkMode(): void {
    this.isDarkMode = !this.isDarkMode;
    if (this.isDarkMode) {
      document.body.classList.add('dark-mode');
      localStorage.setItem('theme', 'dark');
    } else {
      document.body.classList.remove('dark-mode');
      localStorage.setItem('theme', 'light');
    }
  }
}
