package ir.av.dws.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public record TokenResponse(@Schema String access_token, @Schema String token_type) {
}
