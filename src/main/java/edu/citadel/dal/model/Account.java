package edu.citadel.dal.model;

import java.sql.Timestamp;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

@Data
@Entity
@Table(name = "accounts")
public class Account {
  @Id
  @GeneratedValue(strategy= GenerationType.IDENTITY)
  private Long user_id;
  private String username;
  private String email;
  @CreationTimestamp
  private Timestamp created_on;
  @CreationTimestamp
  private Timestamp last_login;
  @Embedded
  private Login login;
}
