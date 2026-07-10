import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { useAuth } from '../hooks/useAuth';
import { api } from '../services/api';

const message = (error) => toast.error(error.response?.data?.detail || 'Request failed');

function ProfilePanel({ user, onRefresh }) {
  const [form, setForm] = useState({
    name: '',
    phone: '',
    qualification: '',
    specialization: '',
    experience: 0,
    consultation_fee: 0,
    clinic_address: '',
  });

  useEffect(() => {
    setForm({
      name: user?.name || '',
      phone: user?.phone || '',
      qualification: user?.qualification || '',
      specialization: user?.specialization || '',
      experience: user?.experience || 0,
      consultation_fee: user?.consultation_fee || 0,
      clinic_address: user?.clinic_address || '',
    });
  }, [user]);

  const save = async (event) => {
    event.preventDefault();
    try {
      await api.patch('/profile/me', form);
      toast.success('Profile updated');
      onRefresh();
    } catch (error) {
      message(error);
    }
  };

  return (
    <section>
      <h2>Profile</h2>
      <form className="inline profile-form" onSubmit={save}>
        <label>
          Name
          <input value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} />
        </label>
        <label>
          Phone
          <input value={form.phone} onChange={(event) => setForm({ ...form, phone: event.target.value })} />
        </label>
        <button type="submit">Save profile</button>
      </form>
      {user?.role === 'DOCTOR' && (
        <form className="inline profile-form" onSubmit={save}>
          <label>
            Qualification
            <input value={form.qualification} onChange={(event) => setForm({ ...form, qualification: event.target.value })} />
          </label>
          <label>
            Specialization
            <input value={form.specialization} onChange={(event) => setForm({ ...form, specialization: event.target.value })} />
          </label>
          <label>
            Experience
            <input type="number" min="0" value={form.experience} onChange={(event) => setForm({ ...form, experience: Number(event.target.value) })} />
          </label>
          <label>
            Fee
            <input type="number" min="0" value={form.consultation_fee} onChange={(event) => setForm({ ...form, consultation_fee: Number(event.target.value) })} />
          </label>
          <label>
            Clinic address
            <textarea value={form.clinic_address} onChange={(event) => setForm({ ...form, clinic_address: event.target.value })} />
          </label>
          <button type="submit">Save doctor profile</button>
        </form>
      )}
    </section>
  );
}

