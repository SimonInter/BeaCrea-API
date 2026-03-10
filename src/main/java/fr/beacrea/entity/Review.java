package fr.beacrea.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "reviews")
public class Review extends PanacheEntityBase {

    @Id
    @Column(name = "id")
    public Long id;

    @Column(name = "product_id", nullable = false)
    public long productId;

    @Column(name = "user_id")
    public String userId;

    @Column(name = "user_name")
    public String userName;

    @Column(name = "rating")
    public int rating;

    @Column(name = "comment", columnDefinition = "text")
    public String comment;

    @Column(name = "date")
    public String date;

    @Column(name = "verified")
    public boolean verified;

    public static List<Review> findByProductId(long pProductId) {
        return list("productId", pProductId);
    }
}
