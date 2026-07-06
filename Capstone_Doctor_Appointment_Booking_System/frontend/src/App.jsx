import React, { useEffect, useState } from 'react';

const API_BASE = 'http://127.0.0.1:8001';

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

function SlotForm({ form, onChange, onSubmit }) {
  return (
    <form onSubmit={onSubmit}>
      <h2>Create Slot</h2>
      <InputField label="Doctor Email" name="doctor_id" value={form.doctor_id} onChange={onChange} placeholder="Doctor Email" />
      <InputField label="Date" name="date" type="date" value={form.date} onChange={onChange} />
      <InputField label="Time" name="time" value={form.time} onChange={onChange} placeholder="Time Slot (e.g. 10:00)" />
      <button type="submit">Create Slot</button>
    </form>
  );
}

function SlotSearchForm({ form, onChange, onSubmit, slots }) {
  return (
    <div>
      <form onSubmit={onSubmit}>
        <h2>Search Slots</h2>
        <InputField label="Doctor Email" name="doctor_id" value={form.doctor_id} onChange={onChange} placeholder="Doctor Email" />
        <InputField label="Date" name="date" type="date" value={form.date} onChange={onChange} />
        <button type="submit">Search Slots</button>
      </form>
      {slots.length > 0 && (
        <div style={{ marginTop: '1rem' }}>
          <h3>Available Slots</h3>
          <ul>
            {slots.map((slot) => (
              <li key={slot.id}>
                {slot.doctor_id} | {slot.date} | {slot.time} | booked: {slot.booked ? 'yes' : 'no'}
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}

function AppointmentList({ appointments }) {
  return (
    <div>
      <h2>My Appointments</h2>
      {appointments.length === 0 ? (
        <p>No appointments yet.</p>
      ) : (
        <ul>
          {appointments.map((appt) => (
            <li key={appt.id}>
              Doctor: {appt.doctor_id} | Date: {appt.appointment_date} | Time: {appt.slot_time} | Status: {appt.status}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

function App() {
  const [screen, setScreen] = useState('login');
  const [token, setToken] = useState('');
  const [profile, setProfile] = useState(null);
  const [doctorProfile, setDoctorProfile] = useState(null);
  const [appointments, setAppointments] = useState([]);
  const [slots, setSlots] = useState([]);
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
  const [slotForm, setSlotForm] = useState({
    doctor_id: '',
    date: '',
    time: '',
  });
  const [slotSearchForm, setSlotSearchForm] = useState({
    doctor_id: '',
    date: '',
  });
  const [message, setMessage] = useState('');

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleDoctorChange = (e) => {
    setDoctorForm({ ...doctorForm, [e.target.name]: e.target.value });
  };

  const handleBookingChange = (e) => {
    setBookingForm({ ...bookingForm, [e.target.name]: e.target.value });
  };

  const handleSlotChange = (e) => {
    setSlotForm({ ...slotForm, [e.target.name]: e.target.value });
  };

  const handleSlotSearchChange = (e) => {
    setSlotSearchForm({ ...slotSearchForm, [e.target.name]: e.target.value });
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

  const handleSlotSubmit = async (e) => {
    e.preventDefault();
    if (!slotForm.doctor_id || !slotForm.date || !slotForm.time) {
      setMessage('Please fill all slot fields');
      return;
    }

    const response = await fetch(`${API_BASE}/appointments/slots`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(slotForm),
    });
    const data = await response.json();
    if (response.ok) {
      setMessage('Slot created successfully');
      setSlotForm({ doctor_id: '', date: '', time: '' });
    } else {
      setMessage(data.detail || 'Unable to create slot');
    }
  };

  const handleSlotSearch = async (e) => {
    e.preventDefault();
    const query = new URLSearchParams();
    if (slotSearchForm.doctor_id) query.append('doctor_id', slotSearchForm.doctor_id);
    if (slotSearchForm.date) query.append('date', slotSearchForm.date);
    const response = await fetch(`${API_BASE}/appointments/slots?${query.toString()}`);
    const data = await response.json();
    if (response.ok) {
      setSlots(data);
      setMessage('Slots loaded');
    } else {
      setMessage(data.detail || 'Unable to load slots');
    }
  };

  const handleBookingSubmit = async (e) => {
    e.preventDefault();
    if (!bookingForm.doctor_id || !bookingForm.appointment_date || !bookingForm.slot_time) {
      setMessage('Please fill all booking fields');
      return;
    }

    try {
      const response = await fetch(`${API_BASE}/appointments/book`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(bookingForm),
      });
      const data = await response.json();
      if (response.ok) {
        setMessage('Appointment booked successfully');
        setBookingForm({ doctor_id: '', appointment_date: '', slot_time: '' });
        await loadPatientAppointments();
      } else {
        setMessage(data.detail || 'Booking failed');
      }
    } catch (err) {
      setMessage('Network error while booking appointment');
    }
  };

  const loadPatientAppointments = async () => {
    const response = await fetch(`${API_BASE}/appointments/patient`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    const data = await response.json();
    if (response.ok) {
      setAppointments(data);
    } else {
      setMessage(data.detail || 'Unable to load appointments');
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
      {screen === 'slots' && token && (
        <div>
          <SlotForm form={slotForm} onChange={handleSlotChange} onSubmit={handleSlotSubmit} />
          <SlotSearchForm form={slotSearchForm} onChange={handleSlotSearchChange} onSubmit={handleSlotSearch} slots={slots} />
        </div>
      )}
      {screen === 'booking' && token && (
        <>
          <BookingForm form={bookingForm} onChange={handleBookingChange} onSubmit={handleBookingSubmit} />
          <AppointmentList appointments={appointments} />
        </>
      )}
    </div>
  );
}

export default App;
