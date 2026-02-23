package com.example.starter.service;

import com.example.starter.domain.model.Product;
import com.example.starter.entity.ProductEntity;
import com.example.starter.exception.BusinessValidationException;
import com.example.starter.exception.ProductNotFoundException;
import com.example.starter.repository.ProductRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final Counter createCounter;
    private final Counter updateCounter;
    private final Timer listTimer;

    public ProductService(ProductRepository productRepository, MeterRegistry meterRegistry) {
        this.productRepository = productRepository;
        this.createCounter = meterRegistry.counter("products.created.total");
        this.updateCounter = meterRegistry.counter("products.updated.total");
        this.listTimer = meterRegistry.timer("products.list.duration");
    }

    public Product create(String name, String description, BigDecimal price) {
        validateInput(name, price);
        if (productRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessValidationException("Product name already exists");
        }

        ProductEntity entity = new ProductEntity();
        entity.setName(name.trim());
        entity.setDescription(description);
        entity.setPrice(price);

        ProductEntity saved = productRepository.save(entity);
        createCounter.increment();
        return toModel(saved);
    }

    public Product update(Long id, String name, String description, BigDecimal price, Long expectedVersion) {
        validateInput(name, price);
        if (expectedVersion == null) {
            throw new BusinessValidationException("Version is required for updates");
        }

        ProductEntity current = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        if (!expectedVersion.equals(current.getVersion())) {
            throw new OptimisticLockingFailureException("Version mismatch");
        }
        if (productRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new BusinessValidationException("Product name already exists");
        }

        current.setName(name.trim());
        current.setDescription(description);
        current.setPrice(price);

        ProductEntity updated = productRepository.save(current);
        updateCounter.increment();
        return toModel(updated);
    }

    @Transactional(readOnly = true)
    public Product findById(Long id) {
        ProductEntity entity = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return toModel(entity);
    }

    @Transactional(readOnly = true)
    public Page<Product> list(String nameContains, Pageable pageable) {
        Timer.Sample sample = Timer.start();
        Page<ProductEntity> page;
        try {
            page = (nameContains == null || nameContains.isBlank())
                    ? productRepository.findAll(pageable)
                    : productRepository.findByNameContainingIgnoreCase(nameContains.trim(), pageable);
        } finally {
            sample.stop(listTimer);
        }
        return page.map(this::toModel);
    }

    private void validateInput(String name, BigDecimal price) {
        if (name == null || name.isBlank()) {
            throw new BusinessValidationException("Product name is required");
        }
        if (price == null || price.signum() < 0) {
            throw new BusinessValidationException("Price must be positive or zero");
        }
    }

    private Product toModel(ProductEntity entity) {
        return new Product(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getVersion()
        );
    }
}
