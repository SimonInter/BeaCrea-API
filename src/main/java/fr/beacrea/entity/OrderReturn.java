package fr.beacrea.entity;

import com.fasterxml.jackson.databind.JsonNode;
import fr.beacrea.converter.JsonNodeConverter;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "order_returns")
public class OrderReturn extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "user_id")
    public String userId;

    @Column(name = "email")
    public String email;

    @Column(name = "user_name")
    public String userName;

    @Column(name = "order_id")
    public String orderId;

    @Column(name = "status")
    public String status;

    @Column(name = "reason")
    public String reason;

    @Column(name = "notes", columnDefinition = "text")
    public String notes;

    @Column(name = "refund_amount")
    public Double refundAmount;

    @Column(name = "rejection_reason")
    public String rejectionReason;

    @Column(name = "created_at")
    public String createdAt;

    @Column(name = "approved_at")
    public String approvedAt;

    @Column(name = "rejected_at")
    public String rejectedAt;

    @Column(name = "refunded_at")
    public String refundedAt;

    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "items", columnDefinition = "text")
    public JsonNode items;

    public static List<OrderReturn> findByUserId(String pUserId) {
        return list("userId", pUserId);
    }
}
