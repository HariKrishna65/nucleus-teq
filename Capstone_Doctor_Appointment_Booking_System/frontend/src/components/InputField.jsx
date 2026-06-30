export default function InputField({ label, name, type = 'text', value, onChange, placeholder }) {
  return (
    <div style={{ marginBottom: '1rem' }}>
      {label && <label style={{ display: 'block', marginBottom: '0.35rem', fontWeight: 600 }}>{label}</label>}
      <input
        name={name}
        type={type}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        style={{ width: '100%', padding: '0.85rem', borderRadius: '0.75rem' }}
      />
    </div>
  );
}
