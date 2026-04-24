import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { UserService } from '../../../core/services/user.service';
import { User } from '../../../core/models/user.model';
import { TimeAgoPipe } from '../../../shared/pipes/time-ago.pipe';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, RouterLink, TimeAgoPipe],
  template: `
    <div class="max-w-6xl mx-auto flex flex-col gap-6">
      <div class="flex justify-between items-end border-b border-surface-border pb-6">
        <div>
          <h1 class="text-3xl font-bold text-text-primary">Admin Control Center</h1>
          <p class="text-text-muted mt-1">Review and action platform entities. Currently showing <span class="text-primary font-bold">Users Management</span>.</p>
        </div>
      </div>

      <div class="bg-surface-card border border-surface-border rounded-xl flex">
        <div class="w-64 border-r border-surface-border hidden md:block">
          <div class="p-4 flex flex-col gap-2 font-medium">
            <a routerLink="/admin/pending-startups" class="p-3 hover:bg-surface text-text-muted hover:text-text-primary rounded-lg">Pending Startups</a>
            <a class="p-3 bg-primary/10 text-primary rounded-lg border-l-4 border-primary">Users Management</a>
            <a routerLink="/admin/investments" class="p-3 hover:bg-surface text-text-muted hover:text-text-primary rounded-lg">Investments Log</a>
          </div>
        </div>

        <div class="flex-1 min-h-[500px] overflow-x-auto">
          <table class="w-full text-left text-sm text-text-primary">
            <thead class="bg-surface text-text-muted uppercase text-xs">
              <tr>
                <th class="px-6 py-4">Name</th>
                <th class="px-6 py-4">Email</th>
                <th class="px-6 py-4">Role</th>
                <th class="px-6 py-4">Location</th>
                <th class="px-6 py-4">Joined</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-surface-border">
              <tr *ngFor="let user of users" class="hover:bg-surface/50 transition-colors">
                <td class="px-6 py-4">
                  <div class="font-semibold">{{ user.name || 'Unnamed User' }}</div>
                  <div class="text-xs text-text-muted mt-1">{{ user.headline || user.primaryGoal || 'No headline added' }}</div>
                </td>
                <td class="px-6 py-4">{{ user.email }}</td>
                <td class="px-6 py-4">{{ user.role }}</td>
                <td class="px-6 py-4">{{ user.location || 'Not set' }}</td>
                <td class="px-6 py-4 text-text-muted">{{ user.createdAt ? (user.createdAt | timeAgo) : 'Unknown' }}</td>
              </tr>
              <tr *ngIf="loading">
                <td colspan="5" class="px-6 py-12 text-center text-text-muted">Loading users...</td>
              </tr>
              <tr *ngIf="!loading && users.length === 0">
                <td colspan="5" class="px-6 py-12 text-center text-text-muted">No users found.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `
})
export class UsersComponent implements OnInit {
  users: User[] = [];
  loading = true;

  constructor(private userService: UserService) {}

  ngOnInit() {
    this.userService.getAllUsers().subscribe({
      next: (users) => {
        this.users = users.sort((a, b) => (a.name || a.email).localeCompare(b.name || b.email));
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }
}
