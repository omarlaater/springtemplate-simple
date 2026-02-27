package com.example.starter.service;

import com.example.starter.domain.model.Product;

import com.example.starter.dto.CreateProductRequest;
import com.example.starter.dto.UpdateProductRequest;
import com.example.starter.entity.ProductEntity;
import com.example.starter.exception.BusinessValidationException;
import com.example.starter.exception.ProductNotFoundException;
import com.example.starter.mapper.ProductMapper;
import com.example.starter.repository.ProductRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final Counter createCounter;
    private final Counter updateCounter;
    private final Timer listTimer;
    private final Tracer tracer;

    public ProductService(ProductRepository productRepository,
                          ProductMapper productMapper,
                          MeterRegistry meterRegistry,
                          Tracer tracer) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.createCounter = meterRegistry.counter("products.created.total");
        this.updateCounter = meterRegistry.counter("products.updated.total");
        this.listTimer = meterRegistry.timer("products.list.duration");
        this.tracer = tracer;
    }

    public Product create(CreateProductRequest request) {
        Span span = tracer.nextSpan().name("product-service.create");
        try (Tracer.SpanInScope ws = tracer.withSpan(span.start())) {
            if (productRepository.existsByNameIgnoreCase(request.name())) {
                throw new BusinessValidationException("Product name already exists");
            }
            ProductEntity saved = productRepository.save(productMapper.toEntity(request));
            createCounter.increment();
            return productMapper.toModel(saved);
        } finally {
            span.end();
        }
    }

    public Product update(Long id, UpdateProductRequest request) {
        Span span = tracer.nextSpan().name("product-service.update");
        try (Tracer.SpanInScope ws = tracer.withSpan(span.start())) {
            ProductEntity current = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

            if (!request.version().equals(current.getVersion())) {
                throw new OptimisticLockingFailureException("Version mismatch");
            }
            if (productRepository.existsByNameIgnoreCaseAndIdNot(request.name(), id)) {
                throw new BusinessValidationException("Product name already exists");
            }

            productMapper.updateEntity(current, request);
            updateCounter.increment();
            return productMapper.toModel(productRepository.save(current));
        } finally {
            span.end();
        }
    }

    @Transactional(readOnly = true)
    public Product findById(Long id) {
        Span span = tracer.nextSpan().name("product-service.findById");
        try (Tracer.SpanInScope ws = tracer.withSpan(span.start())) {
            return productMapper.toModel(
                productRepository.findById(id)
                    .orElseThrow(() -> new ProductNotFoundException(id))
            );
        } finally {
            span.end();
        }
    }

    @Transactional(readOnly = true)
    public Page<Product> list(String nameContains, Pageable pageable) {
        Span span = tracer.nextSpan().name("product-service.list");
        try (Tracer.SpanInScope ws = tracer.withSpan(span.start())) {
            Timer.Sample sample = Timer.start();
            try {
                Page<ProductEntity> page = (nameContains == null || nameContains.isBlank())
                    ? productRepository.findAll(pageable)
                    : productRepository.findByNameContainingIgnoreCase(nameContains.trim(), pageable);
                return page.map(productMapper::toModel);
            } finally {
                sample.stop(listTimer);
            }
        } finally {
            span.end();
        }
    }
}