function PatientDashboard() {
  const [doctors, setDoctors] = useState([]);
  const [appointments, setAppointments] = useState([]);
  const [pendingPayment, setPendingPayment] = useState(null);
  const [paymentDone, setPaymentDone] = useState(false);
  const [appointmentFilter, setAppointmentFilter] = useState('ALL');
  const [filters, setFilters] = useState({ name: '', specialization: '', location: '', min_experience: '', max_fee: '', available: false });

  const loadDoctors = () => {
    const params = Object.fromEntries(Object.entries(filters).filter(([, value]) => value !== '' && value !== false));
    api.get('/doctors', { params }).then(({ data }) => setDoctors(data)).catch(message);
  };
  const loadAppointments = () => api.get('/appointments').then(({ data }) => setAppointments(data)).catch(message);

  useEffect(() => {
    loadDoctors();
    loadAppointments();
  }, []);

  const book = async (doctorId, slotId) => {
    try {
      const { data } = await api.post('/appointments', { doctor_id: doctorId, slot_id: slotId });
      setPendingPayment(data);
      setPaymentDone(false);
      toast.success('Slot reserved. Complete payment to confirm.');
      loadDoctors();
      loadAppointments();
    } catch (error) {
      message(error);
    }
  };

  const completePayment = async () => {
    try {
      await api.post('/payments', { appointment_id: pendingPayment.id, method: 'CARD' });
      setPaymentDone(true);
      toast.success('Payment successful');
      loadAppointments();
      loadDoctors();
    } catch (error) {
      message(error);
    }
  };

  const cancel = async (appointmentId) => {
    try {
      await api.post(`/appointments/${appointmentId}/cancel`);
      toast.success('Appointment cancelled');
      loadAppointments();
      loadDoctors();
    } catch (error) {
      message(error);
    }
  };

  return (
    <>
      <section>
        <h2>Find a doctor</h2>
        <div className="filters">
          <input placeholder="Doctor name" value={filters.name} onChange={(event) => setFilters({ ...filters, name: event.target.value })} />
          <input placeholder="Specialization" value={filters.specialization} onChange={(event) => setFilters({ ...filters, specialization: event.target.value })} />
          <input placeholder="Location" value={filters.location} onChange={(event) => setFilters({ ...filters, location: event.target.value })} />
          <input placeholder="Minimum experience" type="number" min="0" value={filters.min_experience} onChange={(event) => setFilters({ ...filters, min_experience: event.target.value })} />
          <input placeholder="Maximum fee" type="number" min="0" value={filters.max_fee} onChange={(event) => setFilters({ ...filters, max_fee: event.target.value })} />
          <label className="checkline"><input type="checkbox" checked={filters.available} onChange={(event) => setFilters({ ...filters, available: event.target.checked })} />Available only</label>
          <button onClick={loadDoctors}>Search</button>
        </div>
        <div className="grid">
          {doctors.map((doctor) => (
            <article key={doctor.id}>
              <h3>Dr. {doctor.name}</h3>
              <p>{doctor.specialization} | {doctor.qualification}</p>
              <p>{doctor.experience || 0} years | Rs.{doctor.consultation_fee}</p>
              <p>{doctor.clinic_address}</p>
              <h4>Available slots</h4>
              {doctor.available_slots.length ? doctor.available_slots.map((slot) => (
                <button className="slot" key={slot.id} onClick={() => book(doctor.id, slot.id)}>
                  {new Date(slot.starts_at).toLocaleString()}
                </button>
              )) : <p>No slots available</p>}
            </article>
          ))}
        </div>
      </section>
      {pendingPayment && (
        <section>
          <h2>Payment</h2>
          <article className={`payment-panel ${paymentDone ? 'paid' : ''}`}>
            <h3>Appointment summary</h3>
            <p>Dr. {pendingPayment.doctor?.name}</p>
            <p>{new Date(pendingPayment.starts_at).toLocaleString()}</p>
            <p>Amount: Rs.{pendingPayment.doctor?.consultation_fee || 0}</p>
            <span className="status">{paymentDone ? 'PAYMENT_SUCCESS' : 'PENDING_PAYMENT'}</span>
            {!paymentDone && <button onClick={completePayment}>Complete payment</button>}
          </article>
        </section>
      )}
      <section>
        <h2>My appointments</h2>
        <div className="filters">
          {['ALL', 'BOOKED', 'COMPLETED', 'CANCELLED', 'PENDING_PAYMENT'].map((status) => (
            <button className={appointmentFilter === status ? 'active-filter' : 'secondary'} key={status} onClick={() => setAppointmentFilter(status)}>{status}</button>
          ))}
        </div>
        <div className="grid">
          {appointments.filter((item) => appointmentFilter === 'ALL' || item.status === appointmentFilter).map((item) => (
            <article key={item.id}>
              <h3>Dr. {item.doctor?.name}</h3>
              <p>{new Date(item.starts_at).toLocaleString()}</p>
              <span className={`status ${item.status}`}>{item.status}</span>
              {!['CANCELLED', 'COMPLETED'].includes(item.status) && <button onClick={() => cancel(item.id)}>Cancel</button>}
            </article>
          ))}
        </div>
      </section>
    </>
  );
}

