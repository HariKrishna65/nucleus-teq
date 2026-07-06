import { useState } from 'react';

const API_BASE = 'http://127.0.0.1:8000';

function InputField({ label, name, type = 'text', value, onChange, placeholder }) {
  return (
    <div style={{ marginBottom: '0.75rem' }}>
      {label && <label style={{ display: 'block', marginBottom: '0.25rem' }}>{label}</label>}
      <input
        name={name}
        type={type}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        style={{ width: '100%', padding: '0.6rem' }}
      />
    </div>
  );
}

function NavButton({ label, onClick, active = false }) {
  return (
    <button
      onClick={onClick}
      style={{
        marginRight: '0.5rem',
        padding: '0.55rem 0.8rem',
        border: active ? '2px solid #2563eb' : '1px solid #cbd5e1',
        background: active ? '#dbeafe' : '#fff',
        cursor: 'pointer',
      }}
    >
      {label}
    </button>
  );
}

function RegisterForm({ form, onChange, onSubmit }) {
  return (
    <form onSubmit={onSubmit}>
      <h2>Register</h2>
      <InputField label="Full Name" name="full_name" value={form.full_name} onChange={onChange} placeholder="Full Name" />
      <InputField label="Email" name="email" value={form.email} onChange={onChange} placeholder="Email" />
      <InputField label="Password" name="password" type="password" value={form.password} onChange={onChange} placeholder="Password" />
      <InputField label="Phone" name="phone" value={form.phone} onChange={onChange} placeholder="Phone" />
      <div style={{ marginBottom: '0.75rem' }}>
        <label style={{ display: 'block', marginBottom: '0.25rem' }}>Role</label>
        <select name="role" value={form.role} onChange={onChange} style={{ width: '100%', padding: '0.6rem' }}>
          <option value="PATIENT">Patient</option>
          <option value="DOCTOR">Doctor</option>
          <option value="ADMIN">Admin</option>
        </select>
      </div>
      <button type="submit">Register</button>
    </form>
  );
}

function LoginForm({ form, onChange, onSubmit }) {
  return (
    <form onSubmit={onSubmit}>
      <h2>Login</h2>
      <InputField label="Email" name="email" value={form.email} onChange={onChange} placeholder="Email" />
      <InputField label="Password" name="password" type="password" value={form.password} onChange={onChange} placeholder="Password" />
      <button type="submit">Login</button>
    </form>
  );
}

function ProfileView({ profile, onLoad }) {
  return (
    <div>
      <h2>Profile</h2>
      <button onClick={onLoad}>Load Profile</button>
      {profile && (
        <div style={{ marginTop: '1rem', border: '1px solid #ccc', padding: '1rem' }}>
          <p><strong>Name:</strong> {profile.full_name}</p>
          <p><strong>Email:</strong> {profile.email}</p>
          <p><strong>Phone:</strong> {profile.phone}</p>
          <p><strong>Role:</strong> {profile.role}</p>
        </div>
      )}
    </div>
  );
}

function DoctorProfileForm({ form, onChange, onSubmit, doctorProfile }) {
  return (
    <form onSubmit={onSubmit}>
      <h2>Doctor Profile</h2>
      <InputField label="Doctor Email" name="doctor_id" value={form.doctor_id} onChange={onChange} placeholder="Doctor Email" />
      <InputField label="Specialization" name="specialization" value={form.specialization} onChange={onChange} placeholder="Specialization" />
      <InputField label="Qualification" name="qualification" value={form.qualification} onChange={onChange} placeholder="Qualification" />
      <InputField label="Experience Years" name="experience_years" value={form.experience_years} onChange={onChange} placeholder="Experience Years" />
      <InputField label="Consultation Fee" name="consultation_fee" value={form.consultation_fee} onChange={onChange} placeholder="Consultation Fee" />
      <InputField label="Clinic Address" name="clinic_address" value={form.clinic_address} onChange={onChange} placeholder="Clinic Address" />
      <button type="submit">Save Doctor Profile</button>
      {doctorProfile && (
        <div style={{ marginTop: '1rem', border: '1px solid #ccc', padding: '1rem' }}>
          <p><strong>Specialization:</strong> {doctorProfile.specialization}</p>
          <p><strong>Qualification:</strong> {doctorProfile.qualification}</p>
        </div>
      )}
    </form>
  );
}

function BookingForm({ form, onChange, onSubmit }) {
  return (
    <form onSubmit={onSubmit}>
      <h2>Book Appointment</h2>
      <InputField label="Doctor Email" name="doctor_id" value={form.doctor_id} onChange={onChange} placeholder="Doctor Email" />
      <InputField label="Appointment Date" name="appointment_date" type="date" value={form.appointment_date} onChange={onChange} />
      <InputField label="Time Slot" name="slot_time" value={form.slot_time} onChange={onChange} placeholder="Time Slot" />
      <button type="submit">Book Appointment</button>
    </form>
  );
}

