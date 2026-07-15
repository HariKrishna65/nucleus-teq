// Handles patient and doctor registration forms with validation and API submit.
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { api } from '../api';

const qualificationOptions = ['MBBS', 'MD', 'MS', 'DNB', 'BDS', 'MDS'];
const specializationOptions = ['Cardiology', 'Dermatology', 'Pediatrics', 'Orthopedics', 'Neurology', 'General Medicine'];
const namePattern = /^[A-Za-z' -]+$/;
const gmailPattern = /^[A-Za-z0-9._%+-]+@gmail\.com$/i;
const passwordPattern = /^(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{8,12}$/;
const todayIso = new Date().toISOString().slice(0, 10);

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

export default function RegisterPage({ fixedRole = 'PATIENT', title = 'Create account' }) {
  const { register, handleSubmit, watch, formState: { errors, isSubmitting } } = useForm({
    defaultValues: { role: fixedRole, gender: 'FEMALE' },
  });
  const role = fixedRole || watch('role');
  const navigate = useNavigate();
  const [showPassword, setShowPassword] = useState(false);

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
        <label>Full name<input {...register('full_name', {
          required: 'Full name is required',
          minLength: { value: 2, message: 'Full name must be at least 2 characters' },
          pattern: { value: namePattern, message: 'Full name must contain only letters, spaces, hyphens, and apostrophes' },
        })} /></label>
        <small>{errors.full_name?.message}</small>
        <label>Email<input type="email" {...register('email', {
          required: 'Email is required',
          pattern: { value: gmailPattern, message: 'Email must end with @gmail.com' },
        })} /></label>
        <small>{errors.email?.message}</small>
        <label>Phone<input inputMode="numeric" {...register('phone', {
          required: 'Phone number is required',
          pattern: { value: /^\d{10}$/, message: 'Phone number must contain exactly 10 digits' },
        })} /></label>
        <small>{errors.phone?.message}</small>
        <label>Password
          <span className="password-field">
            <input type={showPassword ? 'text' : 'password'} {...register('password', {
              required: 'Password is required',
              pattern: { value: passwordPattern, message: 'Password must be 8-12 characters with one uppercase letter and one special character' },
            })} />
            <button type="button" aria-label={showPassword ? 'Hide characters' : 'Show characters'} onClick={() => setShowPassword(!showPassword)}>
              <EyeIcon hidden={showPassword} />
            </button>
          </span>
        </label>
        <small>{errors.password?.message}</small>
        {!fixedRole && <label>Account type<select {...register('role')}><option>PATIENT</option><option>DOCTOR</option></select></label>}
        {role === 'PATIENT' && <>
          <label>Gender<select {...register('gender', { required: 'Gender is required' })}><option>FEMALE</option><option>MALE</option><option>OTHER</option></select></label>
          <small>{errors.gender?.message}</small>
          <label>Date of birth<input type="date" max={todayIso} {...register('date_of_birth', {
            required: 'Date of birth is required',
            validate: (value) => value < todayIso || 'Date of birth must be in the past',
          })} /></label>
          <small>{errors.date_of_birth?.message}</small>
        </>}
        {role === 'DOCTOR' && <>
          <label>Qualification<select {...register('qualification', { required: 'Qualification is required' })}>
            <option value="">Select qualification</option>
            {qualificationOptions.map((option) => <option key={option}>{option}</option>)}
          </select></label>
          <small>{errors.qualification?.message}</small>
          <label>Specialization<select {...register('specialization', { required: 'Specialization is required' })}>
            <option value="">Select specialization</option>
            {specializationOptions.map((option) => <option key={option}>{option}</option>)}
          </select></label>
          <small>{errors.specialization?.message}</small>
          <label>Experience<input type="number" min="0" max="70" {...register('experience', {
            required: 'Experience is required',
            valueAsNumber: true,
            min: { value: 0, message: 'Experience cannot be negative' },
            max: { value: 70, message: 'Experience must be 70 years or less' },
          })} /></label>
          <small>{errors.experience?.message}</small>
          <label>License number<input {...register('license_number', { required: 'License number is required' })} /></label>
          <small>{errors.license_number?.message}</small>
          <label>Consultation fee<input type="number" min="0" {...register('consultation_fee', {
            required: 'Consultation fee is required',
            valueAsNumber: true,
            min: { value: 0, message: 'Consultation fee cannot be negative' },
          })} /></label>
          <small>{errors.consultation_fee?.message}</small>
          <label>Clinic address<textarea {...register('clinic_address', { required: 'Clinic address is required' })} /></label>
          <small>{errors.clinic_address?.message}</small>
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
