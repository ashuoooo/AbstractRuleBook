package com.ruleengine.ashraf.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruleengine.ashraf.model.Rule;
import com.ruleengine.ashraf.repository.RuleRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RuleService {

    @Autowired
    private RuleRepository ruleRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    // Method to create and save a new rule
    public Rule createRule(String ruleName, String ruleString) throws Exception {
        Map<String, Object> ast = parseRule(ruleString);
        String astJson = objectMapper.writeValueAsString(ast);
        Rule rule = new Rule(ruleName, ruleString, astJson);
        return ruleRepository.save(rule);
    }

    // Method to combine multiple rules using "AND"
    public Rule combineRules(List<Long> ruleIds) throws Exception {
        List<Rule> rules = ruleRepository.findAllById(ruleIds);
        if (rules.size() != ruleIds.size()) {
            throw new RuntimeException("One or more rules not found");
        }

        Map<String, Object> combinedAst = new HashMap<>();
        combinedAst.put("type", "AND");
        combinedAst.put("left", objectMapper.readValue(rules.get(0).getAstJson(), Map.class));
        combinedAst.put("right", objectMapper.readValue(rules.get(1).getAstJson(), Map.class));

        for (int i = 2; i < rules.size(); i++) {
            Map<String, Object> newAst = new HashMap<>();
            newAst.put("type", "AND");
            newAst.put("left", combinedAst);
            newAst.put("right", objectMapper.readValue(rules.get(i).getAstJson(), Map.class));
            combinedAst = newAst;
        }

        String combinedRuleString = rules.stream()
                .map(Rule::getRuleString)
                .reduce((a, b) -> "(" + a + ") AND (" + b + ")")
                .orElseThrow(() -> new RuntimeException("Error combining rules"));

        String combinedAstJson = objectMapper.writeValueAsString(combinedAst);
        Rule combinedRule = new Rule("Combined Rule", combinedRuleString, combinedAstJson);
        return ruleRepository.save(combinedRule);
    }

    // Method to evaluate a rule against some data
    public boolean evaluateRule(Long ruleId, Map<String, Object> data) throws Exception {
        Rule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Rule not found"));
        Map<String, Object> ast = objectMapper.readValue(rule.getAstJson(), Map.class);
        return evaluateAst(ast, data);
    }

    // Method to parse a rule string into an Abstract Syntax Tree (AST)
    private Map<String, Object> parseRule(String ruleString) {
        List<String> tokens = tokenize(ruleString);
        return parseExpression(tokens);
    }

    // Method to tokenize a rule string
    private List<String> tokenize(String ruleString) {
        List<String> tokens = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\w+|>=|<=|>|<|=|'[^']+'");
        Matcher matcher = pattern.matcher(ruleString);
        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        return tokens;
    }

    // Method to parse tokens into an expression (AST)
    private Map<String, Object> parseExpression(List<String> tokens) {
        if (tokens.size() == 1) {
            return createLeafNode(tokens.get(0));
        }

        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).equals("AND") || tokens.get(i).equals("OR")) {
                Map<String, Object> node = new HashMap<>();
                node.put("type", tokens.get(i));
                node.put("left", parseExpression(tokens.subList(0, i)));
                node.put("right", parseExpression(tokens.subList(i + 1, tokens.size())));
                return node;
            }
        }

        if (tokens.get(0).equals("(") && tokens.get(tokens.size() - 1).equals(")")) {
            return parseExpression(tokens.subList(1, tokens.size() - 1));
        }

        return createLeafNode(String.join(" ", tokens));
    }

    // Method to create a leaf node in the AST for a single condition
    private Map<String, Object> createLeafNode(String condition) {
        Map<String, Object> node = new HashMap<>();
        node.put("type", "CONDITION");
        node.put("condition", condition);
        return node;
    }

    // Method to evaluate the AST recursively against input data
    private boolean evaluateAst(Map<String, Object> ast, Map<String, Object> data) {
        String type = (String) ast.get("type");
        switch (type) {
            case "AND":
                return evaluateAst((Map<String, Object>) ast.get("left"), data) &&
                       evaluateAst((Map<String, Object>) ast.get("right"), data);
            case "OR":
                return evaluateAst((Map<String, Object>) ast.get("left"), data) ||
                       evaluateAst((Map<String, Object>) ast.get("right"), data);
            case "CONDITION":
                return evaluateCondition((String) ast.get("condition"), data);
            default:
                throw new RuntimeException("Invalid AST node type: " + type);
        }
    }

    // Method to evaluate a single condition (like age > 18)
    private boolean evaluateCondition(String condition, Map<String, Object> data) {
        String[] parts = condition.split(" ");
        String attribute = parts[0];
        String operator = parts[1];
        String value = parts[2].replace("'", "");

        Object dataValue = data.get(attribute);
        if (dataValue == null) {
            return false;
        }

        switch (operator) {
            case "=":
                return dataValue.toString().equals(value);
            case ">":
                return compareValues(dataValue, value) > 0;
            case "<":
                return compareValues(dataValue, value) < 0;
            case ">=":
                return compareValues(dataValue, value) >= 0;
            case "<=":
                return compareValues(dataValue, value) <= 0;
            default:
                throw new RuntimeException("Invalid operator: " + operator);
        }
    }

    // Method to compare two values (used in condition evaluation)
    private int compareValues(Object value1, String value2) {
        if (value1 instanceof Number && value2.matches("-?\\d+(\\.\\d+)?")) {
            double num1 = ((Number) value1).doubleValue();
            double num2 = Double.parseDouble(value2);
            return Double.compare(num1, num2);
        }
        return value1.toString().compareTo(value2);
    }
}
