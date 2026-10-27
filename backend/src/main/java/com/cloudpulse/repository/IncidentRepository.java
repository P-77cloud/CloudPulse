package com.cloudpulse.repository;

import com.cloudpulse.model.Incident;
import com.cloudpulse.model.IncidentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentRepository extends MongoRepository<Incident, String> {
    Page<Incident> findByOrderByStartedAtDesc(Pageable pageable);
    List<Incident> findByStatus(IncidentStatus status);
    long countByStatus(IncidentStatus status);
    List<Incident> findByServiceIdAndStatusIn(String serviceId, List<IncidentStatus> statuses);
    Optional<Incident> findFirstByServiceIdAndStatusInOrderByStartedAtDesc(String serviceId, List<IncidentStatus> statuses);
    void deleteByServiceId(String serviceId);
}
