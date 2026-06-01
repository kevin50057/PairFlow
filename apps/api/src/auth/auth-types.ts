// Authenticated principal attached to the request by JwtAuthGuard.
export interface AuthUser {
  id: number;
  email: string;
  nickname: string;
  role: string;
}

export interface JwtPayload {
  sub: number;
  role: string;
}

export interface UserProfile {
  id: number;
  email: string;
  nickname: string;
  avatarUrl: string | null;
  role: string;
  status: string;
  createdAt: string;
}

export interface AuthResult {
  token: string;
  user: UserProfile;
}
