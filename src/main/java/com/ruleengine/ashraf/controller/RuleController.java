package com.ruleengine.ashraf.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ruleengine.ashraf.dto.RuleDTO;
import com.ruleengine.ashraf.model.Rule;
import com.ruleengine.ashraf.service.RuleService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rules")
public class RuleController {

    @Autowired
    private RuleService ruleService;

    @PostMapping("/create")
    public ResponseEntity<Long> createRule(@RequestBody RuleDTO ruleDTO) {
        try {
            Rule rule = ruleService.createRule(ruleDTO.getRuleName(), ruleDTO.getRuleString());
            return ResponseEntity.ok(rule.getId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/combine")
    public ResponseEntity<Long> combineRules(@RequestBody List<Long> ruleIds) {
        try {
            Rule combinedRule = ruleService.combineRules(ruleIds);
            return ResponseEntity.ok(combinedRule.getId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/evaluate/{ruleId}")
    public ResponseEntity<Boolean> evaluateRule(@PathVariable Long ruleId, @RequestBody Map<String, Object> data) {
        try {
            boolean result = ruleService.evaluateRule(ruleId, data);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}