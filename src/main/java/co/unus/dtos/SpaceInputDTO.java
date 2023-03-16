package co.unus.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpaceInputDTO {
    @NotNull(message = "Space name should not be null")
    @NotBlank(message = "Space name should not be blank")
    String name;

    Boolean isPublic;
}
