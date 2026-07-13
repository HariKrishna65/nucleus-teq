import { Link } from 'react-router-dom';

const featuredDoctors = [
  { name: 'Anika Sharma', specialty: 'Cardiology', meta: '12 yrs | Rs.700', time: 'Today 10:30 AM', rating: '4.9' },
  { name: 'Rahul Mehta', specialty: 'Dermatology', meta: '8 yrs | Rs.500', time: 'Tomorrow 2:00 PM', rating: '4.8' },
  { name: 'Priya Nair', specialty: 'Pediatrics', meta: '10 yrs | Rs.600', time: 'Friday 11:15 AM', rating: '4.9' },
];

const appointmentSteps = [
  ['Search', 'Find specialists by location, fee, and availability.'],
  ['Book', 'Reserve open slots from approved doctors.'],
  ['Pay', 'Complete mock payment and track visit status.'],
];

const specialties = [
  ['Cardiology', '24 doctors'],
  ['Dermatology', '18 doctors'],
  ['Pediatrics', '15 doctors'],
  ['Orthopedics', '12 doctors'],
];

export default function HomePage() {
  return (
    <main className="home-page">
      <header className="home-header">
        <div className="brand-lockup"><span className="brand-mark">M</span><strong>MediSlot</strong></div>
        <nav>
          <a href="#specialties">Specialties</a>
          <a href="#doctors">Doctors</a>
          <Link to="/doctor/login">For doctors</Link>
          <Link to="/admin/login">Admin</Link>
          <Link className="home-cta" to="/patient/login">Get started</Link>
        </nav>
      </header>

      <section className="home-hero">
        <div className="home-hero-copy">
          <p className="eyebrow">Healthcare appointment system</p>
          <h1>Your health appointments, organized in minutes.</h1>
          <p>Search approved doctors, compare available slots, book consultations, and track every visit from one calm, reliable dashboard.</p>
          <div className="home-actions">
            <Link className="primary-link" to="/patient/login">Book appointment</Link>
            <Link className="secondary-link" to="/doctor/register">Join as doctor</Link>
          </div>

          <form className="home-search-panel" aria-label="Find a doctor">
            <label>
              Specialist
              <select defaultValue="Cardiology">
                <option>Cardiology</option>
                <option>Dermatology</option>
                <option>Pediatrics</option>
                <option>Orthopedics</option>
              </select>
            </label>
            <label>
              Location
              <input type="text" defaultValue="Hyderabad" />
            </label>
            <label>
              Date
              <input type="date" />
            </label>
            <Link className="home-search-button" to="/patient/login">Search</Link>
          </form>
        </div>

        <div className="home-doctor-showcase" aria-label="Doctor appointment preview">
          <div className="home-doctor-portrait"></div>
          <div className="home-appointment-card">
            <span>Next available</span>
            <strong>10:30 AM</strong>
            <p>Cardiology consultation with an approved doctor profile.</p>
            <div className="mini-doctor-row">
              <span></span>
              <div>
                <strong>Dr. Anika Sharma</strong>
                <small>Cardiology | 12 years</small>
              </div>
            </div>
          </div>
          <div className="home-rating-card">
            <span>Patient rating</span>
            <strong>4.9</strong>
          </div>
        </div>
      </section>

      <section className="home-trust-row" aria-label="Platform highlights">
        <article>
          <strong>120+</strong>
          <span>Approved doctors</span>
        </article>
        <article>
          <strong>8k+</strong>
          <span>Appointments booked</span>
        </article>
        <article>
          <strong>15 min</strong>
          <span>Average booking time</span>
        </article>
      </section>

      <section className="home-section" id="specialties">
        <div className="section-heading">
          <div>
            <p className="eyebrow">Browse care</p>
            <h2>Find the right specialist</h2>
          </div>
          <p>Choose a department and continue to live appointment slots after login.</p>
        </div>
        <div className="specialty-grid">
          {specialties.map(([name, count]) => (
            <Link className="specialty-tile" to="/patient/login" key={name}>
              <span>{name.charAt(0)}</span>
              <strong>{name}</strong>
              <small>{count}</small>
            </Link>
          ))}
        </div>
      </section>

      <section className="home-section" id="doctors">
        <div className="section-heading">
          <div>
            <p className="eyebrow">Featured profiles</p>
            <h2>Popular doctors</h2>
          </div>
          <p>See how doctor availability, consultation fees, ratings, and next slots appear for patients.</p>
        </div>
        <div className="home-doctor-grid">
          {featuredDoctors.map((doctor) => (
            <article className="doctor-card" key={doctor.name}>
              <div className="doctor-card-top">
                <div className="doctor-avatar" aria-hidden="true">{doctor.name.charAt(0)}</div>
                <span className="doctor-rating">{doctor.rating}</span>
              </div>
              <h3>Dr. {doctor.name}</h3>
              <p>{doctor.specialty}</p>
              <div className="doctor-meta">
                <span>{doctor.meta}</span>
                <span>{doctor.time}</span>
              </div>
              <Link className="secondary-link" to="/patient/login">View slots</Link>
            </article>
          ))}
        </div>
      </section>

      <section className="home-section">
        <div className="section-heading">
          <div>
            <p className="eyebrow">Appointment flow</p>
            <h2>Simple booking journey</h2>
          </div>
          <p>Built for patients, doctors, and admins to share one clear appointment workflow.</p>
        </div>
        <div className="stats home-steps">
          {appointmentSteps.map(([title, copy], index) => (
            <article key={title}>
              <small>Step {index + 1}</small>
              <strong>{title}</strong>
              <span>{copy}</span>
            </article>
          ))}
        </div>
      </section>
    </main>
  );
}
