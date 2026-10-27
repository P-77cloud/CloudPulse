import React from 'react';

export default function StatCard({ title, value, subtext, icon: Icon, color }) {
  return (
    <div className="stat-card">
      <div className="stat-header">
        <span className="stat-title">{title}</span>
        <div className="stat-icon" style={{ color: color || 'var(--accent-cyan)' }}>
          {Icon && <Icon size={18} />}
        </div>
      </div>
      <div className="stat-value">{value}</div>
      {subtext && <div className="stat-sub">{subtext}</div>}
    </div>
  );
}
