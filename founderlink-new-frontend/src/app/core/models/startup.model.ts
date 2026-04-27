export interface Startup {
  id?: number;
  name?: string;
  tagline?: string;
  description?: string;
  founderEmail?: string;
  category?: string;
  currentRound?: string;
  stage?: string;
  status?: string;
  targetAmount?: number;
  raisedAmount?: number;
  equityOffered?: number;
  websiteUrl?: string;
  logoUrl?: string;
  linkedinUrl?: string;
  twitterUrl?: string;
  followersCount?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface StartupDocument {
  id?: number;
  startupId?: number;
  name?: string;
  url?: string;
  docType?: string;
  createdAt?: string;
  title?: string;
  documentUrl?: string;
  documentType?: string;
  uploadedAt?: string;
}

export interface StartupUpdate {
  id?: number;
  startupId?: number;
  title?: string;
  content?: string;
  createdAt?: string;
  postedAt?: string;
}
