import Button from '../components/Button';

export default function DashboardPage({ token, onLogout }) {
  const [profile, setProfile] = React.useState(null);
  const [message, setMessage] = React.useState('');

  const loadProfile = async () => {
    const response = await fetch('http://127.0.0.1:8000/auth/me', {
      headers: { Authorization: `Bearer ${token}` },
    });
    const data = await response.json();
    if (response.ok) {
      setProfile(data);
    } else {
      setMessage(data.detail || 'Unable to load profile');
    }
  };

  return (
    <div className="card">
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1.5rem' }}>
        <h1>Dashboard</h1>
        <Button onClick={onLogout}>Logout</Button>
      </div>

      <Button onClick={loadProfile}>Load Profile</Button>

      {profile && (
        <div style={{ marginTop: '1rem' }}>
          <p><strong>Name:</strong> {profile.full_name}</p>
          <p><strong>Email:</strong> {profile.email}</p>
          <p><strong>Phone:</strong> {profile.phone}</p>
          <p><strong>Role:</strong> {profile.role}</p>
        </div>
      )}
      {message && <p style={{ marginTop: '1rem' }}>{message}</p>}
    </div>
  );
}
