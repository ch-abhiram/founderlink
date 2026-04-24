export interface Conversation { 
  id: number; 
  startupId: number; 
  participantEmail: string; 
  founderEmail?: string;
  createdAt: string; 
  updatedAt: string; 
}

export interface Message { 
  id: number; 
  conversationId: number; 
  senderEmail: string; 
  content: string; 
  createdAt: string; 
}
