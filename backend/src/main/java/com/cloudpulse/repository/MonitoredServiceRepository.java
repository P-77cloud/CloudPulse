package com.cloudpulse.repository;

import com.cloudpulse.model.MonitoredService;
import com.cloudpulse.model.ServiceStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonitoredServiceRepository extends MongoRepository<MonitoredService, String> {
    List<MonitoredService> findAllByEnabledTrue();
    long countByStatus(ServiceStatus status);
}
