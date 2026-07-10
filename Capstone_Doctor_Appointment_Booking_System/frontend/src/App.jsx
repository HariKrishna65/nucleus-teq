import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './hooks/useAuth';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';

export default function App() {
  const { token } = useAuth();
  return <Routes>
    <Route path="/login" element={<Navigate to="/patient/login" replace />} />
    <Route path="/register" element={<Navigate to="/patient/register" replace />} />
    <Route path="/patient/login" element={<LoginPage role="PATIENT" title="Patient login" registerPath="/patient/register" />} />
    <Route path="/doctor/login" element={<LoginPage role="DOCTOR" title="Doctor login" registerPath="/doctor/register" />} />
    <Route path="/admin/login" element={<LoginPage role="ADMIN" title="Admin login" showRegister={false} />} />
    <Route path="/patient/register" element={<RegisterPage fixedRole="PATIENT" title="Patient registration" />} />
    <Route path="/doctor/register" element={<RegisterPage fixedRole="DOCTOR" title="Doctor registration" />} />
    <Route path="/" element={token ? <DashboardPage /> : <Navigate to="/patient/login" replace />} />
  </Routes>;
}
