import { useState } from 'react';
import { workoutApi } from '../services/api';
import './LogWorkout.css';

const EXERCISE_TYPES = [
  { value: 'DEADLIFT', label: 'Deadlift' },
  { value: 'BACK_SQUAT', label: 'Back Squat' },
  { value: 'BENCH_PRESS', label: 'Bench Press' },
];

function LogWorkout() {
  const [exerciseType, setExerciseType] = useState('');
  const [sets, setSets] = useState('');
  const [reps, setReps] = useState('');
  const [weight, setWeight] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (!exerciseType) {
      setError('Please select an exercise type.');
      return;
    }

    const setsNum = parseInt(sets, 10);
    const repsNum = parseInt(reps, 10);
    const weightNum = parseInt(weight, 10);

    if (!setsNum || setsNum <= 0 || !repsNum || repsNum <= 0 || !weightNum || weightNum <= 0) {
      setError('Sets, reps, and weight must be positive numbers.');
      return;
    }

    setLoading(true);
    try {
      await workoutApi.create({
        exerciseType,
        sets: setsNum,
        reps: repsNum,
        weight: weightNum,
      });
      setSuccess('Workout logged successfully!');
      setExerciseType('');
      setSets('');
      setReps('');
      setWeight('');

      // Clear success message after 3 seconds
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="log-workout-container">
      <div className="log-workout-card">
        <div className="log-workout-header">
          <h1>Log a Workout</h1>
          <p>Record your exercise details below.</p>
        </div>

        <form onSubmit={handleSubmit} className="workout-form">
          <div className="form-group full-width">
            <label htmlFor="exerciseType">Exercise Type</label>
            <div className="select-wrapper">
              <select
                id="exerciseType"
                value={exerciseType}
                onChange={(e) => setExerciseType(e.target.value)}
                disabled={loading}
              >
                <option value="" disabled>Select an exercise...</option>
                {EXERCISE_TYPES.map((ex) => (
                  <option key={ex.value} value={ex.value}>{ex.label}</option>
                ))}
              </select>
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="sets">Sets</label>
              <input
                id="sets"
                type="number"
                min="1"
                value={sets}
                onChange={(e) => setSets(e.target.value)}
                placeholder="e.g. 3"
                disabled={loading}
              />
            </div>
            <div className="form-group">
              <label htmlFor="reps">Reps</label>
              <input
                id="reps"
                type="number"
                min="1"
                value={reps}
                onChange={(e) => setReps(e.target.value)}
                placeholder="e.g. 10"
                disabled={loading}
              />
            </div>
          </div>

          <div className="form-group full-width">
            <label htmlFor="weight">Weight (lbs)</label>
            <div className="input-with-suffix">
              <input
                id="weight"
                type="number"
                min="1"
                value={weight}
                onChange={(e) => setWeight(e.target.value)}
                placeholder="e.g. 135"
                disabled={loading}
              />
              <span className="input-suffix">lbs</span>
            </div>
          </div>

          {error && (
            <div className="error-alert">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
              {error}
            </div>
          )}

          {success && (
            <div className="success-alert">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>
              {success}
            </div>
          )}

          <button type="submit" className="submit-button" disabled={loading}>
            {loading ? 'Saving...' : 'Save Workout'}
          </button>
        </form>
      </div>
    </div>
  );
}

export default LogWorkout;
