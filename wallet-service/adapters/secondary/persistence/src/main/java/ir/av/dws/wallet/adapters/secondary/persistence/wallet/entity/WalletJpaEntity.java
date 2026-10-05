package ir.av.dws.wallet.adapters.secondary.persistence.wallet.entity;

import ir.av.dws.wallet.adapters.secondary.persistence.base.JpaBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Table(name = "WALLET")
@Entity
@Setter
@Getter
public class WalletJpaEntity extends JpaBaseEntity {

    @Column(nullable = false,
            precision = 19,
            scale = 4)
    private BigDecimal balance;

    private UUID userId;
}
