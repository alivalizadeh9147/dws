package ir.av.dws.wallet.adapters.primary.rest.wallet.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Schema
@Setter
@Getter
public class DebitWalletRestRequest {

    @Positive
    private long amount;
}
