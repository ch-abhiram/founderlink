import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  email = ''; password = ''; loading = false; error = ''; showPw = false;

  constructor(private authService: AuthService, private router: Router) {}

  submit() {
    if (!this.email || !this.password) { this.error = 'Please enter your email and password.'; return; }
    this.loading = true; this.error = '';
    this.authService.login({ email: this.email, password: this.password }).subscribe({
      next: (res) => {
        this.loading = false;
        const role = res.role;
        if (role === 'ROLE_FOUNDER' || role === 'ROLE_COFOUNDER') this.router.navigate(['/founder/dashboard']);
        else if (role === 'ROLE_INVESTOR') this.router.navigate(['/investor/dashboard']);
        else if (role === 'ROLE_ADMIN') this.router.navigate(['/admin/pending-startups']);
        else this.router.navigate(['/startups']);
      },
      error: (err) => { this.loading = false; this.error = err?.error?.message || 'Invalid credentials. Please try again.'; }
    });
  }
}
