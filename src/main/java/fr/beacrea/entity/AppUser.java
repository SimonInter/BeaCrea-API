package fr.beacrea.entity;

import com.fasterxml.jackson.databind.JsonNode;
import fr.beacrea.converter.JsonNodeConverter;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class AppUser extends PanacheEntityBase {

    @Id
    @Column(name = "id")
    public String id;

    @Column(name = "email", nullable = false, unique = true)
    public String email;

    @Column(name = "password", nullable = false)
    public String password;

    @Column(name = "first_name")
    public String firstName;

    @Column(name = "last_name")
    public String lastName;

    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "addresses", columnDefinition = "text")
    public JsonNode addresses;

    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "wishlist", columnDefinition = "text")
    public JsonNode wishlist;

    @Column(name = "created_at")
    public String createdAt;

    public static AppUser findByEmail(String pEmail) {
        return find("email", pEmail).firstResult();
    }
}
