package edu.citadel.dal.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "shares")
public class ShareEntity {
    @Id
    @UuidGenerator
    @Column(name = "share_id", nullable = false)
    private UUID share_id;

    @NotNull
    @Column(name = "list_id", nullable = false)
    private Long list_id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @Nullable
    @JsonBackReference
    private Account user;

    @Transient
    private String username = null;

    @NotNull
    @Column(name = "expiry_time", nullable = false)
    private Timestamp expiry_time;

    @NotNull
    @CreationTimestamp
    @Column(name = "created_on", nullable = false)
    private Timestamp created_on;

    @PrePersist
    protected void onCreate() {
        if (expiry_time == null) {
            // Set the expiry time to 10 minutes from now
            expiry_time = new Timestamp(System.currentTimeMillis() + (10 * 60 * 1000));
        }
    }
}
