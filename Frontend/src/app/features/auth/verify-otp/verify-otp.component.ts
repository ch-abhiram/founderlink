import { Component, OnInit, OnDestroy, ViewChildren, QueryList, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormArray } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'app-verify-otp',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, ButtonModule],
  templateUrl: './verify-otp.component.html',
  styles: ``
})
export class VerifyOtpComponent implements OnInit, OnDestroy {
  @ViewChildren('otpInput') otpInputs!: QueryList<ElementRef>;
  
  email = '';
  loading = false;
  otpForm: FormGroup;
  cooldown = 0;
  private intervalRef: any;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private msg: MessageService
  ) {
    this.otpForm = this.fb.group({
      digits: this.fb.array(
        Array(6).fill('').map(() => this.fb.control('', [Validators.required, Validators.pattern('^[0-9]$')]))
      )
    });
  }

  get digitsArr() {
    return this.otpForm.get('digits') as FormArray;
  }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      if (params['email']) {
        this.email = params['email'];
        this.startCooldown(10); // initial 10s cooldown
      } else {
        this.router.navigate(['/login']);
      }
    });
  }

  ngOnDestroy() {
    if (this.intervalRef) clearInterval(this.intervalRef);
  }

  startCooldown(seconds: number) {
    this.cooldown = seconds;
    if (this.intervalRef) clearInterval(this.intervalRef);
    this.intervalRef = setInterval(() => {
      this.cooldown--;
      if (this.cooldown <= 0) {
        clearInterval(this.intervalRef);
      }
    }, 1000);
  }

  resendOtp() {
    if (this.cooldown > 0) return;
    
    this.authService.resendOtp(this.email).subscribe({
      next: () => {
         this.msg.add({ severity: 'success', summary: 'Sent', detail: 'New OTP sent to ' + this.email });
         this.startCooldown(120); // 2 minute cooldown after pressing resend
      },
      error: (err) => {
         const errorMsg = err.error?.message || 'Failed to resend';
         this.msg.add({ severity: 'error', summary: 'Error', detail: errorMsg });
         // Handle backend specific error like "Please wait 45 seconds" if we want to parse it
      }
    });
  }

  onInput(event: any, index: number) {
    const val = event.target.value;
    if (val && !/^[0-9]$/.test(val)) {
       this.digitsArr.at(index).setValue('');
       return;
    }
    if (val && index < 5) {
      this.otpInputs.toArray()[index + 1].nativeElement.focus();
    }
  }

  onKeyDown(event: KeyboardEvent, index: number) {
    if (event.key === 'Backspace' && !this.digitsArr.at(index).value && index > 0) {
      this.otpInputs.toArray()[index - 1].nativeElement.focus();
    }
  }

  onPaste(event: ClipboardEvent) {
    event.preventDefault();
    const pastedData = event.clipboardData?.getData('text');
    if (!pastedData || !/^\d{6}$/.test(pastedData)) return;

    for (let i = 0; i < 6; i++) {
        this.digitsArr.at(i).setValue(pastedData[i]);
    }
    this.otpInputs.toArray()[5].nativeElement.focus();
  }

  onSubmit() {
    if (this.otpForm.invalid) return;
    this.loading = true;
    
    const otp = this.digitsArr.value.join('');
    
    this.authService.verifyOtp(this.email, otp).subscribe({
      next: () => {
         this.msg.add({ severity: 'success', summary: 'Verified', detail: 'Account verified correctly. Please log in.' });
         this.router.navigate(['/login']);
      },
      error: (err) => {
         this.loading = false;
         const errorMsg = err.error?.message || 'Invalid OTP';
         this.msg.add({ severity: 'error', summary: 'Error', detail: errorMsg, life: 6000 });
      }
    });
  }
}
