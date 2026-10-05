package ir.av.dws.wallet.adapters.secondary.persistence.wallet.repository;

import ir.av.dws.wallet.adapters.secondary.persistence.wallet.entity.WalletJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletJpaRepository extends JpaRepository<WalletJpaEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select a
        from WalletJpaEntity a
        where a.id = :id
    """)
    Optional<WalletJpaEntity> findByIdForUpdate(
            @Param("id") UUID id
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select a
        from WalletJpaEntity a
        where a.userId in :userIds
        order by a.id
    """)
    List<WalletJpaEntity> findAllByUserIdForUpdate(
            @Param("userIds") Collection<UUID> ids
    );

    @Modifying
    @Query("""
    update WalletJpaEntity a
       set a.balance = :balance,
           a.version = a.version + 1
     where a.id = :id
""")
    int updateBalance(
            @Param("id") UUID id,
            @Param("balance") BigDecimal balance
    );

    Optional<WalletJpaEntity> findByUserId(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select a
        from WalletJpaEntity a
        where a.userId = :userId
    """)
    Optional<WalletJpaEntity> findForUpdateByUserId(@Param("userId") UUID userId);
}
