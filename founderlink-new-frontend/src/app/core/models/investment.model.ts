export interface Investment {
  id?: number;
  startupId?: number;
  startupName?: string;
  investorEmail?: string;
  amount: number;
  equityPercentage?: number;
  status?: string;
  createdAt?: string;
}
