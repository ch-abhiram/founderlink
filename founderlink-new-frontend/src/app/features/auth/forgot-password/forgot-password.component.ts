import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './forgot-password.component.html',
  styleUrls: ['../_auth-shared.scss']
})
export class ForgotPasswordComponent {
  email = '';
  loading = false;
  error = '';
  message = '';

  constructor(private authService: AuthService, private router: Router) {}

  submit() {
    if (!this.email) {
      this.error = 'Please enter your email address.';
      return;
    }

    this.loading = true;
    this.error = '';
    this.message = '';
    this.authService.forgotPassword(this.email).subscribe({
      next: (res) => {
        this.loading = false;
        this.message = res?.message || 'If your account exists, a reset code has been sent.';
        setTimeout(() => this.router.navigate(['/reset-password'], { queryParams: { email: this.email } }), 900);
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.message || 'Unable to send reset code.';
      }
    });
  }
}
