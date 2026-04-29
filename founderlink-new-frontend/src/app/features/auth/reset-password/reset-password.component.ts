import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './reset-password.component.html',
  styleUrls: ['../_auth-shared.scss']
})
export class ResetPasswordComponent implements OnInit {
  form = { email: '', token: '', newPassword: '', confirmPassword: '' };
  loading = false;
  error = '';
  message = '';
  showPw = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.form.email = this.route.snapshot.queryParamMap.get('email') || '';
  }

  submit() {
    if (!this.form.email || !this.form.token || !this.form.newPassword) {
      this.error = 'Please fill in all fields.';
      return;
    }
    if (this.form.newPassword.length < 8) {
      this.error = 'Password must be at least 8 characters.';
      return;
    }
    if (this.form.newPassword !== this.form.confirmPassword) {
      this.error = 'Passwords do not match.';
      return;
    }

    this.loading = true;
    this.error = '';
    this.message = '';
    this.authService.resetPassword({
      email: this.form.email,
      token: this.form.token,
      newPassword: this.form.newPassword
    }).subscribe({
      next: (res) => {
        this.loading = false;
        this.message = res?.message || 'Password reset successfully.';
        setTimeout(() => this.router.navigate(['/login']), 1000);
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.message || 'Unable to reset password.';
      }
    });
  }
}
