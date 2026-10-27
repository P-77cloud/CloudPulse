import React from 'react';
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
  ReferenceLine
} from 'recharts';
import { Activity } from 'lucide-react';

export default function ResponseTimeChart({ data }) {
  if (!data || data.length === 0) {
    return (
      <div className="chart-card">
        <div className="chart-header">
          <div className="chart-title">Real-time Service Latency Trend</div>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Spring Boot Metrics</span>
        </div>
        <div style={{ height: '240px', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)' }}>
          <Activity size={32} style={{ marginBottom: '8px', opacity: 0.5 }} />
          <p>No health check metrics recorded yet. Metrics will plot as health checks execute.</p>
        </div>
      </div>
    );
  }

  // Format data points for chart
  const formattedData = data.map((d, index) => {
    const time = d.timestamp ? new Date(d.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' }) : `#${index + 1}`;
    return {
      time,
      latency: d.responseTimeMs || 0,
      service: d.serviceName || 'Service',
      success: d.success
    };
  });

  // Calculate p95 latency
  const sortedLatencies = [...formattedData].map(d => d.latency).sort((a, b) => a - b);
  const p95 = sortedLatencies.length > 0 ? sortedLatencies[Math.floor(sortedLatencies.length * 0.95)] : 0;

  return (
    <div className="chart-card">
      <div className="chart-header">
        <div>
          <div className="chart-title">Real-time Service Latency Trend</div>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
            Live response times (ms) across recorded HTTP health checks
          </span>
        </div>
        <div style={{ display: 'flex', gap: '16px', fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
          <div>
            <span style={{ color: 'var(--text-muted)' }}>p95 Latency: </span>
            <strong style={{ color: 'var(--accent-cyan)' }}>{p95}ms</strong>
          </div>
          <div>
            <span style={{ color: 'var(--text-muted)' }}>Data Points: </span>
            <strong style={{ color: 'var(--text-primary)' }}>{formattedData.length}</strong>
          </div>
        </div>
      </div>

      <div style={{ width: '100%', height: 260 }}>
        <ResponsiveContainer>
          <AreaChart data={formattedData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
            <defs>
              <linearGradient id="latencyGradient" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#06b6d4" stopOpacity={0.4} />
                <stop offset="95%" stopColor="#06b6d4" stopOpacity={0.0} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.06)" />
            <XAxis dataKey="time" stroke="#64748b" fontSize={11} tickLine={false} />
            <YAxis stroke="#64748b" fontSize={11} tickLine={false} unit="ms" />
            <Tooltip
              contentStyle={{
                backgroundColor: '#0f172a',
                borderColor: 'rgba(255,255,255,0.15)',
                borderRadius: '8px',
                boxShadow: '0 4px 20px rgba(0,0,0,0.5)',
                color: '#f8fafc',
                fontSize: '0.8rem'
              }}
              formatter={(value, name, props) => [
                `${value}ms (${props.payload.service})`,
                'Latency'
              ]}
            />
            {p95 > 0 && (
              <ReferenceLine y={p95} stroke="#f59e0b" strokeDasharray="3 3" label={{ value: 'p95', fill: '#f59e0b', fontSize: 10 }} />
            )}
            <Area
              type="monotone"
              dataKey="latency"
              stroke="#06b6d4"
              strokeWidth={2}
              fillOpacity={1}
              fill="url(#latencyGradient)"
            />
          </AreaChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
