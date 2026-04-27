export interface TeamMember {
  id: number;
  startupId: number;
  userEmail: string;
  role: 'COFOUNDER' | 'EMPLOYEE' | 'ADVISOR' | 'INTERN';
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED';
  equityPercentage: number;
  permissionLevel: 'OWNER' | 'ADMIN' | 'MEMBER';
  createdAt: string;
}
