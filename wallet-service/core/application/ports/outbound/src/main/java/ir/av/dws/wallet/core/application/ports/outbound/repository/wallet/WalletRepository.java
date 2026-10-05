package ir.av.dws.wallet.core.application.ports.outbound.repository.wallet;

import ir.av.dws.wallet.core.domain.wallet.entity.Wallet;
import ir.av.dws.wallet.core.domain.wallet.vo.WalletId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository {

    Optional<Wallet> findById(WalletId id);

    Optional<Wallet> findByUserId(UUID userId);

    Optional<Wallet> findForUpdateByUserId(UUID userId);

    Optional<Wallet> findForUpdate(WalletId id);

    List<Wallet> findAllForUpdate(Collection<UUID> ids);

    void updateBalance(Wallet wallet);

    void save(Wallet wallet);

    void deleteAll();
}
