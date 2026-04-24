import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { InvestmentService } from '../../../core/services/investment.service';
import { NotificationService } from '../../../core/services/notification.service';
import { AuthService } from '../../../core/services/auth.service';
import { Investment } from '../../../core/models/investment.model';
import { Notification } from '../../../core/models/notification.model';
import { CurrencyFormatPipe } from '../../../shared/pipes/currency-format.pipe';
import { TimeAgoPipe } from '../../../shared/pipes/time-ago.pipe';
import { NgxEchartsDirective, provideEchartsCore } from 'ngx-echarts';
import * as echarts from 'echarts/core';
import { PieChart, LineChart } from 'echarts/charts';
import { GridComponent, TooltipComponent, LegendComponent, TitleComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';

echarts.use([PieChart, LineChart, GridComponent, TooltipComponent, LegendComponent, TitleComponent, CanvasRenderer]);

@Component({
  selector: 'app-investor-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, CurrencyFormatPipe, TimeAgoPipe, NgxEchartsDirective],
  providers: [provideEchartsCore({ echarts })],
  templateUrl: './dashboard.component.html',
  styles: ``
})
export class DashboardComponent implements OnInit {
  investments: Investment[] = [];
  notifications: Notification[] = [];
  
  loading = true;

  chartOptions: any = {};
  trendOptions: any = {};

  get totalCapitalDeployed() {
    return this.investments.filter(i => ['COMPLETED', 'SUCCESS', 'APPROVED'].includes(i.status)).reduce((sum, i) => sum + i.amount, 0);
  }

  get activePortfolios() {
    return new Set(this.investments.filter(i => ['COMPLETED', 'SUCCESS', 'APPROVED'].includes(i.status)).map(i => i.startupId)).size;
  }

  constructor(
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

    this.invService.getMyInvestments().subscribe(res => {
      this.investments = res.sort((a,b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
      this.initChart();
      
      this.notifService.getNotifications().subscribe(n => {
        this.notifications = n.slice(0, 5);
        this.loading = false;
      });
    });
  }

  initChart() {
    const approved = this.investments.filter(i => ['COMPLETED', 'SUCCESS', 'APPROVED'].includes(i.status));
    
    // Group by startup
    const grouped = approved.reduce((acc, curr) => {
      acc[curr.startupName || `Startup ${curr.startupId}`] = (acc[curr.startupName || `Startup ${curr.startupId}`] || 0) + curr.amount;
      return acc;
    }, {} as Record<string, number>);

    const pieData = Object.keys(grouped).map(k => ({ name: k, value: grouped[k] }));

    this.chartOptions = {
      tooltip: { trigger: 'item', formatter: '{b}: ${c} ({d}%)' },
      legend: { bottom: '0%', textStyle: { color: '#94A3B8' } },
      series: [
        {
          name: 'Portfolio',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: { borderRadius: 10, borderColor: '#1E293B', borderWidth: 2 },
          label: { show: false },
          data: pieData.length ? pieData : [{name: 'Empty', value: 0}]
        }
      ]
    };

    // Fake trend line for visuals
    this.trendOptions = {
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'], axisLine: { lineStyle: { color: '#94A3B8'} } },
        yAxis: { type: 'value', splitLine: { lineStyle: { color: '#334155' } } },
        series: [{ data: [120, 200, 150, 80, 70, 110].map(v => v * (this.activePortfolios + 1)), type: 'line', smooth: true, itemStyle: { color: '#10B981' }, areaStyle: { color: 'rgba(16, 185, 129, 0.2)' } }]
    };
  }
}
