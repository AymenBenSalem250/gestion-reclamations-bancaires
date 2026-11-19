import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ClientService {

  private baseUrl = 'http://localhost:8081/api/clients';

  constructor(private http: HttpClient) {}

  searchClientByAccount(numCpt: string): Observable<any> {
    return this.http.get(
      `${this.baseUrl}/search?numCpt=${numCpt}`
    );
  }
}