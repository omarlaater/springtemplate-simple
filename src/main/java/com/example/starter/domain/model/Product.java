package com.example.starter.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Product {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Long version;

    // ── Validated setters ─────────────────────────────────────────────────────

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
        this.name = name;
    }

    public void setPrice(BigDecimal price) {
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException("Price must be positive or zero");
        }
        this.price = price;
    }



    // ── Business methods ──────────────────────────────────────────────────────

    public void applyDiscount(BigDecimal percent) {
        if (percent == null || percent.signum() < 0) {
            throw new IllegalArgumentException("Discount percent must be positive");
        }
        this.price = this.price.multiply(BigDecimal.ONE.subtract(percent));
    }

    public boolean isExpensive() {
        return this.price.compareTo(new BigDecimal("1000")) > 0;
    }
}
