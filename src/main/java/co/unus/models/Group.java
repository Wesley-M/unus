package co.unus.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Entity
@Table(name = "UNUS_GROUP")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotNull
    private String name;

    @NotNull
    private Boolean isOpen = false;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private UnusUser admin;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private Space space;

    @Column(updatable = false)
    private LocalDateTime createdOn;

    @ManyToMany
    @JoinTable(name = "GROUP_USER",
            joinColumns = { @JoinColumn(name = "GROUP_ID") },
            inverseJoinColumns = { @JoinColumn(name = "USER_ID") })
    private Set<UnusUser> members = new HashSet<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Invitation> invitations = new HashSet<>();

    public Group(String name, Boolean isOpen, Space space, UnusUser admin) {
        this.name = name;
        this.isOpen = isOpen;
        this.space = space;
        this.admin = admin;
        this.createdOn = LocalDateTime.now();
    }

    public void removeInvitation(Invitation invitation) {
        invitations.remove(invitation);
    }

    public void addMember(UnusUser user) {
        members.add(user);
    }

    public void removeMember(UnusUser user) {
        members.remove(user);
    }
}
