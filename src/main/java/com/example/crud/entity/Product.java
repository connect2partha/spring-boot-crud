package com.example.crud.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Represents a product in the catalog")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Auto-generated product ID", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    @Schema(description = "Product name", example = "Laptop", maxLength = 100)
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Optional product description", example = "15-inch high-performance laptop", maxLength = 500)
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Column(nullable = false, precision = 10, scale = 2)
    @Schema(description = "Product price (must be > 0)", example = "1299.99")
    private BigDecimal price;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must not be negative")
    @Column(nullable = false)
    @Schema(description = "Stock quantity (must be >= 0)", example = "10")
    private Integer quantity;

    @Column(updatable = false)
    @Schema(description = "Timestamp when the product was created", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of the last update", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
