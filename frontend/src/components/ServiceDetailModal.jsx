import React, { useState, useEffect } from 'react';
import { X, RefreshCw, Zap, CheckCircle2, XCircle, Clock } from 'lucide-react';
import { api } from '../services/api';
import { formatLatency, formatTimeAgo, getStatusBadgeClass } from '../utils/formatters';
import { ResponsiveContainer, AreaChart, Area, XAxis, YAxis, Tooltip, CartesianGrid } from 'recharts';

export default function ServiceDetailModal({ isOpen, onClose, service, onCheckNow }) {
  const [checks, setChecks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [checking, setChecking] = useState(false);

  useEffect(() => {
    if (isOpen && service) {
      loadHistory();
    }
  }, [isOpen, service]);

  const loadHistory = async () => {
    try {
      setLoading(true);
      const data = await api.getRecentChecks(service.id);
      setChecks(data || []);
    } catch (err) {
      console.error('Failed to load check history:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleRunCheck = async () => {
    try {
      setChecking(true);
      await onCheckNow(service.id);
      await loadHistory();
    } catch (err) {
      console.error('Check failed:', err);
    } finally {
      setChecking(false);
    }
  };

  if (!isOpen || !service) return null;

  const chartData = [...checks].reverse().map((c, i) => ({
    time: c.timestamp ? new Date(c.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' }) : `#${i + 1}`,
    latency: c.responseTimeMs || 0
  }));

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" style={{ maxWidth: '780px' }} onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <h2 className="modal-title">{service.name}</h2>
              <span className={getStatusBadgeClass(service.status)}>
                {service.status || 'UNKNOWN'}
              </span>
            </div>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '4px' }}>
              {service.url} &bull; Check every {service.checkIntervalSeconds}s &bull; Uptime: {service.uptimePercentage}%
            </p>
          </div>
          <div style={{ display: 'flex', gap: '8px' }}>
            <button
              className="btn btn-secondary btn-icon"
              onClick={handleRunCheck}
              disabled={checking}
              title="Run Check Now"
              id="btn-detail-run-check"
            >
              <Zap size={16} style={{ color: 'var(--accent-cyan)' }} />
            </button>
            <button
              className="btn btn-secondary btn-icon"
              onClick={loadHistory}
              disabled={loading}
              title="Refresh History"
            >
              <RefreshCw size={16} className={loading ? 'spin-anim' : ''} />
            </button>
            <button className="btn btn-secondary btn-icon" onClick={onClose}>
              <X size={18} />
            </button>
          </div>
        </div>

        {/* Latency History Chart */}
        <div style={{ marginBottom: '24px', backgroundColor: 'var(--bg-input)', padding: '16px', borderRadius: 'var(--radius-md)' }}>
          <div style={{ fontSize: '0.85rem', fontWeight: 600, marginBottom: '12px', color: 'var(--text-secondary)' }}>
            Recent Latency Progression (ms)
          </div>
          {chartData.length > 0 ? (
            <div style={{ width: '100%', height: 180 }}>
              <ResponsiveContainer>
                <AreaChart data={chartData} margin={{ top: 5, right: 10, left: -20, bottom: 0 }}>
                  <defs>
                    <linearGradient id="detailGradient" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#10b981" stopOpacity={0.4} />
                      <stop offset="95%" stopColor="#10b981" stopOpacity={0.0} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" />
                  <XAxis dataKey="time" stroke="#64748b" fontSize={10} tickLine={false} />
                  <YAxis stroke="#64748b" fontSize={10} tickLine={false} unit="ms" />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: '#0f172a',
                      borderColor: 'rgba(255,255,255,0.15)',
                      borderRadius: '6px',
                      color: '#fff',
                      fontSize: '0.75rem'
                    }}
                  />
                  <Area type="monotone" dataKey="latency" stroke="#10b981" strokeWidth={2} fill="url(#detailGradient)" />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <div style={{ height: '140px', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
              No check history recorded yet.
            </div>
          )}
        </div>

        {/* Recent Checks Table */}
        <div>
          <div style={{ fontSize: '0.85rem', fontWeight: 600, marginBottom: '10px', color: 'var(--text-secondary)' }}>
            Recent Health Checks Log (Last {checks.length})
          </div>
          <div style={{ maxHeight: '240px', overflowY: 'auto', border: '1px solid var(--border-subtle)', borderRadius: 'var(--radius-sm)' }}>
            <table className="data-table" style={{ fontSize: '0.8rem' }}>
              <thead>
                <tr>
                  <th style={{ padding: '8px 12px' }}>Result</th>
                  <th style={{ padding: '8px 12px' }}>HTTP Code</th>
                  <th style={{ padding: '8px 12px' }}>Response Time</th>
                  <th style={{ padding: '8px 12px' }}>Time</th>
                  <th style={{ padding: '8px 12px' }}>Details / Error</th>
                </tr>
              </thead>
              <tbody>
                {checks.map((chk) => (
                  <tr key={chk.id}>
                    <td style={{ padding: '8px 12px' }}>
                      {chk.success ? (
                        <span style={{ color: 'var(--status-up)', display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
                          <CheckCircle2 size={14} /> UP
                        </span>
                      ) : (
                        <span style={{ color: 'var(--status-down)', display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
                          <XCircle size={14} /> DOWN
                        </span>
                      )}
                    </td>
                    <td style={{ padding: '8px 12px' }}>
                      <code style={{ color: chk.success ? 'var(--status-up)' : 'var(--status-down)' }}>
                        {chk.statusCode > 0 ? chk.statusCode : 'ERR'}
                      </code>
                    </td>
                    <td style={{ padding: '8px 12px', fontWeight: 600 }}>
                      {formatLatency(chk.responseTimeMs)}
                    </td>
                    <td style={{ padding: '8px 12px', color: 'var(--text-muted)' }}>
                      {formatTimeAgo(chk.timestamp)}
                    </td>
                    <td style={{ padding: '8px 12px', color: chk.errorMessage ? 'var(--status-down)' : 'var(--text-muted)' }}>
                      {chk.errorMessage || 'HTTP check passed'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        <div className="modal-actions" style={{ marginTop: '20px' }}>
          <button className="btn btn-secondary" onClick={onClose}>
            Close
          </button>
        </div>
      </div>
    </div>
  );
}
