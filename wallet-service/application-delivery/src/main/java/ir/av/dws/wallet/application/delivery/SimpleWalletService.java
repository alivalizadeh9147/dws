package ir.av.dws.wallet.application.delivery;

import ir.av.dws.wallet.application.delivery.dto.WalletDto;
import ir.av.dws.wallet.application.idempotency.IdempotencyService;
import ir.av.dws.wallet.application.queue.WalletOperationQueue;
import ir.av.dws.wallet.core.application.ports.inbound.wallet.CreditMoneyRequest;
import ir.av.dws.wallet.core.application.ports.inbound.wallet.DebitMoneyRequest;
import ir.av.dws.wallet.core.application.ports.inbound.wallet.OpenWalletRequest;
import ir.av.dws.wallet.core.application.ports.inbound.wallet.TransferMoneyRequest;
import ir.av.dws.wallet.core.application.ports.inbound.wallet.response.WalletCreatedResponse;
import ir.av.dws.wallet.core.application.ports.inbound.base.UseCaseResult;
import ir.av.dws.wallet.core.application.ports.outbound.repository.wallet.WalletRepository;
import ir.av.dws.wallet.core.application.usecases.wallet.CreditMoneyUseCase;
import ir.av.dws.wallet.core.application.usecases.wallet.DebitMoneyUseCase;
import ir.av.dws.wallet.core.application.usecases.wallet.OpenWalletUseCase;
import ir.av.dws.wallet.core.application.usecases.wallet.TransferMoneyUseCase;
import ir.av.dws.wallet.core.application.usecases.wallet.exception.WalletNotFoundException;
import ir.av.dws.wallet.core.domain.wallet.vo.WalletId;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
public class SimpleWalletService implements WalletService {

    private final WalletOperationQueue queue;

    private final WalletRepository walletRepository;
    private final OpenWalletUseCase openWalletUseCase;
    private final CreditMoneyUseCase creditMoneyUseCase;
    private final DebitMoneyUseCase debitMoneyUseCase;
    private final TransferMoneyUseCase transferMoneyUseCase;
    private final IdempotencyService idempotencyService;

    public SimpleWalletService(
            WalletOperationQueue queue,
            OpenWalletUseCase openWalletUseCase,
            CreditMoneyUseCase creditMoneyUseCase,
            DebitMoneyUseCase debitMoneyUseCase,
            TransferMoneyUseCase transferMoneyUseCase,
            WalletRepository walletRepository,
            IdempotencyService idempotencyService
    ) {
        this.queue = queue;
        this.openWalletUseCase = openWalletUseCase;
        this.creditMoneyUseCase = creditMoneyUseCase;
        this.debitMoneyUseCase = debitMoneyUseCase;
        this.transferMoneyUseCase = transferMoneyUseCase;
        this.walletRepository = walletRepository;
        this.idempotencyService = idempotencyService;
    }

    @Override
    public UUID openWallet(String name) {

        UseCaseResult<WalletCreatedResponse> result =
                openWalletUseCase.execute(
                        new OpenWalletRequest(name)
                );

        return result.data().walletId();
    }

    @Override
    public void credit(
            UUID walletId,
            long amount,
            String idempotencyKey
    ) {
        CompletableFuture<Void> future =
                queue.submit(() -> {
                            idempotencyService.execute(
                                    idempotencyKey,
                                    () -> creditMoneyUseCase.execute(
                                            new CreditMoneyRequest(
                                                    walletId,
                                                    BigDecimal.valueOf(amount)
                                            )
                                    )
                            );
                            return null;
                        }
                );
        future.join();

    }

    @Override
    public void debit(
            UUID walletId,
            long amount,
            String idempotencyKey
    ) {
        CompletableFuture<Void> future =
                queue.submit(() -> {
                    idempotencyService.execute(
                            idempotencyKey,
                            () -> debitMoneyUseCase.execute(
                                    new DebitMoneyRequest(
                                            walletId,
                                            BigDecimal.valueOf(amount)
                                    )
                            )
                    );
                    return null;
                });
        future.join();
    }

    @Override
    public void transfer(
            UUID sourceWalletId,
            UUID destinationWalletId,
            long amount,
            String idempotencyKey
    ) {

        CompletableFuture<Void> future =
                queue.submit(() -> {
                            idempotencyService.execute(
                                    idempotencyKey,
                                    () -> transferMoneyUseCase.execute(
                                            new TransferMoneyRequest(
                                                    sourceWalletId,
                                                    destinationWalletId,
                                                    BigDecimal.valueOf(amount)
                                            )
                                    )
                            );
                            return null;
                        }
                );
        future.join();

    }

    @Override
    public WalletDto get(UUID walletId) {

        return walletRepository.findById(
                        new WalletId(walletId)
                )
                .map(wallet ->
                        new WalletDto(
                                walletId,
                                wallet.name(),
                                wallet.balance().amount()
                        )
                )
                .orElseThrow(() ->
                        new WalletNotFoundException(
                                "Wallet with id '%s' not found"
                                        .formatted(walletId)
                        )
                );
    }
}