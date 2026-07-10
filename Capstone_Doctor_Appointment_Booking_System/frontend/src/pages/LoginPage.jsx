import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { useAuth } from '../hooks/useAuth';

export default function LoginPage({ role = 'PATIENT', title = 'Patient login', registerPath = '/patient/register', showRegister = true }) {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm();
  const { login } = useAuth();
  const navigate = useNavigate();

  const submit = async ({ email, password }) => {
    try {
      await login(email, password, role);
      toast.success('Login successful');
      navigate('/');
    } catch (error) {
      toast.error(error.response?.data?.detail || error.message || 'Login failed');
    }
  };

  return <main className="auth-card">
    <h1>{title}</h1>
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
  </main>;
}
