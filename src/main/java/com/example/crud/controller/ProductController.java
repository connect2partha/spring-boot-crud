package com.example.crud.controller;

import com.example.crud.entity.Product;
import com.example.crud.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "CRUD operations for products")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "List all products", description = "Returns all products, or filters by name when the 'name' query param is provided.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved product list")
    })
    public ResponseEntity<List<Product>> getAll(
            @Parameter(description = "Optional name filter (case-insensitive substring match)")
            @RequestParam(required = false) String name) {
        List<Product> products = (name != null && !name.isBlank())
                ? productService.findByName(name)
                : productService.findAll();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a product by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product found"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Product> getById(
            @Parameter(description = "ID of the product to retrieve", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new product")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Product created successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed — check the 'errors' field in the response")
    })
    public ResponseEntity<Product> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Product data to create", required = true)
            @Valid @RequestBody Product product) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(product));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing product")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product updated successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed — check the 'errors' field in the response"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Product> update(
            @Parameter(description = "ID of the product to update", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated product data", required = true)
            @Valid @RequestBody Product product) {
        return ResponseEntity.ok(productService.update(id, product));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID of the product to delete", required = true)
            @PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
