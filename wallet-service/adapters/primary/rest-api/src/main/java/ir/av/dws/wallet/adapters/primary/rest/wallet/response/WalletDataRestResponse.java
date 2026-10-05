package ir.av.dws.wallet.adapters.primary.rest.wallet.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Schema
@Setter
@Getter
public class WalletDataRestResponse {

    private UUID walletId;
    private BigDecimal balance;
    private String name;
}
