import React, { useState } from 'react';
import {
  AlertCircle,
  AlertTriangle,
  CheckCircle,
  Clock,
  UserCheck,
  ShieldAlert,
  FileText
} from 'lucide-react';
import { formatTimeAgo } from '../utils/formatters';

export default function IncidentManager({ incidents, onAcknowledge, onResolve }) {
  const [activeAction, setActiveAction] = useState(null); // { type: 'ack' | 'res', incident: object }
  const [operatorName, setOperatorName] = useState('SRE-Operator');
  const [rootCauseNotes, setRootCauseNotes] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const openIncidents = incidents.filter(inc => inc.status !== 'RESOLVED');
  const resolvedIncidents = incidents.filter(inc => inc.status === 'RESOLVED');

  const handleActionSubmit = async (e) => {
    e.preventDefault();
    if (!activeAction) return;

    try {
      setSubmitting(true);
      if (activeAction.type === 'ack') {
        await onAcknowledge(activeAction.incident.id, operatorName);
      } else {
        await onResolve(activeAction.incident.id, operatorName, rootCauseNotes);
      }
      setActiveAction(null);
      setRootCauseNotes('');
    } catch (err) {
      console.error('Incident action failed:', err);
    } finally {
      setSubmitting(false);
    }
  };

  const getSeverityStyle = (sev) => {
    switch (sev) {
      case 'CRITICAL':
        return { bg: 'var(--status-down-bg)', text: 'var(--status-down)', border: 'var(--status-down-border)' };
      case 'WARNING':
        return { bg: 'var(--status-degraded-bg)', text: 'var(--status-degraded)', border: 'var(--status-degraded-border)' };
      default:
        return { bg: 'rgba(6, 182, 212, 0.1)', text: 'var(--accent-cyan)', border: 'var(--border-cyan)' };
    }
  };

  return (
    <div style={{ marginBottom: '32px' }}>
      <div className="section-header">
        <div>
          <h2 className="section-title">Incident Management & Post-Mortems</h2>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
            Track service degradation, automatic failure detection, MTTR, and recovery logs.
          </p>
        </div>
      </div>

      {/* Active Incidents */}
      <div style={{ marginBottom: '28px' }}>
        <h3 style={{ fontSize: '1rem', fontWeight: 600, marginBottom: '14px', color: 'var(--text-primary)', display: 'flex', alignItems: 'center', gap: '8px' }}>
          <AlertCircle size={18} style={{ color: 'var(--status-down)' }} />
          <span>Active Outages & Incidents ({openIncidents.length})</span>
        </h3>

        {openIncidents.length === 0 ? (
          <div className="table-wrapper" style={{ padding: '24px', textAlign: 'center', color: 'var(--text-muted)' }}>
            <CheckCircle size={28} style={{ color: 'var(--status-up)', marginBottom: '8px' }} />
            <p>All monitored systems are operational. No open incidents!</p>
          </div>
        ) : (
          <div style={{ display: 'grid', gap: '14px' }}>
            {openIncidents.map((inc) => {
              const sev = getSeverityStyle(inc.severity);
              const isAck = inc.status === 'ACKNOWLEDGED';
              const isDemo = inc.title?.includes('[DEMO]');

              return (
                <div
                  key={inc.id}
                  style={{
                    backgroundColor: 'var(--bg-card)',
                    border: `1px solid ${sev.border}`,
                    borderRadius: 'var(--radius-md)',
                    padding: '18px 20px',
                    boxShadow: 'var(--shadow-card)',
                    backdropFilter: 'blur(12px)'
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '10px' }}>
                    <div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '6px' }}>
                        <span
                          style={{
                            fontSize: '0.7rem',
                            fontWeight: 700,
                            padding: '2px 8px',
                            borderRadius: 'var(--radius-full)',
                            backgroundColor: sev.bg,
                            color: sev.text,
                            border: `1px solid ${sev.border}`
                          }}
                        >
                          {inc.severity}
                        </span>
                        <span
                          style={{
                            fontSize: '0.7rem',
                            fontWeight: 600,
                            padding: '2px 8px',
                            borderRadius: 'var(--radius-full)',
                            backgroundColor: isAck ? 'rgba(245, 158, 11, 0.15)' : 'rgba(239, 68, 68, 0.15)',
                            color: isAck ? 'var(--status-degraded)' : 'var(--status-down)'
                          }}
                        >
                          {inc.status}
                        </span>
                        {isDemo && <span className="demo-badge-tag">DEMO</span>}
                        <strong style={{ fontSize: '1.05rem', color: 'var(--text-primary)' }}>{inc.title}</strong>
                      </div>
                      <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginBottom: '8px' }}>
                        {inc.description}
                      </p>
                      <div style={{ display: 'flex', gap: '16px', fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                        <span style={{ display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
                          <Clock size={12} /> Started {formatTimeAgo(inc.startedAt)}
                        </span>
                        {inc.consecutiveFailures > 0 && (
                          <span>Failed checks: {inc.consecutiveFailures}</span>
                        )}
                        {inc.acknowledgedBy && (
                          <span style={{ color: 'var(--status-degraded)', display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
                            <UserCheck size={12} /> Acked by {inc.acknowledgedBy}
                          </span>
                        )}
                      </div>
                    </div>

                    <div style={{ display: 'flex', gap: '8px' }}>
                      {!isAck && (
                        <button
                          className="btn btn-secondary"
                          style={{ fontSize: '0.8rem', padding: '6px 12px' }}
                          onClick={() => setActiveAction({ type: 'ack', incident: inc })}
                          id={`btn-ack-${inc.id}`}
                        >
                          <UserCheck size={14} />
                          <span>Acknowledge</span>
                        </button>
                      )}
                      <button
                        className="btn btn-primary"
                        style={{ fontSize: '0.8rem', padding: '6px 12px' }}
                        onClick={() => setActiveAction({ type: 'res', incident: inc })}
                        id={`btn-resolve-${inc.id}`}
                      >
                        <CheckCircle size={14} />
                        <span>Resolve</span>
                      </button>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Resolved Incident History */}
      {resolvedIncidents.length > 0 && (
        <div>
          <h3 style={{ fontSize: '1rem', fontWeight: 600, marginBottom: '12px', color: 'var(--text-secondary)', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <CheckCircle size={16} style={{ color: 'var(--status-up)' }} />
            <span>Resolved Incident History ({resolvedIncidents.length})</span>
          </h3>
          <div className="table-wrapper">
            <table className="data-table" style={{ fontSize: '0.85rem' }}>
              <thead>
                <tr>
                  <th>Title & Service</th>
                  <th>Severity</th>
                  <th>Duration / MTTR</th>
                  <th>Resolution / Root Cause</th>
                  <th>Resolved By</th>
                </tr>
              </thead>
              <tbody>
                {resolvedIncidents.map((inc) => (
                  <tr key={inc.id}>
                    <td>
                      <strong>{inc.title}</strong>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                        Started {formatTimeAgo(inc.startedAt)}
                      </div>
                    </td>
                    <td>
                      <span className="status-badge status-up" style={{ fontSize: '0.7rem' }}>
                        RESOLVED
                      </span>
                    </td>
                    <td>
                      {inc.durationSeconds ? `${Math.floor(inc.durationSeconds / 60)}m ${inc.durationSeconds % 60}s` : '--'}
                    </td>
                    <td style={{ color: 'var(--text-secondary)' }}>
                      {inc.rootCauseNotes || 'Auto-recovered on subsequent check'}
                    </td>
                    <td style={{ color: 'var(--accent-cyan)' }}>
                      {inc.resolvedBy || 'SYSTEM'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Action Dialog Modal */}
      {activeAction && (
        <div className="modal-overlay" onClick={() => setActiveAction(null)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3 className="modal-title">
                {activeAction.type === 'ack' ? 'Acknowledge Incident' : 'Resolve Incident'}
              </h3>
            </div>
            <form onSubmit={handleActionSubmit}>
              <div className="form-group">
                <label className="form-label">Operator / Engineer Name</label>
                <input
                  type="text"
                  className="form-input"
                  value={operatorName}
                  onChange={(e) => setOperatorName(e.target.value)}
                  required
                />
              </div>

              {activeAction.type === 'res' && (
                <div className="form-group">
                  <label className="form-label">Root Cause & Resolution Notes</label>
                  <textarea
                    className="form-textarea"
                    rows="3"
                    placeholder="e.g. Cleared network congestion, rolled back deployment v2.1.0"
                    value={rootCauseNotes}
                    onChange={(e) => setRootCauseNotes(e.target.value)}
                    required
                  />
                </div>
              )}

              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => setActiveAction(null)}>
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary" disabled={submitting}>
                  {submitting ? 'Submitting...' : activeAction.type === 'ack' ? 'Confirm Acknowledge' : 'Confirm Resolve'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
