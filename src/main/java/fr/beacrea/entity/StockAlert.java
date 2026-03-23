package fr.beacrea.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "stock_alerts")
public class StockAlert extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "product_id")
    public Long productId;

    @Column(name = "size")
    public String size;

    @Column(name = "user_email")
    public String userEmail;

    @Column(name = "notified")
    public Boolean notified;

    @Column(name = "created_at")
    public String createdAt;
}
