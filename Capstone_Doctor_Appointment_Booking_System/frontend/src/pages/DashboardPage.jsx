import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { api } from '../api';
import { useAuth } from '../AuthContext';

const message = (error) => toast.error(error.response?.data?.detail || 'Request failed');

function EmptyState({ title, copy }) {
  return (
    <div className="empty-state">
      <span></span>
      <strong>{title}</strong>
      <p>{copy}</p>
    </div>
  );
}

function DashboardSidebar({ user, activeSection, onSectionChange, logout }) {
  const items = {
    PATIENT: [
      { key: 'dashboard', label: 'Dashboard' },
      { key: 'find', label: 'Find doctors' },
      { key: 'payments', label: 'Payments' },
      { key: 'appointments', label: 'Appointments' },
      { key: 'profile', label: 'Profile' },
    ],
    DOCTOR: [
      { key: 'dashboard', label: 'Dashboard' },
      { key: 'snapshot', label: 'Snapshot' },
      { key: 'availability', label: 'Availability' },
      { key: 'patients', label: 'Patients' },
      { key: 'profile', label: 'Profile' },
    ],
    ADMIN: [
      { key: 'dashboard', label: 'Dashboard' },
      { key: 'statistics', label: 'Statistics' },
      { key: 'doctors', label: 'Doctors' },
      { key: 'activity', label: 'Activity' },
      { key: 'profile', label: 'Profile' },
    ],
  }[user.role] || [{ key: 'dashboard', label: 'Dashboard' }, { key: 'profile', label: 'Profile' }];

  return (
    <aside className="sidebar dashboard-sidebar">
      <div className="brand-lockup"><span className="brand-mark">M</span><strong>MediSlot</strong></div>
      <div className="sidebar-user">
        <span>{user.role}</span>
        <strong>{user.name}</strong>
        <small>{user.email}</small>
      </div>
      <nav>
        {items.map((item) => (
          <button
            className={activeSection === item.key ? 'active-side-item' : ''}
            key={item.key}
            type="button"
            onClick={() => onSectionChange(item.key)}
          >
            {item.label}
          </button>
        ))}
      </nav>
      <button type="button" className="secondary" onClick={logout}>Logout</button>
    </aside>
  );
}

function ProfilePanel({ user, onRefresh }) {
  const [form, setForm] = useState({
    name: '',
    phone: '',
    gender: '',
    date_of_birth: '',
    qualification: '',
    specialization: '',
    experience: 0,
    license_number: '',
    consultation_fee: 0,
    clinic_address: '',
  });

  useEffect(() => {
    setForm({
      name: user?.name || '',
      phone: user?.phone || '',
      gender: user?.gender || '',
      date_of_birth: user?.date_of_birth || '',
      qualification: user?.qualification || '',
      specialization: user?.specialization || '',
      experience: user?.experience || 0,
      license_number: user?.license_number || '',
      consultation_fee: user?.consultation_fee || 0,
      clinic_address: user?.clinic_address || '',
    });
  }, [user]);

  const save = async (event) => {
    event.preventDefault();
    try {
      const payload = user?.role === 'DOCTOR'
        ? {
          name: form.name,
          phone: form.phone,
          qualification: form.qualification,
          specialization: form.specialization,
          experience: form.experience,
          license_number: form.license_number,
          consultation_fee: form.consultation_fee,
          clinic_address: form.clinic_address,
        }
        : {
          name: form.name,
          phone: form.phone,
          gender: form.gender,
          date_of_birth: form.date_of_birth,
        };
      await api.patch('/profile/me', payload);
      toast.success('Profile updated');
      onRefresh();
    } catch (error) {
      message(error);
    }
  };

  return (
    <section className="panel-section profile-panel">
      <div className="section-heading">
        <p className="eyebrow">Account details</p>
        <h2>Profile</h2>
      </div>
      <div className="profile-details">
        {[
          ['Name', user?.name],
          ['Email', user?.email],
          ['Phone', user?.phone],
          ['Role', user?.role],
          ['Gender', user?.gender],
          ['Date of birth', user?.date_of_birth],
          ['Qualification', user?.qualification],
          ['Specialization', user?.specialization],
          ['Experience', user?.experience],
          ['License number', user?.license_number],
          ['Consultation fee', user?.consultation_fee],
          ['Clinic address', user?.clinic_address],
          ['Active', user?.active === undefined ? undefined : user.active ? 'Yes' : 'No'],
          ['Approval status', user?.approval_status],
        ].filter(([, value]) => value !== undefined && value !== null && value !== '').map(([label, value]) => (
          <article className="detail-card" key={label}>
            <small>{label}</small>
            <strong>{value}</strong>
          </article>
        ))}
      </div>
      <form className="inline profile-form" onSubmit={save}>
        <label>
          Name
          <input value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} />
        </label>
        <label>
          Phone
          <input value={form.phone} onChange={(event) => setForm({ ...form, phone: event.target.value })} />
        </label>
        {user?.role === 'PATIENT' && (
          <>
            <label>
              Gender
              <select value={form.gender} onChange={(event) => setForm({ ...form, gender: event.target.value })}>
                <option value="">Select</option>
                <option>FEMALE</option>
                <option>MALE</option>
                <option>OTHER</option>
              </select>
            </label>
            <label>
              Date of birth
              <input type="date" value={form.date_of_birth} onChange={(event) => setForm({ ...form, date_of_birth: event.target.value })} />
            </label>
          </>
        )}
        {user?.role !== 'DOCTOR' && <button type="submit">Update profile</button>}
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
            License number
            <input value={form.license_number} onChange={(event) => setForm({ ...form, license_number: event.target.value })} />
          </label>
          <label>
            Fee
            <input type="number" min="0" value={form.consultation_fee} onChange={(event) => setForm({ ...form, consultation_fee: Number(event.target.value) })} />
          </label>
          <label>
            Clinic address
            <textarea value={form.clinic_address} onChange={(event) => setForm({ ...form, clinic_address: event.target.value })} />
          </label>
          <button type="submit">Request profile update</button>
        </form>
      )}
    </section>
  );
}

