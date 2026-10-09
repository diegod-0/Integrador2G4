export interface UserSession {
  email: string;
  nombre: string;
  rol: string;
  accessToken: string;
  expiresIn: number;
}

export interface LoginResponse {
  success: boolean;
  message: string;
  data: UserSession;
  timestamp?: string;
}
