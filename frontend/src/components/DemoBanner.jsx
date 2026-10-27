import React from 'react';
import { ShieldCheck, Info } from 'lucide-react';

export default function DemoBanner() {
  return (
    <div className="demo-banner">
      <div className="demo-banner-left">
        <ShieldCheck size={18} style={{ color: 'var(--status-up)', flexShrink: 0 }} />
        <span>
          <strong>Live Infrastructure Monitor:</strong> All uptime metrics and latency measurements are real HTTP/HTTPS health checks executed by Spring Boot and persisted in MongoDB. No simulated CPU/RAM/Disk metrics. Sample evaluation records are strictly tagged <span className="demo-badge-tag">DEMO</span>.
        </span>
      </div>
      <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', whiteSpace: 'nowrap' }}>
        SSRF Protected
      </div>
    </div>
  );
}
