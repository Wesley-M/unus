package co.unus.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SpaceSimpleDTO(
        @NotNull(message = "Space name should not be null")
        @NotBlank(message = "Space name should not be blank")
        String name
) { }
