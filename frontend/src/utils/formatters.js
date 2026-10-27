// Formatters and helpers for UI presentation

export function formatTimeAgo(dateString) {
  if (!dateString) return 'Never';
  const date = new Date(dateString);
  const now = new Date();
  const diffInSeconds = Math.floor((now - date) / 1000);

  if (diffInSeconds < 5) return 'Just now';
  if (diffInSeconds < 60) return `${diffInSeconds}s ago`;
  const diffInMinutes = Math.floor(diffInSeconds / 60);
  if (diffInMinutes < 60) return `${diffInMinutes}m ago`;
  const diffInHours = Math.floor(diffInMinutes / 60);
  if (diffInHours < 24) return `${diffInHours}h ago`;
  return `${Math.floor(diffInHours / 24)}d ago`;
}

export function formatLatency(ms) {
  if (ms === null || ms === undefined) return '--';
  return `${ms}ms`;
}

export function formatUptime(uptime) {
  if (uptime === null || uptime === undefined) return '100.0%';
  return `${Number(uptime).toFixed(1)}%`;
}

export function getStatusBadgeClass(status) {
  switch (status?.toUpperCase()) {
    case 'UP':
      return 'status-badge status-up';
    case 'DOWN':
      return 'status-badge status-down';
    case 'DEGRADED':
      return 'status-badge status-degraded';
    default:
      return 'status-badge status-unknown';
  }
}

export function getLatencyColor(ms) {
  if (!ms || ms === 0) return 'var(--text-muted)';
  if (ms < 300) return 'var(--status-up)';
  if (ms < 1000) return 'var(--status-degraded)';
  return 'var(--status-down)';
}
