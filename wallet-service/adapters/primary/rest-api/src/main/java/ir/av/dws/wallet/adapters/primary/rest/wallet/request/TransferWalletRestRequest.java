package ir.av.dws.wallet.adapters.primary.rest.wallet.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Schema
@Setter
@Getter
public class TransferWalletRestRequest {

    @NotNull
    private UUID destinationWalletId;
    @Positive
    private long amount;
}
