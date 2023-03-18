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

    public Invitation(UnusUser source, UnusUser target, Group group) {
        this.source = source;
        this.target = target;
        this.group = group;
    }
}
