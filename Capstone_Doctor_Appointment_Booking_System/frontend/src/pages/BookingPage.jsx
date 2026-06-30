import InputField from '../components/InputField';
import Button from '../components/Button';

export default function BookingPage({ form, onChange, onSubmit }) {
  return (
    <div className="card">
      <h2>Book Appointment</h2>
      <form onSubmit={onSubmit}>
        <InputField label="Doctor Email" name="doctor_id" value={form.doctor_id} onChange={onChange} placeholder="Doctor Email" />
        <InputField label="Appointment Date" name="appointment_date" type="date" value={form.appointment_date} onChange={onChange} />
        <InputField label="Time Slot" name="slot_time" value={form.slot_time} onChange={onChange} placeholder="Time Slot" />
        <Button type="submit">Book Appointment</Button>
      </form>
    </div>
  );
}
