import { Component, HostBinding, Input, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationEnd, Router, RouterModule } from '@angular/router';
import { filter, Subscription } from 'rxjs';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit, OnDestroy {

  @Input() @HostBinding('class.open') mobileOpen = false;

  reclamationExpanded = false;
  referentielExpanded = false;
  reportingExpanded = false;

  private routerSub?: Subscription;

  constructor(
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.syncExpandedFromRoute(this.router.url);

    this.routerSub = this.router.events
      .pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd))
      .subscribe(event => this.syncExpandedFromRoute(event.urlAfterRedirects));
  }

  ngOnDestroy(): void {
    this.routerSub?.unsubscribe();
  }

  isAdmin(): boolean {
    return this.authService.getRoles().includes('ROLE_ADMIN');
  }

  get roleLabel(): string {
    if (this.isAdmin()) {
      return 'Administrateur';
    }
    if (this.authService.getRoles().includes('ROLE_USER')) {
      return 'Utilisateur';
    }
    return 'Invité';
  }

  toggleReclamation(): void {
    this.reclamationExpanded = !this.reclamationExpanded;
  }

  toggleReferentiel(): void {
    this.referentielExpanded = !this.referentielExpanded;
  }

  toggleReporting(): void {
    this.reportingExpanded = !this.reportingExpanded;
  }

  toggleMobile(): void {
    this.mobileOpen = !this.mobileOpen;
  }

  closeMobile(): void {
    this.mobileOpen = false;
  }

  logout(): void {
    this.authService.logout();
  }

  private syncExpandedFromRoute(url: string): void {
    if (url.startsWith('/reclamation')) {
      this.reclamationExpanded = true;
    }
    if (url.startsWith('/parametre')) {
      this.referentielExpanded = true;
    }
    if (url.startsWith('/reporting')) {
      this.reportingExpanded = true;
    }
  }
}
