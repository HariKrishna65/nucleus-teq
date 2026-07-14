// Renders the administrator dashboard for reviewing doctors and platform data.
import React from 'react';

const API_BASE = 'http://127.0.0.1:8000';

export default function AdminPage({ token }) {
  const [profiles, setProfiles] = React.useState([]);
  const [message, setMessage] = React.useState('');

  const loadProfiles = async () => {
    setMessage('Loading doctor profiles...');
    const resp = await fetch(`${API_BASE}/admin/doctor-profiles`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    const data = await resp.json();
    if (resp.ok) {
      setProfiles(data || []);
      setMessage('');
    } else {
      setMessage(data.detail || 'Failed to load profiles');
    }
  };

  const handleDelete = async (doctor_id) => {
    if (!confirm(`Delete doctor profile ${doctor_id}?`)) return;
    const resp = await fetch(`${API_BASE}/admin/doctors/${encodeURIComponent(doctor_id)}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token}` },
    });
    const data = await resp.json();
    if (resp.ok) {
      setMessage('Deleted');
      loadProfiles();
    } else {
      setMessage(data.detail || 'Delete failed');
    }
  };

  React.useEffect(() => {
    if (token) loadProfiles();
  }, [token]);

  return (
    <div className="card">
      <h2>Admin - Doctor Profiles</h2>
      <p>{message}</p>
      <button onClick={loadProfiles}>Reload</button>
      <div style={{ marginTop: '1rem' }}>
        {profiles.length === 0 && <p>No doctor profiles found.</p>}
        {profiles.map((p) => (
          <div key={p.get('doctor_id') || p.get('doctor_id')} style={{ border: '1px solid #eee', padding: '0.6rem', marginTop: '0.6rem' }}>
            <p><strong>Email:</strong> {p.doctor_id}</p>
            <p><strong>Specialization:</strong> {p.specialization}</p>
            <p><strong>Consultation Fee:</strong> {p.consultation_fee}</p>
            <button onClick={() => handleDelete(p.doctor_id)}>Delete</button>
          </div>
        ))}
      </div>
    </div>
  );
}
