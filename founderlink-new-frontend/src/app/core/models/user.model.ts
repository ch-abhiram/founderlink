export interface User {
  id: number;
  email: string;
  name: string;
  role: string;
  bio?: string;
  experience?: string;
  headline?: string;
  location?: string;
  avatarUrl?: string;
  primaryGoal?: string;
  skills?: string[];
  portfolioLinks?: string[];
  createdAt?: string;
}

export interface UserPreference {
  id: number;
  userEmail: string;
  industries?: string[];
  stages?: string[];
  fundingRange?: string;
  collabStyle?: string;
  linkedinUrl?: string;
  updatedAt?: string;
}
