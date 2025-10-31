package edu.citadel.dal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "LIST")
public class ListEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LIST_ID", nullable = false)
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "TITLE", nullable = false, length = 50)
    private String title = "New List";

    @NotNull
    @CreationTimestamp
    @Column(name = "CREATED_ON", nullable = false)
    private Timestamp createdOn;

    @Column(name = "COMPLETED_ON")
    private Instant completedOn;

    @OneToMany(mappedBy = "list")
    private Set<ListItemEntity> listItems = new LinkedHashSet<>();


}