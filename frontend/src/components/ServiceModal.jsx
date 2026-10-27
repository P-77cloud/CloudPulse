import React, { useState, useEffect } from 'react';
import { X, ShieldAlert, Check } from 'lucide-react';

export default function ServiceModal({ isOpen, onClose, onSave, serviceToEdit }) {
  const [formData, setFormData] = useState({
    name: '',
    url: '',
    httpMethod: 'GET',
    expectedStatusCode: 200,
    checkIntervalSeconds: 30,
    timeoutMs: 5000,
    failureThreshold: 3,
    authorizedEndpoint: false,
    enabled: true
  });
  const [error, setError] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (serviceToEdit) {
      setFormData({
        name: serviceToEdit.name || '',
        url: serviceToEdit.url || '',
        httpMethod: serviceToEdit.httpMethod || 'GET',
        expectedStatusCode: serviceToEdit.expectedStatusCode || 200,
        checkIntervalSeconds: serviceToEdit.checkIntervalSeconds || 30,
        timeoutMs: serviceToEdit.timeoutMs || 5000,
        failureThreshold: serviceToEdit.failureThreshold || 3,
        authorizedEndpoint: serviceToEdit.authorizedEndpoint || false,
        enabled: serviceToEdit.enabled ?? true
      });
    } else {
      setFormData({
        name: '',
        url: 'https://',
        httpMethod: 'GET',
        expectedStatusCode: 200,
        checkIntervalSeconds: 30,
        timeoutMs: 5000,
        failureThreshold: 3,
        authorizedEndpoint: false,
        enabled: true
      });
    }
    setError(null);
  }, [serviceToEdit, isOpen]);

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    if (!formData.name.trim()) {
      setError('Service name is required.');
      return;
    }

    if (!formData.url.startsWith('http://') && !formData.url.startsWith('https://')) {
      setError('URL must start with http:// or https://');
      return;
    }

    if (!formData.authorizedEndpoint) {
      setError('You must check the authorization box to confirm you are authorized to monitor this target URL.');
      return;
    }

    try {
      setIsSubmitting(true);
      await onSave(formData);
      onClose();
    } catch (err) {
      setError(err.message || 'Failed to save service.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2 className="modal-title">
            {serviceToEdit ? 'Edit Monitored Service' : 'Add New Monitored Service'}
          </h2>
          <button className="btn btn-secondary btn-icon" onClick={onClose} id="btn-close-service-modal">
            <X size={18} />
          </button>
        </div>

        {error && (
          <div style={{
            backgroundColor: 'var(--status-down-bg)',
            border: '1px solid var(--status-down-border)',
            color: '#fca5a5',
            padding: '12px',
            borderRadius: 'var(--radius-sm)',
            marginBottom: '16px',
            fontSize: '0.85rem',
            display: 'flex',
            alignItems: 'center',
            gap: '8px'
          }}>
            <ShieldAlert size={16} />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Service Name</label>
            <input
              type="text"
              className="form-input"
              placeholder="e.g. Production Auth API"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              required
              id="input-service-name"
            />
          </div>

          <div className="form-group">
            <label className="form-label">Target URL</label>
            <input
              type="url"
              className="form-input"
              placeholder="https://api.example.com/health"
              value={formData.url}
              onChange={(e) => setFormData({ ...formData, url: e.target.value })}
              required
              id="input-service-url"
            />
            <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)', display: 'block', marginTop: '4px' }}>
              CloudPulse validates all URLs against SSRF. Private IPs (10.*, 192.168.*, 127.*) and cloud metadata (169.254.169.254) are blocked.
            </span>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px' }}>
            <div className="form-group">
              <label className="form-label">HTTP Method</label>
              <select
                className="form-select"
                value={formData.httpMethod}
                onChange={(e) => setFormData({ ...formData, httpMethod: e.target.value })}
                id="select-http-method"
              >
                <option value="GET">GET</option>
                <option value="HEAD">HEAD</option>
                <option value="POST">POST</option>
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Expected Status Code</label>
              <input
                type="number"
                className="form-input"
                value={formData.expectedStatusCode}
                onChange={(e) => setFormData({ ...formData, expectedStatusCode: parseInt(e.target.value) || 200 })}
                min="100"
                max="599"
                id="input-expected-status"
              />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px' }}>
            <div className="form-group">
              <label className="form-label">Check Interval (Seconds)</label>
              <input
                type="number"
                className="form-input"
                value={formData.checkIntervalSeconds}
                onChange={(e) => setFormData({ ...formData, checkIntervalSeconds: parseInt(e.target.value) || 30 })}
                min="5"
                max="3600"
                id="input-check-interval"
              />
            </div>

            <div className="form-group">
              <label className="form-label">Timeout (ms)</label>
              <input
                type="number"
                className="form-input"
                value={formData.timeoutMs}
                onChange={(e) => setFormData({ ...formData, timeoutMs: parseInt(e.target.value) || 5000 })}
                min="500"
                max="30000"
                id="input-timeout"
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Consecutive Failures for Incident</label>
            <input
              type="number"
              className="form-input"
              value={formData.failureThreshold}
              onChange={(e) => setFormData({ ...formData, failureThreshold: parseInt(e.target.value) || 3 })}
              min="1"
              max="10"
              id="input-failure-threshold"
            />
            <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)', display: 'block', marginTop: '4px' }}>
              An automatic incident will open when this service fails this number of consecutive health checks.
            </span>
          </div>

          <div className="form-group" style={{ backgroundColor: 'rgba(6, 182, 212, 0.05)', padding: '12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-cyan)' }}>
            <label className="form-checkbox-label">
              <input
                type="checkbox"
                checked={formData.authorizedEndpoint}
                onChange={(e) => setFormData({ ...formData, authorizedEndpoint: e.target.checked })}
                id="checkbox-authorized"
              />
              <span>
                <strong>Explicit Endpoint Authorization:</strong> I verify that I own or am authorized to monitor this endpoint, and agree that CloudPulse will perform automated health checks.
              </span>
            </label>
          </div>

          <div className="modal-actions">
            <button type="button" className="btn btn-secondary" onClick={onClose}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={isSubmitting} id="btn-save-service">
              <Check size={16} />
              <span>{isSubmitting ? 'Saving...' : serviceToEdit ? 'Update Service' : 'Create Service'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
