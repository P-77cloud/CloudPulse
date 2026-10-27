package com.cloudpulse.repository;

import com.cloudpulse.model.AlertRule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRuleRepository extends MongoRepository<AlertRule, String> {
    List<AlertRule> findByEnabledTrue();
    List<AlertRule> findByServiceIdOrServiceIdIsNull(String serviceId);
    void deleteByServiceId(String serviceId);
}
