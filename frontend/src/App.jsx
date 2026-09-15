import React from 'react';
import './App.css';

function App() {
  return (
    <div className="app-container">
      <header className="hero-header">
        <div className="badge-wrapper">
          <span className="badge-dot"></span>
          <span>Foundation Ready &bull; Stage 1</span>
        </div>
        <h1 className="hero-title">Multilingual Ticket Translator</h1>
        <p className="hero-subtitle">
          Intelligent customer support workspace bridging global communication barriers with real-time bidirectional translation.
        </p>
      </header>

      <section className="status-grid" aria-label="System Components">
        <article className="status-card">
          <div className="card-header">
            <span className="card-icon">⚡</span>
            <h2 className="card-title">Frontend Foundation</h2>
          </div>
          <p className="card-description">
            React.js + Vite development environment configured with responsive CSS design tokens.
          </p>
          <span className="card-badge">Initialized</span>
        </article>

        <article className="status-card">
          <div className="card-header">
            <span className="card-icon">☕</span>
            <h2 className="card-title">Backend Architecture</h2>
          </div>
          <p className="card-description">
            Spring Boot 3.x REST API baseline configured with Maven, Spring Data JPA, and MySQL connector.
          </p>
          <span className="card-badge">Scaffolded</span>
        </article>

        <article className="status-card">
          <div className="card-header">
            <span className="card-icon">🗄️</span>
            <h2 className="card-title">Database &amp; Docs</h2>
          </div>
          <p className="card-description">
            MySQL schema repository and system architecture documentation ready for feature development.
          </p>
          <span className="card-badge">Structured</span>
        </article>
      </section>

      <footer className="roadmap-preview">
        <h2>Next Stage</h2>
        <p>Awaiting instructions to proceed with Database Schema and API Model implementations.</p>
      </footer>
    </div>
  );
}

export default App;
