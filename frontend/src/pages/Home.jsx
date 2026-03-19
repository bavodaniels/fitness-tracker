import { useAuth } from '../services/AuthContext';
import { Link } from 'react-router-dom';
import './Home.css';

function Home() {
  const { user } = useAuth();

  return (
    <div className="home-container">
      <div className="hero-section">
        <h1 className="hero-title">
          Track Your Fitness Journey <br />
          <span className="text-highlight">With Confidence</span>
        </h1>
        <p className="hero-subtitle">
          Log workouts, monitor your progress, and achieve your fitness goals with our intuitive platform.
        </p>

        <div className="hero-actions">
          {user ? (
            <Link to="/workouts" className="btn btn-primary btn-large">Go to Dashboard</Link>
          ) : (
            <>
              <Link to="/login" className="btn btn-primary btn-large">Start for Free</Link>
              <Link to="/login" className="btn btn-secondary btn-large">Learn More</Link>
            </>
          )}
        </div>
      </div>

      <div className="features-grid">
        <div className="feature-card">
          <div className="feature-icon">🏋️</div>
          <h3>Log Workouts</h3>
          <p>Easily record sets, reps, and weights for all your exercises.</p>
        </div>
        <div className="feature-card">
          <div className="feature-icon">📈</div>
          <h3>Track Progress</h3>
          <p>Visualize your history and see your strength gains over time.</p>
        </div>
        <div className="feature-card">
          <div className="feature-icon">🎯</div>
          <h3>Hit Goals</h3>
          <p>Stay motivated by setting and crushing your personal bests.</p>
        </div>
      </div>
    </div>
  );
}

export default Home;
