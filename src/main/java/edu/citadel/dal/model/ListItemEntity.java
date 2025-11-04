package edu.citadel.dal.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "LIST_ITEM")
public class ListItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LIST_ITEM_ID", nullable = false)
    private Long id;

    @NotNull
    @JsonBackReference
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "LIST_ID", nullable = false)
    private ListEntity list;

    @Size(max = 50)
    @NotNull
    @ColumnDefault("'New List Item'")
    @Column(name = "LIST_ITEM_NAME", nullable = false, length = 50)
    private String listItemName = "New List Item";

    @Size(max = 255)
    @Column(name = "LIST_ITEM_DESC")
    private String listItemDesc;


}