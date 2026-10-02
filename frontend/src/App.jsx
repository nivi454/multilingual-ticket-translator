import React, { useState, useEffect, useRef } from 'react';
import './App.css';
import LoginPage from './components/LoginPage';
const API_BASE_URL = 'https://multilingual-ticket-translator-production.up.railway.app';

const ALLOWED_MIME_TYPES = [
  'image/jpeg',
  'image/png',
  'image/webp',
  'application/pdf'
];

const ALLOWED_EXTENSIONS = ['.jpg', '.jpeg', '.png', '.webp', '.pdf'];

const LANGUAGE_OPTIONS = [
  { code: 'en', name: 'English (English)' },
  { code: 'es', name: 'Spanish (Español)' },
  { code: 'fr', name: 'French (Français)' },
  { code: 'de', name: 'German (Deutsch)' },
  { code: 'hi', name: 'Hindi (हिन्दी)' },
  { code: 'ja', name: 'Japanese (日本語)' },
  { code: 'zh', name: 'Chinese (中文 - 简体)' },
  { code: 'ar', name: 'Arabic (العربية)' },
  { code: 'pt', name: 'Portuguese (Português)' },
  { code: 'it', name: 'Italian (Italiano)' },
  { code: 'ru', name: 'Russian (Русский)' },
  { code: 'ko', name: 'Korean (한국어)' }
];

