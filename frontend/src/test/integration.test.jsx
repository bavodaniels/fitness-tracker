import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Routes, Route, Link, useNavigate } from 'react-router-dom';
import { describe, it, expect, beforeEach, vi } from 'vitest';

// Mock fetch globally
const mockFetch = vi.fn();
global.fetch = mockFetch;
import Login from '../pages/Login';
import Home from '../pages/Home';
import Workouts from '../pages/Workouts';
import LogWorkout from '../pages/LogWorkout';
import ProtectedRoute from '../components/ProtectedRoute';
import { AuthProvider, useAuth } from '../services/AuthContext';

function TestNavBar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };
  return (
    <nav>
      <Link to="/">Home</Link>
      {user ? (
        <>
          <Link to="/log-workout">Log Workout</Link>
          <Link to="/workouts">Workouts</Link>
          <button onClick={handleLogout}>Logout</button>
        </>
      ) : (
        <Link to="/login">Login</Link>
      )}
    </nav>
  );
}

function TestApp() {
  return (
    <AuthProvider>
      <TestNavBar />
      <main>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/workouts" element={<ProtectedRoute><Workouts /></ProtectedRoute>} />
          <Route path="/log-workout" element={<ProtectedRoute><LogWorkout /></ProtectedRoute>} />
        </Routes>
      </main>
    </AuthProvider>
  );
}

function renderTestApp(initialRoute = '/') {
  return render(
    <MemoryRouter initialEntries={[initialRoute]}>
      <TestApp />
    </MemoryRouter>
  );
}

beforeEach(() => {
  mockFetch.mockReset();
  sessionStorage.clear();
});

describe('Login Flow', () => {
  it('shows login form and validates empty fields', async () => {
    const user = userEvent.setup();
    renderTestApp('/login');

    expect(screen.getByRole('heading', { name: 'Login' })).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Login' }));
    expect(screen.getByText('Username and password are required.')).toBeInTheDocument();
  });

  it('toggles between login and register', async () => {
    const user = userEvent.setup();
    renderTestApp('/login');

    expect(screen.getByRole('heading', { name: 'Login' })).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Register' }));
    expect(screen.getByRole('heading', { name: 'Register' })).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Login' }));
    expect(screen.getByRole('heading', { name: 'Login' })).toBeInTheDocument();
  });

  it('logs in successfully and redirects to home', async () => {
    const user = userEvent.setup();
    mockFetch.mockResolvedValueOnce({
      ok: true,
      text: () => Promise.resolve(JSON.stringify({
        id: '123', username: 'testuser', token: 'abc-token'
      })),
    });

    renderTestApp('/login');

    await user.type(screen.getByLabelText('Username'), 'testuser');
    await user.type(screen.getByLabelText('Password'), 'password');
    await user.click(screen.getByRole('button', { name: 'Login' }));

    await waitFor(() => {
      expect(screen.getByText(/Welcome back, testuser/)).toBeInTheDocument();
    });
  });

  it('shows error on failed login', async () => {
    const user = userEvent.setup();
    mockFetch.mockResolvedValueOnce({
      ok: false,
      json: () => Promise.resolve({ error: 'Invalid credentials' }),
    });

    renderTestApp('/login');

    await user.type(screen.getByLabelText('Username'), 'testuser');
    await user.type(screen.getByLabelText('Password'), 'wrong');
    await user.click(screen.getByRole('button', { name: 'Login' }));

    await waitFor(() => {
      expect(screen.getByText('Invalid credentials')).toBeInTheDocument();
    });
  });
});

describe('Registration Flow', () => {
  it('registers and auto-logs in', async () => {
    const user = userEvent.setup();
    // Register call
    mockFetch.mockResolvedValueOnce({
      ok: true,
      text: () => Promise.resolve(JSON.stringify({ id: '123', username: 'newuser' })),
    });
    // Auto-login call
    mockFetch.mockResolvedValueOnce({
      ok: true,
      text: () => Promise.resolve(JSON.stringify({
        id: '123', username: 'newuser', token: 'new-token'
      })),
    });

    renderTestApp('/login');

    // Switch to register mode
    await user.click(screen.getByRole('button', { name: 'Register' }));
    await user.type(screen.getByLabelText('Username'), 'newuser');
    await user.type(screen.getByLabelText('Password'), 'password');
    await user.click(screen.getByRole('button', { name: 'Register' }));

    await waitFor(() => {
      expect(screen.getByText(/Welcome back, newuser/)).toBeInTheDocument();
    });
  });
});