function PatientDashboard({ activeSection }) {
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
      {activeSection === 'find' && <section className="panel-section discovery-section">
        <div className="section-heading">
          <div>
            <p className="eyebrow">Care discovery</p>
            <h2>Find a doctor</h2>
          </div>
          <p>Filter by specialty, clinic location, fee, and live availability.</p>
        </div>
        <div className="filters">
          <input placeholder="Doctor name" value={filters.name} onChange={(event) => setFilters({ ...filters, name: event.target.value })} />
          <input placeholder="Specialization" value={filters.specialization} onChange={(event) => setFilters({ ...filters, specialization: event.target.value })} />
          <input placeholder="Location" value={filters.location} onChange={(event) => setFilters({ ...filters, location: event.target.value })} />
          <input placeholder="Minimum experience" type="number" min="0" value={filters.min_experience} onChange={(event) => setFilters({ ...filters, min_experience: event.target.value })} />
          <input placeholder="Maximum fee" type="number" min="0" value={filters.max_fee} onChange={(event) => setFilters({ ...filters, max_fee: event.target.value })} />
          <label className="checkline"><input type="checkbox" checked={filters.available} onChange={(event) => setFilters({ ...filters, available: event.target.checked })} />Available only</label>
          <button onClick={loadDoctors}>Search</button>
        </div>
        <div className="grid doctor-grid">
          {doctors.map((doctor) => (
            <article className="doctor-card" key={doctor.id}>
              <div className="doctor-avatar" aria-hidden="true">{doctor.name?.charAt(0) || 'D'}</div>
              <div className="doctor-card-top">
                <div>
                  <h3>Dr. {doctor.name}</h3>
                  <p>{doctor.specialization} | {doctor.qualification}</p>
                </div>
                <span className="status">{doctor.available_slots.length ? 'AVAILABLE' : 'NO SLOTS'}</span>
              </div>
              <div className="doctor-meta">
                <span>{doctor.experience || 0}+ yrs</span>
                <span>Rs.{doctor.consultation_fee}</span>
                <span>{doctor.clinic_address || 'Clinic address pending'}</span>
              </div>
              <h4>Available slots</h4>
              <div className="slot-list">
                {doctor.available_slots.length ? doctor.available_slots.map((slot) => (
                  <button className="slot" key={slot.id} onClick={() => book(doctor.id, slot.id)}>
                    {new Date(slot.starts_at).toLocaleString()}
                  </button>
                )) : <p>No slots available</p>}
              </div>
            </article>
          ))}
          {!doctors.length && <EmptyState title="No doctors found" copy="Try clearing filters or register a doctor account and approve it from admin." />}
        </div>
      </section>}
      {activeSection === 'payments' && (
        <section className="panel-section">
          <div className="section-heading">
            <p className="eyebrow">Checkout</p>
            <h2>Payment</h2>
          </div>
          {pendingPayment ? (
            <article className={`payment-panel ${paymentDone ? 'paid' : ''}`}>
              <h3>Appointment summary</h3>
              <p>Dr. {pendingPayment.doctor?.name}</p>
              <p>{new Date(pendingPayment.starts_at).toLocaleString()}</p>
              <p>Amount: Rs.{pendingPayment.doctor?.consultation_fee || 0}</p>
              <span className="status">{paymentDone ? 'PAYMENT_SUCCESS' : 'PENDING_PAYMENT'}</span>
              {!paymentDone && <button onClick={completePayment}>Complete payment</button>}
            </article>
          ) : <EmptyState title="No payment pending" copy="Reserve a doctor slot and your checkout summary will appear here." />}
        </section>
      )}
      {activeSection === 'appointments' && <section className="panel-section">
        <div className="section-heading">
          <p className="eyebrow">Visit timeline</p>
          <h2>My appointments</h2>
        </div>
        <div className="filters">
          {['ALL', 'BOOKED', 'COMPLETED', 'CANCELLED', 'PENDING_PAYMENT'].map((status) => (
            <button className={appointmentFilter === status ? 'active-filter' : 'secondary'} key={status} onClick={() => setAppointmentFilter(status)}>{status}</button>
          ))}
        </div>
        <div className="grid timeline-grid">
          {appointments.filter((item) => appointmentFilter === 'ALL' || item.status === appointmentFilter).map((item) => (
            <article className="timeline-card" key={item.id}>
              <h3>Dr. {item.doctor?.name}</h3>
              <p>{new Date(item.starts_at).toLocaleString()}</p>
              <span className={`status ${item.status}`}>{item.status}</span>
              {!['CANCELLED', 'COMPLETED'].includes(item.status) && <button onClick={() => cancel(item.id)}>Cancel</button>}
            </article>
          ))}
          {!appointments.filter((item) => appointmentFilter === 'ALL' || item.status === appointmentFilter).length && (
            <EmptyState title="No appointments yet" copy="Book an available doctor slot to start your visit timeline." />
          )}
        </div>
      </section>}
    </>
  );
}

