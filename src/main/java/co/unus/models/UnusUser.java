package co.unus.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "UNUS_USER")
public class UnusUser implements Serializable {
    @Id
    @Email
    @Column(name = "EMAIL", nullable=false)
    private String email;

    @Column(name = "PASSWORD")
    @NotNull
    private String password;

    @Column(name = "NAME", length=255)
    @NotNull
    private String name;

    @Column(name = "BIRTHDATE")
    private LocalDate birthDate;
}