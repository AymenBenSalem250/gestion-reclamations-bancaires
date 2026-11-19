import {
  Component,
  OnInit,
  OnDestroy,
  AfterViewChecked,
  ViewChild,
  ElementRef,
  ChangeDetectionStrategy,
  ChangeDetectorRef
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { SocketService } from '../../core/socket.service';

import {
  ReclamationChatService,
  ReclamationMessage
} from '../../core/reclamation-chat.service';
import { ReclamationService } from '../../core/reclamation.service';
import { Reclamation } from '../../core/models';

import { PageHeaderComponent } from '../../shared/page-header/page-header.component';

@Component({
  selector: 'app-messages',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, PageHeaderComponent],
  templateUrl: './messages.component.html',
  styleUrls: ['../saisie/saisie.component.css', './messages.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MessagesComponent implements OnInit, OnDestroy, AfterViewChecked {

  @ViewChild('messagesScroll') private messagesScroll!: ElementRef<HTMLDivElement>;
  @ViewChild('fileInput') private fileInput!: ElementRef<HTMLInputElement>;

  // -- Données réclamation
  refRec = '';
  reclamation: Reclamation | null = null;
  loadingReclamation = false;

  // -- Messages
  messages: ReclamationMessage[] = [];
  loading = false;
  sending = false;
  error = '';
  sendError = '';
  readonly maxLength = 1000;

  // -- Form
  chatForm!: FormGroup;

  // -- Upload
  selectedFile: File | null = null;
  fileError = '';
  readonly MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

  // -- Emojis
  showEmojiPicker = false;
  emojis: string[] = ['😀', '😂', '😍', '🤔', '👍', '🙏', '👏', '🔥', '✅', '❌', '👀', '💡'];

  // -- Timestamps (stocke les index des messages dont l'heure est visible)
  selectedMessageId: number | undefined;

  // -- Fichiers distants (Blobs)
  fileUrls: { [fileName: string]: string } = {};
  loadingFiles: { [fileName: string]: boolean } = {};

  private shouldScrollToBottom = false;
  private destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private chatService: ReclamationChatService,
    private reclamationService: ReclamationService,
    private socketService: SocketService,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef
  ) {}

  // -- LIFECYCLE
  ngOnInit(): void {
    this.chatForm = this.fb.group({
      message: ['', [Validators.required, Validators.maxLength(this.maxLength)]]
    });

    this.refRec = this.route.snapshot.paramMap.get('refRec') ?? '';
    
    if (this.refRec) {
      this.loadReclamation();
      this.loadMessages();

      try {
        this.socketService.connect(() => {
          this.socketService.subscribeToTopic(`/topic/reclamation/${this.refRec}`)
            .pipe(takeUntil(this.destroy$))
            .subscribe({
              next: (msg: ReclamationMessage) => {
                if (msg && !this.messages.some(m => m.id === msg.id)) {
                  this.messages = [...this.messages, msg];
                  this.shouldScrollToBottom = true;
                  this.cdr.markForCheck();
                  
                  if (msg.fileName && msg.fileType?.startsWith('image/')) {
                    this.loadFile(msg);
                  }
                }
              },
              error: () => {}
            });
        });
      } catch (err) {}
    }
  }

  ngAfterViewChecked(): void {
    if (this.shouldScrollToBottom) {
      this.scrollToBottom();
      this.shouldScrollToBottom = false;
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.socketService.disconnect();

    // Nettoyage de la mémoire pour éviter les fuites (Memory Leak)
    Object.values(this.fileUrls).forEach(url => {
      URL.revokeObjectURL(url);
    });
  }

  // -- CHARGEMENT RÉCLAMATION
  loadReclamation(): void {
    this.loadingReclamation = true;
    this.reclamationService.getById(this.refRec).subscribe({
      next: rec => {
        this.reclamation = rec;
        this.loadingReclamation = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.loadingReclamation = false;
        this.cdr.markForCheck();
      }
    });
  }

  // -- CHARGEMENT MESSAGES
  loadMessages(): void {
    this.loading = true;
    this.error = '';

    this.chatService.getMessages(this.refRec).subscribe({
      next: msgs => {
        this.messages = msgs;
        this.loading = false;
        this.shouldScrollToBottom = true;
        this.cdr.markForCheck();

        // Précharger les miniatures d'images
        this.messages.forEach(msg => {
          if (msg.fileName && msg.fileType?.startsWith('image/')) {
            this.loadFile(msg);
          }
        });
      },
      error: () => {
        this.error = 'Impossible de charger les messages. Veuillez réessayer.';
        this.loading = false;
        this.cdr.markForCheck();
      }
    });
  }

  // -- ENVOI
  sendMessage(): void {
    if (this.chatForm.invalid && !this.selectedFile) return;
    if (this.sending) return;

    const text = (this.chatForm.get('message')!.value as string).trim();
    if (!text) return; // backend requires non-empty message

    this.sending = true;
    this.sendError = '';

    this.chatService.sendMessage(this.refRec, text, this.selectedFile).subscribe({
      next: msg => {
        // Le message est poussé uniquement par WebSocket
        this.chatForm.reset();
        this.removeFile();
        this.sending = false;
        this.shouldScrollToBottom = true;
        this.cdr.markForCheck();

        if (msg.fileName && msg.fileType?.startsWith('image/')) {
          this.loadFile(msg);
        }
      },
      error: (err) => {
        if (err.status === 400 && err.error?.errors) {
            this.sendError = err.error.errors.map((e: any) => e.message).join(', ');
        } else if (err.status === 400 && err.error?.message) {
            this.sendError = err.error.message;
        } else {
            this.sendError = "L'envoi du message a échoué. Veuillez réessayer.";
        }
        this.sending = false;
        this.cdr.markForCheck();
      }
    });
  }

  // -- GESTION FICHIERS
  triggerFileInput(): void {
    if (this.fileInput) {
      this.fileInput.nativeElement.click();
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      this.fileError = '';

      // Validation taille
      if (file.size > this.MAX_FILE_SIZE) {
        this.fileError = 'Le fichier est trop volumineux (max 5 MB).';
        this.selectedFile = null;
        input.value = '';
        this.cdr.markForCheck();
        return;
      }

      // Validation type
      if (!file.type.startsWith('image/') && file.type !== 'application/pdf') {
        this.fileError = 'Seuls les images et les PDF sont autorisés.';
        this.selectedFile = null;
        input.value = '';
        this.cdr.markForCheck();
        return;
      }

      this.selectedFile = file;
      this.cdr.markForCheck();
    }
  }

  removeFile(): void {
    this.selectedFile = null;
    this.fileError = '';
    if (this.fileInput) {
      this.fileInput.nativeElement.value = '';
    }
    this.cdr.markForCheck();
  }

  loadFile(msg: ReclamationMessage, download: boolean = false): void {
    if (!msg.fileName) return;

    // Si on l'a déjà en cache et qu'on ne veut pas télécharger explicitement un fichier lourd
    if (this.fileUrls[msg.fileName] && !download) {
      return;
    }

    // Cas du téléchargement d'un fichier existant en cache
    if (this.fileUrls[msg.fileName] && download) {
      this.triggerDownload(this.fileUrls[msg.fileName], msg.fileName);
      return;
    }

    this.loadingFiles[msg.fileName] = true;
    this.cdr.markForCheck();

    this.chatService.downloadFile(this.refRec, msg.fileName).subscribe({
      next: (blob: Blob) => {
        const url = URL.createObjectURL(blob);
        this.fileUrls[msg.fileName!] = url;
        this.loadingFiles[msg.fileName!] = false;
        this.cdr.markForCheck();

        if (download) {
          this.triggerDownload(url, msg.fileName!);
        }
      },
      error: () => {
        this.loadingFiles[msg.fileName!] = false;
        this.cdr.markForCheck();
      }
    });
  }

  private triggerDownload(url: string, fileName: string): void {
    const a = document.createElement('a');
    a.href = url;
    a.download = fileName.split('_').slice(1).join('_') || fileName;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  }

  getFileUrl(fileName: string): string {
    // Cette méthode est dépréciée pour l'affichage, mais conservée si besoin
    return this.chatService.getFileUrl(this.refRec, fileName);
  }

  formatFileSize(bytes?: number): string {
    if (!bytes) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
  }

  // -- GESTION EMOJIS
  toggleEmojiPicker(): void {
    this.showEmojiPicker = !this.showEmojiPicker;
  }

  addEmoji(emoji: string): void {
    const current = this.chatForm.get('message')?.value || '';
    if (current.length + emoji.length <= this.maxLength) {
      this.chatForm.patchValue({ message: current + emoji });
    }
    this.showEmojiPicker = false;
    this.cdr.markForCheck();
  }

  // -- GESTION TIMESTAMPS
  toggleTimestamp(msg: ReclamationMessage, event: Event): void {
    event.stopPropagation();
    if (this.selectedMessageId === msg.id) {
      this.selectedMessageId = undefined;
    } else {
      this.selectedMessageId = msg.id;
    }
    this.cdr.markForCheck();
  }

  

  // -- KEYBOARD
  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  // -- NAVIGATION
  goBack(): void {
    this.router.navigate(['/reclamation/consultation']);
  }

  // -- HELPERS
  get canSend(): boolean {
    const val = (this.chatForm.get('message')?.value ?? '') as string;
    return val.trim().length > 0 && !this.sending;
  }

  get remainingChars(): number {
    const val = (this.chatForm.get('message')?.value ?? '') as string;
    return this.maxLength - val.length;
  }

  get messageValue(): string {
    return (this.chatForm.get('message')?.value ?? '') as string;
  }

  getStatutBadgeClass(statut?: string): string {
    if (!statut) return 'statut-badge-default';
    const s = statut.toLowerCase();
    if (s.includes('favorable') || s.includes('trait')) return 'statut-badge-green';
    if (s.includes('cours'))  return 'statut-badge-orange';
    if (s.includes('rejet'))  return 'statut-badge-red';
    if (s.includes('compl'))  return 'statut-badge-blue';
    if (s.includes('clôt') || s.includes('clot')) return 'statut-badge-gray';
    if (s.includes('nouv'))   return 'statut-badge-purple';
    return 'statut-badge-default';
  }

  isAdminMessage(msg: ReclamationMessage): boolean {
     return msg.isAdmin === true;
  }

  formatDate(date: string): string {
    if (!date) return '';
    const d = new Date(date);
    return d.toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    }) + ' ' + d.toLocaleTimeString('fr-FR', {
      hour: '2-digit',
      minute: '2-digit'
   });
  }

  senderLabel(msg: ReclamationMessage): string {
     return msg.isAdmin ? 'Agent TSB' : 'Client';
  }

  private scrollToBottom(): void {
    try {
      const el = this.messagesScroll?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    } catch {}
  }
}
