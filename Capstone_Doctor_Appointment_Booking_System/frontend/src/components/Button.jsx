export default function Button({ children, onClick, active = false, type = 'button' }) {
  return (
    <button
      type={type}
      onClick={onClick}
      style={{
        marginRight: '0.5rem',
        padding: '0.65rem 1rem',
        border: active ? '2px solid #3b82f6' : '1px solid #cbd5e1',
        background: active ? '#eff6ff' : '#ffffff',
        borderRadius: '0.75rem',
        fontWeight: 500,
      }}
    >
      {children}
    </button>
  );
}
