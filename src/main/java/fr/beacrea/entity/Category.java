package fr.beacrea.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category extends PanacheEntityBase {

    @Id
    @Column(name = "id")
    public String id;

    @Column(name = "name", nullable = false)
    public String name;

    @Column(name = "icon")
    public String icon;

    @Column(name = "count")
    public int count;
}
