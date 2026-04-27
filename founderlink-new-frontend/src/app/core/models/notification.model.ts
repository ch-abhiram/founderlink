export interface Notification {
  id: number;
  type: string;
  title?: string;
  message: string;
  read: boolean;
  createdAt: string;
}
