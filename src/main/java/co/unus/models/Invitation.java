package co.unus.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Entity
public class Invitation {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private UnusUser source;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private UnusUser target;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private Group group;

    private Boolean sentByAdmin = false;

    public Invitation(UnusUser source, UnusUser target, Group group, Boolean sentByAdmin) {
        this.source = source;
        this.target = target;
        this.group = group;
        this.sentByAdmin = sentByAdmin;
    }
}
