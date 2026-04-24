import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { UserService } from '../../core/services/user.service';
import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { PasswordModule } from 'primeng/password';
import { ChipsModule } from 'primeng/chips';
import { MultiSelectModule } from 'primeng/multiselect';
import { DropdownModule } from 'primeng/dropdown';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ButtonModule, InputTextModule, InputTextareaModule, PasswordModule, ChipsModule, MultiSelectModule, DropdownModule],
  templateUrl: './profile.component.html',
  styles: ``
})
export class ProfileComponent implements OnInit {
  profileForm: FormGroup;
  prefForm: FormGroup;
  passwordForm: FormGroup;
  
  loadingProfile = false;
  loadingPref = false;
  loadingPass = false;

  email = '';

  industryOptions = [
    {label: 'SaaS', value: 'SaaS'}, {label: 'Healthtech', value: 'Healthtech'},
    {label: 'Fintech', value: 'Fintech'}, {label: 'Edtech', value: 'Edtech'},
    {label: 'Web3', value: 'Web3'}, {label: 'E-commerce', value: 'E-commerce'}
  ];
  stageOptions = [
    {label: 'Idea', value: 'Idea'}, {label: 'MVP', value: 'MVP'},
    {label: 'Revenue', value: 'Revenue'}, {label: 'Scaling', value: 'Scaling'}
  ];

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private userService: UserService,
    private msg: MessageService
  ) {
    this.email = this.authService.getEmail() || '';
    
    this.profileForm = this.fb.group({
      name: ['', Validators.required],
      headline: [''],
      bio: [''],
      experience: [''],
      location: [''],
      avatarUrl: [''],
      primaryGoal: [''],
      skills: [[]],
      portfolioLinks: [[]]
    });

    this.prefForm = this.fb.group({
      industries: [[]],
      stages: [[]],
      fundingRange: [''],
      collabStyle: [''],
      linkedinUrl: ['']
    });

    this.passwordForm = this.fb.group({
      currentPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.userService.getUserProfile(this.email).subscribe(res => {
      this.profileForm.patchValue(res);
    });
    this.userService.getPreferences(this.email).subscribe(res => {
      this.prefForm.patchValue(res);
    });
  }

  saveProfile() {
    if (this.profileForm.invalid) return;
    this.loadingProfile = true;
    this.userService.updateProfile(this.email, this.profileForm.value).subscribe({
      next: () => {
         this.loadingProfile = false;
         this.msg.add({severity:'success', summary:'Success', detail:'Profile updated.'});
      },
      error: () => this.loadingProfile = false
    });
  }

  savePreferences() {
    if (this.prefForm.invalid) return;
    this.loadingPref = true;
    this.userService.updatePreferences(this.email, this.prefForm.value).subscribe({
      next: () => {
         this.loadingPref = false;
         this.msg.add({severity:'success', summary:'Success', detail:'Preferences updated.'});
      },
      error: () => this.loadingPref = false
    });
  }

  changePassword() {
    if (this.passwordForm.invalid) return;
    this.loadingPass = true;
    this.authService.changePassword(this.passwordForm.value).subscribe({
      next: () => {
         this.loadingPass = false;
         this.passwordForm.reset();
         this.msg.add({severity:'success', summary:'Success', detail:'Password changed.'});
      },
      error: () => this.loadingPass = false
    });
  }
}
