import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-verify-otp',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
<div class="auth-page">
  <div class="orb orb-1"></div>
  <div class="orb orb-2"></div>
  <div class="auth-card glass-strong animate-scale-in">
    <div class="auth-brand">
      <div class="brand-icon"><i class="pi pi-link"></i></div>
      <span class="brand-name display">FounderLink</span>
    </div>
    <div class="otp-icon"><i class="pi pi-envelope"></i></div>
    <h2 class="auth-title">Verify your email</h2>
    <p class="auth-sub">We sent a 6-digit code to <strong>{{ email }}</strong>. Enter it below.</p>
    <div class="error-msg" *ngIf="error"><i class="pi pi-exclamation-circle"></i> {{ error }}</div>
    <div class="success-msg" *ngIf="resent"><i class="pi pi-check-circle"></i> A new code has been sent.</div>
    <form class="auth-form" (ngSubmit)="submit()">
      <div class="field">
        <label class="fl-label">OTP Code</label>
        <input class="fl-input otp-input" type="text" [(ngModel)]="otp" name="otp" placeholder="000000" maxlength="6" inputmode="numeric" />
      </div>
      <button type="submit" class="btn-primary submit-btn" [disabled]="loading">
        <span class="spinner" *ngIf="loading"></span>
        <span *ngIf="!loading"><i class="pi pi-check"></i> Verify & Continue</span>
        <span *ngIf="loading">Verifying…</span>
      </button>
    </form>
    <p class="auth-switch">Didn't receive it? <a href="#" (click)="resend($event)">Resend code</a></p>
  </div>
</div>`,
  styles: [`
    @use '../auth-shared';
    .otp-icon { width: 56px; height: 56px; border-radius: 50%; background: rgba(99,102,241,0.1); border: 1px solid rgba(99,102,241,0.25); display: flex; align-items: center; justify-content: center; font-size: 1.4rem; color: var(--accent-primary); margin-bottom: 16px; }
    .otp-input { font-size: 1.5rem; text-align: center; letter-spacing: 0.4em; font-family: var(--font-display); }
  `]
})
export class VerifyOtpComponent implements OnInit {
  email = ''; otp = ''; loading = false; error = ''; resent = false;

  constructor(private authService: AuthService, private router: Router, private route: ActivatedRoute) {}

  ngOnInit() { this.email = this.route.snapshot.queryParamMap.get('email') || ''; }

  submit() {
    if (!this.otp || this.otp.length < 4) { this.error = 'Enter the code sent to your email.'; return; }
    this.loading = true; this.error = '';
    this.authService.verifyOtp(this.email, this.otp).subscribe({
      next: () => { this.loading = false; this.router.navigate(['/login']); },
      error: (err) => { this.loading = false; this.error = err?.error?.message || 'Invalid OTP. Please try again.'; }
    });
  }

  resend(e: Event) {
    e.preventDefault(); this.resent = false;
    this.authService.resendOtp(this.email).subscribe({ next: () => { this.resent = true; }, error: () => {} });
  }
}
