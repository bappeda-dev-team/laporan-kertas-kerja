package cc.kertaskerja.laporan.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EncryptDTO {

    @NotBlank(message = "Payload tidak boleh kosong")
    private String data;
}

