package fr.beacrea.entity;

import com.fasterxml.jackson.databind.JsonNode;
import fr.beacrea.converter.JsonNodeConverter;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order extends PanacheEntityBase {

    @Id
    @Column(name = "id")
    public long id;

    @Column(name = "user_id")
    public String userId;

    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "items", columnDefinition = "text", nullable = false)
    public JsonNode items;

    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "shipping_address", columnDefinition = "text")
    public JsonNode shippingAddress;

    @Column(name = "shipping_method")
    public String shippingMethod;

    @Column(name = "payment_method")
    public String paymentMethod;

    @Column(name = "subtotal", precision = 10, scale = 2)
    public BigDecimal subtotal;

    @Column(name = "shipping_cost", precision = 10, scale = 2)
    public BigDecimal shippingCost;

    @Column(name = "total", precision = 10, scale = 2)
    public BigDecimal total;

    @Column(name = "email")
    public String email;

    @Column(name = "status")
    public String status;

    @Column(name = "payment_intent_id")
    public String paymentIntentId;

    @Column(name = "tracking_number")
    public String trackingNumber;

    @Column(name = "carrier")
    public String carrier;

    @Column(name = "created_at")
    public String createdAt;

    public static List<Order> findByUserId(String pUserId) {
        return list("userId", pUserId);
    }
}
