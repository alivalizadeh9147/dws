package ir.av.dws.wallet.adapters.primary.rest.wallet.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema
@Setter
@Getter
public class OpenWalletRestRequest {

    private String name;
}
