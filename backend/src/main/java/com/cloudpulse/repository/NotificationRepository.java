package com.cloudpulse.repository;

import com.cloudpulse.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    Page<Notification> findByOrderByTimestampDesc(Pageable pageable);
    List<Notification> findTop20ByOrderByTimestampDesc();
    long countByReadFalse();
    void deleteByServiceId(String serviceId);
}
