package co.unus.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(exclude = "groups")
@Entity
public class Space implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotNull
    private String name;

    private Boolean isPublic = false;

    @NotNull
    private String code;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private UnusUser admin;

    @Column(updatable = false)
    private LocalDateTime createdOn;

    @OneToMany(mappedBy = "space", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Group> groups = new HashSet<>();

    @ManyToMany(mappedBy = "joinedSpaces")
    private Set<UnusUser> members = new HashSet<>();

    public Space(String name, String code, Boolean isPublic, UnusUser admin) {
        this.name = name;
        this.admin = admin;
        this.code = code;
        this.isPublic = isPublic;
        this.createdOn = LocalDateTime.now();
    }

    public void addMember(UnusUser user) {
        members.add(user);
    }

    public void removeMember(UnusUser user) {
        members.remove(user);
    }

    public void addGroup(Group group) {
        groups.add(group);
    }

    public void removeGroup(Group group) {
        groups.remove(group);
    }
}