package co.unus.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupOutputDTO {
    Long id;
    String name;
    Boolean isOpen;
    LocalDateTime createdOn;
}
