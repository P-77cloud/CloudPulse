import React, { useState } from 'react';
import { Sliders, Plus, Trash2, Bell, AlertTriangle } from 'lucide-react';

export default function AlertRuleManager({ rules, services, onCreateRule, onDeleteRule }) {
  const [showForm, setShowForm] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    serviceId: '',
    condition: 'RESPONSE_TIME_EXCEEDS',
    threshold: 1000,
    enabled: true
  });
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.name.trim()) return;

    try {
      setSubmitting(true);
      await onCreateRule({
        ...formData,
        serviceId: formData.serviceId || null,
        threshold: Number(formData.threshold)
      });
      setShowForm(false);
      setFormData({
        name: '',
        serviceId: '',
        condition: 'RESPONSE_TIME_EXCEEDS',
        threshold: 1000,
        enabled: true
      });
    } catch (err) {
      console.error('Failed to create rule:', err);
    } finally {
      setSubmitting(false);
    }
  };

  const formatCondition = (rule) => {
    switch (rule.condition) {
      case 'RESPONSE_TIME_EXCEEDS':
        return `Latency > ${rule.threshold}ms`;
      case 'CONSECUTIVE_FAILURES':
        return `Failures ≥ ${rule.threshold} consecutive checks`;
      case 'STATUS_DOWN':
        return `Service transitions to DOWN`;
      default:
        return rule.condition;
    }
  };

  return (
    <div style={{ marginBottom: '32px' }}>
      <div className="section-header">
        <div>
          <h2 className="section-title">Automated Alert Rules & Thresholds</h2>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
            Configure alarms for high latency, consecutive health failures, and outage states.
          </p>
        </div>
        <button
          className="btn btn-primary"
          onClick={() => setShowForm(!showForm)}
          id="btn-toggle-add-rule"
        >
          <Plus size={16} />
          <span>{showForm ? 'Cancel' : 'New Alert Rule'}</span>
        </button>
      </div>

      {showForm && (
        <div className="table-wrapper" style={{ padding: '20px', marginBottom: '20px' }}>
          <h3 style={{ fontSize: '1rem', marginBottom: '14px' }}>Create Alert Rule</h3>
          <form onSubmit={handleSubmit}>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px' }}>
              <div className="form-group">
                <label className="form-label">Rule Name</label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="e.g. High P95 Latency Alert"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Target Service</label>
                <select
                  className="form-select"
                  value={formData.serviceId}
                  onChange={(e) => setFormData({ ...formData, serviceId: e.target.value })}
                >
                  <option value="">Apply to ALL Services</option>
                  {services.map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.name}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px' }}>
              <div className="form-group">
                <label className="form-label">Trigger Condition</label>
                <select
                  className="form-select"
                  value={formData.condition}
                  onChange={(e) => setFormData({ ...formData, condition: e.target.value })}
                >
                  <option value="RESPONSE_TIME_EXCEEDS">Response Time Exceeds (ms)</option>
                  <option value="CONSECUTIVE_FAILURES">Consecutive Failures Count</option>
                  <option value="STATUS_DOWN">Service Status becomes DOWN</option>
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Threshold Value</label>
                <input
                  type="number"
                  className="form-input"
                  value={formData.threshold}
                  onChange={(e) => setFormData({ ...formData, threshold: e.target.value })}
                  min="1"
                  required
                />
              </div>
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
              <button type="button" className="btn btn-secondary" onClick={() => setShowForm(false)}>
                Cancel
              </button>
              <button type="submit" className="btn btn-primary" disabled={submitting}>
                {submitting ? 'Creating...' : 'Save Rule'}
              </button>
            </div>
          </form>
        </div>
      )}

      <div className="table-wrapper">
        <table className="data-table">
          <thead>
            <tr>
              <th>Rule Name</th>
              <th>Target Service</th>
              <th>Condition & Threshold</th>
              <th>Status</th>
              <th style={{ textAlign: 'right' }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {rules.map((rule) => {
              const target = services.find((s) => s.id === rule.serviceId);
              return (
                <tr key={rule.id}>
                  <td>
                    <strong>{rule.name}</strong>
                  </td>
                  <td style={{ color: 'var(--text-secondary)' }}>
                    {target ? target.name : 'All Monitored Services'}
                  </td>
                  <td>
                    <span style={{ color: 'var(--accent-cyan)', fontWeight: 600 }}>
                      {formatCondition(rule)}
                    </span>
                  </td>
                  <td>
                    <span className="status-badge status-up" style={{ fontSize: '0.7rem' }}>
                      ACTIVE
                    </span>
                  </td>
                  <td style={{ textAlign: 'right' }}>
                    <button
                      className="btn btn-secondary btn-icon"
                      onClick={() => onDeleteRule(rule.id)}
                      title="Delete alert rule"
                    >
                      <Trash2 size={14} style={{ color: 'var(--status-down)' }} />
                    </button>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
