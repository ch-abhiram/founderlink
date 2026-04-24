import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MessagingService } from '../../../core/services/messaging.service';
import { AuthService } from '../../../core/services/auth.service';
import { Conversation, Message } from '../../../core/models/messaging.model';
import { TimeAgoPipe } from '../../../shared/pipes/time-ago.pipe';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';

@Component({
  selector: 'app-inbox',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, TimeAgoPipe, ButtonModule, InputTextModule],
  templateUrl: './inbox.component.html',
  styles: ``
})
export class InboxComponent implements OnInit, OnDestroy {
  conversations: Conversation[] = [];
  selectedConv: Conversation | null = null;
  messages: Message[] = [];
  
  messageForm: FormGroup;
  currentUserEmail = '';
  loading = true;
  loadingMsgs = false;

  private pollInterval: any;

  constructor(
    private msgService: MessagingService,
    private auth: AuthService,
    private fb: FormBuilder
  ) {
    this.currentUserEmail = this.auth.getEmail() || '';
    this.messageForm = this.fb.group({
      content: ['', Validators.required]
    });
  }

  ngOnInit() {
    this.loadConversations();
    // Simple polling for new messages every 15s
    this.pollInterval = setInterval(() => {
      if (this.selectedConv) {
         this.refreshMessages(this.selectedConv.id);
      }
    }, 15000);
  }

  ngOnDestroy() {
    if (this.pollInterval) clearInterval(this.pollInterval);
  }

  loadConversations() {
    this.msgService.getMyConversations().subscribe(res => {
      this.conversations = res.sort((a,b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime());
      this.loading = false;
    });
  }

  selectConversation(c: Conversation) {
    this.selectedConv = c;
    this.loadingMsgs = true;
    this.messages = [];
    this.msgService.getConversationMessages(c.id).subscribe(res => {
      this.messages = res.sort((a,b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime());
      this.loadingMsgs = false;
      this.scrollToBottom();
    });
  }

  refreshMessages(convId: number) {
    this.msgService.getConversationMessages(convId).subscribe(res => {
      const sorted = res.sort((a,b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime());
      if (sorted.length > this.messages.length) {
         this.messages = sorted;
         this.scrollToBottom();
      }
    });
  }

  sendMessage() {
    if (this.messageForm.invalid || !this.selectedConv) return;
    const content = this.messageForm.value.content;
    const isFounderConversation = this.selectedConv.founderEmail === this.currentUserEmail;
    const payload = {
      startupId: this.selectedConv.startupId,
      participantEmail: isFounderConversation ? this.selectedConv.participantEmail : undefined,
      content: content
    };

    // Optimistic UI update
    const tempMsg: Message = {
      id: Date.now(),
      conversationId: this.selectedConv.id,
      senderEmail: this.currentUserEmail,
      content,
      createdAt: new Date().toISOString()
    };
    this.messages.push(tempMsg);
    this.messageForm.reset();
    this.scrollToBottom();

    this.msgService.sendMessage(payload).subscribe(() => {
      this.refreshMessages(this.selectedConv!.id);
      this.loadConversations();
    });
  }

  getOtherParty(c: Conversation) {
    if (c.founderEmail === this.currentUserEmail) {
      return c.participantEmail;
    }
    return c.founderEmail || 'Founder';
  }

  scrollToBottom() {
    setTimeout(() => {
      const el = document.getElementById('chatContainer');
      if (el) el.scrollTop = el.scrollHeight;
    }, 50);
  }
}
