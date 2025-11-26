package edu.citadel.dal.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Embeddable
public class Login {
    @Column(name = "login_id")
    private Long loginId;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_provider")
    private LoginProvider loginProvider;

}
