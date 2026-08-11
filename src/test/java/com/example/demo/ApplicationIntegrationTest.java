package com.example.demo;

import com.example.demo.entity.Order;
import com.example.demo.entity.OrderDetail;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void contextLoadsAndSchemaInitializes() {
        // App starts successfully, meaning context loads and Flyway migration runs.
    }

    @Test
    public void testUserCrud() {
        // Create User
        User user = new User();
        user.setName("Test User");
        user.setEmail("test" + System.currentTimeMillis() + "@gmail.com");

        ResponseEntity<User> createResponse = restTemplate.postForEntity("/api/users", user, User.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        User createdUser = createResponse.getBody();
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isNotNull();

        // Get User
        ResponseEntity<User> getResponse = restTemplate.getForEntity("/api/users/" + createdUser.getId(), User.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getName()).isEqualTo("Test User");

        // Update User
        createdUser.setName("Updated User");
        restTemplate.put("/api/users/" + createdUser.getId(), createdUser);

        // Delete User
        restTemplate.delete("/api/users/" + createdUser.getId());
    }

    @Test
    public void testProductOrderAndOrderDetailCrud() {
        // 1. Create User for FK
        User user = new User();
        user.setName("Order User");
        user.setEmail("order" + System.currentTimeMillis() + "@example.com");
        User createdUser = restTemplate.postForEntity("/api/users", user, User.class).getBody();

        // 2. Create Product
        Product product = new Product();
        product.setName("Test Product");
        product.setDescription("Test Desc");
        product.setPrice(new BigDecimal("10.50"));
        product.setQuantity(100);

        ResponseEntity<Product> createProductResp = restTemplate.postForEntity("/api/products", product, Product.class);
        assertThat(createProductResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Product createdProduct = createProductResp.getBody();
        assertThat(createdProduct.getId()).isNotNull();

        // 3. Create Order
        Order order = new Order();
        order.setUser(createdUser);
        order.setTotalAmount(new BigDecimal("21.00"));
        order.setStatus("CONFIRMED");

        ResponseEntity<Order> createOrderResp = restTemplate.postForEntity("/api/orders", order, Order.class);
        assertThat(createOrderResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Order createdOrder = createOrderResp.getBody();
        assertThat(createdOrder.getId()).isNotNull();
        assertThat(createdOrder.getCreatedAt()).isNotNull();

        // 4. Create OrderDetail
        OrderDetail detail = new OrderDetail();
        detail.setOrder(createdOrder);
        detail.setProduct(createdProduct);
        detail.setQuantity(2);
        detail.setPrice(new BigDecimal("10.50"));

        ResponseEntity<OrderDetail> createDetailResp = restTemplate.postForEntity("/api/order-details", detail, OrderDetail.class);
        assertThat(createDetailResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OrderDetail createdDetail = createDetailResp.getBody();
        assertThat(createdDetail.getId()).isNotNull();

        // 5. Test FK Validation (Invalid Order ID)
        OrderDetail invalidDetail = new OrderDetail();
        Order invalidOrder = new Order();
        invalidOrder.setId(9999L);
        invalidDetail.setOrder(invalidOrder);
        invalidDetail.setProduct(createdProduct);
        invalidDetail.setQuantity(1);
        invalidDetail.setPrice(new BigDecimal("10.50"));
        
        ResponseEntity<String> invalidDetailResp = restTemplate.postForEntity("/api/order-details", invalidDetail, String.class);
        assertThat(invalidDetailResp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // 6. Delete OrderDetail
        restTemplate.delete("/api/order-details/" + createdDetail.getId());

        // 7. Delete Order
        restTemplate.delete("/api/orders/" + createdOrder.getId());

        // 8. Delete Product
        restTemplate.delete("/api/products/" + createdProduct.getId());

        // 9. Delete User
        restTemplate.delete("/api/users/" + createdUser.getId());
    }
}
