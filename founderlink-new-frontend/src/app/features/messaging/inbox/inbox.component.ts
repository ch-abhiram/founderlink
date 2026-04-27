import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MessagingService } from '../../../core/services/messaging.service';
import { AuthService } from '../../../core/services/auth.service';
import { Conversation, Message } from '../../../core/models/messaging.model';
import { interval, Subscription } from 'rxjs';
import { switchMap, startWith } from 'rxjs/operators';

@Component({
  selector: 'app-inbox',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './inbox.component.html',
  styleUrls: ['./inbox.component.scss']
})
export class InboxComponent implements OnInit, OnDestroy {
  conversations: Conversation[] = [];
  messages: Message[] = [];
  selectedConv: Conversation | null = null;
  newMessage = '';
  loading = true; sending = false;
  currentUserEmail = '';
  private subs: Subscription[] = [];

  constructor(private msgSvc: MessagingService, private authSvc: AuthService) {}

  ngOnInit() {
    this.currentUserEmail = this.authSvc.getEmail() || '';
    this.loadConversations();
  }

  loadConversations() {
    this.msgSvc.getMyConversations().subscribe({
      next: convs => { this.conversations = convs; this.loading = false; if (convs.length > 0) this.selectConv(convs[0]); },
      error: () => this.loading = false
    });
  }

  selectConv(conv: Conversation) {
    this.selectedConv = conv;
    this.subs.forEach(s => s.unsubscribe());
    const poll$ = interval(5000).pipe(startWith(0), switchMap(() => this.msgSvc.getConversationMessages(conv.id))).subscribe(msgs => this.messages = msgs);
    this.subs.push(poll$);
  }

  send() {
    if (!this.newMessage.trim() || !this.selectedConv || this.sending) return;
    this.sending = true;
    this.msgSvc.sendMessage({
      startupId: this.selectedConv.startupId,
      participantEmail: this.selectedConv.participantEmail,
      content: this.newMessage
    }).subscribe({
      next: () => { this.newMessage = ''; this.sending = false; },
      error: () => this.sending = false
    });
  }

  getOtherParty(c: Conversation): string {
    if (c.founderEmail === this.currentUserEmail) return c.participantEmail || 'Investor';
    return c.founderEmail || 'Founder';
  }

  ngOnDestroy() { this.subs.forEach(s => s.unsubscribe()); }
}
