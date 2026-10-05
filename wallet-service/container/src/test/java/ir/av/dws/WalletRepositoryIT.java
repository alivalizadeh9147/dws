package ir.av.dws;

import ir.av.dws.wallet.adapters.secondary.persistence.wallet.repository.WalletJpaRepository;
import ir.av.dws.wallet.container.WalletSpringBootApplication;
import ir.av.dws.wallet.core.application.ports.outbound.repository.wallet.WalletRepository;
import ir.av.dws.wallet.core.domain.shared.vo.Money;
import ir.av.dws.wallet.core.domain.wallet.entity.Wallet;
import ir.av.dws.wallet.core.domain.wallet.vo.WalletId;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = WalletSpringBootApplication.class)
@Testcontainers
public class WalletRepositoryIT {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("wallet_db")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {

        registry.add(
                "spring.datasource.url",
                postgres::getJdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                postgres::getUsername
        );

        registry.add(
                "spring.datasource.password",
                postgres::getPassword
        );

        registry.add(
                "spring.datasource.driver-class-name",
                postgres::getDriverClassName
        );
    }

    @Autowired
    private WalletRepository repository;

    @Autowired
    private WalletJpaRepository jpaRepository;

    @Autowired
    private EntityManager entityManager;

    UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        jpaRepository.deleteAll();
    }

    @Test
    public void should_save_and_find_wallet() {


        Wallet wallet = Wallet.open(userId);

        repository.save(wallet);

        Optional<Wallet> result =
                repository.findById(wallet.getId());

        Assertions.assertThat(result)
                .isPresent();

        Wallet found = result.orElseThrow();

        Assertions.assertThat(found.getId())
                .isEqualTo(wallet.getId());

        Assertions.assertThat(found.userId())
                .isEqualTo(userId);

        Assertions.assertThat(found.balance())
                .isEqualTo(Money.zero());
    }

    @Test
    @Transactional
    public void should_find_wallet_for_update() {

        Wallet wallet =
                Wallet.open(userId)
                        .deposit(
                                Money.of(BigDecimal.valueOf(1000))
                        );

        repository.save(wallet);

        Optional<Wallet> result =
                repository.findForUpdate(wallet.getId());

        Assertions.assertThat(result)
                .isPresent();

        Assertions.assertThat(result.orElseThrow().balance())
                .isEqualTo(
                        Money.of(BigDecimal.valueOf(1000))
                );
    }

    @Test
    @Transactional
    public void should_return_empty_when_wallet_does_not_exist() {

        Optional<Wallet> result =
                repository.findForUpdate(
                        new WalletId(UUID.randomUUID())
                );

        Assertions.assertThat(result)
                .isEmpty();
    }

    @Test
    @Transactional
    public void should_update_balance_multiple_times() {
        Wallet wallet = Wallet.open(userId);

        repository.save(wallet);

        Wallet first = wallet.deposit(
                Money.of(BigDecimal.valueOf(500))
        );

        repository.updateBalance(first);

        Wallet second = first.deposit(
                Money.of(BigDecimal.valueOf(800))
        );

        repository.updateBalance(second);

        entityManager.clear();

        Wallet actual = repository.findById(wallet.getId())
                .orElseThrow();

        assertThat(actual.balance())
                .isEqualTo(Money.of(BigDecimal.valueOf(1300)));
    }

    @Test
    @Transactional
    void should_update_wallet_balance() {

        Wallet wallet = Wallet.open(userId);

        repository.save(wallet);

        Wallet updated = wallet.deposit(
                Money.of(BigDecimal.valueOf(500))
        );

        repository.updateBalance(updated);

        entityManager.clear();

        Wallet actual = repository.findById(wallet.getId())
                .orElseThrow();

        assertThat(actual.balance())
                .isEqualTo(Money.of(BigDecimal.valueOf(500)));
    }
}