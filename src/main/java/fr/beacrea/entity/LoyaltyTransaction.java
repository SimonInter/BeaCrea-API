package fr.beacrea.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "loyalty_transactions")
public class LoyaltyTransaction extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "user_id")
    public String userId;

    @Column(name = "order_id")
    public String orderId;

    @Column(name = "points")
    public Integer points;

    @Column(name = "type")
    public String type;

    @Column(name = "description")
    public String description;

    @Column(name = "created_at")
    public String createdAt;

    public static List<LoyaltyTransaction> findByUserId(String pUserId) {
        return list("userId", pUserId);
    }
}
