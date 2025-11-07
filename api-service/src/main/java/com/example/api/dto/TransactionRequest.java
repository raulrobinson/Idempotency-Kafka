package com.example.api.dto;

import java.math.BigDecimal;
import java.util.Map;

public record TransactionRequest(BigDecimal amount, Map<String, Object> payload) {}