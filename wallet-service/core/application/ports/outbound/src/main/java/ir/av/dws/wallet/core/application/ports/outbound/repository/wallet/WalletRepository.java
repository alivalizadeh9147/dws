package ir.av.dws.wallet.core.application.ports.outbound.repository.wallet;

import ir.av.dws.wallet.core.domain.wallet.entity.Wallet;
import ir.av.dws.wallet.core.domain.wallet.vo.WalletId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface WalletRepository {

    Optional<Wallet> findById(WalletId id);

    Optional<Wallet> findForUpdate(WalletId id);

    List<Wallet> findAllForUpdate(Collection<WalletId> ids);

    void updateBalance(Wallet wallet);

    void save(Wallet wallet);

    void deleteAll();
}
