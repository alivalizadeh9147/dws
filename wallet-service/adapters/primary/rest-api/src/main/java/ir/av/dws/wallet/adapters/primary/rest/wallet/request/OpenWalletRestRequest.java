package ir.av.dws.wallet.adapters.primary.rest.wallet.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Schema
@Setter
@Getter
public class OpenWalletRestRequest {

    private UUID userId;
}
