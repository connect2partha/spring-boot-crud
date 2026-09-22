package edu.codelounge.apps.service;

import edu.codelounge.apps.entity.Product;
import edu.codelounge.apps.exception.ResourceNotFoundException;
import edu.codelounge.apps.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final Environment environment;

    @PostConstruct
    void logRuntimeConfiguration() {
        String datasourcePassword = environment.getProperty("spring.datasource.password");
        log.info(
                "Product service started with profile={}, serverPort={}, datasourceUser={}, datasourcePasswordConfigured={}",
                environment.getProperty("spring.profiles.active", "default"),
                environment.getProperty("server.port", "8080"),
                environment.getProperty("spring.datasource.username", "not configured"),
                datasourcePassword != null && !datasourcePassword.isBlank()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public Product create(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product update(Long id, Product updated) {
        Product existing = findById(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setPrice(updated.getPrice());
        existing.setQuantity(updated.getQuantity());
        return productRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        Product product = findById(id);
        productRepository.delete(product);
    }
}
