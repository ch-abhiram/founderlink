import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {
  form = { firstName: '', lastName: '', email: '', password: '', confirmPassword: '', role: 'ROLE_FOUNDER' };
  loading = false; error = ''; showPw = false;
  roles = [
    { value: 'ROLE_FOUNDER', label: 'Founder', icon: 'pi-briefcase', desc: "I'm building a startup" },
    { value: 'ROLE_INVESTOR', label: 'Investor', icon: 'pi-dollar', desc: "I'm looking to invest" },
    { value: 'ROLE_COFOUNDER', label: 'Co-Founder', icon: 'pi-users', desc: "I'm joining a team" },
  ];

  constructor(private authService: AuthService, private router: Router) {}

  selectRole(role: string) { this.form.role = role; }

  submit() {
    if (this.form.password !== this.form.confirmPassword) { this.error = 'Passwords do not match.'; return; }
    if (this.form.password.length < 8) { this.error = 'Password must be at least 8 characters.'; return; }
    this.loading = true; this.error = '';
    this.authService.register(this.form).subscribe({
      next: () => { this.loading = false; this.router.navigate(['/verify-otp'], { queryParams: { email: this.form.email } }); },
      error: (err) => { this.loading = false; this.error = err?.error?.message || 'Registration failed. Please try again.'; }
    });
  }
}
