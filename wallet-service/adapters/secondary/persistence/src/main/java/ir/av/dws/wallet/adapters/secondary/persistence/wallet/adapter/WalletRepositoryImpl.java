package ir.av.dws.wallet.adapters.secondary.persistence.wallet.adapter;

import ir.av.dws.wallet.adapters.secondary.persistence.wallet.entity.WalletJpaEntity;
import ir.av.dws.wallet.adapters.secondary.persistence.wallet.mapper.WalletJpaMapper;
import ir.av.dws.wallet.adapters.secondary.persistence.wallet.repository.WalletJpaRepository;
import ir.av.dws.wallet.core.application.ports.outbound.repository.wallet.WalletRepository;
import ir.av.dws.wallet.core.domain.wallet.entity.Wallet;
import ir.av.dws.wallet.core.domain.wallet.vo.WalletId;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class WalletRepositoryImpl implements WalletRepository {

    private final WalletJpaRepository jpaRepository;
    private final WalletJpaMapper mapper;

    public WalletRepositoryImpl(WalletJpaRepository jpaRepository,
                                WalletJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Wallet> findById(WalletId id) {
        return jpaRepository.findById(id.value()).map(mapper::map);
    }

    @Override
    public Optional<Wallet> findForUpdate(WalletId id) {
        return jpaRepository.findByIdForUpdate(id.value()).map(mapper::map);
    }

    @Override
    public List<Wallet> findAllForUpdate(Collection<WalletId> ids) {
        List<UUID> walletsId = ids.stream()
                .map(WalletId::value)
                .toList();

        return jpaRepository
                .findAllByIdForUpdate(walletsId)
                .stream()
                .map(mapper::map)
                .toList();
    }

    @Override
    public void updateBalance(Wallet wallet) {
        int updated = jpaRepository.updateBalance(
                wallet.getId().value(),
                wallet.balance().amount()
        );

        if (updated != 1) {
            throw new IllegalStateException(
                    "Wallet was not updated: " + wallet.getId().value()
            );
        }
    }

    @Override
    public void save(Wallet wallet) {
        WalletJpaEntity map = mapper.map(wallet);
        jpaRepository.saveAndFlush(map);
    }

    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
    }
}
