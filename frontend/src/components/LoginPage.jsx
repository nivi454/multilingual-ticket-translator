import React, { useState } from 'react';
import './LoginPage.css';

export default function LoginPage({ onLoginSuccess }) {
  const [isSignUp, setIsSignUp] = useState(false);
  const [name, setName] = useState('');
  const [email, setEmail] = useState('agent@tickettranslator.com');
  const [password, setPassword] = useState('password123');
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(true);
  const [userRole, setUserRole] = useState('Support Agent');
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    setErrorMessage('');

    if (!email.trim() || !email.includes('@')) {
      setErrorMessage('Please enter a valid email address.');
      return;
    }

    if (!password || password.length < 6) {
      setErrorMessage('Password must be at least 6 characters.');
      return;
    }

    if (isSignUp && !name.trim()) {
      setErrorMessage('Please enter your full name.');
      return;
    }

    setIsLoading(true);

    // Simulate brief authentication handshake
    setTimeout(() => {
      setIsLoading(false);
      const userObj = {
        name: isSignUp ? name.trim() : (email.startsWith('agent') ? 'Alex Rivera' : email.split('@')[0]),
        email: email.trim(),
        role: isSignUp ? userRole : 'Support Agent',
        avatar: isSignUp ? '👤' : '🚆',
        token: 'auth_' + Math.random().toString(36).substring(2, 10),
      };

      if (rememberMe) {
        localStorage.setItem('ticket_translator_user', JSON.stringify(userObj));
      } else {
        sessionStorage.setItem('ticket_translator_user', JSON.stringify(userObj));
      }

      onLoginSuccess(userObj);
    }, 450);
  };

  const handleQuickDemoLogin = (roleType) => {
    setIsLoading(true);
    setErrorMessage('');
    setTimeout(() => {
      setIsLoading(false);
      const demoUser = {
        name: roleType === 'agent' ? 'Alex Rivera (Agent)' : 'Elena Rostova (Traveler)',
        email: roleType === 'agent' ? 'alex.rivera@tickettranslator.com' : 'elena.rostova@traveler.org',
        role: roleType === 'agent' ? 'Senior Support Agent' : 'International Traveler',
        avatar: roleType === 'agent' ? '🎫' : '✈️',
        token: 'demo_' + Date.now(),
      };
      localStorage.setItem('ticket_translator_user', JSON.stringify(demoUser));
      onLoginSuccess(demoUser);
    }, 300);
  };

  return (
    <div className="login-page-wrapper">
      {/* Background ambient lighting */}
      <div className="login-bg-glow glow-1"></div>
      <div className="login-bg-glow glow-2"></div>

      <div className="login-main-container">
        {/* Left Side: Travel & Translation Hero Showcase */}
        <div className="login-hero-pane">
          <div className="login-brand-header">
            <div className="login-brand-badge">
              <span className="badge-pulse-dot"></span>
              <span>Global Ticket Intelligence</span>
            </div>
            <h1 className="login-brand-title">
              Multilingual Ticket <span className="gradient-text">Translator</span>
            </h1>
            <p className="login-brand-desc">
              Next-generation OCR document extraction, intelligent route parsing, and multilingual translation for global railway, airline, and bus transit tickets.
            </p>
          </div>

          {/* Travel Badges Row */}
          <div className="login-carrier-strip">
            <span className="carrier-pill">🚆 Renfe AVE</span>
            <span className="carrier-pill">🚄 SNCF TGV</span>
            <span className="carrier-pill">🚅 DB ICE</span>
            <span className="carrier-pill">🚇 Eurostar</span>
            <span className="carrier-pill">✈️ Flight Pass</span>
          </div>

          {/* Feature Highlights Grid */}
          <div className="login-features-list">
            <div className="login-feature-card">
              <div className="feature-icon-box">🔍</div>
              <div className="feature-text-content">
                <h3>Multi-Format OCR Engine</h3>
                <p>High-resolution text extraction from PDF, PNG, JPG, and WEBP tickets with automated preprocessing.</p>
              </div>
            </div>

            <div className="login-feature-card">
              <div className="feature-icon-box">🌐</div>
              <div className="feature-text-content">
                <h3>12+ Language Detection</h3>
                <p>N-gram script profiling across Spanish, French, German, Hindi, Japanese, Arabic, and more.</p>
              </div>
            </div>

            <div className="login-feature-card">
              <div className="feature-icon-box">📍</div>
              <div className="feature-text-content">
                <h3>Smart Itinerary & PNR Parsing</h3>
                <p>Instantly extracts passenger names, origin/destination stations, departure times, seats, and booking codes.</p>
              </div>
            </div>
          </div>

          {/* Stats Bar */}
          <div className="login-stats-bar">
            <div className="stat-col">
              <span className="stat-num">99.4%</span>
              <span className="stat-lbl">OCR Accuracy</span>
            </div>
            <div className="stat-divider"></div>
            <div className="stat-col">
              <span className="stat-num">&lt;1.5s</span>
              <span className="stat-lbl">Processing Speed</span>
            </div>
            <div className="stat-divider"></div>
            <div className="stat-col">
              <span className="stat-num">100%</span>
              <span className="stat-lbl">Private & Secure</span>
            </div>
          </div>
        </div>

        {/* Right Side: Authentication Card */}
        <div className="login-form-pane">
          <div className="auth-card">
            <div className="auth-card-header">
              <div className="auth-mode-toggle">
                <button
                  type="button"
                  className={`auth-toggle-btn ${!isSignUp ? 'active' : ''}`}
                  onClick={() => {
                    setIsSignUp(false);
                    setErrorMessage('');
                  }}
                >
                  Sign In
                </button>
                <button
                  type="button"
                  className={`auth-toggle-btn ${isSignUp ? 'active' : ''}`}
                  onClick={() => {
                    setIsSignUp(true);
                    setErrorMessage('');
                  }}
                >
                  Create Account
                </button>
              </div>

              <h2 className="auth-title">
                {isSignUp ? 'Create your agent account' : 'Welcome back to workspace'}
              </h2>
              <p className="auth-subtitle">
                {isSignUp
                  ? 'Get started translating international transport tickets in seconds.'
                  : 'Enter your credentials to access the ticket translation suite.'}
              </p>
            </div>

            {/* Error Message */}
            {errorMessage && (
              <div className="auth-error-alert" role="alert">
                <span className="error-icon">⚠️</span>
                <span>{errorMessage}</span>
              </div>
            )}

            {/* Form */}
            <form onSubmit={handleSubmit} className="auth-form">
              {isSignUp && (
                <div className="auth-input-group">
                  <label className="auth-label">Full Name</label>
                  <div className="input-with-icon">
                    <span className="input-icon">👤</span>
                    <input
                      type="text"
                      className="auth-input"
                      placeholder="e.g. Alex Rivera"
                      value={name}
                      onChange={(e) => setName(e.target.value)}
                      required
                    />
                  </div>
                </div>
              )}

              <div className="auth-input-group">
                <label className="auth-label">Email Address</label>
                <div className="input-with-icon">
                  <span className="input-icon">✉️</span>
                  <input
                    type="email"
                    className="auth-input"
                    placeholder="agent@tickettranslator.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                  />
                </div>
              </div>

              <div className="auth-input-group">
                <div className="auth-label-row">
                  <label className="auth-label">Password</label>
                  {!isSignUp && (
                    <button
                      type="button"
                      className="auth-forgot-link"
                      onClick={() => setErrorMessage('Demo Mode: Use any password (6+ chars) or click Quick Demo Login.')}
                    >
                      Forgot?
                    </button>
                  )}
                </div>
                <div className="input-with-icon">
                  <span className="input-icon">🔒</span>
                  <input
                    type={showPassword ? 'text' : 'password'}
                    className="auth-input"
                    placeholder="Enter at least 6 characters"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                  />
                  <button
                    type="button"
                    className="toggle-password-btn"
                    onClick={() => setShowPassword(!showPassword)}
                    title={showPassword ? 'Hide password' : 'Show password'}
                  >
                    {showPassword ? '👁️' : '👁️‍🗨️'}
                  </button>
                </div>
              </div>

              {isSignUp && (
                <div className="auth-input-group">
                  <label className="auth-label">Workspace Role</label>
                  <select
                    className="auth-select"
                    value={userRole}
                    onChange={(e) => setUserRole(e.target.value)}
                  >
                    <option value="Support Agent">Support Agent (Default)</option>
                    <option value="Travel Manager">Travel Manager / Coordinator</option>
                    <option value="Passenger / Traveler">Passenger / Traveler</option>
                    <option value="Administrator">Translation Administrator</option>
                  </select>
                </div>
              )}

              <div className="auth-options-row">
                <label className="remember-checkbox-label">
                  <input
                    type="checkbox"
                    checked={rememberMe}
                    onChange={(e) => setRememberMe(e.target.checked)}
                  />
                  <span>Keep me signed in</span>
                </label>
              </div>

              {/* Primary Submit Button */}
              <button
                type="submit"
                className="btn-auth-submit"
                disabled={isLoading}
              >
                {isLoading ? (
                  <>
                    <span className="auth-spinner"></span>
                    <span>Authenticating...</span>
                  </>
                ) : (
                  <>
                    <span>{isSignUp ? 'Create Free Account' : 'Sign In to Workspace'}</span>
                    <span className="btn-arrow">➔</span>
                  </>
                )}
              </button>
            </form>

            {/* Quick Demo Login Option */}
            <div className="demo-login-divider">
              <span>Or explore instantly</span>
            </div>

            <div className="demo-actions-grid">
              <button
                type="button"
                className="btn-demo-quick"
                onClick={() => handleQuickDemoLogin('agent')}
                disabled={isLoading}
              >
                <span className="demo-btn-icon">⚡</span>
                <span className="demo-btn-text">
                  <strong>Demo Support Agent</strong>
                  <small>Full OCR & Glossary Access</small>
                </span>
              </button>

              <button
                type="button"
                className="btn-demo-quick secondary"
                onClick={() => handleQuickDemoLogin('traveler')}
                disabled={isLoading}
              >
                <span className="demo-btn-icon">✈️</span>
                <span className="demo-btn-text">
                  <strong>Demo Traveler</strong>
                  <small>Ticket Analysis Preview</small>
                </span>
              </button>
            </div>

            {/* Footer Notice */}
            <div className="auth-card-footer">
              <span className="security-icon">🔒</span>
              <span>Encrypted Session &bull; ISO-27001 Security Standard</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
