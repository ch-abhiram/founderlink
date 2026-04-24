import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, InputTextModule, PasswordModule, ButtonModule],
  templateUrl: './register.component.html',
  styles: ``
})
export class RegisterComponent implements OnInit {
  registerForm: FormGroup;
  loading = false;
  selectedRole = 'ROLE_FOUNDER';

  roles = [
    { value: 'ROLE_FOUNDER', title: 'Founder', desc: 'Launch a startup and raise capital', icon: 'pi-rocket' },
    { value: 'ROLE_INVESTOR', title: 'Investor', desc: 'Discover and invest in startups', icon: 'pi-chart-line' },
    { value: 'ROLE_COFOUNDER', title: 'Co-founder', desc: 'Join an early stage team', icon: 'pi-users' }
  ];

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.registerForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });
  }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      if (params['role']) {
        const val = `ROLE_${params['role'].toUpperCase()}`;
        if (this.roles.find(r => r.value === val)) {
           this.selectedRole = val;
        }
      }
    });
  }

  passwordMatchValidator(g: FormGroup) {
    return g.get('password')?.value === g.get('confirmPassword')?.value
      ? null : { mismatch: true };
  }

  selectRole(role: string) {
    this.selectedRole = role;
  }

  onSubmit() {
    if (this.registerForm.invalid) return;
    this.loading = true;
    
    const payload = {
      email: this.registerForm.value.email,
      password: this.registerForm.value.password,
      role: this.selectedRole
    };

    this.authService.register(payload).subscribe({
      next: (res) => {
        this.loading = false;
        this.router.navigate(['/verify-otp'], { queryParams: { email: payload.email } });
      },
      error: () => this.loading = false
    });
  }
}
