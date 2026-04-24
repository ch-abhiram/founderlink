import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TeamService } from '../../../core/services/team.service';
import { TeamMember } from '../../../core/models/team.model';
import { MessageService, ConfirmationService } from 'primeng/api';
import { InputTextModule } from 'primeng/inputtext';
import { DropdownModule } from 'primeng/dropdown';
import { InputNumberModule } from 'primeng/inputnumber';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { TagModule } from 'primeng/tag';
import { ConfirmDialogModule } from 'primeng/confirmdialog';

@Component({
  selector: 'app-team-manage',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, InputTextModule, DropdownModule, InputNumberModule, ButtonModule, DialogModule, TagModule, ConfirmDialogModule],
  providers: [ConfirmationService],
  templateUrl: './team-manage.component.html',
  styles: ``
})
export class TeamManageComponent implements OnInit {
  startupId!: number;
  team: TeamMember[] = [];
  loading = true;
  
  showInviteModal = false;
  inviteForm: FormGroup;
  inviting = false;

  roles = [{label:'Cofounder', value:'COFOUNDER'}, {label:'Employee', value:'EMPLOYEE'}, {label:'Advisor', value:'ADVISOR'}, {label:'Intern', value:'INTERN'}];
  perms = [{label:'Owner', value:'OWNER'}, {label:'Admin', value:'ADMIN'}, {label:'Member', value:'MEMBER'}];

  constructor(
    private route: ActivatedRoute,
    private teamService: TeamService,
    private fb: FormBuilder,
    private msg: MessageService,
    private confirm: ConfirmationService
  ) {
    this.inviteForm = this.fb.group({
      userEmail: ['', [Validators.required, Validators.email]],
      role: ['EMPLOYEE', Validators.required],
      equityPercentage: [0, [Validators.min(0), Validators.max(100)]],
      permissionLevel: ['MEMBER', Validators.required]
    });
  }

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      this.startupId = Number(params.get('id'));
      this.loadTeam();
    });
  }

  loadTeam() {
    this.loading = true;
    this.teamService.getTeamForStartup(this.startupId).subscribe({
      next: (res) => { this.team = res; this.loading = false; },
      error: () => this.loading = false
    });
  }

  sendInvite() {
    if (this.inviteForm.invalid) return;
    this.inviting = true;
    
    const payload = {
      startupId: this.startupId,
      ...this.inviteForm.value
    };

    this.teamService.inviteMember(payload).subscribe({
      next: () => {
        this.msg.add({severity:'success', summary:'Sent', detail:'Invite sent successfully'});
        this.showInviteModal = false;
        this.inviting = false;
        this.inviteForm.reset({role:'EMPLOYEE', permissionLevel:'MEMBER', equityPercentage:0});
        this.loadTeam();
      },
      error: () => this.inviting = false
    });
  }

  removeMember(id: number, event: Event) {
    this.confirm.confirm({
      target: event.target as EventTarget,
      message: 'Are you sure you want to remove this member?',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.teamService.removeMember(id).subscribe(() => {
           this.msg.add({severity:'success', summary:'Removed', detail:'Member removed.'});
           this.loadTeam();
        });
      }
    });
  }
}
