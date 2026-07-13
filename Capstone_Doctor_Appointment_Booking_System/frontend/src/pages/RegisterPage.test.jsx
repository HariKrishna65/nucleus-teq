import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import RegisterPage from './RegisterPage';
import { api } from '../api';

vi.mock('../api', () => ({
  api: { post: vi.fn() },
}));

vi.mock('react-toastify', () => ({
  toast: { success: vi.fn(), error: vi.fn() },
}));

const renderPage = (props = {}) => render(<MemoryRouter><RegisterPage {...props} /></MemoryRouter>);

describe('RegisterPage', () => {
  beforeEach(() => {
    api.post.mockReset();
  });

  afterEach(() => {
    cleanup();
  });

  it('shows doctor registration fields when doctor role is selected', async () => {
    renderPage({ fixedRole: 'DOCTOR', title: 'Doctor registration' });
    expect(screen.getByLabelText(/qualification/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/license number/i)).toBeInTheDocument();
    expect(screen.queryByLabelText(/date of birth/i)).not.toBeInTheDocument();
  });

  it('submits patient registration to the auth API', async () => {
    api.post.mockResolvedValueOnce({ data: {} });
    renderPage();
    fireEvent.change(screen.getByLabelText(/full name/i), { target: { value: 'Jane Patient' } });
    fireEvent.change(screen.getByLabelText(/email/i), { target: { value: 'jane@example.com' } });
    fireEvent.change(screen.getByLabelText(/phone/i), { target: { value: '9876543210' } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'Secure@1' } });
    fireEvent.change(screen.getByLabelText(/date of birth/i), { target: { value: '1995-04-12' } });
    fireEvent.click(screen.getByRole('button', { name: /register/i }));
    await waitFor(() => expect(api.post).toHaveBeenCalledWith('/auth/patient/register', expect.objectContaining({
      email: 'jane@example.com',
    })));
  });

  it('submits doctor registration to the doctor auth API', async () => {
    api.post.mockResolvedValueOnce({ data: {} });
    renderPage({ fixedRole: 'DOCTOR', title: 'Doctor registration' });
    fireEvent.change(screen.getByLabelText(/full name/i), { target: { value: 'Dana Doctor' } });
    fireEvent.change(screen.getByLabelText(/email/i), { target: { value: 'doctor@example.com' } });
    fireEvent.change(screen.getByLabelText(/phone/i), { target: { value: '9876543211' } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'Secure@1' } });
    fireEvent.change(screen.getByLabelText(/qualification/i), { target: { value: 'MBBS' } });
    fireEvent.change(screen.getByLabelText(/specialization/i), { target: { value: 'Cardiology' } });
    fireEvent.change(screen.getByLabelText(/experience/i), { target: { value: '8' } });
    fireEvent.change(screen.getByLabelText(/license number/i), { target: { value: 'MED-1001' } });
    fireEvent.change(screen.getByLabelText(/consultation fee/i), { target: { value: '500' } });
    fireEvent.change(screen.getByLabelText(/clinic address/i), { target: { value: 'Central Clinic' } });
    fireEvent.click(screen.getByRole('button', { name: /register/i }));
    await waitFor(() => expect(api.post).toHaveBeenCalledWith('/auth/doctor/register', expect.objectContaining({
      email: 'doctor@example.com',
      qualification: 'MBBS',
    })));
  });
});
