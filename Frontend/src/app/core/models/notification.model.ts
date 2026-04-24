export interface Notification {
  id: number;
  userEmail: string;
  title: string;
  message: string;
  type: 'INVESTMENT' | 'TEAM' | 'SYSTEM';
  status: 'UNREAD' | 'READ';
  createdAt: string;
}
