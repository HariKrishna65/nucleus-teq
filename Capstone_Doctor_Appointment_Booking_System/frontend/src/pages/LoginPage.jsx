// Handles role-specific sign-in and stores the returned access token.
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { useAuth } from '../AuthContext';

export default function LoginPage({ role = 'PATIENT', title = 'Patient login', registerPath = '/patient/register', showRegister = true }) {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm();
  const { login } = useAuth();
  const navigate = useNavigate();

  const submit = async ({ email, password }) => {
    try {
      await login(email, password, role);
      toast.success('Login successful');
      navigate('/dashboard');
    } catch (error) {
      toast.error(error.response?.data?.detail || error.message || 'Login failed');
    }
  };

  return <main className="auth-shell">
    <button className="corner-back-button" type="button" onClick={() => navigate(-1)} aria-label="Go back">&larr;</button>
    <section className="auth-visual" aria-label="MediSlot overview">
      <div className="auth-nav">
        <div className="brand-lockup"><span className="brand-mark">M</span><strong>MediSlot</strong></div>
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
        <label>Email<input type="email" {...register('email', { required: 'Email is required' })} /></label>
        <small>{errors.email?.message}</small>
        <label>Password<input type="password" {...register('password', { required: 'Password is required' })} /></label>
        <small>{errors.password?.message}</small>
        <button disabled={isSubmitting}>Sign in</button>
      </form>
      {showRegister && <p>New here? <Link to={registerPath}>Create an account</Link></p>}
      <p className="auth-links">
        <Link to="/patient/login">Patient</Link>
        <Link to="/doctor/login">Doctor</Link>
        <Link to="/admin/login">Admin</Link>
      </p>
    </section>
  </main>;
}
