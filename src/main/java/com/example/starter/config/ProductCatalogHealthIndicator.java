package com.example.starter.config;

import com.example.starter.repository.ProductRepository;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("catalog")
public class ProductCatalogHealthIndicator implements HealthIndicator {

    private final ProductRepository productRepository;

    public ProductCatalogHealthIndicator(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Health health() {
        try {
            long totalProducts = productRepository.count();
            return Health.up()
                    .withDetail("totalProducts", totalProducts)
                    .build();
        } catch (Exception ex) {
            return Health.down(ex).build();
        }
    }
}
