package co.unus.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
public class SpaceBaseDTO {
    String name;
    String code;
    LocalDateTime createdOn;
}