function DoctorDashboard() {
  const [slots, setSlots] = useState([]);
  const [appointments, setAppointments] = useState([]);
  const [appointmentFilter, setAppointmentFilter] = useState('ALL');
  const [form, setForm] = useState({ starts_at: '', ends_at: '' });
  const [editingId, setEditingId] = useState(null);

  const load = () => Promise.all([api.get('/doctor/slots'), api.get('/appointments')])
    .then(([slotResponse, appointmentResponse]) => {
      setSlots(slotResponse.data);
      setAppointments(appointmentResponse.data);
    })
    .catch(message);

  useEffect(() => {
    load();
  }, []);

  const resetForm = () => {
    setForm({ starts_at: '', ends_at: '' });
    setEditingId(null);
  };

  const submit = async (event) => {
    event.preventDefault();
    const payload = {
      starts_at: new Date(form.starts_at).toISOString(),
      ends_at: new Date(form.ends_at).toISOString(),
    };
    try {
      if (editingId) {
        await api.put(`/doctor/slots/${editingId}`, payload);
        toast.success('Availability updated');
      } else {
        await api.post('/doctor/slots', payload);
        toast.success('Availability added');
      }
      resetForm();
      load();
    } catch (error) {
      message(error);
    }
  };

  const startEdit = (slot) => {
    setEditingId(slot.id);
    setForm({
      starts_at: slot.starts_at.slice(0, 16),
      ends_at: slot.ends_at.slice(0, 16),
    });
  };

  const remove = async (slotId) => {
    try {
      await api.delete(`/doctor/slots/${slotId}`);
      toast.success('Availability removed');
      load();
    } catch (error) {
      message(error);
    }
  };

  const updateStatus = async (appointmentId, status) => {
    try {
      await api.patch(`/appointments/${appointmentId}/status`, { status });
      toast.success(`Marked ${status}`);
      load();
    } catch (error) {
      message(error);
    }
  };

  return (
    <>
      <section>
        <h2>Appointment cards</h2>
        <div className="stats">
          <article><strong>{appointments.filter((item) => new Date(item.starts_at).toDateString() === new Date().toDateString()).length}</strong><span>today</span></article>
          <article><strong>{appointments.filter((item) => new Date(item.starts_at) > new Date() && item.status !== 'CANCELLED').length}</strong><span>upcoming</span></article>
          <article><strong>{appointments.filter((item) => item.status === 'COMPLETED').length}</strong><span>completed</span></article>
          <article><strong>{appointments.filter((item) => item.status === 'CANCELLED').length}</strong><span>cancelled</span></article>
        </div>
      </section>
      <section>
        <h2>Manage availability</h2>
        <form className="inline" onSubmit={submit}>
          <label>
            Start
            <input type="datetime-local" required value={form.starts_at} onChange={(event) => setForm({ ...form, starts_at: event.target.value })} />
          </label>
          <label>
            End
            <input type="datetime-local" required value={form.ends_at} onChange={(event) => setForm({ ...form, ends_at: event.target.value })} />
          </label>
          <button type="submit">{editingId ? 'Update slot' : 'Add slot'}</button>
          {editingId && <button type="button" className="secondary" onClick={resetForm}>Cancel edit</button>}
        </form>
        <div className="grid">
          {slots.map((slot) => (
            <article key={slot.id}>
              <p>{new Date(slot.starts_at).toLocaleString()}</p>
              <span className="status">{slot.booked ? 'BOOKED' : 'AVAILABLE'}</span>
              {!slot.booked && (
                <div className="actions">
                  <button onClick={() => startEdit(slot)}>Edit</button>
                  <button className="secondary" onClick={() => remove(slot.id)}>Delete</button>
                </div>
              )}
            </article>
          ))}
        </div>
      </section>
      <section>
        <h2>Patient appointments</h2>
        <div className="filters">
          {['ALL', 'BOOKED', 'COMPLETED', 'CANCELLED', 'NO_SHOW', 'PENDING_PAYMENT'].map((status) => (
            <button className={appointmentFilter === status ? 'active-filter' : 'secondary'} key={status} onClick={() => setAppointmentFilter(status)}>{status}</button>
          ))}
        </div>
        <div className="grid">
          {appointments.filter((item) => appointmentFilter === 'ALL' || item.status === appointmentFilter).map((item) => (
            <article key={item.id}>
              <h3>{item.patient?.name}</h3>
              <p>{item.patient?.email} | {item.patient?.phone}</p>
              <p>{new Date(item.starts_at).toLocaleString()}</p>
              <span className="status">{item.status}</span>
              {item.status === 'BOOKED' && (
                <div className="actions">
                  <button onClick={() => updateStatus(item.id, 'COMPLETED')}>Complete</button>
                  <button className="secondary" onClick={() => updateStatus(item.id, 'NO_SHOW')}>No show</button>
                </div>
              )}
            </article>
          ))}
        </div>
      </section>
    </>
  );
}

