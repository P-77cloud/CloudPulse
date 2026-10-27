import React from 'react';
import { X, CheckCheck, Bell, AlertTriangle, AlertCircle, Info } from 'lucide-react';
import { formatTimeAgo } from '../utils/formatters';

export default function NotificationCenter({
  isOpen,
  onClose,
  notifications,
  onMarkRead,
  onMarkAllRead
}) {
  if (!isOpen) return null;

  const getIcon = (severity) => {
    switch (severity) {
      case 'CRITICAL':
        return <AlertCircle size={16} style={{ color: 'var(--status-down)' }} />;
      case 'WARNING':
        return <AlertTriangle size={16} style={{ color: 'var(--status-degraded)' }} />;
      default:
        return <Info size={16} style={{ color: 'var(--accent-cyan)' }} />;
    }
  };

  return (
    <div className="notification-drawer">
      <div className="drawer-header">
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Bell size={18} style={{ color: 'var(--accent-cyan)' }} />
          <strong style={{ fontSize: '0.95rem' }}>Notification Center</strong>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
          <button
            className="btn btn-secondary"
            style={{ fontSize: '0.75rem', padding: '4px 8px' }}
            onClick={onMarkAllRead}
            title="Mark all notifications as read"
            id="btn-mark-all-read"
          >
            <CheckCheck size={14} />
            <span>Mark all read</span>
          </button>
          <button className="btn btn-secondary btn-icon" onClick={onClose} style={{ padding: '4px' }}>
            <X size={16} />
          </button>
        </div>
      </div>

      <div className="drawer-body">
        {notifications.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '30px 16px', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
            No recent alerts or notifications.
          </div>
        ) : (
          notifications.map((n) => (
            <div
              key={n.id}
              className={`notification-item ${!n.read ? 'unread' : ''}`}
              onClick={() => onMarkRead(n.id)}
              style={{ cursor: 'pointer' }}
            >
              <div style={{ display: 'flex', alignItems: 'flex-start', gap: '8px' }}>
                <div style={{ marginTop: '2px' }}>{getIcon(n.severity)}</div>
                <div style={{ flex: 1 }}>
                  <div className="notification-title">{n.title}</div>
                  <div className="notification-message">{n.message}</div>
                  <div className="notification-time">{formatTimeAgo(n.timestamp)}</div>
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
