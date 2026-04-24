import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { StartupService } from '../../../core/services/startup.service';
import { Startup } from '../../../core/models/startup.model';
import { CurrencyFormatPipe } from '../../../shared/pipes/currency-format.pipe';
import { TagModule } from 'primeng/tag';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextModule } from 'primeng/inputtext';
import { PaginatorModule } from 'primeng/paginator';

@Component({
  selector: 'app-discovery',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, CurrencyFormatPipe, TagModule, DropdownModule, InputTextModule, PaginatorModule],
  templateUrl: './discovery.component.html',
  styles: ``
})
export class DiscoveryComponent implements OnInit {
  startups: Startup[] = [];
  loading = true;
  totalElements = 0;
  
  filters = {
    category: null,
    stage: null,
    currentRound: null,
    status: 'OPEN'
  };

  categories = [
    { label: 'All', value: null },
    { label: 'SaaS', value: 'SaaS' },
    { label: 'Fintech', value: 'Fintech' },
    { label: 'Healthtech', value: 'Healthtech' },
    { label: 'Edtech', value: 'Edtech' },
    { label: 'Web3', value: 'Web3' }
  ];

  stages = [
    { label: 'All', value: null },
    { label: 'Idea', value: 'Idea' },
    { label: 'MVP', value: 'MVP' },
    { label: 'Revenue', value: 'Revenue' },
    { label: 'Scaling', value: 'Scaling' }
  ];

  rounds = [
    { label: 'All', value: null },
    { label: 'Pre-Seed', value: 'Pre-Seed' },
    { label: 'Seed', value: 'Seed' },
    { label: 'Series A', value: 'Series A' },
    { label: 'Series B', value: 'Series B' }
  ];

  page = 0;
  size = 9;

  constructor(private startupService: StartupService) {}

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.loading = true;
    this.startupService.search(this.filters, this.page, this.size).subscribe({
      next: (res) => {
        this.startups = res.content;
        this.totalElements = res.totalElements;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  onFilterChange() {
    this.page = 0;
    this.loadData();
  }

  onPageChange(event: any) {
    this.page = event.page;
    this.loadData();
  }
}
