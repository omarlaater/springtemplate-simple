package com.example.starter.service;

import com.example.starter.domain.model.Product;
import com.example.starter.entity.ProductEntity;
import com.example.starter.exception.BusinessValidationException;
import com.example.starter.exception.ProductNotFoundException;
import com.example.starter.repository.ProductRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, new SimpleMeterRegistry());
    }

    @Test
    void createShouldPersistAndReturnProduct() {
        ProductEntity savedEntity = entity(10L, "Mouse", "Wireless", BigDecimal.valueOf(29.99), 0L);
        when(productRepository.existsByNameIgnoreCase("Mouse")).thenReturn(false);
        when(productRepository.save(any(ProductEntity.class))).thenReturn(savedEntity);

        Product result = productService.create("Mouse", "Wireless", BigDecimal.valueOf(29.99));

        assertEquals(10L, result.id());
        assertEquals("Mouse", result.name());
        verify(productRepository).existsByNameIgnoreCase("Mouse");
        verify(productRepository).save(any(ProductEntity.class));
    }

    @Test
    void createShouldFailWhenNameExists() {
        when(productRepository.existsByNameIgnoreCase("Mouse")).thenReturn(true);

        assertThrows(
                BusinessValidationException.class,
                () -> productService.create("Mouse", "Wireless", BigDecimal.valueOf(29.99))
        );
        verify(productRepository, never()).save(any(ProductEntity.class));
    }

    @Test
    void updateShouldFailOnVersionMismatch() {
        ProductEntity existing = entity(1L, "Keyboard", "Mech", BigDecimal.valueOf(99.00), 3L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(
                OptimisticLockingFailureException.class,
                () -> productService.update(1L, "Keyboard", "Mech", BigDecimal.valueOf(99.00), 2L)
        );
        verify(productRepository, never()).save(any(ProductEntity.class));
    }

    @Test
    void findByIdShouldThrowWhenMissing() {
        when(productRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.findById(42L));
    }

    @Test
    void listShouldUseNameFilterWhenProvided() {
        ProductEntity entity = entity(5L, "Laptop", "Work laptop", BigDecimal.valueOf(1500), 0L);
        PageRequest pageable = PageRequest.of(0, 10);
        when(productRepository.findByNameContainingIgnoreCase("lap", pageable))
                .thenReturn(new PageImpl<>(List.of(entity), pageable, 1));

        Page<Product> page = productService.list("lap", pageable);

        assertEquals(1, page.getTotalElements());
        assertTrue(page.getContent().stream().anyMatch(product -> "Laptop".equals(product.name())));
    }

    private ProductEntity entity(Long id, String name, String description, BigDecimal price, Long version) {
        ProductEntity productEntity = new ProductEntity();
        productEntity.setId(id);
        productEntity.setName(name);
        productEntity.setDescription(description);
        productEntity.setPrice(price);
        productEntity.setVersion(version);
        return productEntity;
    }
}
