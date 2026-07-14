// Handles patient and doctor registration forms with validation and API submit.
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { api } from '../api';

export default function RegisterPage({ fixedRole = 'PATIENT', title = 'Create account' }) {
  const { register, handleSubmit, watch, formState: { errors, isSubmitting } } = useForm({
    defaultValues: { role: fixedRole, gender: 'FEMALE' },
  });
  const role = fixedRole || watch('role');
  const navigate = useNavigate();

  const submit = async (values) => {
    const payload = { ...values };
    if (role === 'DOCTOR') {
      delete payload.gender;
      delete payload.date_of_birth;
    }
    delete payload.role;
    try {
      await api.post(role === 'DOCTOR' ? '/auth/doctor/register' : '/auth/patient/register', payload);
      toast.success(role === 'DOCTOR' ? 'Doctor registration submitted for admin approval' : 'Registration successful');
      navigate(role === 'DOCTOR' ? '/doctor/login' : '/patient/login');
    } catch (error) {
      const detail = error.response?.data?.detail;
      toast.error(typeof detail === 'string' ? detail : 'Please correct the registration details');
    }
  };

  return <main className="auth-shell register-shell">
    <button className="corner-back-button" type="button" onClick={() => navigate(-1)} aria-label="Go back">&larr;</button>
    <section className="auth-visual" aria-label="Registration overview">
      <div className="auth-nav">
        <div className="brand-lockup"><span className="brand-mark">M</span><strong>MediSlot</strong></div>
        <div className="auth-nav-actions">
          <Link to={role === 'DOCTOR' ? '/doctor/login' : '/patient/login'}>Sign in</Link>
        </div>
      </div>
      <div className="hero-copy">
        <p className="eyebrow">Join the care network</p>
        <h1>{role === 'DOCTOR' ? 'Grow your clinic with smarter scheduling.' : 'Meet the right doctor, right when you need care.'}</h1>
        <p>{role === 'DOCTOR' ? 'Publish availability, review appointments, and keep patient activity beautifully organized.' : 'Create one account to discover specialists, reserve slots, and complete payment.'}</p>
      </div>
      <div className="doctor-showcase register-showcase" aria-hidden="true">
        <div className="doctor-photo"></div>
        <div className="floating-card appointment-card">
          <small>{role === 'DOCTOR' ? 'Practice setup' : 'Care plan'}</small>
          <strong>{role === 'DOCTOR' ? 'Profile review' : '3 easy steps'}</strong>
          <span>{role === 'DOCTOR' ? 'Pending admin approval' : 'Search, book, pay'}</span>
        </div>
        <div className="floating-card rating-card">
          <strong>24/7</strong>
          <span>Access</span>
        </div>
      </div>
    </section>
    <section className="auth-card register-card">
      <p className="eyebrow">{role.toLowerCase()} onboarding</p>
      <h2>{title}</h2>
      <p className="form-intro">{role === 'DOCTOR' ? 'Doctor accounts are reviewed before login access is enabled.' : 'Your patient account gives you access to booking and visit history.'}</p>
      <form onSubmit={handleSubmit(submit)}>
        <label>Full name<input {...register('full_name', { required: true, minLength: 2, pattern: /^[A-Za-z ]+$/ })} /></label>
        {errors.full_name && <small>Enter a valid name</small>}
        <label>Email<input type="email" {...register('email', { required: true })} /></label>
        <label>Phone<input inputMode="numeric" {...register('phone', { required: true, pattern: /^\d{10}$/ })} /></label>
        <label>Password<input type="password" {...register('password', { required: true, pattern: /^(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{8,12}$/ })} /></label>
        {!fixedRole && <label>Account type<select {...register('role')}><option>PATIENT</option><option>DOCTOR</option></select></label>}
        {role === 'PATIENT' && <>
          <label>Gender<select {...register('gender')}><option>FEMALE</option><option>MALE</option><option>OTHER</option></select></label>
          <label>Date of birth<input type="date" {...register('date_of_birth', { required: true })} /></label>
        </>}
        {role === 'DOCTOR' && <>
          <label>Qualification<input {...register('qualification', { required: true })} /></label>
          <label>Specialization<input {...register('specialization', { required: true })} /></label>
          <label>Experience<input type="number" min="0" {...register('experience', { valueAsNumber: true })} /></label>
          <label>License number<input {...register('license_number', { required: true })} /></label>
          <label>Consultation fee<input type="number" min="0" {...register('consultation_fee', { required: true, valueAsNumber: true })} /></label>
          <label>Clinic address<textarea {...register('clinic_address', { required: true })} /></label>
        </>}
        <button disabled={isSubmitting}>Register</button>
      </form>
      <p>Already registered? <Link to={role === 'DOCTOR' ? '/doctor/login' : '/patient/login'}>Sign in</Link></p>
      <p className="auth-links">
        <Link to="/patient/register">Patient register</Link>
        <Link to="/doctor/register">Doctor register</Link>
        <Link to="/admin/login">Admin login</Link>
      </p>
    </section>
  </main>;
}
