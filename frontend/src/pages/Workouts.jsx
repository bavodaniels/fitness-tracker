import { useState, useEffect } from 'react';
import { workoutApi } from '../services/api';
import './Workouts.css';

const EXERCISE_LABELS = {
  DEADLIFT: 'Deadlift',
  BACK_SQUAT: 'Back Squat',
  BENCH_PRESS: 'Bench Press',
};

function formatDate(iso) {
  const date = new Date(iso);
  return new Intl.DateTimeFormat('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: 'numeric',
    minute: '2-digit'
  }).format(date);
}

function Workouts() {
  const [workouts, setWorkouts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    workoutApi.getAll()
      .then((data) => {
        const sorted = data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
        setWorkouts(sorted);
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return (
    <div className="state-container">
      <div className="spinner"></div>
      <p>Loading your workouts...</p>
    </div>
  );

  if (error) return (
    <div className="state-container">
      <div className="error-alert">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
        {error}
      </div>
    </div>
  );

  return (
    <div className="workouts-container">
      <div className="workouts-header">
        <h1>Workout History</h1>
        <p>Review your past performances and track your progress.</p>
      </div>

      {workouts.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">📊</div>
          <h2>No workouts found</h2>
          <p>You haven't logged any workouts yet. Time to hit the gym!</p>
        </div>
      ) : (
        <div className="workouts-grid">
          {workouts.map((w) => (
            <div className="workout-card" key={w.id}>
              <div className="workout-card-header">
                <span className="workout-type">{EXERCISE_LABELS[w.exerciseType] || w.exerciseType}</span>
                <span className="workout-date">{formatDate(w.createdAt)}</span>
              </div>
              <div className="workout-stats">
                <div className="stat-box">
                  <span className="stat-value">{w.sets}</span>
                  <span className="stat-label">Sets</span>
                </div>
                <div className="stat-divider">×</div>
                <div className="stat-box">
                  <span className="stat-value">{w.reps}</span>
                  <span className="stat-label">Reps</span>
                </div>
                <div className="stat-divider">@</div>
                <div className="stat-box weight-box">
                  <span className="stat-value">{w.weight}</span>
                  <span className="stat-label">lbs</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default Workouts;