function AdminDashboard() {
  const [stats, setStats] = useState({});
  const [doctors, setDoctors] = useState([]);
  const [appointments, setAppointments] = useState([]);

  const load = () => Promise.all([api.get('/admin/statistics'), api.get('/admin/doctors'), api.get('/admin/appointments')])
    .then(([statsResponse, doctorsResponse, appointmentsResponse]) => {
      setStats(statsResponse.data);
      setDoctors(doctorsResponse.data);
      setAppointments(appointmentsResponse.data);
    })
    .catch(message);

  useEffect(() => {
    load();
  }, []);

  const activate = async (doctorId, active) => {
    try {
      await api.patch(`/admin/doctors/${doctorId}/activation`, { active });
      toast.success('Doctor account updated');
      load();
    } catch (error) {
      message(error);
    }
  };

  const doctorActionLabel = (doctor) => {
    if (doctor.approval_status === 'PENDING') {
      return 'Approve';
    }
    return doctor.active ? 'Deactivate' : 'Activate';
  };

  return (
    <>
      <section>
        <h2>Platform statistics</h2>
        <div className="stats">
          {Object.entries(stats).map(([key, value]) => (
            <article key={key}>
              <strong>{value}</strong>
              <span>{key.replaceAll('_', ' ')}</span>
            </article>
          ))}
        </div>
      </section>
      <section>
        <h2>Manage doctors</h2>
        <div className="grid">
          {doctors.map((doctor) => (
            <article key={doctor.id}>
              <h3>Dr. {doctor.name}</h3>
              <p>{doctor.specialization}</p>
              <p>{doctor.approval_status || (doctor.active ? 'APPROVED' : 'INACTIVE')}</p>
              <button onClick={() => activate(doctor.id, !doctor.active)}>{doctorActionLabel(doctor)}</button>
            </article>
          ))}
        </div>
      </section>
      <section>
        <h2>Monitor appointments</h2>
        <div className="grid">
          {appointments.map((appointment) => (
            <article key={appointment.id}>
              <h3>{appointment.patient?.name}{' -> '}Dr. {appointment.doctor?.name}</h3>
              <p>{new Date(appointment.starts_at).toLocaleString()}</p>
              <span className="status">{appointment.status}</span>
            </article>
          ))}
        </div>
      </section>
    </>
  );
}

export default function DashboardPage() {
  const { user, logout, refreshUser } = useAuth();

  if (!user) {
    return <main className="auth-card"><p>Loading your dashboard...</p></main>;
  }

  return (
    <div className="app-shell">
      <header>
        <div>
          <strong>MediSlot</strong>
          <span>Doctor Appointment System</span>
        </div>
        <nav>
          <span>{user.name} | {user.role}</span>
          <button onClick={logout}>Logout</button>
        </nav>
      </header>
      <main>
        <ProfilePanel user={user} onRefresh={refreshUser} />
        {user.role === 'PATIENT' ? <PatientDashboard /> : user.role === 'DOCTOR' ? <DoctorDashboard /> : <AdminDashboard />}
      </main>
    </div>
  );
}
