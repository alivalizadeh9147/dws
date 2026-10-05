package ir.av.dws.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(implementation = UserRequest.class)
public record UserRequest(@NotNull String username, @NotNull String password) {
}
