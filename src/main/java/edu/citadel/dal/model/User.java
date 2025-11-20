package edu.citadel.dal.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String provider; // "github"
  private String providerId; // github id
  private String username; // github login
  private String displayName;
  private String email;
  private String avatarUrl;

  // getters and setters omitted for brevity
  // add equals/hashCode if needed
}
