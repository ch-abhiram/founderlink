import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { UserService } from '../../core/services/user.service';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, ToastModule],
  providers: [MessageService],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit {
  email = '';
  role = '';
  activeSection: 'profile' | 'security' | 'preferences' = 'profile';
  saving = false;

  profile = { firstName: '', lastName: '', bio: '', linkedinUrl: '', twitterUrl: '' };
  pwForm = { currentPassword: '', newPassword: '', confirmPassword: '' };
  showPw = false;

  constructor(private authSvc: AuthService, private userSvc: UserService, private msg: MessageService) {}

  ngOnInit() {
    this.email = this.authSvc.getEmail() || '';
    this.role = this.authSvc.getRole() || '';
    this.userSvc.getProfile().subscribe({
      next: u => {
        if (!u) return;
        const [firstName = '', ...rest] = (u.name || '').split(' ');
        this.profile = {
          ...this.profile,
          firstName,
          lastName: rest.join(' '),
          bio: u.bio || '',
          linkedinUrl: u.portfolioLinks?.[0] || '',
          twitterUrl: u.portfolioLinks?.[1] || ''
        };
      },
      error: () => {}
    });
  }

  get roleLabel(): string {
    const map: Record<string,string> = { ROLE_FOUNDER:'Founder', ROLE_COFOUNDER:'Co-Founder', ROLE_INVESTOR:'Investor', ROLE_ADMIN:'Administrator' };
    return map[this.role] || this.role;
  }

  get avatarInitials(): string { return (this.email || 'U').substring(0, 2).toUpperCase(); }

  saveProfile() {
    this.saving = true;
    const payload = {
      name: [this.profile.firstName, this.profile.lastName].filter(Boolean).join(' ').trim(),
      bio: this.profile.bio,
      portfolioLinks: [this.profile.linkedinUrl, this.profile.twitterUrl].filter(Boolean)
    };
    this.userSvc.updateProfile(payload).subscribe({
      next: () => { this.saving = false; this.msg.add({ severity: 'success', summary: 'Saved', detail: 'Profile updated.' }); },
      error: err => { this.saving = false; this.msg.add({ severity: 'error', summary: 'Error', detail: err?.error?.message || 'Save failed.' }); }
    });
  }

  changePassword() {
    if (this.pwForm.newPassword !== this.pwForm.confirmPassword) { this.msg.add({ severity: 'warn', summary: 'Passwords do not match' }); return; }
    this.saving = true;
    this.authSvc.changePassword({ currentPassword: this.pwForm.currentPassword, newPassword: this.pwForm.newPassword }).subscribe({
      next: () => { this.saving = false; this.pwForm = { currentPassword: '', newPassword: '', confirmPassword: '' }; this.msg.add({ severity: 'success', summary: 'Password changed successfully.' }); },
      error: err => { this.saving = false; this.msg.add({ severity: 'error', summary: 'Error', detail: err?.error?.message || 'Failed.' }); }
    });
  }
}
