import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [RouterLink, CommonModule],
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.scss']
})
export class LandingComponent {
  stats = [
    { value: '2,400+', label: 'Startups Listed' },
    { value: '$180M+', label: 'Capital Deployed' },
    { value: '900+',   label: 'Active Investors' },
    { value: '94%',    label: 'Match Accuracy' },
  ];

  features = [
    { icon: 'pi-briefcase',  title: 'Curated Deal Flow',    desc: 'AI-filtered startup listings matched to your thesis — no noise, just signal.' },
    { icon: 'pi-shield',     title: 'Verified Founders',    desc: 'Every founder and co-founder is verified. Trust built into the infrastructure.' },
    { icon: 'pi-chart-line', title: 'Real-Time Tracking',   desc: 'Live portfolio dashboards, cap table snapshots, and investor updates in one place.' },
    { icon: 'pi-comments',   title: 'Direct Messaging',     desc: 'Founders and investors communicate directly — no intermediaries, no friction.' },
    { icon: 'pi-users',      title: 'Team Building',        desc: 'Invite co-founders, set equity terms, and manage your team from day one.' },
    { icon: 'pi-bell',       title: 'Smart Notifications',  desc: 'Get alerted when startups you follow raise rounds or post milestone updates.' },
  ];
}