function DoctorDashboard({ activeSection }) {
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
      {activeSection === 'snapshot' && <section className="panel-section">
        <div className="section-heading">
          <p className="eyebrow">Practice snapshot</p>
          <h2>Appointment cards</h2>
        </div>
        <div className="stats">
          <article><small>Today</small><strong>{appointments.filter((item) => new Date(item.starts_at).toDateString() === new Date().toDateString()).length}</strong><span>visits</span></article>
          <article><small>Upcoming</small><strong>{appointments.filter((item) => new Date(item.starts_at) > new Date() && item.status !== 'CANCELLED').length}</strong><span>scheduled</span></article>
          <article><small>Completed</small><strong>{appointments.filter((item) => item.status === 'COMPLETED').length}</strong><span>closed</span></article>
          <article><small>Cancelled</small><strong>{appointments.filter((item) => item.status === 'CANCELLED').length}</strong><span>removed</span></article>
        </div>
      </section>}
      {activeSection === 'availability' && <section className="panel-section">
        <div className="section-heading">
          <p className="eyebrow">Calendar control</p>
          <h2>Manage availability</h2>
        </div>
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
        <div className="grid slot-grid">
          {slots.map((slot) => (
            <article className="timeline-card" key={slot.id}>
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
          {!slots.length && <EmptyState title="No availability added" copy="Add your first start and end time so patients can book you." />}
        </div>
      </section>}
      {activeSection === 'patients' && <section className="panel-section">
        <div className="section-heading">
          <p className="eyebrow">Clinical queue</p>
          <h2>Patient appointments</h2>
        </div>
        <div className="filters">
          {['ALL', 'BOOKED', 'COMPLETED', 'CANCELLED', 'NO_SHOW', 'PENDING_PAYMENT'].map((status) => (
            <button className={appointmentFilter === status ? 'active-filter' : 'secondary'} key={status} onClick={() => setAppointmentFilter(status)}>{status}</button>
          ))}
        </div>
        <div className="grid timeline-grid">
          {appointments.filter((item) => appointmentFilter === 'ALL' || item.status === appointmentFilter).map((item) => (
            <article className="timeline-card" key={item.id}>
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
          {!appointments.filter((item) => appointmentFilter === 'ALL' || item.status === appointmentFilter).length && (
            <EmptyState title="No patient visits" copy="Appointments will appear here after patients reserve your slots." />
          )}
        </div>
      </section>}
    </>
  );
}

function AdminDashboard({ activeSection }) {
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
      {activeSection === 'statistics' && <section className="panel-section">
        <div className="section-heading">
          <p className="eyebrow">Operations</p>
          <h2>Platform statistics</h2>
        </div>
        <div className="stats">
          {Object.entries(stats).map(([key, value]) => (
            <article key={key}>
              <small>{key.replaceAll('_', ' ')}</small>
              <strong>{value}</strong>
              <span>{key.replaceAll('_', ' ')}</span>
            </article>
          ))}
        </div>
      </section>}
      {activeSection === 'doctors' && <section className="panel-section">
        <div className="section-heading">
          <p className="eyebrow">Network quality</p>
          <h2>Manage doctors</h2>
        </div>
        <div className="grid doctor-grid">
          {doctors.map((doctor) => (
            <article className="doctor-card" key={doctor.id}>
              <div className="doctor-avatar" aria-hidden="true">{doctor.name?.charAt(0) || 'D'}</div>
              <h3>Dr. {doctor.name}</h3>
              <p>{doctor.specialization || 'Specialization pending'}</p>
              <p>{doctor.approval_status || (doctor.active ? 'APPROVED' : 'INACTIVE')}</p>
              <button onClick={() => activate(doctor.id, !doctor.active)}>{doctorActionLabel(doctor)}</button>
            </article>
          ))}
          {!doctors.length && <EmptyState title="No doctors yet" copy="Doctor registrations will appear here for approval." />}
        </div>
      </section>}
      {activeSection === 'activity' && <section className="panel-section">
        <div className="section-heading">
          <p className="eyebrow">Live activity</p>
          <h2>Monitor appointments</h2>
        </div>
        <div className="grid timeline-grid">
          {appointments.map((appointment) => (
            <article className="timeline-card" key={appointment.id}>
              <h3>{appointment.patient?.name}{' -> '}Dr. {appointment.doctor?.name}</h3>
              <p>{new Date(appointment.starts_at).toLocaleString()}</p>
              <span className="status">{appointment.status}</span>
            </article>
          ))}
          {!appointments.length && <EmptyState title="No appointment activity" copy="Platform bookings will appear in this monitor." />}
        </div>
      </section>}
    </>
  );
}

export default function DashboardPage() {
  const { user, logout, refreshUser } = useAuth();
  const [activeSection, setActiveSection] = useState(null);
  const navigate = useNavigate();

  if (!user) {
    return <main className="auth-card"><p>Loading your dashboard...</p></main>;
  }

  const dashboardCopy = {
    PATIENT: {
      eyebrow: 'Patient dashboard',
      title: `Find care faster, ${user.name}`,
      copy: 'Search doctors, book available slots, complete payments, and track every visit from one patient workspace.',
      metric: 'Booking area',
    },
    DOCTOR: {
      eyebrow: 'Doctor dashboard',
      title: `Clinic schedule for Dr. ${user.name}`,
      copy: 'Manage availability, review patient appointments, update visit status, and keep your practice profile ready.',
      metric: 'Practice area',
    },
    ADMIN: {
      eyebrow: 'Admin dashboard',
      title: `Platform control center`,
      copy: 'Approve doctors, monitor appointments, and watch platform statistics across the full care network.',
      metric: 'Admin area',
    },
  }[user.role] || {
    eyebrow: `${user.role.toLowerCase()} dashboard`,
    title: `Welcome, ${user.name}`,
    copy: 'Manage appointments, availability, profiles, and care activity from one focused dashboard.',
    metric: 'Workspace',
  };
  const defaultSection = 'dashboard';
  const selectedSection = activeSection || defaultSection;

    return (
      <div className={`app-shell role-${user.role.toLowerCase()}`}>
        <button className="corner-back-button" type="button" onClick={() => navigate(-1)} aria-label="Go back">&larr;</button>
        <DashboardSidebar user={user} activeSection={selectedSection} onSectionChange={setActiveSection} logout={logout} />
        <header>
        <div>
          <span className="brand-mark small">M</span>
          <strong>MediSlot</strong>
          <span>Doctor Appointment System</span>
        </div>
          <nav>
            <span>{user.name} | {user.role}</span>
            <button onClick={logout}>Logout</button>
          </nav>
      </header>
      <main>
        {selectedSection === 'dashboard' && <section className="dashboard-hero">
          <div>
            <p className="eyebrow">{dashboardCopy.eyebrow}</p>
            <h1>{dashboardCopy.title}</h1>
            <p>{dashboardCopy.copy}</p>
          </div>
          <div className="hero-metric" aria-label="Current role">
            <div className="mini-calendar">
              <span>{new Date().toLocaleDateString(undefined, { weekday: 'short' })}</span>
              <strong>{new Date().getDate()}</strong>
            </div>
            <span>{dashboardCopy.metric}</span>
            <strong>{user.role}</strong>
          </div>
        </section>}
        {user.role === 'PATIENT' && (
          <section className="role-dashboard patient-dashboard">
            {selectedSection === 'profile' ? <ProfilePanel user={user} onRefresh={refreshUser} /> : <PatientDashboard activeSection={selectedSection} />}
          </section>
        )}
        {user.role === 'DOCTOR' && (
          <section className="role-dashboard doctor-dashboard">
            {selectedSection === 'profile' ? <ProfilePanel user={user} onRefresh={refreshUser} /> : <DoctorDashboard activeSection={selectedSection} />}
          </section>
        )}
        {user.role === 'ADMIN' && (
          <section className="role-dashboard admin-dashboard">
            {selectedSection === 'profile' ? <ProfilePanel user={user} onRefresh={refreshUser} /> : <AdminDashboard activeSection={selectedSection} />}
          </section>
        )}
      </main>
    </div>
  );
}
