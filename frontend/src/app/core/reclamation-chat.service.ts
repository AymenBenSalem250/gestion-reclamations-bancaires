import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// ─────────────────────────────────────────────────────────
// ✅ Message DTO — aligné avec backend Spring Boot
// ─────────────────────────────────────────────────────────
export interface ReclamationMessage {
  id?: number;
  message: string;
  createdAt: string;
  isAdmin: boolean;
  userId?: string;
  userFullName?: string;
  fileName?: string;
  fileType?: string;
  fileSize?: number;
}

@Injectable({
  providedIn: 'root'
})
export class ReclamationChatService {

  private readonly baseUrl = 'http://localhost:8081/api/reclamations';

  constructor(private http: HttpClient) {}

  /**
   * GET /api/reclamations/{refRec}/messages
   * Charge tous les messages d'une réclamation.
   */
  getMessages(refRec: string): Observable<ReclamationMessage[]> {
    return this.http.get<ReclamationMessage[]>(
      `${this.baseUrl}/${encodeURIComponent(refRec)}/messages`
    );
  }

  /**
   * POST /api/reclamations/{refRec}/messages
   * Envoie un nouveau message avec pièce jointe optionnelle via FormData.
   */
  sendMessage(refRec: string, message: string, file: File | null = null): Observable<ReclamationMessage> {
    const formData = new FormData();
    formData.append('message', message);
    if (file) {
      formData.append('file', file);
    }

    return this.http.post<ReclamationMessage>(
      `${this.baseUrl}/${encodeURIComponent(refRec)}/messages`,
      formData
    );
  }

  /**
   * Retourne l'URL directe pour télécharger/afficher une pièce jointe (déprécié, car non intercepté par JWT).
   */
  getFileUrl(refRec: string, fileName: string): string {
    return `${this.baseUrl}/${encodeURIComponent(refRec)}/messages/files/${encodeURIComponent(fileName)}`;
  }

  /**
   * Télécharge un fichier via HttpClient pour bénéficier de l'Interceptor JWT.
   */
  downloadFile(refRec: string, fileName: string): Observable<Blob> {
    return this.http.get(
      `${this.baseUrl}/${encodeURIComponent(refRec)}/messages/files/${encodeURIComponent(fileName)}`,
      { responseType: 'blob' }
    );
  }
}
