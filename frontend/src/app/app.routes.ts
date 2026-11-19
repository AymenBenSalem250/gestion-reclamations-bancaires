import { Routes } from '@angular/router';

import { LoginComponent } from './auth/login/login.component';
import { SignupComponent } from './auth/signup/signup.component';
import { LayoutComponent } from './layout/layout.component';

import { authGuard } from './core/auth.guard';
import { roleGuard } from './core/role.guard';

export const routes: Routes = [

  // ✅ Routes publiques
  { path: 'login', component: LoginComponent },
  { path: 'signup', component: SignupComponent },

  // ✅ Routes protégées
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [

      // ✅ Dashboard
      {
        path: 'dashboard',
        canActivate: [roleGuard],
        data: { roles: ['ROLE_ADMIN', 'ROLE_USER'] },
        loadComponent: () =>
          import('./dashboard/dashboard.component')
            .then(m => m.DashboardComponent)
      },

      // ✅ GESTION DES RÉCLAMATIONS

      {
        path: 'reclamation/saisie',
        canActivate: [roleGuard],
        data: { roles: ['ROLE_ADMIN', 'ROLE_USER'] },
        loadComponent: () =>
          import('./reclamation/saisie/saisie.component')
            .then(m => m.SaisieComponent)
      },

      // ✅ Redirection si paramètre manquant
      {
        path: 'reclamation/modification',
        redirectTo: 'reclamation/consultation',
        pathMatch: 'full'
      },
      {
        path: 'reclamation/traitement',
        redirectTo: 'reclamation/consultation',
        pathMatch: 'full'
      },
      {
        path: 'reclamation/messages',
        redirectTo: 'reclamation/consultation',
        pathMatch: 'full'
      },

      // ✅ Routes avec paramètre obligatoire
      {
        path: 'reclamation/modification/:refRec',
        canActivate: [roleGuard],
        data: { roles: ['ROLE_ADMIN', 'ROLE_USER'] },
        loadComponent: () =>
          import('./reclamation/modification/modification.component')
            .then(m => m.ModificationComponent)
      },
      {
        path: 'reclamation/traitement/:refRec',
        canActivate: [roleGuard],
        data: { roles: ['ROLE_ADMIN', 'ROLE_USER'] },
        loadComponent: () =>
          import('./reclamation/traitement/traitement.component')
            .then(m => m.TraitementComponent)
      },
      {
        path: 'reclamation/consultation',
        canActivate: [roleGuard],
        data: { roles: ['ROLE_ADMIN', 'ROLE_USER'] },
        loadComponent: () =>
          import('./reclamation/consultation/consultation.component')
            .then(m => m.ConsultationComponent)
      },

      // ✅ Chat messages par réclamation
      {
        path: 'reclamation/messages/:refRec',
        canActivate: [roleGuard],
        data: { roles: ['ROLE_ADMIN', 'ROLE_USER'] },
        loadComponent: () =>
          import('./reclamation/messages/messages.component')
            .then(m => m.MessagesComponent)
      },

      // ✅ PARAMÉTRAGE (ADMIN)
      {
        path: 'parametre/nature-client',
        canActivate: [roleGuard],
        data: { roles: ['ROLE_ADMIN'] },
        loadComponent: () =>
          import('./parametre/nature-client/nature-client-page-list.component')
            .then(m => m.NatureClientPageListComponent)
      },
      {
        path: 'parametre/sort-reclamation',
        canActivate: [roleGuard],
        data: { roles: ['ROLE_ADMIN'] },
        loadComponent: () =>
          import('./parametre/sort-reclamation/sort-reclamation-page-list.component')
            .then(m => m.SortReclamationPageListComponent)
      },
      {
        path: 'parametre/canal-reception',
        canActivate: [roleGuard],
        data: { roles: ['ROLE_ADMIN'] },
        loadComponent: () =>
          import('./parametre/canal-reception/canal-reception-page-list.component')
            .then(m => m.CanalReceptionPageListComponent)
      },
      {
        path: 'parametre/service-traitement',
        canActivate: [roleGuard],
        data: { roles: ['ROLE_ADMIN'] },
        loadComponent: () =>
          import('./parametre/service-traitement/service-traitement-page-list.component')
            .then(m => m.ServiceTraitementPageListComponent)
      },
      {
        path: 'parametre/objet-reclamation',
        canActivate: [roleGuard],
        data: { roles: ['ROLE_ADMIN'] },
        loadComponent: () =>
          import('./parametre/objet-reclamation/objet-reclamation-page-list.component')
            .then(m => m.ObjetReclamationPageListComponent)
      },
      {
        path: 'parametre/notification-type',
        canActivate: [roleGuard],
        data: { roles: ['ROLE_ADMIN'] },
        loadComponent: () =>
          import('./parametre/notification-type/notification-type-page-list.component')
            .then(m => m.NotificationTypePageListComponent)
      },

      // ✅ REPORTING (ADMIN)
      {
        path: 'reporting/xml',
        canActivate: [roleGuard],
        data: { roles: ['ROLE_ADMIN'] },
        loadComponent: () =>
          import('./reporting/xml/xml.component')
            .then(m => m.XmlComponent)
      },
      {
        path: 'reporting/etat',
        canActivate: [roleGuard],
        data: { roles: ['ROLE_ADMIN'] },
        loadComponent: () =>
          import('./reporting/etat/etat.component')
            .then(m => m.EtatComponent)
      },

      // ✅ Default
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }

    ]
  },

  // ✅ Catch-all
  { path: '**', redirectTo: 'login' }
];