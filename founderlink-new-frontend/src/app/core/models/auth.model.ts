export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  email: string;
  role: string;
  tokenType?: string;
}

export interface RegisterResponse {
  message: string;
  email?: string;
}
