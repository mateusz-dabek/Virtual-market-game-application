package project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordDTO {

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotNull
    private String newPassword;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotNull
    private String oldPassword;
}
