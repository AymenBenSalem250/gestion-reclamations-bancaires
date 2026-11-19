import { Component } from '@angular/core';

@Component({
  selector: 'app-users',
  standalone: true,
  template: `
    <div class="page-container">
      <h2>Utilisateurs</h2>
      <p>Gestion des accès et utilisateurs internes.</p>
    </div>
  `,
  styles: [`
    .page-container {
      padding: 24px;
      background: white;
      border-radius: 12px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.05);
    }
  `]
})
export class UsersComponent {}
