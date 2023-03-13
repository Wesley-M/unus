package co.unus.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "UNUS_USER")
public class UnusUser implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Email
    private String email;

    @NotNull
    private String password;

    @NotNull
    private String name;

    @Past
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate birthDate;

    @ManyToMany
    @JoinTable(
            name = "user_joined_space",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "space_id"))
    private Set<Space> joinedSpaces = new HashSet<>();

    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Space> administeredSpaces = new HashSet<>();

    public UnusUser(String email, String password, String name, @Past LocalDate birthDate) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.birthDate = birthDate;
    }

    public void joinSpaceAsAdmin(Space space) {
        administeredSpaces.add(space);
    }

    public void leaveSpaceAsAdmin(Space space) {
        administeredSpaces.remove(space);
    }

    public void joinSpace(Space space) {
        joinedSpaces.add(space);
        space.addMember(this);
    }

    public void leaveSpace(Space space) {
        joinedSpaces.remove(space);
        space.removeMember(this);
    }

    public Set<Space> getJoinedSpaces() {
        return Collections.unmodifiableSet(joinedSpaces);
    }

    public Set<Space> getAdministeredSpaces() {
        return Collections.unmodifiableSet(administeredSpaces);
    }
}