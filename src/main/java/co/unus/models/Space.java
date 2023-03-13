package co.unus.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Space implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    private String name;

    @NotNull
    private String code;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private UnusUser admin;

    @Column(updatable = false)
    private LocalDateTime createdOn;

    @ManyToMany(mappedBy = "joinedSpaces")
    private Set<UnusUser> members;

    public Space(String name, String code, UnusUser admin) {
        this.name = name;
        this.admin = admin;
        this.code = code;
        this.createdOn = LocalDateTime.now();
    }

    public void addMember(UnusUser user) {
        members.add(user);
    }

    public void removeMember(UnusUser user) {
        members.remove(user);
    }
}