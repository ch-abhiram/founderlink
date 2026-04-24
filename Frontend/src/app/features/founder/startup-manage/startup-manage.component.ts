import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { StartupService } from '../../../core/services/startup.service';
import { MessageService } from 'primeng/api';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { InputNumberModule } from 'primeng/inputnumber';
import { DropdownModule } from 'primeng/dropdown';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'app-startup-manage',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, InputTextModule, InputTextareaModule, InputNumberModule, DropdownModule, ButtonModule],
  templateUrl: './startup-manage.component.html',
  styles: ``
})
export class StartupManageComponent implements OnInit {
  startupForm: FormGroup;
  isEdit = false;
  startupId?: number;
  loading = false;

  categories = ['SaaS', 'Fintech', 'Healthtech', 'Edtech', 'Web3', 'E-commerce', 'AI', 'Other'].map(c => ({label:c, value:c}));
  stages = ['Idea', 'MVP', 'Revenue', 'Scaling'].map(s => ({label:s, value:s}));
  rounds = ['Pre-Seed', 'Seed', 'Series A', 'Series B', 'Series C+'].map(r => ({label:r, value:r}));

  constructor(
    private fb: FormBuilder,
    private startupService: StartupService,
    private router: Router,
    private route: ActivatedRoute,
    private msg: MessageService
  ) {
    this.startupForm = this.fb.group({
      name: ['', Validators.required],
      description: ['', Validators.required],
      tagline: [''],
      category: ['', Validators.required],
      fundingGoal: [null, [Validators.required, Validators.min(1)]],
      location: [''],
      foundedYear: [new Date().getFullYear()],
      teamSize: [1],
      mrr: [0],
      stage: [''],
      currentRound: [''],
      valuation: [0]
    });
  }

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.isEdit = true;
        this.startupId = +id;
        this.loadStartup(this.startupId);
      }
    });
  }

  loadStartup(id: number) {
    this.startupService.getById(id).subscribe(res => {
      this.startupForm.patchValue(res);
    });
  }

  onSubmit() {
    if (this.startupForm.invalid) return;
    this.loading = true;

    const req = this.isEdit 
      ? this.startupService.update(this.startupId!, this.startupForm.value)
      : this.startupService.create(this.startupForm.value);

    req.subscribe({
      next: () => {
        this.loading = false;
        this.msg.add({severity:'success', summary:'Success', detail: `Startup ${this.isEdit ? 'updated' : 'created'} correctly.`});
        this.router.navigate(['/founder/my-startups']);
      },
      error: () => this.loading = false
    });
  }
}
