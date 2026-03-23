package fr.beacrea.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "gift_cards")
public class GiftCard extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "code", nullable = false, unique = true)
    public String code;

    @Column(name = "amount", nullable = false)
    public Double amount;

    @Column(name = "balance", nullable = false)
    public Double balance;

    @Column(name = "recipient_email", nullable = false)
    public String recipientEmail;

    @Column(name = "message", columnDefinition = "text")
    public String message;

    @Column(name = "active", nullable = false)
    public Boolean active = true;

    @Column(name = "created_at")
    public String createdAt;

    @Column(name = "used_at")
    public String usedAt;

    public static GiftCard findByCode(String pCode) {
        return find("code", pCode).firstResult();
    }

    public static List<GiftCard> findActive() {
        return list("active", true);
    }
}
