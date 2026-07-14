// Handles role-specific sign-in and stores the returned access token.
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { useAuth } from '../AuthContext';
import { api } from '../api';

export default function LoginPage({ role = 'PATIENT', title = 'Patient login', registerPath = '/patient/register', showRegister = true }) {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm();
  const { login } = useAuth();
  const navigate = useNavigate();
  
  const [inactiveAccount, setInactiveAccount] = useState(false);
  const [loginData, setLoginData] = useState(null);

  const submit = async ({ email, password }) => {
    setInactiveAccount(false);
    try {
      await login(email, password, role);
      toast.success('Login successful');
      navigate('/dashboard');
    } catch (error) {
      const detail = error.response?.data?.detail || '';
      if (role === 'DOCTOR' && (detail.toLowerCase().includes('inactive') || error.response?.status === 403)) {
        setInactiveAccount(true);
        setLoginData({ email, password });
      }
      toast.error(detail || error.message || 'Login failed');
    }
  };

  return <main className="auth-shell">
    <button className="corner-back-button" type="button" onClick={() => navigate(-1)} aria-label="Go back">&larr;</button>
    <section className="auth-visual" aria-label="MediSlot overview">
      <div className="auth-nav">
        <div className="brand-lockup"><span className="brand-mark">M</span><strong>MediSlot</strong></div>
        <div className="auth-nav-actions">
          <Link to="/">Home</Link>
        </div>
      </div>
      <div className="hero-copy">
        <p className="eyebrow">Trusted healthcare booking</p>
        <h1>Book your doctor appointment in minutes.</h1>
        <p>Search specialists, reserve available slots, and keep your care journey organized from one bright workspace.</p>
      </div>
      <div className="doctor-showcase" aria-hidden="true">
        <div className="doctor-photo"></div>
        <div className="hero-search">
          <span>Find doctor</span>
          <strong>Cardiology near you</strong>
        </div>
        <div className="floating-card appointment-card">
          <small>Next appointment</small>
          <strong>10:30 AM</strong>
          <span>Consultation confirmed</span>
        </div>
        <div className="floating-card rating-card">
          <strong>4.9</strong>
          <span>Patient rating</span>
        </div>
      </div>
    </section>
    <section className="auth-card">
      <p className="eyebrow">{role.toLowerCase()} access</p>
      <h2>{title}</h2>
      <p className="form-intro">Welcome back. Continue to your secure care dashboard.</p>
      <form onSubmit={handleSubmit(submit)}>
        <label>Email
          <input type="email" {...register('email', { 
            required: 'Email is required',
            pattern: {
              value: /^[a-zA-Z0-9._%+-]+@gmail\.com$/,
              message: 'Email must end with @gmail.com'
            }
          })} />
        </label>
        {errors.email && <small>{errors.email.message}</small>}
        <label>Password<input type="password" {...register('password', { required: 'Password is required' })} /></label>
        {errors.password && <small>{errors.password.message}</small>}
        <button disabled={isSubmitting}>Sign in</button>
      </form>

      {inactiveAccount && (
        <div style={{ marginTop: '1.2rem', padding: '1rem', border: '1px solid var(--line)', borderRadius: '8px', background: '#fff9e6' }}>
          <p style={{ margin: 0, fontSize: '0.9rem', color: '#8a6d3b', fontWeight: 'bold' }}>Your account is deactivated.</p>
          <p style={{ margin: '0.2rem 0 0', fontSize: '0.85rem', color: '#8a6d3b' }}>Would you like to request admin reactivation?</p>
          <button type="button" style={{ marginTop: '0.6rem', minHeight: '36px', background: 'linear-gradient(135deg, var(--orange), #ffb88c)', boxShadow: 'none' }} onClick={async () => {
            try {
              await api.post('/auth/doctor/request-activation', loginData);
              toast.success('Reactivation request submitted for admin approval');
              setInactiveAccount(false);
            } catch (err) {
              toast.error(err.response?.data?.detail || 'Failed to submit reactivation request');
            }
          }}>
            Request Reactivation
          </button>
        </div>
      )}

      {showRegister && <p style={{ marginTop: '1rem' }}>New here? <Link to={registerPath}>Create an account</Link></p>}
      <p className="auth-links">
        <Link to="/patient/login">Patient</Link>
        <Link to="/doctor/login">Doctor</Link>
        <Link to="/admin/login">Admin</Link>
      </p>
    </section>
  </main>;
}