describe('Protected Routes', () => {
  it('redirects to login when accessing workouts unauthenticated', () => {
    renderTestApp('/workouts');
    expect(screen.getByRole('heading', { name: 'Login' })).toBeInTheDocument();
  });

  it('redirects to login when accessing log-workout unauthenticated', () => {
    renderTestApp('/log-workout');
    expect(screen.getByRole('heading', { name: 'Login' })).toBeInTheDocument();
  });
});

describe('Session Persistence', () => {
  it('persists session across renders', () => {
    sessionStorage.setItem('user', JSON.stringify({
      id: '123', username: 'testuser', token: 'abc-token'
    }));

    renderTestApp('/');
    expect(screen.getByText(/Welcome back, testuser/)).toBeInTheDocument();
  });
});

describe('Workout Logging Form', () => {
  beforeEach(() => {
    sessionStorage.setItem('user', JSON.stringify({
      id: '123', username: 'testuser', token: 'abc-token'
    }));
  });

  it('validates all fields required', async () => {
    const user = userEvent.setup();
    renderTestApp('/log-workout');

    await user.click(screen.getByRole('button', { name: 'Log Workout' }));
    expect(screen.getByText('Please select an exercise type.')).toBeInTheDocument();
  });

  it('validates positive numbers', async () => {
    const user = userEvent.setup();
    renderTestApp('/log-workout');

    // Select exercise but leave numeric fields empty
    await user.selectOptions(screen.getByLabelText('Exercise'), 'DEADLIFT');
    await user.click(screen.getByRole('button', { name: 'Log Workout' }));

    expect(screen.getByText('Sets, reps, and weight must be positive numbers.')).toBeInTheDocument();
  });

  it('submits workout successfully', async () => {
    const user = userEvent.setup();
    mockFetch.mockResolvedValueOnce({
      ok: true,
      text: () => Promise.resolve(JSON.stringify({
        id: 'w1', exerciseType: 'DEADLIFT', sets: 3, reps: 5, weight: 225
      })),
    });

    renderTestApp('/log-workout');

    await user.selectOptions(screen.getByLabelText('Exercise'), 'DEADLIFT');
    await user.type(screen.getByLabelText('Sets'), '3');
    await user.type(screen.getByLabelText('Reps'), '5');
    await user.type(screen.getByLabelText('Weight (lbs)'), '225');
    await user.click(screen.getByRole('button', { name: 'Log Workout' }));

    await waitFor(() => {
      expect(screen.getByText('Workout logged successfully!')).toBeInTheDocument();
    });
  });
});

describe('Workout History View', () => {
  beforeEach(() => {
    sessionStorage.setItem('user', JSON.stringify({
      id: '123', username: 'testuser', token: 'abc-token'
    }));
  });

  it('shows loading then displays workouts', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      text: () => Promise.resolve(JSON.stringify([
        { id: 'w1', exerciseType: 'DEADLIFT', sets: 3, reps: 5, weight: 225, createdAt: '2026-03-19T10:00:00Z' },
        { id: 'w2', exerciseType: 'BENCH_PRESS', sets: 4, reps: 8, weight: 135, createdAt: '2026-03-18T10:00:00Z' },
      ])),
    });

    renderTestApp('/workouts');

    expect(screen.getByText('Loading workouts...')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText('Deadlift')).toBeInTheDocument();
      expect(screen.getByText('Bench Press')).toBeInTheDocument();
    });
  });

  it('shows empty state when no workouts', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      text: () => Promise.resolve('[]'),
    });

    renderTestApp('/workouts');

    await waitFor(() => {
      expect(screen.getByText(/No workouts logged yet/)).toBeInTheDocument();
    });
  });

  it('shows error on fetch failure', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: false,
      json: () => Promise.resolve({ error: 'Unauthorized' }),
    });

    renderTestApp('/workouts');

    await waitFor(() => {
      expect(screen.getByText('Unauthorized')).toBeInTheDocument();
    });
  });
});

describe('Logout Flow', () => {
  it('clears session and redirects to login', async () => {
    const user = userEvent.setup();
    sessionStorage.setItem('user', JSON.stringify({
      id: '123', username: 'testuser', token: 'abc-token'
    }));

    mockFetch.mockResolvedValueOnce({
      ok: true,
      text: () => Promise.resolve(JSON.stringify({ message: 'Logged out successfully' })),
    });

    renderTestApp('/');

    expect(screen.getByText(/Welcome back, testuser/)).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Logout' }));

    await waitFor(() => {
      expect(screen.getByRole('heading', { name: 'Login' })).toBeInTheDocument();
    });
    expect(sessionStorage.getItem('user')).toBeNull();
  });
});
