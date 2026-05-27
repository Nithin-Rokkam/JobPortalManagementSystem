import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-unauthorized',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div style="min-height:100vh;display:flex;flex-direction:column;align-items:center;justify-content:center;background:var(--bg-base);gap:16px;text-align:center;padding:40px">
      <div style="font-size:4rem">🚫</div>
      <h1 style="font-family:'Sora',sans-serif;font-size:1.5rem;color:var(--text-primary)">Access Denied</h1>
      <p style="color:var(--text-secondary);font-size:0.9rem">You don't have permission to view this page.</p>
      <a routerLink="/auth/login" style="background:var(--accent-primary);color:var(--text-inverse);padding:10px 24px;border-radius:8px;font-weight:600;font-size:0.9rem">Go to Login</a>
    </div>
  `
})
export class UnauthorizedComponent { }
