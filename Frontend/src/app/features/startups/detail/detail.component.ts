import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { StartupService } from '../../../core/services/startup.service';
import { TeamService } from '../../../core/services/team.service';
import { AuthService } from '../../../core/services/auth.service';
import { Startup, StartupUpdate, StartupDocument } from '../../../core/models/startup.model';
import { TeamMember } from '../../../core/models/team.model';
import { CurrencyFormatPipe } from '../../../shared/pipes/currency-format.pipe';
import { TimeAgoPipe } from '../../../shared/pipes/time-ago.pipe';
import { TagModule } from 'primeng/tag';
import { ButtonModule } from 'primeng/button';
import { TabViewModule } from 'primeng/tabview';
import { MessageService } from 'primeng/api';
import { DialogModule } from 'primeng/dialog';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { environment } from '../../../../environments/environment';
import { InvestmentService } from '../../../core/services/investment.service';

declare var window: any;

@Component({
  selector: 'app-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, CurrencyFormatPipe, TimeAgoPipe, TagModule, ButtonModule, TabViewModule, DialogModule, InputNumberModule, InputTextModule, ReactiveFormsModule],
  templateUrl: './detail.component.html',
  styles: ``
})
export class DetailComponent implements OnInit {
  startup: Startup | null = null;
  team: TeamMember[] = [];
  updates: StartupUpdate[] = [];
  documents: StartupDocument[] = [];
  loading = true;
  
  isFounder = false;
  isInvestor = false;
  isAuthenticated = false;
  isFollowing = false;

  showInvestModal = false;
  investForm: FormGroup;
  investing = false;

  constructor(
    private route: ActivatedRoute,
    private startupService: StartupService,
    private teamService: TeamService,
    private authService: AuthService,
    private msg: MessageService,
    private fb: FormBuilder,
    private invService: InvestmentService
  ) {
    this.investForm = this.fb.group({
      amount: [10000, [Validators.required, Validators.min(1000)]],
      investorFirm: ['']
    });
  }

  ngOnInit() {
    this.isAuthenticated = this.authService.isAuthenticated();
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.loadStartup(+id);
      }
    });
  }

  loadStartup(id: number) {
    this.loading = true;
    this.startupService.getById(id).subscribe({
      next: (res) => {
        this.startup = res;
        this.loading = false;
        this.checkPermissions();
        this.loadAdditionalData(id);
      },
      error: () => this.loading = false
    });
  }

  checkPermissions() {
    if (!this.startup || !this.isAuthenticated) return;
    const currentUserEmail = this.authService.getEmail();
    const role = this.authService.getRole();
    if (this.startup.founderEmail === currentUserEmail || role === 'ROLE_FOUNDER') {
      this.isFounder = true;
    }
    if (role === 'ROLE_INVESTOR') {
      this.isInvestor = true;
    }
    
    // check following
    this.startupService.getFollowers(this.startup.id).subscribe(followers => {
      this.isFollowing = followers.includes(currentUserEmail || '');
    });
  }

  loadAdditionalData(id: number) {
    this.teamService.getTeamForStartup(id).subscribe(t => this.team = t.filter(x => x.status === 'ACCEPTED'));
    this.startupService.getUpdates(id).subscribe(u => this.updates = u);
    this.startupService.getDocuments(id).subscribe(d => this.documents = d);
  }

  toggleFollow() {
    if (!this.startup) return;
    if (this.isFollowing) {
      this.startupService.unfollow(this.startup.id).subscribe(() => {
        this.isFollowing = false;
        this.startup!.followersCount--;
      });
    } else {
      this.startupService.follow(this.startup.id).subscribe(() => {
        this.isFollowing = true;
        this.startup!.followersCount++;
      });
    }
  }

  initiateInvestment() {
    if (!this.isAuthenticated) {
      this.msg.add({severity:'warn', summary:'Login Required', detail:'Please login as an Investor to commit capital.'});
      return;
    }
    if (!this.isInvestor) {
      this.msg.add({severity:'error', summary:'Access Denied', detail:'Only registered Investors can commit capital.'});
      return;
    }
    this.showInvestModal = true;
  }

  proceedWithInvestment() {
    if (this.investForm.invalid || !this.startup) return;
    this.investing = true;
    
    const payload = {
      startupId: this.startup.id,
      amount: this.investForm.value.amount,
      investorFirm: this.investForm.value.investorFirm
    };

    // 1. Create investment request (PENDING status on backend)
    this.invService.createInvestment(payload).subscribe({
      next: (inv: any) => {
        // 2. Open Razorpay Window
        const options = {
          key: environment.razorpayKeyId,
          amount: payload.amount * 100, // in paise/cents
          currency: 'USD',
          name: this.startup?.name,
          description: `Investment in ${this.startup?.name}`,
          image: this.startup?.logoUrl || 'https://dummyimage.com/128x128/6366f1/ffffff.png&text=FL',
          handler: (response: any) => {
             // 3. On success, update investment status to COMPLETED
             this.invService.updateStatus(inv.id, 'COMPLETED').subscribe(() => {
               this.msg.add({severity:'success', summary:'Investment Successful', detail:'Your capital has been securely transferred via Razorpay.'});
               this.showInvestModal = false;
               this.investing = false;
               this.loadStartup(this.startup!.id); // reload funding state
             });
          },
          prefill: {
            email: this.authService.getEmail()
          },
          theme: {
            color: '#6366f1' // Primary indigo
          },
          modal: {
            ondismiss: () => {
              this.investing = false;
              this.msg.add({severity:'info', summary:'Cancelled', detail:'Payment cancelled.'});
            }
          }
        };

        if (typeof window.Razorpay === 'undefined') {
          this.msg.add({severity:'error', summary:'Error', detail:'Razorpay SDK not loaded.'});
          this.investing = false;
          return;
        }

        const rzp = new window.Razorpay(options);
        rzp.open();
      },
      error: () => this.investing = false
    });
  }
}

