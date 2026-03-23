package fr.beacrea.entity;

import com.fasterxml.jackson.databind.JsonNode;
import fr.beacrea.converter.JsonNodeConverter;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "custom_orders")
public class CustomOrder extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "user_id", nullable = false)
    public String userId;

    @Column(name = "status", nullable = false)
    public String status = "received";

    @Column(name = "description", columnDefinition = "text")
    public String description;

    @Column(name = "notes", columnDefinition = "text")
    public String notes;

    @Column(name = "estimated_budget")
    public Double estimatedBudget;

    @Column(name = "requested_date")
    public String requestedDate;

    // Tableau d'URLs d'images (JSON)
    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "images", columnDefinition = "text")
    public JsonNode images;

    // Tableau de messages { id, from, authorName, content, createdAt } (JSON)
    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "messages", columnDefinition = "text")
    public JsonNode messages;

    // Devis { amount, description, validUntil } (JSON)
    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "quote", columnDefinition = "text")
    public JsonNode quote;

    @Column(name = "created_at")
    public String createdAt;

    public static List<CustomOrder> findByUserId(String pUserId) {
        return list("userId", pUserId);
    }
}