function App() {
  // Authentication State
  const [currentUser, setCurrentUser] = useState(() => {
    try {
      const saved = localStorage.getItem('ticket_translator_user') || sessionStorage.getItem('ticket_translator_user');
      return saved ? JSON.parse(saved) : null;
    } catch {
      return null;
    }
  });

  // Navigation Tabs: 'analyzer' | 'history' | 'glossary'
  const [activeTab, setActiveTab] = useState('analyzer');

  // Analyzer State
  const [selectedFile, setSelectedFile] = useState(null);
  const [filePreview, setFilePreview] = useState(null);
  const [uploadError, setUploadError] = useState(null);
  const [selectedLanguage, setSelectedLanguage] = useState('en');
  const [isDragging, setIsDragging] = useState(false);
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [analysisResult, setAnalysisResult] = useState(null);
  const [analysisError, setAnalysisError] = useState(null);
  const [showRawText, setShowRawText] = useState(false);
  const [copiedSummary, setCopiedSummary] = useState(false);

  // History State
  const [historyTickets, setHistoryTickets] = useState([]);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [historySearch, setHistorySearch] = useState('');
  const [historyFilterType, setHistoryFilterType] = useState('ALL');
  const [selectedHistoryModalTicket, setSelectedHistoryModalTicket] = useState(null);

  // Glossary State
  const [glossaryTerms, setGlossaryTerms] = useState([]);
  const [glossaryLoading, setGlossaryLoading] = useState(false);
  const [glossaryFilterSource, setGlossaryFilterSource] = useState('all');
  const [newTermSourceLang, setNewTermSourceLang] = useState('es');
  const [newTermTargetLang, setNewTermTargetLang] = useState('en');
  const [newTermSourceText, setNewTermSourceText] = useState('');
  const [newTermTranslatedText, setNewTermTranslatedText] = useState('');
  const [newTermCategory, setNewTermCategory] = useState('General');
  const [isAddingTerm, setIsAddingTerm] = useState(false);

  // Toast Notification State
  const [toastMessage, setToastMessage] = useState(null);

  const fileInputRef = useRef(null);

  const showToast = (text, type = 'info') => {
    setToastMessage({ text, type });
    setTimeout(() => {
      setToastMessage(null);
    }, 3500);
  };

  // Fetch History from Backend
  const fetchHistory = async () => {
    setHistoryLoading(true);
    try {
      const url = historySearch.trim()
        ? `/api/tickets/history?query=${encodeURIComponent(historySearch.trim())}`
        : '/api/tickets/history';
      const res = await fetch(`${API_BASE_URL}${url}`);
      if (res.ok) {
        const data = await res.json();
        setHistoryTickets(data.tickets || []);
      }
    } catch (err) {
      console.error('Failed to fetch ticket history:', err);
    } finally {
      setHistoryLoading(false);
    }
  };

  // Fetch Glossary from Backend
  const fetchGlossary = async () => {
    setGlossaryLoading(true);
    try {
      const res = await fetch(`${API_BASE_URL}/api/glossary`);
      if (res.ok) {
        const data = await res.json();
        setGlossaryTerms(data || []);
      }
    } catch (err) {
      console.error('Failed to fetch glossary terms:', err);
    } finally {
      setGlossaryLoading(false);
    }
  };

  useEffect(() => {
    if (activeTab === 'history') {
      fetchHistory();
    } else if (activeTab === 'glossary') {
      fetchGlossary();
    }
  }, [activeTab]);

  const isValidFile = (file) => {
    if (!file) return false;
    if (ALLOWED_MIME_TYPES.includes(file.type)) return true;
    const lowerName = file.name.toLowerCase();
    return ALLOWED_EXTENSIONS.some((ext) => lowerName.endsWith(ext));
  };

  const getReadableFileType = (file) => {
    if (!file) return '';
    const lowerName = file.name.toLowerCase();
    if (file.type === 'application/pdf' || lowerName.endsWith('.pdf')) return 'PDF Document';
    if (file.type === 'image/png' || lowerName.endsWith('.png')) return 'PNG Image';
    if (file.type === 'image/jpeg' || lowerName.endsWith('.jpg') || lowerName.endsWith('.jpeg')) return 'JPEG Image';
    if (file.type === 'image/webp' || lowerName.endsWith('.webp')) return 'WEBP Image';
    return file.type || 'Document';
  };

  const isImageFile = (file) => {
    if (!file) return false;
    const lowerName = file.name.toLowerCase();
    return (
      file.type.startsWith('image/') ||
      lowerName.endsWith('.jpg') ||
      lowerName.endsWith('.jpeg') ||
      lowerName.endsWith('.png') ||
      lowerName.endsWith('.webp')
    );
  };

  const processFile = (file) => {
    setUploadError(null);
    setAnalysisResult(null);
    setAnalysisError(null);

    if (!isValidFile(file)) {
      setUploadError('Unsupported file type. Please upload a JPG, PNG, WEBP, or PDF ticket file.');
      return;
    }

    const maxSizeBytes = 20 * 1024 * 1024;
    if (file.size > maxSizeBytes) {
      setUploadError('File size exceeds 20MB limit. Please upload a smaller ticket file.');
      return;
    }

    setSelectedFile(file);

    if (isImageFile(file)) {
      const reader = new FileReader();
      reader.onload = () => setFilePreview(reader.result);
      reader.onerror = () => {
        setFilePreview(null);
        setUploadError('Failed to read image preview.');
      };
      reader.readAsDataURL(file);
    } else {
      setFilePreview(null);
    }
  };

  const handleFileChange = (e) => {
    const file = e.target.files && e.target.files[0];
    if (file) processFile(file);
  };

  const handleDragOver = (e) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = (e) => {
    e.preventDefault();
    setIsDragging(false);
  };

  const handleDrop = (e) => {
    e.preventDefault();
    setIsDragging(false);
    const file = e.dataTransfer.files && e.dataTransfer.files[0];
    if (file) processFile(file);
  };

  const handleRemoveFile = (e) => {
    if (e) e.stopPropagation();
    setSelectedFile(null);
    setFilePreview(null);
    setUploadError(null);
    setAnalysisResult(null);
    setAnalysisError(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const formatFileSize = (bytes) => {
    if (!bytes || bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
  };

  const handleAnalyze = async () => {
    if (!selectedFile) {
      setUploadError('Please select or upload a ticket file first.');
      return;
    }

    setIsAnalyzing(true);
    setAnalysisError(null);

    const formData = new FormData();
    formData.append('file', selectedFile);
    formData.append('targetLanguage', selectedLanguage);

    try {
      const response = await fetch(`${API_BASE_URL}/api/tickets/analyze`, {
        method: 'POST',
        body: formData,
      });

      if (!response.ok) {
        const errData = await response.json().catch(() => ({}));
        throw new Error(errData.message || `Server responded with status ${response.status}`);
      }

      const data = await response.json();
      if (data.success) {
        setAnalysisResult(data);
        showToast('Ticket analyzed and saved to history!', 'success');
      } else {
        throw new Error(data.message || 'Analysis could not be completed.');
      }
    } catch (err) {
      console.error('Ticket analysis failed:', err);
      setAnalysisError(err.message || 'Failed to connect to OCR analysis service.');
      showToast('Analysis error: ' + (err.message || 'Connection failed'), 'error');
    } finally {
      setIsAnalyzing(false);
    }
  };

  const handleCopySummary = (text) => {
    if (!text) return;
    navigator.clipboard.writeText(text);
    setCopiedSummary(true);
    showToast('Summary copied to clipboard!', 'success');
    setTimeout(() => setCopiedSummary(false), 2000);
  };

  const handleDeleteTicket = async (id, e) => {
    if (e) e.stopPropagation();
    if (!window.confirm('Are you sure you want to delete this saved ticket?')) return;

    try {
      const res = await fetch(`${API_BASE_URL}/api/tickets/${id}`, { method: 'DELETE' });
      if (res.ok) {
        setHistoryTickets(historyTickets.filter((t) => t.id !== id));
        if (selectedHistoryModalTicket && selectedHistoryModalTicket.id === id) {
          setSelectedHistoryModalTicket(null);
        }
        showToast('Ticket removed from history.', 'info');
      }
    } catch (err) {
      console.error('Failed to delete ticket:', err);
      showToast('Failed to delete ticket', 'error');
    }
  };

  const handleAddGlossaryTerm = async (e) => {
    e.preventDefault();
    if (!newTermSourceText.trim() || !newTermTranslatedText.trim()) {
      showToast('Please enter both source and translated terms', 'error');
      return;
    }

    try {
      const res = await fetch(`${API_BASE_URL}/api/glossary`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          sourceLanguage: newTermSourceLang,
          targetLanguage: newTermTargetLang,
          sourceTerm: newTermSourceText.trim(),
          translatedTerm: newTermTranslatedText.trim(),
          category: newTermCategory,
        }),
      });

      if (res.ok) {
        const created = await res.json();
        setGlossaryTerms([created, ...glossaryTerms.filter((g) => g.id !== created.id)]);
        setNewTermSourceText('');
        setNewTermTranslatedText('');
        setIsAddingTerm(false);
        showToast('Custom glossary term saved!', 'success');
      }
    } catch (err) {
      console.error('Failed to save glossary term:', err);
      showToast('Error saving glossary term', 'error');
    }
  };

  const handleDeleteGlossaryTerm = async (id, e) => {
    if (e) e.stopPropagation();
    try {
      const res = await fetch(`${API_BASE_URL}/api/glossary/${id}`, { method: 'DELETE' });
      if (res.ok) {
        setGlossaryTerms(glossaryTerms.filter((t) => t.id !== id));
        showToast('Glossary term deleted', 'info');
      }
    } catch (err) {
      console.error('Failed to delete term:', err);
    }
  };

  const exportHistoryJson = () => {
    const dataStr = 'data:text/json;charset=utf-8,' + encodeURIComponent(JSON.stringify(historyTickets, null, 2));
    const downloadAnchor = document.createElement('a');
    downloadAnchor.setAttribute('href', dataStr);
    downloadAnchor.setAttribute('download', `ticket_history_${new Date().toISOString().slice(0, 10)}.json`);
    document.body.appendChild(downloadAnchor);
    downloadAnchor.click();
    downloadAnchor.remove();
    showToast('Exported history as JSON', 'success');
  };

  const handleLogout = () => {
    localStorage.removeItem('ticket_translator_user');
    sessionStorage.removeItem('ticket_translator_user');
    setCurrentUser(null);
    showToast('Signed out of workspace.', 'info');
  };

  const getTransportIcon = (type) => {
    const t = (type || '').toLowerCase();
    if (t.includes('flight') || t.includes('plane') || t.includes('air')) return '✈️';
    if (t.includes('train') || t.includes('ave') || t.includes('tgv') || t.includes('ice') || t.includes('rail')) return '🚆';
    if (t.includes('bus') || t.includes('autobus')) return '🚌';
    if (t.includes('ferry') || t.includes('ship')) return '🚢';
    return '🎫';
  };

  const filteredHistoryTickets = historyTickets.filter((t) => {
    if (historyFilterType === 'ALL') return true;
    const type = (t.transportType || '').toUpperCase();
    return type.includes(historyFilterType);
  });

  const filteredGlossaryTerms = glossaryTerms.filter((g) => {
    if (glossaryFilterSource === 'all') return true;
    return g.sourceLanguage === glossaryFilterSource;
  });

  // If user is not logged in, render the full-screen Login Page
  if (!currentUser) {
    return (
      <>
        {toastMessage && (
          <div className={`toast-banner toast-${toastMessage.type}`} role="status">
            <span>{toastMessage.text}</span>
          </div>
        )}
        <LoginPage
          onLoginSuccess={(user) => {
            setCurrentUser(user);
            showToast(`Welcome back, ${user.name}!`, 'success');
          }}
        />
      </>
    );
  }

  return (
    <div className="app-container">
      {/* Toast Notification */}
      {toastMessage && (
        <div className={`toast-banner toast-${toastMessage.type}`} role="status">
          <span>{toastMessage.text}</span>
        </div>
      )}

      {/* Top User Profile Header Bar */}
      <div className="user-profile-bar">
        <div className="user-info-group">
          <div className="user-avatar-badge">{currentUser.avatar || '👤'}</div>
          <div className="user-name-role">
            <span className="user-display-name">{currentUser.name}</span>
            <span className="user-role-tag">{currentUser.role || 'Support Agent'} &bull; {currentUser.email}</span>
          </div>
        </div>

        <button
          type="button"
          className="btn-signout"
          onClick={handleLogout}
          title="Sign out of workspace"
        >
          <span>🚪</span>
          <span>Sign Out</span>
        </button>
      </div>

      {/* Header */}
      <header className="app-header">
        <div className="header-badge">
          <span className="badge-dot"></span>
          <span>Multilingual OCR &bull; Travel Assistant</span>
        </div>
        <h1 className="header-title">Multilingual Ticket Translator</h1>
        <p className="header-subtitle">
          Extract, translate, and manage international transit tickets with intelligent OCR, route parsing, and custom travel glossaries.
        </p>

        {/* Navigation Tabs */}
        <nav className="tab-navigation" aria-label="Main Application Views">
          <button
            type="button"
            className={`tab-btn ${activeTab === 'analyzer' ? 'active' : ''}`}
            onClick={() => setActiveTab('analyzer')}
          >
            <span className="tab-icon">🔍</span>
            <span>Ticket Analyzer</span>
          </button>
          <button
            type="button"
            className={`tab-btn ${activeTab === 'history' ? 'active' : ''}`}
            onClick={() => setActiveTab('history')}
          >
            <span className="tab-icon">📜</span>
            <span>Ticket History</span>
            {historyTickets.length > 0 && <span className="tab-counter">{historyTickets.length}</span>}
          </button>
          <button
            type="button"
            className={`tab-btn ${activeTab === 'glossary' ? 'active' : ''}`}
            onClick={() => setActiveTab('glossary')}
          >
            <span className="tab-icon">📖</span>
            <span>Glossary Manager</span>
            {glossaryTerms.length > 0 && <span className="tab-counter">{glossaryTerms.length}</span>}
          </button>
        </nav>
      </header>

      {/* Main Content Area */}
      <main className="main-content">
        {/* ==================================================================== */}
        {/* TAB 1: TICKET ANALYZER */}
        {/* ==================================================================== */}
        {activeTab === 'analyzer' && (
          <div className="workspace-grid">
            {/* Upload & Configuration Card */}
            <section className="card upload-card" aria-labelledby="upload-heading">
              <div className="card-header">
                <div className="card-icon-title">
                  <span className="step-number">1</span>
                  <div>
                    <h2 id="upload-heading" className="card-title">Upload Ticket</h2>
                    <p className="card-subtitle">Select an image or PDF of your ticket</p>
                  </div>
                </div>
              </div>

              <div className="card-body">
                {/* File Upload Dropzone */}
                <div
                  className={`dropzone ${isDragging ? 'dragging' : ''} ${selectedFile ? 'has-file' : ''}`}
                  onDragOver={handleDragOver}
                  onDragLeave={handleDragLeave}
                  onDrop={handleDrop}
                  onClick={() => fileInputRef.current && fileInputRef.current.click()}
                  role="button"
                  tabIndex={0}
                  aria-label="Upload ticket file"
                  onKeyDown={(e) => {
                    if (e.key === 'Enter' || e.key === ' ') {
                      if (fileInputRef.current) fileInputRef.current.click();
                    }
                  }}
                >
                  <input
                    ref={fileInputRef}
                    id="ticket-file-input"
                    type="file"
                    accept=".jpg,.jpeg,.png,.webp,.pdf,image/jpeg,image/png,image/webp,application/pdf"
                    onChange={handleFileChange}
                    style={{ display: 'none' }}
                  />

                  {selectedFile ? (
                    <div className="file-preview-container">
                      {filePreview ? (
                        <div className="image-preview-wrapper">
                          <img src={filePreview} alt="Selected Ticket Preview" className="preview-img" />
                        </div>
                      ) : (
                        <div className="doc-icon-wrapper">
                          <span className="file-type-icon">📄</span>
                        </div>
                      )}
                      <div className="file-info">
                        <div className="file-name" title={selectedFile.name}>{selectedFile.name}</div>
                        <div className="file-meta">
                          <span className="file-size">{formatFileSize(selectedFile.size)}</span>
                          <span className="file-format-badge">{getReadableFileType(selectedFile)}</span>
                        </div>
                      </div>
                      <button
                        type="button"
                        className="btn-remove-file"
                        onClick={handleRemoveFile}
                        aria-label="Remove selected ticket file"
                        title="Remove file"
                      >
                        ✕
                      </button>
                    </div>
                  ) : (
                    <div className="dropzone-empty">
                      <div className="upload-icon-circle">
                        <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                          <polyline points="17 8 12 3 7 8"></polyline>
                          <line x1="12" y1="3" x2="12" y2="15"></line>
                        </svg>
                      </div>
                      <p className="dropzone-primary-text">
                        <strong>Click to browse</strong> or drag &amp; drop ticket here
                      </p>
                      <p className="dropzone-secondary-text">
                        Supports JPG, PNG, WEBP, and PDF files up to 20MB
                      </p>
                    </div>
                  )}
                </div>

                {/* Upload Error Banner */}
                {uploadError && (
                  <div className="upload-error-msg" role="alert">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <circle cx="12" cy="12" r="10"></circle>
                      <line x1="12" y1="8" x2="12" y2="12"></line>
                      <line x1="12" y1="16" x2="12.01" y2="16"></line>
                    </svg>
                    <span>{uploadError}</span>
                  </div>
                )}

                {/* Target Language Selection */}
                <div className="form-group">
                  <label htmlFor="language-select" className="form-label">
                    Target Language
                  </label>
                  <div className="select-wrapper">
                    <select
                      id="language-select"
                      className="form-select"
                      value={selectedLanguage}
                      onChange={(e) => setSelectedLanguage(e.target.value)}
                      disabled={isAnalyzing}
                    >
                      {LANGUAGE_OPTIONS.map((lang) => (
                        <option key={lang.code} value={lang.code}>
                          {lang.name}
                        </option>
                      ))}
                    </select>
                  </div>
                  <span className="input-hint">Select the language you want the ticket details translated into</span>
                </div>

                {/* Analyze Ticket Button */}
                <button
                  type="button"
                  id="analyze-ticket-btn"
                  className={`btn-analyze ${isAnalyzing ? 'loading' : ''}`}
                  onClick={handleAnalyze}
                  disabled={isAnalyzing}
                >
                  {isAnalyzing ? (
                    <>
                      <span className="spinner-icon"></span>
                      <span>Analyzing &amp; Translating...</span>
                    </>
                  ) : (
                    <>
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="btn-icon">
                        <circle cx="11" cy="11" r="8"></circle>
                        <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                      </svg>
                      <span>Analyze Ticket</span>
                    </>
                  )}
                </button>
              </div>
            </section>

            {/* Results Section */}
            <section className="card results-card" aria-labelledby="results-heading">
              <div className="card-header">
                <div className="card-icon-title">
                  <span className="step-number">2</span>
                  <div>
                    <h2 id="results-heading" className="card-title">Extracted Ticket Information</h2>
                    <p className="card-subtitle">Parsed journey details and translated summary</p>
                  </div>
                </div>
                {analysisResult ? (
                  <span className="status-pill status-success">✓ Saved to Database</span>
                ) : isAnalyzing ? (
                  <span className="status-pill status-active">Processing...</span>
                ) : (
                  <span className="status-pill">Ready for Analysis</span>
                )}
              </div>

              <div className="card-body">
                {/* Analysis Error Alert */}
                {analysisError && (
                  <div className="upload-error-msg" role="alert">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <circle cx="12" cy="12" r="10"></circle>
                      <line x1="12" y1="8" x2="12" y2="12"></line>
                      <line x1="12" y1="16" x2="12.01" y2="16"></line>
                    </svg>
                    <span>{analysisError}</span>
                  </div>
                )}

                {/* Active Results View */}
                {analysisResult ? (
                  <div className="results-display-container">
                    {/* Language Detection Banner */}
                    <div className="language-banner">
                      <div className="lang-tag">
                        <span className="lang-label">Detected:</span>
                        <strong className="lang-val">{analysisResult.detectedLanguage?.name || 'Unknown'}</strong>
                      </div>
                      <span className="lang-arrow">➔</span>
                      <div className="lang-tag">
                        <span className="lang-label">Translated to:</span>
                        <strong className="lang-val highlight">{analysisResult.targetLanguage?.name || selectedLanguage.toUpperCase()}</strong>
                      </div>
                    </div>

                    {/* Journey Route Hero */}
                    <div className="route-hero-card">
                      <div className="route-header">
                        <span className="transport-badge">
                          {getTransportIcon(analysisResult.translatedDetails?.transportType || analysisResult.extractedDetails?.transportType)}
                          <span>{analysisResult.translatedDetails?.transportType || analysisResult.extractedDetails?.transportType || 'Travel'}</span>
                        </span>
                        {analysisResult.extractedDetails?.carrierNumber && (
                          <span className="carrier-badge">{analysisResult.extractedDetails.carrierNumber}</span>
                        )}
                      </div>

                      <div className="route-path">
                        <div className="station-node">
                          <span className="station-type">Departure</span>
                          <span className="station-name">
                            {analysisResult.translatedDetails?.departureStation || analysisResult.extractedDetails?.departureStation || 'Station Origin'}
                          </span>
                          {analysisResult.extractedDetails?.departureTime && (
                            <span className="station-time">⏰ {analysisResult.extractedDetails.departureTime}</span>
                          )}
                        </div>

                        <div className="route-connector">
                          <div className="connector-line"></div>
                          <span className="connector-plane">➔</span>
                        </div>

                        <div className="station-node right">
                          <span className="station-type">Destination</span>
                          <span className="station-name">
                            {analysisResult.translatedDetails?.arrivalDestination || analysisResult.extractedDetails?.arrivalDestination || 'Destination'}
                          </span>
                          {analysisResult.extractedDetails?.arrivalTime && (
                            <span className="station-time">🏁 {analysisResult.extractedDetails.arrivalTime}</span>
                          )}
                        </div>
                      </div>
                    </div>

                    {/* Structured Details Grid */}
                    <div className="details-info-grid">
                      <div className="detail-item">
                        <span className="detail-label">👤 Passenger Name</span>
                        <span className="detail-value">
                          {analysisResult.extractedDetails?.passengerName || 'Not Specified'}
                        </span>
                      </div>

                      <div className="detail-item">
                        <span className="detail-label">📅 Travel Date</span>
                        <span className="detail-value">
                          {analysisResult.extractedDetails?.travelDate || 'Not Specified'}
                        </span>
                      </div>

                      <div className="detail-item">
                        <span className="detail-label">💺 Seat / Coach</span>
                        <span className="detail-value">
                          {analysisResult.translatedDetails?.coach || analysisResult.extractedDetails?.coach
                            ? `Coach ${analysisResult.translatedDetails?.coach || analysisResult.extractedDetails?.coach}, `
                            : ''}
                          {analysisResult.translatedDetails?.seat || analysisResult.extractedDetails?.seat
                            ? `Seat ${analysisResult.translatedDetails?.seat || analysisResult.extractedDetails?.seat}`
                            : 'General Seating'}
                        </span>
                      </div>

                      <div className="detail-item">
                        <span className="detail-label">⭐ Class</span>
                        <span className="detail-value">
                          {analysisResult.translatedDetails?.ticketClass || analysisResult.extractedDetails?.ticketClass || 'Standard Class'}
                        </span>
                      </div>

                      <div className="detail-item full-width-item">
                        <span className="detail-label">🔢 Booking Ref / PNR</span>
                        <span className="detail-value pnr-highlight">
                          {analysisResult.extractedDetails?.pnrNumber || 'N/A'}
                        </span>
                      </div>
                    </div>

                    {/* Translated Summary Card */}
                    <div className="summary-box">
                      <div className="summary-box-header">
                        <span className="summary-title">📝 Translated Summary</span>
                        <button
                          type="button"
                          className="btn-copy"
                          onClick={() => handleCopySummary(analysisResult.translatedDetails?.summary || analysisResult.translatedFullText)}
                          title="Copy Summary"
                        >
                          {copiedSummary ? '✓ Copied' : '📋 Copy'}
                        </button>
                      </div>
                      <p className="summary-text">
                        {analysisResult.translatedDetails?.summary || analysisResult.translatedFullText || 'Ticket translated successfully.'}
                      </p>
                    </div>

                    {/* Collapsible Raw Text Inspector */}
                    <div className="raw-text-accordion">
                      <button
                        type="button"
                        className="accordion-toggle-btn"
                        onClick={() => setShowRawText(!showRawText)}
                      >
                        <span>{showRawText ? '▼ Hide Full OCR & Translation' : '▶ View Extracted OCR & Full Translation'}</span>
                      </button>

                      {showRawText && (
                        <div className="raw-text-content">
                          <div className="text-split-view">
                            <div className="text-split-col">
                              <span className="text-split-heading">Original OCR Text ({analysisResult.detectedLanguage?.code?.toUpperCase()})</span>
                              <pre className="text-pre">{analysisResult.rawExtractedText}</pre>
                            </div>
                            <div className="text-split-col">
                              <span className="text-split-heading">Translated Text ({analysisResult.targetLanguage?.code?.toUpperCase()})</span>
                              <pre className="text-pre">{analysisResult.translatedFullText}</pre>
                            </div>
                          </div>
                        </div>
                      )}
                    </div>
                  </div>
                ) : (
                  /* Empty Results Placeholder */
                  <div className="empty-results-container">
                    <div className="empty-illustration">
                      <div className="empty-icon-circle">
                        <svg width="34" height="34" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round">
                          <rect x="2" y="4" width="20" height="16" rx="2"></rect>
                          <line x1="2" y1="10" x2="22" y2="10"></line>
                          <line x1="7" y1="15" x2="7.01" y2="15"></line>
                          <line x1="12" y1="15" x2="17" y2="15"></line>
                        </svg>
                      </div>
                    </div>
                    <h3 className="empty-title">No Ticket Analyzed Yet</h3>
                    <p className="empty-description">
                      Upload a ticket image or PDF on the left and click <strong>Analyze Ticket</strong>. The extracted itinerary, passenger information, and translated summary will appear here.
                    </p>
                  </div>
                )}
              </div>
            </section>
          </div>
        )}

        {/* ==================================================================== */}
        {/* TAB 2: TICKET HISTORY */}
        {/* ==================================================================== */}
        {activeTab === 'history' && (
          <div className="history-container">
            {/* History Toolbar */}
            <div className="history-toolbar">
              <div className="search-box-wrapper">
                <input
                  type="text"
                  className="history-search-input"
                  placeholder="Search by passenger, PNR, carrier, station, file..."
                  value={historySearch}
                  onChange={(e) => setHistorySearch(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') fetchHistory();
                  }}
                />
                <button type="button" className="btn-search" onClick={fetchHistory}>
                  🔍 Search
                </button>
              </div>

              <div className="filter-chips">
                {['ALL', 'TRAIN', 'FLIGHT', 'BUS', 'FERRY'].map((mode) => (
                  <button
                    key={mode}
                    type="button"
                    className={`filter-chip ${historyFilterType === mode ? 'active' : ''}`}
                    onClick={() => setHistoryFilterType(mode)}
                  >
                    {mode === 'ALL' ? 'All Tickets' : `${getTransportIcon(mode)} ${mode}`}
                  </button>
                ))}
              </div>

              <div className="toolbar-actions">
                <button type="button" className="btn-secondary" onClick={fetchHistory} title="Refresh history">
                  🔄 Refresh
                </button>
                {historyTickets.length > 0 && (
                  <button type="button" className="btn-export" onClick={exportHistoryJson} title="Export as JSON">
                    📥 Export JSON
                  </button>
                )}
              </div>
            </div>

            {/* History List / Grid */}
            {historyLoading ? (
              <div className="loading-state">
                <span className="spinner-icon"></span>
                <p>Loading ticket history from database...</p>
              </div>
            ) : filteredHistoryTickets.length === 0 ? (
              <div className="empty-history-card">
                <span className="empty-history-icon">🗄️</span>
                <h3>No Tickets Found</h3>
                <p>
                  {historySearch ? 'No tickets matched your search query.' : 'Scanned tickets will automatically be saved and displayed here.'}
                </p>
              </div>
            ) : (
              <div className="history-grid">
                {filteredHistoryTickets.map((ticket) => (
                  <article key={ticket.id} className="history-card" onClick={() => setSelectedHistoryModalTicket(ticket)}>
                    <div className="history-card-top">
                      <div className="history-type-badge">
                        <span>{getTransportIcon(ticket.transportType)}</span>
                        <span>{ticket.translatedTransportType || ticket.transportType || 'Travel'}</span>
                      </div>
                      <span className="history-date">
                        {ticket.createdAt ? new Date(ticket.createdAt).toLocaleDateString() : ''}
                      </span>
                    </div>

                    <h3 className="history-route">
                      {ticket.translatedDepartureStation || ticket.departureStation || 'Origin'} ➔{' '}
                      {ticket.translatedArrivalDestination || ticket.arrivalDestination || 'Destination'}
                    </h3>

                    <div className="history-meta-row">
                      <span className="history-passenger">👤 {ticket.passengerName || 'Passenger'}</span>
                      {ticket.carrierNumber && <span className="history-carrier">🚆 {ticket.carrierNumber}</span>}
                    </div>

                    <div className="history-footer">
                      <span className="history-pnr">PNR: <strong>{ticket.pnrNumber || 'N/A'}</strong></span>
                      <div className="history-actions">
                        <button
                          type="button"
                          className="btn-view-details"
                          onClick={() => setSelectedHistoryModalTicket(ticket)}
                        >
                          Details
                        </button>
                        <button
                          type="button"
                          className="btn-delete-ticket"
                          onClick={(e) => handleDeleteTicket(ticket.id, e)}
                          title="Delete Ticket"
                        >
                          🗑️
                        </button>
                      </div>
                    </div>
                  </article>
                ))}
              </div>
            )}
          </div>
        )}

        {/* ==================================================================== */}
        {/* TAB 3: GLOSSARY MANAGER */}
        {/* ==================================================================== */}
        {activeTab === 'glossary' && (
          <div className="glossary-container">
            <div className="glossary-header-card">
              <div className="glossary-intro">
                <h2>Multilingual Travel Terminology Glossary</h2>
                <p>
                  Manage custom term translations for transit systems, rail operators, stations, and fare classes to ensure high-accuracy translation.
                </p>
              </div>
              <button
                type="button"
                className="btn-primary"
                onClick={() => setIsAddingTerm(!isAddingTerm)}
              >
                {isAddingTerm ? '✕ Close Form' : '+ Add Custom Term'}
              </button>
            </div>

            {/* Add New Term Drawer / Form */}
            {isAddingTerm && (
              <form className="add-term-form" onSubmit={handleAddGlossaryTerm}>
                <h3>Add New Glossary Translation</h3>
                <div className="form-row-grid">
                  <div className="form-group">
                    <label className="form-label">Source Language</label>
                    <select
                      className="form-select"
                      value={newTermSourceLang}
                      onChange={(e) => setNewTermSourceLang(e.target.value)}
                    >
                      {LANGUAGE_OPTIONS.map((l) => (
                        <option key={l.code} value={l.code}>{l.name}</option>
                      ))}
                    </select>
                  </div>

                  <div className="form-group">
                    <label className="form-label">Target Language</label>
                    <select
                      className="form-select"
                      value={newTermTargetLang}
                      onChange={(e) => setNewTermTargetLang(e.target.value)}
                    >
                      {LANGUAGE_OPTIONS.map((l) => (
                        <option key={l.code} value={l.code}>{l.name}</option>
                      ))}
                    </select>
                  </div>

                  <div className="form-group">
                    <label className="form-label">Category</label>
                    <select
                      className="form-select"
                      value={newTermCategory}
                      onChange={(e) => setNewTermCategory(e.target.value)}
                    >
                      <option value="General">General</option>
                      <option value="Carrier">Carrier / Operator</option>
                      <option value="Station">Station / Airport</option>
                      <option value="Class">Class / Fare</option>
                    </select>
                  </div>
                </div>

                <div className="form-row-grid">
                  <div className="form-group">
                    <label className="form-label">Source Term / Phrase</label>
                    <input
                      type="text"
                      className="form-input"
                      placeholder="e.g. Renfe Ave, Coche, Gare de l'Est"
                      value={newTermSourceText}
                      onChange={(e) => setNewTermSourceText(e.target.value)}
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Preferred Translation</label>
                    <input
                      type="text"
                      className="form-input"
                      placeholder="e.g. High-Speed Train, Coach, East Station"
                      value={newTermTranslatedText}
                      onChange={(e) => setNewTermTranslatedText(e.target.value)}
                      required
                    />
                  </div>
                </div>

                <div className="form-submit-row">
                  <button type="submit" className="btn-primary">Save Glossary Term</button>
                  <button type="button" className="btn-secondary" onClick={() => setIsAddingTerm(false)}>Cancel</button>
                </div>
              </form>
            )}

            {/* Glossary Table */}
            <div className="glossary-table-container">
              <div className="glossary-filters">
                <span className="filter-label">Filter Source Language:</span>
                <div className="select-wrapper mini">
                  <select
                    className="form-select mini"
                    value={glossaryFilterSource}
                    onChange={(e) => setGlossaryFilterSource(e.target.value)}
                  >
                    <option value="all">All Languages</option>
                    <option value="es">Spanish (es)</option>
                    <option value="fr">French (fr)</option>
                    <option value="de">German (de)</option>
                    <option value="it">Italian (it)</option>
                  </select>
                </div>
              </div>

              {glossaryLoading ? (
                <div className="loading-state">
                  <span className="spinner-icon"></span>
                  <p>Loading glossary...</p>
                </div>
              ) : filteredGlossaryTerms.length === 0 ? (
                <div className="empty-history-card">
                  <p>No glossary terms found for this language.</p>
                </div>
              ) : (
                <table className="glossary-table">
                  <thead>
                    <tr>
                      <th>Source Language</th>
                      <th>Target</th>
                      <th>Original Term</th>
                      <th>Custom Translation</th>
                      <th>Category</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredGlossaryTerms.map((term) => (
                      <tr key={term.id}>
                        <td><span className="lang-pill">{term.sourceLanguage.toUpperCase()}</span></td>
                        <td><span className="lang-pill highlight">{term.targetLanguage.toUpperCase()}</span></td>
                        <td><strong>{term.sourceTerm}</strong></td>
                        <td className="translated-td">{term.translatedTerm}</td>
                        <td><span className="category-badge">{term.category || 'General'}</span></td>
                        <td>
                          <button
                            type="button"
                            className="btn-delete-sm"
                            onClick={(e) => handleDeleteGlossaryTerm(term.id, e)}
                            title="Delete term"
                          >
                            🗑️
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        )}

        {/* Modal: Ticket History Full Detail View */}
        {selectedHistoryModalTicket && (
          <div className="modal-overlay" onClick={() => setSelectedHistoryModalTicket(null)}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
              <div className="modal-header">
                <div className="modal-title-wrapper">
                  <span className="modal-icon">{getTransportIcon(selectedHistoryModalTicket.transportType)}</span>
                  <div>
                    <h3 className="modal-title">
                      {selectedHistoryModalTicket.translatedDepartureStation || selectedHistoryModalTicket.departureStation} ➔{' '}
                      {selectedHistoryModalTicket.translatedArrivalDestination || selectedHistoryModalTicket.arrivalDestination}
                    </h3>
                    <span className="modal-subtitle">{selectedHistoryModalTicket.fileName} ({selectedHistoryModalTicket.fileSize})</span>
                  </div>
                </div>
                <button
                  type="button"
                  className="modal-close-btn"
                  onClick={() => setSelectedHistoryModalTicket(null)}
                >
                  ✕
                </button>
              </div>

              <div className="modal-body">
                <div className="details-info-grid">
                  <div className="detail-item">
                    <span className="detail-label">👤 Passenger Name</span>
                    <span className="detail-value">{selectedHistoryModalTicket.passengerName || 'N/A'}</span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">📅 Travel Date</span>
                    <span className="detail-value">{selectedHistoryModalTicket.travelDate || 'N/A'}</span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">⏰ Departure / Arrival</span>
                    <span className="detail-value">
                      {selectedHistoryModalTicket.departureTime || '-'} / {selectedHistoryModalTicket.arrivalTime || '-'}
                    </span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">🚆 Carrier / Flight</span>
                    <span className="detail-value">{selectedHistoryModalTicket.carrierNumber || 'N/A'}</span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">💺 Seat / Coach</span>
                    <span className="detail-value">
                      {selectedHistoryModalTicket.coach ? `Coach ${selectedHistoryModalTicket.coach}, ` : ''}
                      {selectedHistoryModalTicket.seat ? `Seat ${selectedHistoryModalTicket.seat}` : 'General'}
                    </span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">⭐ Class</span>
                    <span className="detail-value">{selectedHistoryModalTicket.translatedTicketClass || selectedHistoryModalTicket.ticketClass || 'Standard'}</span>
                  </div>
                  <div className="detail-item full-width-item">
                    <span className="detail-label">🔢 Booking Ref / PNR</span>
                    <span className="detail-value pnr-highlight">{selectedHistoryModalTicket.pnrNumber || 'N/A'}</span>
                  </div>
                </div>

                <div className="summary-box">
                  <div className="summary-box-header">
                    <span className="summary-title">📝 Translated Summary</span>
                    <button
                      type="button"
                      className="btn-copy"
                      onClick={() => handleCopySummary(selectedHistoryModalTicket.summary || selectedHistoryModalTicket.translatedFullText)}
                    >
                      📋 Copy Summary
                    </button>
                  </div>
                  <p className="summary-text">{selectedHistoryModalTicket.summary || selectedHistoryModalTicket.translatedFullText}</p>
                </div>
              </div>
            </div>
          </div>
        )}
      </main>

      {/* Footer */}
      <footer className="app-footer">
        <p>Multilingual Ticket Translator &bull; Database &amp; Travel Assistant Ready</p>
      </footer>
    </div>
  );
}

export default App;