function App() {
  const [screen, setScreen] = useState('login');
  const [token, setToken] = useState('');
  const [profile, setProfile] = useState(null);
  const [doctorProfile, setDoctorProfile] = useState(null);
  const [form, setForm] = useState({
    full_name: '',
    email: '',
    password: '',
    phone: '',
    role: 'PATIENT',
  });
  const [doctorForm, setDoctorForm] = useState({
    doctor_id: '',
    specialization: '',
    qualification: '',
    experience_years: '',
    consultation_fee: '',
    clinic_address: '',
  });
  const [bookingForm, setBookingForm] = useState({
    doctor_id: '',
    appointment_date: '',
    slot_time: '',
  });
  const [message, setMessage] = useState('');
  const [appointmentResult, setAppointmentResult] = useState(null);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleDoctorChange = (e) => {
    setDoctorForm({ ...doctorForm, [e.target.name]: e.target.value });
  };

  const handleBookingChange = (e) => {
    setBookingForm({ ...bookingForm, [e.target.name]: e.target.value });
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    const response = await fetch(`${API_BASE}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(form),
    });
    const data = await response.json();
    setMessage(response.ok ? 'Registration successful' : data.detail || 'Registration failed');
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    const response = await fetch(`${API_BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: form.email, password: form.password }),
    });
    const data = await response.json();
    if (response.ok) {
      setToken(data.access_token);
      setScreen('profile');
      setMessage('Login successful');
    } else {
      setMessage(data.detail || 'Login failed');
    }
  };

  const handleProfile = async () => {
    const response = await fetch(`${API_BASE}/auth/me`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    const data = await response.json();
    if (response.ok) {
      setProfile(data);
    } else {
      setMessage(data.detail || 'Unable to fetch profile');
    }
  };

  const handleDoctorProfileSubmit = async (e) => {
    e.preventDefault();
    const response = await fetch(`${API_BASE}/doctors/profile`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({
        ...doctorForm,
        experience_years: Number(doctorForm.experience_years),
        consultation_fee: Number(doctorForm.consultation_fee),
      }),
    });
    const data = await response.json();
    if (response.ok) {
      setDoctorProfile(data);
      setMessage('Doctor profile saved');
    } else {
      setMessage(data.detail || 'Unable to save doctor profile');
    }
  };

  const handleBookingSubmit = async (e) => {
    e.preventDefault();
    setMessage('Booking appointment...');

    // ensure profile is loaded
    if (!profile) {
      await handleProfile();
    }

    const payload = {
      patient_email: profile?.email || form.email,
      doctor_email: bookingForm.doctor_id,
      slot: `${bookingForm.appointment_date} ${bookingForm.slot_time}`,
    };

    try {
      const resp = await fetch(`${API_BASE}/appointments/create`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
        body: JSON.stringify(payload),
      });
      const data = await resp.json();
      if (!resp.ok) {
        setMessage(data.detail || 'Failed to create appointment');
        return;
      }

      const { appointment_id, amount } = data;
      setMessage(`Appointment created (${appointment_id}).`);

      // If amount exists, simulate payment flow
      if (amount) {
        setMessage((m) => m + ` Initiating payment for ₹${amount}...`);
        const initResp = await fetch(`${API_BASE}/payments/initiate?appointment_id=${appointment_id}`, {
          method: 'POST',
          headers: { Authorization: `Bearer ${token}` },
        });
        const initData = await initResp.json();
        if (!initResp.ok) {
          setMessage(initData.detail || 'Failed to initiate payment');
          return;
        }

        // Immediately confirm the mock payment
        const confirmResp = await fetch(`${API_BASE}/payments/confirm?appointment_id=${appointment_id}&transaction_id=tx_${Date.now()}`, {
          method: 'POST',
          headers: { Authorization: `Bearer ${token}` },
        });
        const confirmData = await confirmResp.json();
        if (!confirmResp.ok) {
          setMessage(confirmData.detail || 'Payment confirmation failed');
          return;
        }

        setAppointmentResult(confirmData.appointment || confirmData);
        setMessage('Payment completed and appointment updated to PAID.');
      } else {
        setAppointmentResult({ appointment_id, amount: null, status: 'PENDING' });
        setMessage('Appointment created without payment.');
      }
    } catch (err) {
      setMessage('Network error when booking appointment');
    }
  };

  return (
    <div style={{ fontFamily: 'Arial, sans-serif', maxWidth: '900px', margin: '2rem auto', padding: '1rem' }}>
      <h1>Doctor Appointment Booking System</h1>
      <p>React-based frontend for authentication, profile management, doctor profiles, and appointment booking.</p>

      <div style={{ marginBottom: '1rem' }}>
        <NavButton label="Login" onClick={() => setScreen('login')} active={screen === 'login'} />
        <NavButton label="Register" onClick={() => setScreen('register')} active={screen === 'register'} />
        {token && (
          <>
            <NavButton label="Profile" onClick={() => setScreen('profile')} active={screen === 'profile'} />
            <NavButton label="Doctor Profile" onClick={() => setScreen('doctor-profile')} active={screen === 'doctor-profile'} />
            <NavButton label="Book Appointment" onClick={() => setScreen('booking')} active={screen === 'booking'} />
          </>
        )}
      </div>

      {message && <p style={{ color: 'green' }}>{message}</p>}

      {screen === 'register' && <RegisterForm form={form} onChange={handleChange} onSubmit={handleRegister} />}
      {screen === 'login' && <LoginForm form={form} onChange={handleChange} onSubmit={handleLogin} />}
      {screen === 'profile' && token && <ProfileView profile={profile} onLoad={handleProfile} />}
      {screen === 'doctor-profile' && token && (
        <DoctorProfileForm form={doctorForm} onChange={handleDoctorChange} onSubmit={handleDoctorProfileSubmit} doctorProfile={doctorProfile} />
      )}
      {screen === 'booking' && token && <BookingForm form={bookingForm} onChange={handleBookingChange} onSubmit={handleBookingSubmit} />}
      {appointmentResult && (
        <div style={{ marginTop: '1rem', border: '1px solid #cbd5e1', padding: '1rem' }}>
          <h3>Appointment Result</h3>
          <pre style={{ whiteSpace: 'pre-wrap' }}>{JSON.stringify(appointmentResult, null, 2)}</pre>
        </div>
      )}
    </div>
  );
}

export default App;
