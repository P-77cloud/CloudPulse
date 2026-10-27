package com.cloudpulse.repository;

import com.cloudpulse.model.HealthCheck;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface HealthCheckRepository extends MongoRepository<HealthCheck, String> {
    Page<HealthCheck> findByServiceIdOrderByTimestampDesc(String serviceId, Pageable pageable);
    List<HealthCheck> findTop50ByServiceIdOrderByTimestampDesc(String serviceId);
    List<HealthCheck> findByTimestampAfterOrderByTimestampAsc(Instant timestamp);
    long countByServiceIdAndTimestampAfter(String serviceId, Instant timestamp);
    long countByServiceIdAndIsSuccessTrueAndTimestampAfter(String serviceId, Instant timestamp);
    void deleteByServiceId(String serviceId);
}
