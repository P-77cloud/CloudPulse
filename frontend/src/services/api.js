// CloudPulse API Client

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

async function request(endpoint, options = {}) {
  const url = `${BASE_URL}${endpoint}`;
  const config = {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  };

  try {
    const response = await fetch(url, config);
    const result = await response.json().catch(() => null);

    if (!response.ok) {
      const errorMsg = result?.message || (result?.data ? JSON.stringify(result.data) : `HTTP ${response.status} Error`);
      throw new Error(errorMsg);
    }
    return result?.data;
  } catch (error) {
    console.error(`API Error [${endpoint}]:`, error);
    throw error;
  }
}

export const api = {
  // Dashboard
  getDashboardSummary: () => request('/dashboard/summary'),

  // Services
  getServices: () => request('/services'),
  getService: (id) => request(`/services/${id}`),
  createService: (data) => request('/services', {
    method: 'POST',
    body: JSON.stringify(data),
  }),
  updateService: (id, data) => request(`/services/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  }),
  deleteService: (id) => request(`/services/${id}`, {
    method: 'DELETE',
  }),
  toggleService: (id) => request(`/services/${id}/toggle`, {
    method: 'PATCH',
  }),
  triggerCheck: (id) => request(`/services/${id}/check`, {
    method: 'POST',
  }),

  // Health Checks
  getRecentChecks: (serviceId) => request(`/health-checks/service/${serviceId}/recent`),
  getServiceChecks: (serviceId, page = 0, size = 20) =>
    request(`/health-checks/service/${serviceId}?page=${page}&size=${size}`),

  // Incidents
  getIncidents: (page = 0, size = 20) =>
    request(`/incidents?page=${page}&size=${size}`),
  acknowledgeIncident: (id, user) => request(`/incidents/${id}/acknowledge`, {
    method: 'POST',
    body: JSON.stringify({ user }),
  }),
  resolveIncident: (id, user, rootCauseNotes) => request(`/incidents/${id}/resolve`, {
    method: 'POST',
    body: JSON.stringify({ user, rootCauseNotes }),
  }),

  // Alert Rules
  getAlertRules: () => request('/alert-rules'),
  createAlertRule: (data) => request('/alert-rules', {
    method: 'POST',
    body: JSON.stringify(data),
  }),
  deleteAlertRule: (id) => request(`/alert-rules/${id}`, {
    method: 'DELETE',
  }),

  // Notifications
  getRecentNotifications: () => request('/notifications/recent'),
  getUnreadCount: () => request('/notifications/unread-count'),
  markNotificationRead: (id) => request(`/notifications/${id}/read`, {
    method: 'PATCH',
  }),
  markAllNotificationsRead: () => request('/notifications/read-all', {
    method: 'POST',
  }),

  // Demo Seed
  seedDemoData: () => request('/demo/seed', {
    method: 'POST',
  }),
};
