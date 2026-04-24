export interface Investment {
  id: number;
  startupId: number;
  investorEmail: string;
  investorFirm?: string;
  founderEmail?: string;
  amount: number;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'COMPLETED' | 'SUCCESS' | 'FAILED';
  createdAt: string;
  startupName?: string;
}
