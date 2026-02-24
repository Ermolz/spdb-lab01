package com.kaerna.lab01.service;

import java.math.BigDecimal;

public record ProductAggregate(long count, BigDecimal avgPrice) {
}
