import { useState } from 'react';
import Button from '../components/Button';
import InputField from '../components/InputField';

export default function AuthPage({ onLoginSuccess, setToken }) {
  const [mode, setMode] = useState('login');
  const [form, setForm] = useState({ full_name: '', email: '', password: '', phone: '', role: 'PATIENT' });
  const [message, setMessage] = useState('');

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const handleRegister = async (event) => {
    event.preventDefault();
    const response = await fetch('http://127.0.0.1:8000/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(form),
    });

    const data = await response.json();
    setMessage(response.ok ? 'Registration completed' : data.detail || 'Registration failed');
    if (response.ok) {
      setMode('login');
    }
  };

  const handleLogin = async (event) => {
    event.preventDefault();
    const response = await fetch('http://127.0.0.1:8000/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: form.email, password: form.password }),
    });

    const data = await response.json();
    if (response.ok) {
      setToken(data.access_token);
      onLoginSuccess();
    } else {
      setMessage(data.detail || 'Login failed');
    }
  };

  return (
    <div className="card">
      <div style={{ display: 'flex', marginBottom: '1.5rem' }}>
        <Button onClick={() => setMode('login')} active={mode === 'login'}>Login</Button>
        <Button onClick={() => setMode('register')} active={mode === 'register'}>Register</Button>
      </div>

      {mode === 'login' ? (
        <form onSubmit={handleLogin}>
          <InputField label="Email" name="email" value={form.email} onChange={handleChange} placeholder="Enter your email" />
          <InputField label="Password" name="password" type="password" value={form.password} onChange={handleChange} placeholder="Enter your password" />
          <Button type="submit">Login</Button>
        </form>
      ) : (
        <form onSubmit={handleRegister}>
          <InputField label="Full Name" name="full_name" value={form.full_name} onChange={handleChange} placeholder="Enter full name" />
          <InputField label="Email" name="email" value={form.email} onChange={handleChange} placeholder="Enter email" />
          <InputField label="Password" name="password" type="password" value={form.password} onChange={handleChange} placeholder="Enter password" />
          <InputField label="Phone" name="phone" value={form.phone} onChange={handleChange} placeholder="Enter phone" />
          <div style={{ marginBottom: '1rem' }}>
            <label style={{ display: 'block', marginBottom: '0.35rem', fontWeight: 600 }}>Role</label>
            <select name="role" value={form.role} onChange={handleChange} style={{ width: '100%', padding: '0.85rem', borderRadius: '0.75rem' }}>
              <option value="PATIENT">Patient</option>
              <option value="DOCTOR">Doctor</option>
              <option value="ADMIN">Admin</option>
            </select>
          </div>
          <Button type="submit">Register</Button>
        </form>
      )}

      {message && <p style={{ marginTop: '1rem', color: '#334155' }}>{message}</p>}
    </div>
  );
}
