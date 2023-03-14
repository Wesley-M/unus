package co.unus.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpaceOutputDTO {
    String name;
    String code;
    Boolean isPublic;
    LocalDateTime createdOn;
}
