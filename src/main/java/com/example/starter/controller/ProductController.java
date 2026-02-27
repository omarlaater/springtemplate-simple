package com.example.starter.controller;


import com.example.starter.dto.CreateProductRequest;
import com.example.starter.dto.PagedResponse;
import com.example.starter.dto.ProductResponse;
import com.example.starter.dto.UpdateProductRequest;
import com.example.starter.mapper.ProductMapper;
import com.example.starter.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    public ProductController(ProductService productService, ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody CreateProductRequest request) {
        return productMapper.toResponse(productService.create(request));
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id,
                                  @Valid @RequestBody UpdateProductRequest request) {
        return productMapper.toResponse(productService.update(id, request));
    }

    @GetMapping("/{id}")
    public ProductResponse findById(@PathVariable Long id) {
        return productMapper.toResponse(productService.findById(id));
    }

    @GetMapping
    public PagedResponse<ProductResponse> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "ASC") String direction,
        @RequestParam(required = false) String name
    ) {
        Sort.Direction sortDirection = "DESC".equalsIgnoreCase(direction)
            ? Sort.Direction.DESC
            : Sort.Direction.ASC;

        Page<ProductResponse> result = productService
            .list(name, PageRequest.of(page, size, Sort.by(sortDirection, sortBy)))
            .map(productMapper::toResponse);

        return new PagedResponse<>(
            result.getContent(),
            result.getTotalElements(),
            result.getNumber(),
            result.getSize()
        );
    }
}
