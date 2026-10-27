import React from 'react';
import { Activity, Bell, Plus, RefreshCw, Sparkles, RotateCcw } from 'lucide-react';

export default function Navbar({
  unreadCount,
  onOpenNotifications,
  onOpenAddService,
  onRefresh,
  onSeedDemo,
  onResetDemo,
  hasDemoService,
  isRefreshing,
  isSeeding,
  isResetting
}) {
  return (
    <header className="navbar">
      <div className="navbar-inner">
        <div className="brand-wrapper">
          <div className="brand-icon">
            <Activity size={24} />
          </div>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <span className="brand-title">CloudPulse</span>
              <span className="brand-tag">PRO MONITOR</span>
            </div>
            <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
              Cloud Infrastructure Health & Incident Platform
            </p>
          </div>
        </div>

        <div className="navbar-actions">
          <button
            className="btn btn-secondary"
            onClick={onRefresh}
            disabled={isRefreshing}
            title="Refresh dashboard metrics"
            id="btn-refresh"
          >
            <RefreshCw size={15} className={isRefreshing ? 'spin-anim' : ''} />
            <span>Refresh</span>
          </button>

          {hasDemoService && (
            <button
              className="btn btn-secondary"
              onClick={onResetDemo}
              disabled={isResetting}
              title="Purge all records marked [DEMO] (keeps real endpoints untouched)"
              id="btn-reset-demo"
              style={{ color: '#f87171', borderColor: 'rgba(239, 68, 68, 0.3)' }}
            >
              <RotateCcw size={15} className={isResetting ? 'spin-anim' : ''} />
              <span>{isResetting ? 'Resetting...' : 'Reset Demo'}</span>
            </button>
          )}

          <button
            className="btn btn-demo"
            onClick={onSeedDemo}
            disabled={isSeeding}
            title="Seed single canonical sample outage and alerts (idempotent)"
            id="btn-seed-demo"
          >
            <Sparkles size={15} />
            <span>{isSeeding ? 'Seeding...' : hasDemoService ? 'Re-seed Demo' : 'Seed Demo Outage'}</span>
          </button>

          <button
            className="btn btn-icon"
            onClick={onOpenNotifications}
            title="Notifications"
            id="btn-notifications"
          >
            <Bell size={18} />
            {unreadCount > 0 && (
              <span className="badge-unread">{unreadCount > 9 ? '9+' : unreadCount}</span>
            )}
          </button>

          <button
            className="btn btn-primary"
            onClick={onOpenAddService}
            id="btn-add-service"
          >
            <Plus size={16} />
            <span>Add Service</span>
          </button>
        </div>
      </div>
    </header>
  );
}
