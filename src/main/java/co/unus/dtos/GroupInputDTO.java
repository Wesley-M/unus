package co.unus.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupInputDTO {
    @NotNull(message = "Group name should not be null")
    @NotBlank(message = "Group name should not be blank")
    String name;

    @NotNull(message = "Space code should not be null")
    @NotBlank(message = "Space code should not be blank")
    String spaceCode;

    Boolean isOpen = false;
}
