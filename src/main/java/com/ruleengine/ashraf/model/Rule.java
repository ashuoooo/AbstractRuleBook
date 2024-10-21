package com.ruleengine.ashraf.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Rule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ruleName;

    @Column(columnDefinition = "TEXT")
    private String ruleString;

    @Column(columnDefinition = "TEXT")
    private String astJson;

    // Default constructor
    public Rule() {}

    // Parameterized constructor
    public Rule(String ruleName, String ruleString, String astJson) {
        this.ruleName = ruleName;
        this.ruleString = ruleString;
        this.astJson = astJson;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRuleString() {
        return ruleString;
    }

    public void setRuleString(String ruleString) {
        this.ruleString = ruleString;
    }

    public String getAstJson() {
        return astJson;
    }

    public void setAstJson(String astJson) {
        this.astJson = astJson;
    }
}