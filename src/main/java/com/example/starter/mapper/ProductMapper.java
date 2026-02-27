package com.example.starter.mapper;

import com.example.starter.domain.model.Product;

import com.example.starter.dto.CreateProductRequest;
import com.example.starter.dto.ProductResponse;
import com.example.starter.dto.UpdateProductRequest;
import com.example.starter.entity.ProductEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    // ── Entity → Domain model ─────────────────────────────────────────────────

    public Product toModel(ProductEntity entity) {
        return Product.builder()
            .id(entity.getId())
            .name(entity.getName())
            .description(entity.getDescription())
            .price(entity.getPrice())
            .version(entity.getVersion())
            .build();
    }

    // ── Domain model → Response DTO ───────────────────────────────────────────

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getVersion()
        );
    }

    // ── Create request → Entity ───────────────────────────────────────────────

    public ProductEntity toEntity(CreateProductRequest request) {
        ProductEntity entity = new ProductEntity();
        entity.setName(request.name().trim());
        entity.setDescription(request.description());
        entity.setPrice(request.price());
        return entity;
    }

    // ── Update request → mutate existing Entity ───────────────────────────────

    public void updateEntity(ProductEntity entity, UpdateProductRequest request) {
        entity.setName(request.name().trim());
        entity.setDescription(request.description());
        entity.setPrice(request.price());
    }
}
