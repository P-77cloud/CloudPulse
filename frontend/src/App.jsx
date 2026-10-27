import React, { useState, useEffect, useCallback } from 'react';
import Navbar from './components/Navbar';
import StatCard from './components/StatCard';
import ResponseTimeChart from './components/ResponseTimeChart';
import ServiceList from './components/ServiceList';
import ServiceModal from './components/ServiceModal';
import ServiceDetailModal from './components/ServiceDetailModal';
import IncidentManager from './components/IncidentManager';
import AlertRuleManager from './components/AlertRuleManager';
import NotificationCenter from './components/NotificationCenter';
import DemoBanner from './components/DemoBanner';
import { api } from './services/api';
import {
  Activity,
  CheckCircle2,
  AlertTriangle,
  Clock,
  Layers,
  Database,
  Sliders,
  ShieldCheck,
  ServerOff
} from 'lucide-react';

export default function App() {
  const [activeTab, setActiveTab] = useState('overview'); // 'overview' | 'incidents' | 'alerts'
  const [summary, setSummary] = useState(null);
  const [services, setServices] = useState([]);
  const [incidents, setIncidents] = useState([]);
  const [alertRules, setAlertRules] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);

  // Modals & Drawers
  const [isServiceModalOpen, setIsServiceModalOpen] = useState(false);
  const [serviceToEdit, setServiceToEdit] = useState(null);
  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);
  const [selectedService, setSelectedService] = useState(null);
  const [isNotificationOpen, setIsNotificationOpen] = useState(false);

  // Loading States & Error
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [isSeeding, setIsSeeding] = useState(false);
  const [checkingServiceId, setCheckingServiceId] = useState(null);
  const [connectionError, setConnectionError] = useState(null);

  const fetchDashboardData = useCallback(async () => {
    try {
      setConnectionError(null);
      const [sumData, svcData, incData, rulesData, notifData, countData] = await Promise.all([
        api.getDashboardSummary().catch(e => { throw e; }),
        api.getServices().catch(() => []),
        api.getIncidents(0, 50).catch(() => ({ content: [] })),
        api.getAlertRules().catch(() => []),
        api.getRecentNotifications().catch(() => []),
        api.getUnreadCount().catch(() => ({ unreadCount: 0 })),
      ]);

      setSummary(sumData);
      setServices(svcData || []);
      setIncidents(incData?.content || (Array.isArray(incData) ? incData : []));
      setAlertRules(rulesData || []);
      setNotifications(notifData || []);
      setUnreadCount(countData?.unreadCount || 0);
    } catch (err) {
      console.warn('Dashboard fetch issue:', err);
      setConnectionError(err.message || 'Unable to communicate with Spring Boot API backend');
    }
  }, []);

  // Initial load and periodic polling every 15s
  useEffect(() => {
    fetchDashboardData();
    const interval = setInterval(fetchDashboardData, 15000);
    return () => clearInterval(interval);
  }, [fetchDashboardData]);

  const handleRefresh = async () => {
    setIsRefreshing(true);
    await fetchDashboardData();
    setIsRefreshing(false);
  };

  const handleSeedDemo = async () => {
    try {
      setIsSeeding(true);
      await api.seedDemoData();
      await fetchDashboardData();
      setActiveTab('incidents');
    } catch (err) {
      alert(`Demo seed failed: ${err.message}`);
    } finally {
      setIsSeeding(false);
    }
  };

  const handleCheckNow = async (serviceId) => {
    try {
      setCheckingServiceId(serviceId);
      await api.triggerCheck(serviceId);
      await fetchDashboardData();
    } catch (err) {
      alert(`On-demand check failed: ${err.message}`);
    } finally {
      setCheckingServiceId(null);
    }
  };

  const handleSaveService = async (formData) => {
    if (serviceToEdit) {
      await api.updateService(serviceToEdit.id, formData);
    } else {
      await api.createService(formData);
    }
    await fetchDashboardData();
  };

  const handleDeleteService = async (id, name) => {
    if (window.confirm(`Are you sure you want to delete service "${name}" and its metrics history?`)) {
      try {
        await api.deleteService(id);
        await fetchDashboardData();
      } catch (err) {
        alert(`Delete failed: ${err.message}`);
      }
    }
  };

  const handleToggleService = async (id) => {
    try {
      await api.toggleService(id);
      await fetchDashboardData();
    } catch (err) {
      alert(`Toggle failed: ${err.message}`);
    }
  };

  const handleAcknowledgeIncident = async (id, user) => {
    await api.acknowledgeIncident(id, user);
    await fetchDashboardData();
  };

  const handleResolveIncident = async (id, user, notes) => {
    await api.resolveIncident(id, user, notes);
    await fetchDashboardData();
  };

  const handleCreateRule = async (ruleData) => {
    await api.createAlertRule(ruleData);
    await fetchDashboardData();
  };

  const handleDeleteRule = async (id) => {
    await api.deleteAlertRule(id);
    await fetchDashboardData();
  };

  const handleMarkRead = async (id) => {
    await api.markNotificationRead(id);
    setNotifications(prev => prev.map(n => n.id === id ? { ...n, read: true } : n));
    setUnreadCount(prev => Math.max(0, prev - 1));
  };

  const handleMarkAllRead = async () => {
    await api.markAllNotificationsRead();
    setNotifications(prev => prev.map(n => ({ ...n, read: true })));
    setUnreadCount(0);
  };

  const openEditModal = (service) => {
    setServiceToEdit(service);
    setIsServiceModalOpen(true);
  };

  const openAddModal = () => {
    setServiceToEdit(null);
    setIsServiceModalOpen(true);
  };

  const openDetailModal = (service) => {
    setSelectedService(service);
    setIsDetailModalOpen(true);
  };

  const activeIncidentsCount = incidents.filter(i => i.status !== 'RESOLVED').length;

  return (
    <div>
      <Navbar
        unreadCount={unreadCount}
        onOpenNotifications={() => setIsNotificationOpen(!isNotificationOpen)}
        onOpenAddService={openAddModal}
        onRefresh={handleRefresh}
        onSeedDemo={handleSeedDemo}
        isRefreshing={isRefreshing}
        isSeeding={isSeeding}
      />

      <main className="app-container">
        <DemoBanner />

        {connectionError && (
          <div style={{
            backgroundColor: 'rgba(239, 68, 68, 0.15)',
            border: '1px solid var(--status-down-border)',
            borderRadius: 'var(--radius-md)',
            padding: '16px 20px',
            marginBottom: '24px',
            display: 'flex',
            alignItems: 'center',
            gap: '12px',
            color: '#fca5a5'
          }}>
            <ServerOff size={22} style={{ flexShrink: 0 }} />
            <div>
              <strong>Backend Disconnected:</strong> {connectionError}
              <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '2px' }}>
                Ensure your Spring Boot backend is running locally on port 8080 (<code>mvn spring-boot:run</code>).
              </div>
            </div>
          </div>
        )}

        {/* Global Summary Stat Cards */}
        <div className="stat-grid">
          <StatCard
            title="System Availability"
            value={`${summary?.overallUptimePercentage ?? 100}%`}
            subtext="Calculated across all healthy checks"
            icon={ShieldCheck}
            color="var(--status-up)"
          />

          <StatCard
            title="Monitored Services"
            value={`${summary?.upServices ?? 0} / ${summary?.totalServices ?? services.length}`}
            subtext={`${summary?.upServices ?? 0} UP, ${summary?.downServices ?? 0} DOWN`}
            icon={Layers}
            color="var(--accent-cyan)"
          />

          <StatCard
            title="Active Incidents"
            value={activeIncidentsCount}
            subtext={activeIncidentsCount > 0 ? "Requires engineer attention" : "All services operational"}
            icon={AlertTriangle}
            color={activeIncidentsCount > 0 ? "var(--status-down)" : "var(--status-up)"}
          />

          <StatCard
            title="Avg Response Time"
            value={`${summary?.averageResponseTimeMs ?? 0}ms`}
            subtext="Measured across all endpoints"
            icon={Clock}
            color="var(--accent-blue)"
          />

          <StatCard
            title="Health Checks Logged"
            value={summary?.totalHealthChecksRecorded ?? 0}
            subtext="Persisted in MongoDB"
            icon={Database}
            color="var(--accent-violet)"
          />
        </div>

        {/* Navigation Tabs */}
        <div className="tabs-container">
          <button
            className={`tab-btn ${activeTab === 'overview' ? 'active' : ''}`}
            onClick={() => setActiveTab('overview')}
            id="tab-overview"
          >
            <Activity size={16} />
            <span>Dashboard & Services</span>
          </button>

          <button
            className={`tab-btn ${activeTab === 'incidents' ? 'active' : ''}`}
            onClick={() => setActiveTab('incidents')}
            id="tab-incidents"
          >
            <AlertTriangle size={16} />
            <span>Incidents ({activeIncidentsCount})</span>
          </button>

          <button
            className={`tab-btn ${activeTab === 'alerts' ? 'active' : ''}`}
            onClick={() => setActiveTab('alerts')}
            id="tab-alerts"
          >
            <Sliders size={16} />
            <span>Alert Rules ({alertRules.length})</span>
          </button>
        </div>

        {/* Tab 1: Overview & Services */}
        {activeTab === 'overview' && (
          <>
            <ResponseTimeChart data={summary?.latencyHistory} />

            <section className="services-section">
              <div className="section-header">
                <div>
                  <h2 className="section-title">Monitored Cloud Services</h2>
                  <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                    Continuous HTTP/HTTPS probes with automatic incident escalation.
                  </p>
                </div>
                <button className="btn btn-secondary" onClick={openAddModal}>
                  + Add Service
                </button>
              </div>

              <ServiceList
                services={services}
                onCheckNow={handleCheckNow}
                onViewMetrics={openDetailModal}
                onEdit={openEditModal}
                onToggle={handleToggleService}
                onDelete={handleDeleteService}
                checkingServiceId={checkingServiceId}
              />
            </section>
          </>
        )}

        {/* Tab 2: Incidents */}
        {activeTab === 'incidents' && (
          <IncidentManager
            incidents={incidents}
            onAcknowledge={handleAcknowledgeIncident}
            onResolve={handleResolveIncident}
          />
        )}

        {/* Tab 3: Alert Rules */}
        {activeTab === 'alerts' && (
          <AlertRuleManager
            rules={alertRules}
            services={services}
            onCreateRule={handleCreateRule}
            onDeleteRule={handleDeleteRule}
          />
        )}
      </main>

      {/* Notification Drawer */}
      <NotificationCenter
        isOpen={isNotificationOpen}
        onClose={() => setIsNotificationOpen(false)}
        notifications={notifications}
        onMarkRead={handleMarkRead}
        onMarkAllRead={handleMarkAllRead}
      />

      {/* Add / Edit Service Modal */}
      <ServiceModal
        isOpen={isServiceModalOpen}
        onClose={() => setIsServiceModalOpen(false)}
        onSave={handleSaveService}
        serviceToEdit={serviceToEdit}
      />

      {/* Service Metrics Deep Dive Modal */}
      <ServiceDetailModal
        isOpen={isDetailModalOpen}
        onClose={() => setIsDetailModalOpen(false)}
        service={selectedService}
        onCheckNow={handleCheckNow}
      />
    </div>
  );
}
