import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { MessageService } from 'primeng/api';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, InputTextModule, PasswordModule, ButtonModule],
  templateUrl: './login.component.html',
  styles: ``
})
export class LoginComponent {
  loginForm: FormGroup;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private msg: MessageService
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]]
    });
  }

  onSubmit() {
    if (this.loginForm.invalid) return;
    this.loading = true;
    
    this.authService.login(this.loginForm.value).subscribe({
      next: (res) => {
        this.loading = false;
        // Redirect logic based on role guards will handle this basically, but let's do it here explicitly
        if (res.role === 'ROLE_INVESTOR') this.router.navigate(['/investor/dashboard']);
        else if (res.role === 'ROLE_ADMIN') this.router.navigate(['/admin/pending-startups']);
        else this.router.navigate(['/founder/dashboard']);
      },
      error: (err) => {
        this.loading = false;
        // If unverified, guide them
        if (err.status === 403 && err.error?.message?.includes('verified')) {
           this.msg.add({severity:'warn', summary: 'Verification Required', detail: 'Please verify your email to log in.'});
           this.router.navigate(['/verify-otp'], { queryParams: { email: this.loginForm.value.email }});
        }
      }
    });
  }
}
