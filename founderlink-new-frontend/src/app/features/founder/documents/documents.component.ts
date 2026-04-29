import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { StartupService } from '../../../core/services/startup.service';
import { StartupDocument } from '../../../core/models/startup.model';
import { MessageService } from 'primeng/api';
import { InputTextModule } from 'primeng/inputtext';
import { DropdownModule } from 'primeng/dropdown';
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';
import { TimeAgoPipe } from '../../../shared/pipes/time-ago.pipe';

@Component({
  selector: 'app-documents',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, InputTextModule, DropdownModule, ButtonModule, ToastModule, TimeAgoPipe],
  providers: [MessageService],
  templateUrl: './documents.component.html',
  styles: ``
})
export class DocumentsComponent implements OnInit {
  startupId!: number;
  documents: StartupDocument[] = [];
  docForm: FormGroup;
  loading = true;
  adding = false;
  selectedFile: File | null = null;

  docTypes = [
    { label: 'Pitch Deck', value: 'PITCH_DECK' },
    { label: 'Financials', value: 'FINANCIALS' },
    { label: 'Legal', value: 'LEGAL' },
    { label: 'Other', value: 'OTHER' }
  ];

  constructor(
    private route: ActivatedRoute,
    private startupService: StartupService,
    private fb: FormBuilder,
    private msg: MessageService
  ) {
    this.docForm = this.fb.group({
      name: ['', Validators.required],
      url: ['', Validators.pattern('https?://.+')],
      docType: ['PITCH_DECK', Validators.required]
    });
  }

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      this.startupId = Number(params.get('id'));
      this.loadDocuments();
    });
  }

  loadDocuments() {
    this.loading = true;
    this.startupService.getDocuments(this.startupId).subscribe({
      next: (res) => {
        this.documents = res;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  addDocument() {
    if (this.docForm.invalid || (!this.selectedFile && !this.docForm.value.url)) {
      this.docForm.markAllAsTouched();
      return;
    }
    this.adding = true;

    const request$ = this.selectedFile
      ? this.startupService.uploadDocument(this.startupId, this.selectedFile, {
          name: this.docForm.value.name,
          docType: this.docForm.value.docType
        })
      : this.startupService.addDocument(this.startupId, this.docForm.value);

    request$.subscribe({
      next: () => {
        this.adding = false;
        this.msg.add({severity:'success', summary:'Added', detail:'Document securely added.'});
        this.docForm.reset({docType: 'PITCH_DECK'});
        this.selectedFile = null;
        this.loadDocuments();
      },
      error: () => this.adding = false
    });
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files?.[0] ?? null;
    if (this.selectedFile && !this.docForm.value.name) {
      this.docForm.patchValue({ name: this.selectedFile.name });
    }
  }

  openDocument(document: StartupDocument) {
    if (!document.url) return;
    if (!this.startupService.isUploadedDocument(document)) {
      window.open(document.url, '_blank');
      return;
    }

    const viewer = window.open('', '_blank');
    this.startupService.downloadDocument(document).subscribe({
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
    if (!document.id) return;
    this.startupService.deleteDocument(this.startupId, document.id).subscribe({
      next: () => {
        this.documents = this.documents.filter(item => item.id !== document.id);
        this.msg.add({severity:'success', summary:'Deleted', detail:'Document removed.'});
      },
      error: () => this.msg.add({severity:'error', summary:'Error', detail:'Could not delete document.'})
    });
  }
}
