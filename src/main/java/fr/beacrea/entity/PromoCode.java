package fr.beacrea.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "promo_codes")
public class PromoCode extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "code", nullable = false, unique = true)
    public String code;

    @Column(name = "active")
    public Boolean active;

    @Column(name = "valid_from")
    public String validFrom;

    @Column(name = "valid_to")
    public String validTo;

    @Column(name = "max_uses")
    public Integer maxUses;

    @Column(name = "used_count")
    public Integer usedCount;

    @Column(name = "min_order")
    public Double minOrder;

    @Column(name = "type")
    public String type;

    @Column(name = "value")
    public Double value;

    @Column(name = "created_at")
    public String createdAt;

    public static PromoCode findByCode(String pCode) {
        return find("code", pCode).firstResult();
    }
}
