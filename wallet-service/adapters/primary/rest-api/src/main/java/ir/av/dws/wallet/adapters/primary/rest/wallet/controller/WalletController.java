package ir.av.dws.wallet.adapters.primary.rest.wallet.controller;

import ir.av.dws.wallet.adapters.primary.rest.base.BaseResponse;
import ir.av.dws.wallet.adapters.primary.rest.security.SecurityUtils;
import ir.av.dws.wallet.adapters.primary.rest.wallet.request.CreditWalletRestRequest;
import ir.av.dws.wallet.adapters.primary.rest.wallet.request.DebitWalletRestRequest;
import ir.av.dws.wallet.adapters.primary.rest.wallet.request.TransferWalletRestRequest;
import ir.av.dws.wallet.adapters.primary.rest.wallet.response.WalletDataRestResponse;
import ir.av.dws.wallet.application.delivery.WalletService;
import ir.av.dws.wallet.application.delivery.dto.WalletDto;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping()
    public ResponseEntity<?> openWallet() {
        UUID uuid = SecurityUtils.currentUserId();
        UUID walletId = walletService.openWallet(uuid);
        return ResponseEntity.created(URI.create("/wallets/".concat(walletId.toString()))).build();
    }

    @GetMapping()
    public ResponseEntity<BaseResponse<WalletDataRestResponse>> get() {
        UUID uuid = SecurityUtils.currentUserId();
        WalletDto walletDto = walletService.get(uuid);
        WalletDataRestResponse response = new WalletDataRestResponse();
        response.setWalletId(walletDto.id());
        response.setBalance(walletDto.balance());
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @PostMapping("/credit")
    public ResponseEntity<BaseResponse<?>> credit(@RequestBody CreditWalletRestRequest request,
                                                  @NotBlank @RequestHeader("Idempotency-Key") String idempotencyKey) {
        UUID uuid = SecurityUtils.currentUserId();
        walletService.credit(uuid, request.getAmount(), idempotencyKey);
        return ResponseEntity.ok(BaseResponse.defaultSuccess());
    }

    @PostMapping("/debit")
    public ResponseEntity<BaseResponse<?>> debit(@RequestBody DebitWalletRestRequest request,
                                                 @NotBlank @RequestHeader("Idempotency-Key") String idempotencyKey) {
        UUID uuid = SecurityUtils.currentUserId();
        walletService.debit(uuid, request.getAmount(), idempotencyKey);
        return ResponseEntity.ok(BaseResponse.defaultSuccess());
    }

    @PostMapping("/transfer")
    public ResponseEntity<BaseResponse<?>> transfer(@RequestBody TransferWalletRestRequest request,
                                                    @NotBlank @RequestHeader("Idempotency-Key") String idempotencyKey) {
        UUID uuid = SecurityUtils.currentUserId();
        walletService.transfer(uuid, request.getDestinationUserId(), request.getAmount(), idempotencyKey);
        return ResponseEntity.ok(BaseResponse.defaultSuccess());
    }

}
