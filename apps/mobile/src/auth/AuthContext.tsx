import { createContext, ReactNode, useContext, useEffect, useState } from 'react';
import { getMe, login as apiLogin, register as apiRegister, updateProfile as apiUpdateProfile } from '../api/auth';
import { getToken, setToken } from '../api/client';
import type { UserProfile } from '../api/types';

interface AuthContextValue {
  user: UserProfile | null;
  ready: boolean; // initial token bootstrap finished
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string, nickname: string) => Promise<void>;
  logout: () => void;
  updateProfile: (data: { nickname?: string; avatarUrl?: string }) => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserProfile | null>(null);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    (async () => {
      if (getToken()) {
        try {
          setUser(await getMe());
        } catch {
          setToken(null); // stale/expired token
        }
      }
      setReady(true);
    })();
  }, []);

  const value: AuthContextValue = {
    user,
    ready,
    login: async (email, password) => setUser(await apiLogin(email, password)),
    register: async (email, password, nickname) => setUser(await apiRegister(email, password, nickname)),
    logout: () => {
      setToken(null);
      setUser(null);
    },
    updateProfile: async (data) => setUser(await apiUpdateProfile(data)),
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within <AuthProvider>');
  return ctx;
}
