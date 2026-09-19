package com.example.crud.service;

import com.example.crud.entity.Product;

import java.util.List;

public interface ProductService {

    List<Product> findAll();

    Product findById(Long id);

    List<Product> findByName(String name);

    Product create(Product product);

    Product update(Long id, Product product);

    void delete(Long id);
}
