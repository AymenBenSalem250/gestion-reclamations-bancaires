import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';

export type HeaderIconType = 
  | 'bar-chart-2' 
  | 'folder' 
  | 'edit-3' 
  | 'refresh-cw' 
  | 'eye' 
  | 'search'
  | 'message-circle' 
  | 'settings' 
  | 'file-text' 
  | 'code' 
  | 'users';

@Component({
  selector: 'app-page-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './page-header.component.html',
  styleUrls: ['./page-header.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PageHeaderComponent {
  @Input() title: string = '';
  @Input() subtitle: string = '';
  @Input() icon: HeaderIconType = 'folder';
  @Input() badgeText?: string;
  @Input() badgeType: 'admin' | 'user' | 'default' = 'default';

  getBadgeClass(): string {
    if (this.badgeType === 'admin') return 'sla-badge-admin';
    if (this.badgeType === 'user') return 'sla-badge-user';
    return '';
  }
}
