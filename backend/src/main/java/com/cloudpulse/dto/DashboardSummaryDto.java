package com.cloudpulse.dto;

import java.util.List;

public class DashboardSummaryDto {

    private long totalServices;
    private long upServices;
    private long downServices;
    private long degradedServices;
    private long unknownServices;
    private long openIncidents;
    private double overallUptimePercentage;
    private double averageResponseTimeMs;
    private long totalHealthChecksRecorded;
    private List<MetricsTimeseriesDto> latencyHistory;

    public DashboardSummaryDto() {
    }

    public long getTotalServices() { return totalServices; }
    public void setTotalServices(long totalServices) { this.totalServices = totalServices; }
    public long getUpServices() { return upServices; }
    public void setUpServices(long upServices) { this.upServices = upServices; }
    public long getDownServices() { return downServices; }
    public void setDownServices(long downServices) { this.downServices = downServices; }
    public long getDegradedServices() { return degradedServices; }
    public void setDegradedServices(long degradedServices) { this.degradedServices = degradedServices; }
    public long getUnknownServices() { return unknownServices; }
    public void setUnknownServices(long unknownServices) { this.unknownServices = unknownServices; }
    public long getOpenIncidents() { return openIncidents; }
    public void setOpenIncidents(long openIncidents) { this.openIncidents = openIncidents; }
    public double getOverallUptimePercentage() { return overallUptimePercentage; }
    public void setOverallUptimePercentage(double overallUptimePercentage) { this.overallUptimePercentage = overallUptimePercentage; }
    public double getAverageResponseTimeMs() { return averageResponseTimeMs; }
    public void setAverageResponseTimeMs(double averageResponseTimeMs) { this.averageResponseTimeMs = averageResponseTimeMs; }
    public long getTotalHealthChecksRecorded() { return totalHealthChecksRecorded; }
    public void setTotalHealthChecksRecorded(long totalHealthChecksRecorded) { this.totalHealthChecksRecorded = totalHealthChecksRecorded; }
    public List<MetricsTimeseriesDto> getLatencyHistory() { return latencyHistory; }
    public void setLatencyHistory(List<MetricsTimeseriesDto> latencyHistory) { this.latencyHistory = latencyHistory; }
}
