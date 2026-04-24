export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  email: string;
  role: string;
}

export interface RegisterResponse {
  message: string;
  email: string;
  role: string;
}
