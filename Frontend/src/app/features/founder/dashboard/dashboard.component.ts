import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { StartupService } from '../../../core/services/startup.service';
import { TeamService } from '../../../core/services/team.service';
import { InvestmentService } from '../../../core/services/investment.service';
import { NotificationService } from '../../../core/services/notification.service';
import { AuthService } from '../../../core/services/auth.service';
import { Startup } from '../../../core/models/startup.model';
import { Investment } from '../../../core/models/investment.model';
import { Notification } from '../../../core/models/notification.model';
import { CurrencyFormatPipe } from '../../../shared/pipes/currency-format.pipe';
import { TimeAgoPipe } from '../../../shared/pipes/time-ago.pipe';
import { NgxEchartsDirective, provideEchartsCore } from 'ngx-echarts';
import * as echarts from 'echarts/core';
import { BarChart } from 'echarts/charts';
import { GridComponent, TooltipComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';

echarts.use([BarChart, GridComponent, TooltipComponent, CanvasRenderer]);

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, CurrencyFormatPipe, TimeAgoPipe, NgxEchartsDirective],
  providers: [provideEchartsCore({ echarts })],
  templateUrl: './dashboard.component.html',
  styles: ``
})
export class DashboardComponent implements OnInit {
  startups: Startup[] = [];
  investments: Investment[] = [];
  notifications: Notification[] = [];
  
  loading = true;

  chartOptions: any = {};

  get totalInvestmentReceived() {
    return this.investments.filter(i => i.status === 'COMPLETED' || i.status === 'SUCCESS').reduce((sum, i) => sum + i.amount, 0);
  }

  get pendingInvestmentRequests() {
    return this.investments.filter(i => i.status === 'PENDING').length;
  }

  constructor(
    private startupService: StartupService,
    private teamService: TeamService,
    private invService: InvestmentService,
    private notifService: NotificationService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    const email = this.authService.getEmail();
    if (!email) return;

    this.startupService.search({}, 0, 100).subscribe(res => {
      this.startups = res.content.filter(s => s.founderEmail === email);
      this.initChart();
      this.loadInvestments();
    });

    this.notifService.getNotifications().subscribe(n => {
      this.notifications = n.slice(0, 5);
      this.loading = false;
    });
  }

  loadInvestments() {
    this.investments = [];
    this.startups.forEach(s => {
      this.invService.getInvestmentsForStartup(s.id).subscribe(invs => {
        this.investments = [...this.investments, ...invs].sort((a,b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
      });
    });
  }

  initChart() {
    const names = this.startups.map(s => s.name);
    const goals = this.startups.map(s => s.fundingGoal);
    const current = this.startups.map(s => s.currentFunding);

    this.chartOptions = {
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' }
      },
      grid: {
        left: '3%', right: '4%', bottom: '3%', containLabel: true
      },
      xAxis: {
        type: 'value',
        splitLine: { lineStyle: { color: '#334155' } }
      },
      yAxis: {
        type: 'category',
        data: names,
        axisLine: { lineStyle: { color: '#94A3B8' } }
      },
      series: [
        {
          name: 'Current Funding',
          type: 'bar',
          stack: 'total',
          itemStyle: { color: '#10B981' },
          data: current
        },
        {
          name: 'Remaining Goal',
          type: 'bar',
          stack: 'total',
          itemStyle: { color: '#4F46E5', opacity: 0.3 },
          data: goals.map((g, i) => g - current[i])
        }
      ]
    };
  }
}
