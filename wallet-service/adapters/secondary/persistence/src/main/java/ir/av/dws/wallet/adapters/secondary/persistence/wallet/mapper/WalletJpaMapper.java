package ir.av.dws.wallet.adapters.secondary.persistence.wallet.mapper;

import ir.av.dws.wallet.adapters.secondary.persistence.wallet.entity.WalletJpaEntity;
import ir.av.dws.wallet.core.domain.wallet.entity.Wallet;
import ir.av.dws.wallet.core.domain.wallet.vo.WalletId;
import ir.av.dws.wallet.core.domain.shared.vo.Money;
import org.springframework.stereotype.Component;

@Component
public class WalletJpaMapper {
    public Wallet map(WalletJpaEntity entity) {
        return Wallet.builder()
                .id(new WalletId(entity.getId()))
                .balance(new Money(entity.getBalance()))
                .userId(entity.getUserId())
                .build();
    }

    public WalletJpaEntity map(Wallet wallet) {
        WalletJpaEntity entity = new WalletJpaEntity();
        entity.setId(wallet.getId().value());
        entity.setBalance(wallet.balance().amount());
        entity.setUserId(wallet.userId());
        return entity;
    }
}
