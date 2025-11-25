package edu.citadel.dal.model;

import java.sql.Timestamp;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
//  private String password;
  private String email;
  @CreationTimestamp
  private Timestamp created_on;
  @CreationTimestamp
  private Timestamp last_login;
}
