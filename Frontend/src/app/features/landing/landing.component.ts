import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { StartupService } from '../../core/services/startup.service';
import { Startup } from '../../core/models/startup.model';
import { CurrencyFormatPipe } from '../../shared/pipes/currency-format.pipe';
import { TagModule } from 'primeng/tag';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule, RouterLink, CurrencyFormatPipe, TagModule],
  templateUrl: './landing.component.html',
  styles: ``
})
export class LandingComponent implements OnInit {
  startups: Startup[] = [];
  loading = true;

  constructor(private startupService: StartupService) {}

  ngOnInit() {
    this.startupService.search({ status: 'OPEN' }, 0, 6).subscribe({
      next: (res) => {
        this.startups = res.content;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }
}
