package fr.beacrea.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "newsletters")
public class Newsletter extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "email", unique = true)
    public String email;

    @Column(name = "active")
    public Boolean active;

    @Column(name = "subscribed_at")
    public String subscribedAt;

    public static Newsletter findByEmail(String pEmail) {
        return find("email", pEmail).firstResult();
    }
}
