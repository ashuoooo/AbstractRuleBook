package com.ruleengine.ashraf.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ruleengine.ashraf.model.Rule;

public interface RuleRepository extends JpaRepository<Rule, Long> {
}