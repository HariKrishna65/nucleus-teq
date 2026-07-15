// Handles role-specific sign-in and stores the returned access token.
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { useAuth } from '../AuthContext';
import { api } from '../api';

function EyeIcon({ hidden }) {
  return (
    <svg aria-hidden="true" viewBox="0 0 24 24">
      {hidden ? (
        <>
          <path d="M3 3l18 18" />
          <path d="M10.6 10.6a2 2 0 0 0 2.8 2.8" />
          <path d="M9.3 5.4A10.8 10.8 0 0 1 12 5c5 0 8.5 4.2 9.7 6.1a1.8 1.8 0 0 1 0 1.8 16.4 16.4 0 0 1-2.4 3" />
          <path d="M6.7 6.7A16.5 16.5 0 0 0 2.3 11a1.8 1.8 0 0 0 0 1.9C3.5 14.8 7 19 12 19c1.5 0 2.9-.4 4.1-1" />
        </>
      ) : (
        <>
          <path d="M2.3 11.1a1.8 1.8 0 0 0 0 1.8C3.5 14.8 7 19 12 19s8.5-4.2 9.7-6.1a1.8 1.8 0 0 0 0-1.8C20.5 9.2 17 5 12 5s-8.5 4.2-9.7 6.1z" />
          <circle cx="12" cy="12" r="3" />
        </>
      )}
    </svg>
  );
}

export default function LoginPage({ role = 'PATIENT', title = 'Patient login', registerPath = '/patient/register', showRegister = true }) {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm();
  const { login } = useAuth();
  const navigate = useNavigate();
  const [showPassword, setShowPassword] = useState(false);

  const submit = async ({ email, password }) => {
    try {
      await login(email, password, role);
      toast.success('Login successful');
      navigate('/dashboard');
    } catch (error) {
      toast.error(error.response?.data?.detail || error.message || 'Login failed');
    }
  };

  const requestActivation = async ({ email, password }) => {
    try {
      await api.post('/auth/doctor/request-activation', { email, password });
      toast.success('Activation request sent for admin approval');
    } catch (error) {
      toast.error(error.response?.data?.detail || 'Activation request failed');
    }
  };

  return <main className="auth-shell">
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
        <label>Email<input type="email" {...register('email', {
          required: 'Email is required',
          pattern: { value: /^[A-Za-z0-9._%+-]+@gmail\.com$/i, message: 'Email must end with @gmail.com' },
        })} /></label>
        <small>{errors.email?.message}</small>
        <label>Password
          <span className="password-field">
            <input type={showPassword ? 'text' : 'password'} {...register('password', { required: 'Password is required' })} />
            <button type="button" aria-label={showPassword ? 'Hide characters' : 'Show characters'} onClick={() => setShowPassword(!showPassword)}>
              <EyeIcon hidden={showPassword} />
            </button>
          </span>
        </label>
        <small>{errors.password?.message}</small>
        <button disabled={isSubmitting}>Sign in</button>
      </form>
      {role === 'DOCTOR' && (
        <button className="secondary request-activation-button" type="button" onClick={handleSubmit(requestActivation)}>
          Request activation
        </button>
      )}
      {showRegister && <p>New here? <Link to={registerPath}>Create an account</Link></p>}
      <p className="auth-links">
        <Link to="/patient/login">Patient</Link>
        <Link to="/doctor/login">Doctor</Link>
        <Link to="/admin/login">Admin</Link>
      </p>
    </section>
  </main>;
}
