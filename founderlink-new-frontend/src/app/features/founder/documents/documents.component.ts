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
import { TimeAgoPipe } from '../../../shared/pipes/time-ago.pipe';

@Component({
  selector: 'app-documents',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, InputTextModule, DropdownModule, ButtonModule, TimeAgoPipe],
  templateUrl: './documents.component.html',
  styles: ``
})
export class DocumentsComponent implements OnInit {
  startupId!: number;
  documents: StartupDocument[] = [];
  docForm: FormGroup;
  loading = true;
  adding = false;

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
      url: ['', [Validators.required, Validators.pattern('https?://.+')]],
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
    if (this.docForm.invalid) return;
    this.adding = true;

    this.startupService.addDocument(this.startupId, this.docForm.value).subscribe({
      next: () => {
        this.adding = false;
        this.msg.add({severity:'success', summary:'Added', detail:'Document securely added.'});
        this.docForm.reset({docType: 'PITCH_DECK'});
        this.loadDocuments();
      },
      error: () => this.adding = false
    });
  }
}
