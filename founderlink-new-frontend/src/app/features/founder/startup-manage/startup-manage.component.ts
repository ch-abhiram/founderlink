import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { StartupService } from '../../../core/services/startup.service';
import { AuthService } from '../../../core/services/auth.service';
import { Startup, StartupDocument } from '../../../core/models/startup.model';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { of } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';

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
  documents: StartupDocument[] = [];
  selectedDocumentFile: File | null = null;
  docTypes = [
    { label: 'Pitch Deck', value: 'PITCH_DECK' },
    { label: 'Financials', value: 'FINANCIALS' },
    { label: 'Legal', value: 'LEGAL' },
    { label: 'Other', value: 'OTHER' }
  ];

  form = {
    name: '', tagline: '', description: '', category: '',
    currentRound: '', stage: '', targetAmount: null as number|null,
    raisedAmount: null as number|null, equityOffered: null as number|null,
    websiteUrl: '', logoUrl: '', linkedinUrl: '', twitterUrl: ''
  };
  documentForm = {
    name: '',
    docType: 'PITCH_DECK',
    url: ''
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
        next: s => { Object.assign(this.form, s); this.loading = false; this.loadDocuments(); },
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
    req$.pipe(
      switchMap(startup => this.saveDocumentIfPresent(startup))
    ).subscribe({
      next: () => { this.saving = false; this.msg.add({severity:'success', summary:'Saved', detail: this.isEdit ? 'Startup updated.' : 'Startup created — pending review.'}); setTimeout(() => this.router.navigate(['/founder/my-startups']), 1200); },
      error: err => { this.saving = false; this.msg.add({severity:'error', summary:'Error', detail: err?.error?.message || 'Save failed.'}); }
    });
  }

  cancel() { this.router.navigate(['/founder/my-startups']); }

  onDocumentSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    this.selectedDocumentFile = input.files?.[0] ?? null;
    if (this.selectedDocumentFile && !this.documentForm.name) {
      this.documentForm.name = this.selectedDocumentFile.name;
    }
  }

  private loadDocuments() {
    if (!this.startupId) return;
    this.startupSvc.getDocuments(this.startupId).subscribe({
      next: documents => this.documents = documents,
      error: () => this.documents = []
    });
  }

  private saveDocumentIfPresent(startup: Startup) {
    const startupId = startup.id || this.startupId;
    if (!startupId || (!this.selectedDocumentFile && !this.documentForm.url)) {
      return of(startup);
    }

    const request$ = this.selectedDocumentFile
      ? this.startupSvc.uploadDocument(startupId, this.selectedDocumentFile, {
          name: this.documentForm.name,
          docType: this.documentForm.docType
        })
      : this.startupSvc.addDocument(startupId, {
          name: this.documentForm.name || 'Startup document',
          url: this.documentForm.url,
          docType: this.documentForm.docType
        });

    return request$.pipe(map(() => startup));
  }

  openDocument(document: StartupDocument) {
    if (!document.url) return;
    if (!this.startupSvc.isUploadedDocument(document)) {
      window.open(document.url, '_blank');
      return;
    }

    const viewer = window.open('', '_blank');
    this.startupSvc.downloadDocument(document).subscribe({
      next: blob => {
        const blobUrl = URL.createObjectURL(blob);
        if (viewer) {
          viewer.location.href = blobUrl;
        } else {
          window.open(blobUrl, '_blank');
        }
        setTimeout(() => URL.revokeObjectURL(blobUrl), 60000);
      },
      error: () => {
        viewer?.close();
        this.msg.add({severity:'error', summary:'Document missing', detail:'The saved file was not found on the server. Please upload it again.'});
      }
    });
  }

  deleteDocument(document: StartupDocument) {
    if (!this.startupId || !document.id) return;
    this.startupSvc.deleteDocument(this.startupId, document.id).subscribe({
      next: () => {
        this.documents = this.documents.filter(item => item.id !== document.id);
        this.msg.add({severity:'success', summary:'Deleted', detail:'Document removed.'});
      },
      error: () => this.msg.add({severity:'error', summary:'Error', detail:'Could not delete document.'})
    });
  }
}
