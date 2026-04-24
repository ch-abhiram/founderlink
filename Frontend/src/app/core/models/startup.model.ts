export interface Startup {
  id: number;
  name: string;
  description: string;
  founderEmail: string;
  tagline?: string;
  location?: string;
  foundedYear?: number;
  teamSize?: number;
  mrr?: number;
  logoUrl?: string;
  fundingGoal: number;
  currentFunding: number;
  category: string;
  stage?: string;
  currentRound?: string;
  valuation?: number;
  status: 'PENDING' | 'OPEN' | 'CLOSED' | 'REJECTED';
  followersCount: number;
  createdAt: string;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface StartupUpdate { 
  id: number; 
  startupId: number; 
  title: string; 
  content: string; 
  createdAt: string; 
}

export interface StartupDocument { 
  id: number; 
  startupId: number; 
  name: string; 
  url: string; 
  docType?: string; 
  createdAt: string; 
}
