package edu.codelounge.apps;

import edu.codelounge.apps.entity.Product;
import edu.codelounge.apps.service.ProductService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class ProductControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Autowired
    private ProductService productService;

    @Test
    void getAllProducts_returnsOk() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    void createProduct_returnsCreated() throws Exception {
        Product product = Product.builder()
                .name("Test Product")
                .description("A test product")
                .price(BigDecimal.valueOf(49.99))
                .quantity(5)
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Test Product")));
    }

    @Test
    void createProduct_invalidBody_returnsBadRequest() throws Exception {
        Product product = Product.builder().name("").price(BigDecimal.valueOf(-1)).build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", notNullValue()));
    }

    @Test
    void getProductById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/products/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateProduct_returnsOk() throws Exception {
        Product created = productService.create(Product.builder()
                .name("Original")
                .price(BigDecimal.valueOf(10.00))
                .quantity(1)
                .build());

        created.setName("Updated");
        created.setPrice(BigDecimal.valueOf(20.00));

        mockMvc.perform(put("/api/products/" + created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(created)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated")));
    }

    @Test
    void deleteProduct_returnsNoContent() throws Exception {
        Product created = productService.create(Product.builder()
                .name("To Delete")
                .price(BigDecimal.valueOf(5.00))
                .quantity(1)
                .build());

        mockMvc.perform(delete("/api/products/" + created.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/products/" + created.getId()))
                .andExpect(status().isNotFound());
    }
}
