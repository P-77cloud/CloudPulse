import React from 'react';
import {
  Play,
  Pause,
  Edit2,
  Trash2,
  BarChart2,
  Zap,
  ExternalLink,
  ShieldCheck,
  AlertTriangle
} from 'lucide-react';
import {
  formatLatency,
  formatTimeAgo,
  formatUptime,
  getStatusBadgeClass,
  getLatencyColor
} from '../utils/formatters';

export default function ServiceList({
  services,
  onCheckNow,
  onViewMetrics,
  onEdit,
  onToggle,
  onDelete,
  checkingServiceId
}) {
  if (!services || services.length === 0) {
    return (
      <div className="table-wrapper" style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
        <p style={{ fontSize: '1.1rem', marginBottom: '8px', color: 'var(--text-secondary)' }}>
          No services monitored yet
        </p>
        <p style={{ fontSize: '0.85rem' }}>
          Click the "Add Service" button to register your first API or web service for health checks.
        </p>
      </div>
    );
  }

  return (
    <div className="table-wrapper">
      <table className="data-table">
        <thead>
          <tr>
            <th>Service Name & URL</th>
            <th>Status</th>
            <th>Latency</th>
            <th>24h Uptime</th>
            <th>Last Checked</th>
            <th style={{ textAlign: 'right' }}>Actions</th>
          </tr>
        </thead>
        <tbody>
          {services.map((service) => {
            const isChecking = checkingServiceId === service.id;
            const isDemo = service.name.includes('[DEMO]');

            return (
              <tr key={service.id} style={{ opacity: service.enabled ? 1 : 0.6 }}>
                <td>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <strong style={{ fontSize: '0.95rem' }}>{service.name}</strong>
                    {isDemo && (
                      <span className="demo-badge-tag" style={{ fontSize: '0.65rem', padding: '1px 5px' }}>
                        DEMO
                      </span>
                    )}
                    {service.authorizedEndpoint && (
                      <span title="Explicitly authorized & SSRF-protected" style={{ color: 'var(--status-up)', display: 'inline-flex' }}>
                        <ShieldCheck size={14} />
                      </span>
                    )}
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginTop: '3px' }}>
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                      {service.httpMethod || 'GET'}
                    </span>
                    <a
                      href={service.url}
                      target="_blank"
                      rel="noopener noreferrer"
                      style={{ fontSize: '0.8rem', color: 'var(--accent-cyan)', textDecoration: 'none', display: 'inline-flex', alignItems: 'center', gap: '3px' }}
                    >
                      <span>{service.url}</span>
                      <ExternalLink size={11} />
                    </a>
                  </div>
                </td>

                <td>
                  <span className={getStatusBadgeClass(service.status)}>
                    <span
                      className={`pulse-dot ${
                        service.status === 'UP' ? 'pulse-up' : service.status === 'DOWN' ? 'pulse-down' : ''
                      }`}
                    />
                    {service.status || 'UNKNOWN'}
                  </span>
                  {service.consecutiveFailures > 0 && (
                    <div style={{ fontSize: '0.7rem', color: 'var(--status-down)', marginTop: '3px', display: 'flex', alignItems: 'center', gap: '3px' }}>
                      <AlertTriangle size={11} />
                      <span>{service.consecutiveFailures} consecutive failure(s)</span>
                    </div>
                  )}
                </td>

                <td>
                  <span
                    style={{
                      fontWeight: 600,
                      color: getLatencyColor(service.lastResponseTimeMs),
                      fontSize: '0.9rem'
                    }}
                  >
                    {formatLatency(service.lastResponseTimeMs)}
                  </span>
                </td>

                <td>
                  <div>
                    <span style={{ fontWeight: 600, fontSize: '0.9rem' }}>
                      {formatUptime(service.uptimePercentage)}
                    </span>
                    <div className="sla-strip" style={{ marginTop: '4px' }}>
                      {[...Array(12)].map((_, i) => {
                        const isFail = service.status === 'DOWN' && i > 9;
                        return (
                          <div
                            key={i}
                            className={`sla-segment ${isFail ? 'fail' : ''}`}
                            title={`Window ${i + 1}`}
                          />
                        );
                      })}
                    </div>
                  </div>
                </td>

                <td>
                  <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                    {formatTimeAgo(service.lastCheckTime)}
                  </span>
                  <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>
                    Every {service.checkIntervalSeconds}s
                  </div>
                </td>

                <td>
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end', gap: '6px' }}>
                    <button
                      className="btn btn-secondary btn-icon"
                      onClick={() => onCheckNow(service.id)}
                      disabled={isChecking}
                      title="Run on-demand health check right now"
                      id={`btn-check-${service.id}`}
                    >
                      <Zap size={15} style={{ color: isChecking ? 'var(--status-degraded)' : 'var(--accent-cyan)' }} />
                    </button>

                    <button
                      className="btn btn-secondary btn-icon"
                      onClick={() => onViewMetrics(service)}
                      title="View historical latency & checks"
                      id={`btn-metrics-${service.id}`}
                    >
                      <BarChart2 size={15} />
                    </button>

                    <button
                      className="btn btn-secondary btn-icon"
                      onClick={() => onToggle(service.id)}
                      title={service.enabled ? 'Pause monitoring' : 'Resume monitoring'}
                      id={`btn-toggle-${service.id}`}
                    >
                      {service.enabled ? <Pause size={15} /> : <Play size={15} style={{ color: 'var(--status-up)' }} />}
                    </button>

                    <button
                      className="btn btn-secondary btn-icon"
                      onClick={() => onEdit(service)}
                      title="Edit configuration"
                      id={`btn-edit-${service.id}`}
                    >
                      <Edit2 size={15} />
                    </button>

                    <button
                      className="btn btn-secondary btn-icon"
                      onClick={() => onDelete(service.id, service.name)}
                      title="Delete service"
                      id={`btn-delete-${service.id}`}
                    >
                      <Trash2 size={15} style={{ color: 'var(--status-down)' }} />
                    </button>
                  </div>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
