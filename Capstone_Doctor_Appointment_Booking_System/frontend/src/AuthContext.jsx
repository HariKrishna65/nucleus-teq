// Provides authentication state, token persistence, and auth actions to the UI.
import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { toast } from 'react-toastify';
import { api } from './api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('accessToken'));
  const [user, setUser] = useState(null);
  const refreshUser = async () => {
    if (!token) {
      setUser(null);
      return;
    }
    try {
      const { data } = await api.get('/profile/me');
      setUser(data);
    } catch {
      localStorage.removeItem('accessToken');
      setToken(null);
      setUser(null);
    }
  };
  useEffect(() => { refreshUser(); }, [token]);
  useEffect(() => {
    const expire = () => {
      setToken(null);
      setUser(null);
      toast.error('Session expired. Please sign in again.');
    };
    window.addEventListener('session-expired', expire);
    return () => window.removeEventListener('session-expired', expire);
  }, []);

  const value = useMemo(() => ({
    token, user, refreshUser,
    async login(email, password, expectedRole) {
      const rolePath = (expectedRole || 'PATIENT').toLowerCase();
      const { data } = await api.post(`/auth/${rolePath}/login`, { email, password });
      localStorage.setItem('accessToken', data.access_token);
      setToken(data.access_token);
      const profile = await api.get('/profile/me').then((response) => response.data);
      if (expectedRole && profile.role !== expectedRole) {
        localStorage.removeItem('accessToken');
        setToken(null);
        setUser(null);
        throw new Error(`Please use the ${expectedRole.toLowerCase()} login page.`);
      }
      setUser(profile);
      return profile;
    },
    logout() {
      localStorage.removeItem('accessToken');
      setToken(null); setUser(null);
    },
  }), [token, user]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export const useAuth = () => useContext(AuthContext);
