package co.unus.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvitationInputDTO {
    @NotNull(message="Source email should not be null")
    @NotBlank(message="Source email should not be blank")
    private String sourceEmail;

    @NotNull(message="Target email should not be null")
    @NotBlank(message="Target email should not be blank")
    private String targetEmail;

    @NotNull(message="Group id should not be null")
    private Long groupId;
}
