package fr.beacrea.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import fr.beacrea.converter.JsonNodeConverter;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "products")
public class Product extends PanacheEntityBase {

    @Id
    public Long id;

    @Column(name = "name", nullable = false)
    public String name;

    @Column(name = "slug", nullable = false, unique = true)
    public String slug;

    @Column(name = "category", nullable = false)
    public String category;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    public BigDecimal price;

    @Column(name = "original_price", precision = 10, scale = 2)
    public BigDecimal originalPrice;

    @Column(name = "description", columnDefinition = "text")
    public String description;

    @Column(name = "long_description", columnDefinition = "text")
    public String longDescription;

    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "images", columnDefinition = "text")
    public JsonNode images;

    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "sizes", columnDefinition = "text")
    public JsonNode sizes;

    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "colors", columnDefinition = "text")
    public JsonNode colors;

    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "stock", columnDefinition = "text")
    public JsonNode stock;

    @Column(name = "rating")
    public double rating;

    @Column(name = "review_count")
    public int reviewCount;

    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "tags", columnDefinition = "text")
    public JsonNode tags;

    @Column(name = "featured")
    public boolean featured;

    @JsonProperty("new")
    @Column(name = "is_new")
    public boolean isNew;

    @Column(name = "badge")
    public String badge;

    public static Product findBySlug(String pSlug) {
        return find("slug", pSlug).firstResult();
    }

    public static List<Product> findByCategory(String pCategory) {
        return list("category", pCategory);
    }

    public static List<Product> findFeatured() {
        return list("featured", true);
    }
}
