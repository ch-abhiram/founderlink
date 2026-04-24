import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { StartupService } from '../../../core/services/startup.service';
import { StartupUpdate } from '../../../core/models/startup.model';
import { MessageService } from 'primeng/api';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { ButtonModule } from 'primeng/button';
import { TimeAgoPipe } from '../../../shared/pipes/time-ago.pipe';

@Component({
  selector: 'app-updates',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, InputTextModule, InputTextareaModule, ButtonModule, TimeAgoPipe],
  templateUrl: './updates.component.html',
  styles: ``
})
export class UpdatesComponent implements OnInit {
  startupId!: number;
  updates: StartupUpdate[] = [];
  updateForm: FormGroup;
  loading = true;
  posting = false;

  constructor(
    private route: ActivatedRoute,
    private startupService: StartupService,
    private fb: FormBuilder,
    private msg: MessageService
  ) {
    this.updateForm = this.fb.group({
      title: ['', Validators.required],
      content: ['', Validators.required]
    });
  }

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      this.startupId = Number(params.get('id'));
      this.loadUpdates();
    });
  }

  loadUpdates() {
    this.loading = true;
    this.startupService.getUpdates(this.startupId).subscribe({
      next: (res) => {
        this.updates = res.sort((a,b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  postUpdate() {
    if (this.updateForm.invalid) return;
    this.posting = true;

    this.startupService.postUpdate(this.startupId, this.updateForm.value).subscribe({
      next: () => {
        this.posting = false;
        this.msg.add({severity:'success', summary:'Posted', detail:'Update posted to startup.'});
        this.updateForm.reset();
        this.loadUpdates();
      },
      error: () => this.posting = false
    });
  }
}
