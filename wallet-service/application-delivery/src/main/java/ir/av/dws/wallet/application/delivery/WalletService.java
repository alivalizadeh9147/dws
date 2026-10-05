package ir.av.dws.wallet.application.delivery;

import ir.av.dws.wallet.application.delivery.dto.WalletDto;

import java.util.UUID;

public interface WalletService {

    UUID openWallet(String name);

    void credit(UUID walletId,
                long amount,
                String transactionId);

    void debit(UUID walletId,
               long amount,
               String transactionId);

    void transfer(UUID sourceWalletId,
                  UUID destinationWalletId,
                  long amount,
                  String transactionId);

    WalletDto get(UUID walletId);
}
