package ir.av.dws.wallet.adapters.primary.rest.wallet.controller;

import ir.av.dws.wallet.adapters.primary.rest.wallet.request.CreditWalletRestRequest;
import ir.av.dws.wallet.adapters.primary.rest.wallet.request.DebitWalletRestRequest;
import ir.av.dws.wallet.adapters.primary.rest.wallet.request.OpenWalletRestRequest;
import ir.av.dws.wallet.adapters.primary.rest.wallet.request.TransferWalletRestRequest;
import ir.av.dws.wallet.adapters.primary.rest.wallet.response.WalletDataRestResponse;
import ir.av.dws.wallet.adapters.primary.rest.base.BaseResponse;
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
    public ResponseEntity<?> openWallet(@RequestBody OpenWalletRestRequest request) {
        UUID walletId = walletService.openWallet(request.getName());
        return ResponseEntity.created(URI.create("/wallets/".concat(walletId.toString()))).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<WalletDataRestResponse>> get(@PathVariable("id") UUID id) {
        WalletDto walletDto = walletService.get(id);
        WalletDataRestResponse response = new WalletDataRestResponse();
        response.setWalletId(id);
        response.setName(walletDto.name());
        response.setBalance(walletDto.balance());
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @PostMapping("/{id}/credit")
    public ResponseEntity<BaseResponse<?>> credit(@PathVariable("id") UUID id,
                                                  @RequestBody CreditWalletRestRequest request,
                                                  @NotBlank @RequestHeader("Idempotency-Key") String idempotencyKey) {
        walletService.credit(id, request.getAmount(), idempotencyKey);
        return ResponseEntity.ok(BaseResponse.defaultSuccess());
    }

    @PostMapping("/{id}/debit")
    public ResponseEntity<BaseResponse<?>> debit(@PathVariable("id") UUID id,
                                                 @RequestBody DebitWalletRestRequest request,
                                                 @NotBlank @RequestHeader("Idempotency-Key") String idempotencyKey) {
        walletService.debit(id, request.getAmount(), idempotencyKey);
        return ResponseEntity.ok(BaseResponse.defaultSuccess());
    }

    @PostMapping("/{id}/transfer")
    public ResponseEntity<BaseResponse<?>> transfer(@PathVariable("id") UUID id,
                                                    @RequestBody TransferWalletRestRequest request,
                                                    @NotBlank @RequestHeader("Idempotency-Key") String idempotencyKey) {
        walletService.transfer(id, request.getDestinationWalletId(), request.getAmount(), idempotencyKey);
        return ResponseEntity.ok(BaseResponse.defaultSuccess());
    }

}
