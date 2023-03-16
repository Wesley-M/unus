package co.unus.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnusUserOutputDTO {
    @NotNull
    private Long id;
    @NotNull
    private String name;
}
