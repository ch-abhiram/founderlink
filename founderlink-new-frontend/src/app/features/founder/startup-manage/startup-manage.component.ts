import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { StartupService } from '../../../core/services/startup.service';
import { AuthService } from '../../../core/services/auth.service';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';

const CATEGORIES = ['FinTech','HealthTech','EdTech','E-Commerce','SaaS','DeepTech','AI','CleanTech','Consumer','Other'];
const ROUNDS = ['Pre-Seed','Seed','Series A','Series B','Series C','Growth'];
const STAGES = ['Idea','MVP','Early Traction','Growth','Scale'];

@Component({
  selector: 'app-startup-manage',
  standalone: true,
  imports: [CommonModule, FormsModule, ToastModule],
  providers: [MessageService],
  templateUrl: './startup-manage.component.html',
  styleUrls: ['./startup-manage.component.scss']
})
export class StartupManageComponent implements OnInit {
  isEdit = false; loading = false; saving = false; startupId: number | null = null;
  categories = CATEGORIES; rounds = ROUNDS; stages = STAGES;

  form = {
    name: '', tagline: '', description: '', category: '',
    currentRound: '', stage: '', targetAmount: null as number|null,
    raisedAmount: null as number|null, equityOffered: null as number|null,
    websiteUrl: '', logoUrl: '', linkedinUrl: '', twitterUrl: ''
  };

  constructor(
    private startupSvc: StartupService, private authService: AuthService,
    private router: Router, private route: ActivatedRoute, private msg: MessageService
  ) {}

  ngOnInit() {
    this.startupId = Number(this.route.snapshot.paramMap.get('id')) || null;
    this.isEdit = !!this.startupId;
    if (this.isEdit) {
      this.loading = true;
      this.startupSvc.getById(this.startupId!).subscribe({
        next: s => { Object.assign(this.form, s); this.loading = false; },
        error: () => { this.msg.add({severity:'error', summary:'Error', detail:'Could not load startup.'}); this.loading = false; }
      });
    }
  }

  submit() {
    if (!this.form.name || !this.form.description || !this.form.category || !this.form.targetAmount) {
      this.msg.add({severity:'warn', summary:'Validation', detail:'Name, description, category, and target amount are required.'});
      return;
    }
    this.saving = true;
    const payload = { ...this.form, founderEmail: this.authService.getEmail() };
    const req$ = this.isEdit ? this.startupSvc.update(this.startupId!, payload) : this.startupSvc.create(payload);
    req$.subscribe({
      next: () => { this.saving = false; this.msg.add({severity:'success', summary:'Saved', detail: this.isEdit ? 'Startup updated.' : 'Startup created — pending review.'}); setTimeout(() => this.router.navigate(['/founder/my-startups']), 1200); },
      error: err => { this.saving = false; this.msg.add({severity:'error', summary:'Error', detail: err?.error?.message || 'Save failed.'}); }
    });
  }

  cancel() { this.router.navigate(['/founder/my-startups']); }
}
