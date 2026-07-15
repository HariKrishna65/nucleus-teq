// Provides authentication state, token persistence, and auth actions to the UI.
import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { toast } from 'react-toastify';
import { api } from './api';

const AuthContext = createContext(null);

const storedUser = () => {
  try {
    return JSON.parse(localStorage.getItem('authUser'));
  } catch {
    return null;
  }
};

const profilePathForRole = (role) => {
  if (role === 'DOCTOR') return '/doctor/profile';
  if (role === 'ADMIN') return '/admin/profile';
  return '/patient/profile';
};

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('accessToken'));
  const [user, setUser] = useState(storedUser);
  const refreshUser = async () => {
    if (!token) {
      setUser(null);
      return;
    }
    try {
      const savedRole = localStorage.getItem('authRole');
      if (!savedRole) throw new Error('Missing saved role');
      const { data } = await api.get(profilePathForRole(savedRole));
      localStorage.setItem('authRole', data.role);
      localStorage.setItem('authUser', JSON.stringify(data));
      setUser(data);
    } catch {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('authRole');
      localStorage.removeItem('authUser');
      setToken(null);
      setUser(null);
    }
  };
  useEffect(() => {
    if (token && !user) refreshUser();
    if (!token) setUser(null);
  }, [token]);
  useEffect(() => {
    const expire = () => {
      localStorage.removeItem('authRole');
      localStorage.removeItem('authUser');
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
      const profile = data.user;
      if (expectedRole && profile.role !== expectedRole) {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('authRole');
        localStorage.removeItem('authUser');
        setToken(null);
        setUser(null);
        throw new Error(`Please use the ${expectedRole.toLowerCase()} login page.`);
      }
      localStorage.setItem('accessToken', data.access_token);
      localStorage.setItem('authRole', profile.role);
      localStorage.setItem('authUser', JSON.stringify(profile));
      setToken(data.access_token);
      setUser(profile);
      return profile;
    },
    logout() {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('authRole');
      localStorage.removeItem('authUser');
      setToken(null); setUser(null);
    },
  }), [token, user]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export const useAuth = () => useContext(AuthContext);
