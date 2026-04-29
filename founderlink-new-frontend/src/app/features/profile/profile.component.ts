import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { UserService } from '../../core/services/user.service';
import { TeamService } from '../../core/services/team.service';
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
  activeSection: 'profile' | 'security' | 'preferences' | 'invites' = 'profile';
  saving = false;
  inviteActionId: number | null = null;

  profile = { firstName: '', lastName: '', bio: '', linkedinUrl: '', twitterUrl: '' };
  preferences = { industries: '', stages: '', fundingRange: '', collabStyle: '', linkedinUrl: '' };
  pwForm = { currentPassword: '', newPassword: '', confirmPassword: '' };
  showPw = false;
  invites: any[] = [];

  constructor(
    private authSvc: AuthService,
    private userSvc: UserService,
    private teamSvc: TeamService,
    private msg: MessageService
  ) {}

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

    this.userSvc.getPreferences().subscribe({
      next: prefs => {
        if (!prefs) return;
        this.preferences = {
          industries: (prefs.industries || []).join(', '),
          stages: (prefs.stages || []).join(', '),
          fundingRange: prefs.fundingRange || '',
          collabStyle: prefs.collabStyle || '',
          linkedinUrl: prefs.linkedinUrl || ''
        };
      },
      error: () => {}
    });

    this.loadInvites();
  }

  get roleLabel(): string {
    const map: Record<string,string> = {
      ROLE_FOUNDER: 'Founder',
      ROLE_COFOUNDER: 'Co-Founder',
      ROLE_INVESTOR: 'Investor',
      ROLE_ADMIN: 'Administrator'
    };
    return map[this.role] || this.role;
  }

  get avatarInitials(): string {
    return (this.email || 'U').substring(0, 2).toUpperCase();
  }

  get pendingInvitesCount(): number {
    return this.invites.filter(invite => invite.status === 'PENDING').length;
  }

  saveProfile() {
    this.saving = true;
    const payload = {
      name: [this.profile.firstName, this.profile.lastName].filter(Boolean).join(' ').trim(),
      bio: this.profile.bio,
      portfolioLinks: [this.profile.linkedinUrl, this.profile.twitterUrl].filter(Boolean)
    };
    this.userSvc.updateProfile(payload).subscribe({
      next: () => {
        this.saving = false;
        this.msg.add({ severity: 'success', summary: 'Saved', detail: 'Profile updated.' });
      },
      error: err => {
        this.saving = false;
        this.msg.add({ severity: 'error', summary: 'Error', detail: err?.error?.message || 'Save failed.' });
      }
    });
  }

  changePassword() {
    if (this.pwForm.newPassword !== this.pwForm.confirmPassword) {
      this.msg.add({ severity: 'warn', summary: 'Passwords do not match' });
      return;
    }
    this.saving = true;
    this.authSvc.changePassword({
      currentPassword: this.pwForm.currentPassword,
      newPassword: this.pwForm.newPassword
    }).subscribe({
      next: () => {
        this.saving = false;
        this.pwForm = { currentPassword: '', newPassword: '', confirmPassword: '' };
        this.msg.add({ severity: 'success', summary: 'Password changed successfully.' });
      },
      error: err => {
        this.saving = false;
        this.msg.add({ severity: 'error', summary: 'Error', detail: err?.error?.message || 'Failed.' });
      }
    });
  }

  savePreferences() {
    this.saving = true;
    this.userSvc.updatePreferences({
      industries: this.toList(this.preferences.industries),
      stages: this.toList(this.preferences.stages),
      fundingRange: this.preferences.fundingRange,
      collabStyle: this.preferences.collabStyle,
      linkedinUrl: this.preferences.linkedinUrl
    }).subscribe({
      next: () => {
        this.saving = false;
        this.msg.add({ severity: 'success', summary: 'Saved', detail: 'Preferences updated.' });
      },
      error: err => {
        this.saving = false;
        this.msg.add({ severity: 'error', summary: 'Error', detail: err?.error?.message || 'Save failed.' });
      }
    });
  }

  loadInvites() {
    this.teamSvc.getMyInvites().subscribe({
      next: invites => {
        this.invites = invites.sort((a, b) => {
          if (a.status === b.status) return new Date(b.createdAt || '').getTime() - new Date(a.createdAt || '').getTime();
          if (a.status === 'PENDING') return -1;
          if (b.status === 'PENDING') return 1;
          return new Date(b.createdAt || '').getTime() - new Date(a.createdAt || '').getTime();
        });
      },
      error: () => {}
    });
  }

  respondToInvite(invite: any, status: 'ACCEPTED' | 'REJECTED') {
    if (this.inviteActionId === invite.id || invite.status !== 'PENDING') return;
    this.inviteActionId = invite.id;
    this.teamSvc.updateInviteStatus(invite.id, status).subscribe({
      next: updated => {
        const finalize = () => {
          this.inviteActionId = null;
          this.invites = this.invites.map(item => item.id === updated.id ? updated : item);
          this.msg.add({
            severity: 'success',
            summary: status === 'ACCEPTED' ? 'Invite accepted' : 'Invite declined',
            detail: status === 'ACCEPTED' ? 'You are now part of the startup team.' : 'The founder can invite someone else.'
          });
        };

        if (status === 'ACCEPTED' && updated.role === 'COFOUNDER') {
          this.authSvc.refresh().subscribe({ next: finalize, error: finalize });
          return;
        }

        finalize();
      },
      error: err => {
        this.inviteActionId = null;
        this.msg.add({ severity: 'error', summary: 'Error', detail: err?.error?.message || 'Unable to update invite.' });
      }
    });
  }

  private toList(value: string): string[] {
    return value.split(',').map(item => item.trim()).filter(Boolean);
  }
}
