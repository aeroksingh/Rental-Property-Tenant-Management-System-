import { createContext, useContext, useState, useCallback } from 'react';
import * as authApi from '../api/authApi';

const AuthContext = createContext(null);

function readStoredUser() {
  try {
    const raw = localStorage.getItem('user');
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(readStoredUser);

  const persistSession = useCallback((data) => {
    const sessionUser = {
      id: data.userId,
      name: data.name,
      email: data.email,
      role: data.role,
    };
    localStorage.setItem('token', data.token);
    localStorage.setItem('user', JSON.stringify(sessionUser));
    setUser(sessionUser);
    return sessionUser;
  }, []);

  const login = useCallback(
    async (credentials) => {
      const { data } = await authApi.login(credentials);
      return persistSession(data);
    },
    [persistSession]
  );

  const register = useCallback(
    async (details) => {
      const { data } = await authApi.register(details);
      return persistSession(data);
    },
    [persistSession]
  );

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider');
  return ctx;
}
