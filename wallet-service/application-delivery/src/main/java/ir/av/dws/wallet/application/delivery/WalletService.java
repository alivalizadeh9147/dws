package ir.av.dws.wallet.application.delivery;

import ir.av.dws.wallet.application.delivery.dto.WalletDto;

import java.util.UUID;

public interface WalletService {

    UUID openWallet(UUID userId);

    void credit(UUID userId,
                long amount,
                String transactionId);

    void debit(UUID userId,
               long amount,
               String transactionId);

    void transfer(UUID sourceUserId,
                  UUID destinationUserId,
                  long amount,
                  String transactionId);

    WalletDto get(UUID walletId);
}
