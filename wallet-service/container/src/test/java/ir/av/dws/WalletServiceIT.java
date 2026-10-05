package ir.av.dws;

import ir.av.dws.wallet.application.delivery.WalletService;
import ir.av.dws.wallet.application.delivery.dto.WalletDto;
import ir.av.dws.wallet.container.WalletSpringBootApplication;
import ir.av.dws.wallet.core.application.ports.outbound.repository.wallet.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = WalletSpringBootApplication.class)
@Testcontainers
class WalletServiceIT {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>("redis:7-alpine")
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(
            DynamicPropertyRegistry registry
    ) {

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

        registry.add(
                "spring.data.redis.host",
                redis::getHost
        );

        registry.add(
                "spring.data.redis.port",
                () -> redis.getMappedPort(6379)
        );
    }

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    private UUID userId = UUID.randomUUID();

    @Test
    void should_open_wallet() {

        UUID walletId =
                walletService.openWallet(userId);

        assertThat(walletId)
                .isNotNull();

        WalletDto wallet =
                walletService.get(walletId);

        assertThat(wallet.id())
                .isEqualTo(walletId);

        assertThat(wallet.userId())
                .isEqualTo(userId);

        assertThat(wallet.balance())
                .isEqualByComparingTo("0");
    }

    @Test
    void should_get_wallet() {

        UUID walletId =
                walletService.openWallet(userId);

        WalletDto wallet =
                walletService.get(walletId);

        assertThat(wallet.id())
                .isEqualTo(walletId);

        assertThat(wallet.userId())
                .isEqualTo(userId);

        assertThat(wallet.balance())
                .isEqualByComparingTo("0");
    }

    @Test
    void should_credit_wallet() {

        UUID walletId =
                walletService.openWallet(userId);

        String idempotencyKey =
                UUID.randomUUID().toString();

        walletService.credit(
                walletId,
                1000,
                idempotencyKey
        );

        WalletDto wallet =
                walletService.get(walletId);

        assertThat(wallet.balance())
                .isEqualByComparingTo("1000");
    }

    @Test
    void should_debit_wallet() {

        UUID walletId =
                walletService.openWallet(userId);

        walletService.credit(
                walletId,
                1000,
                UUID.randomUUID().toString()
        );

        walletService.debit(
                walletId,
                400,
                UUID.randomUUID().toString()
        );

        WalletDto wallet =
                walletService.get(walletId);

        assertThat(wallet.balance())
                .isEqualByComparingTo("600");
    }

    private UUID sourceUserId = UUID.randomUUID();
    private UUID destinationUserId = UUID.randomUUID();

    @Test
    void should_transfer_money() {

        UUID source =
                walletService.openWallet(sourceUserId);

        UUID destination =
                walletService.openWallet(destinationUserId);

        walletService.credit(
                source,
                1000,
                UUID.randomUUID().toString()
        );

        walletService.transfer(
                source,
                destination,
                400,
                UUID.randomUUID().toString()
        );

        WalletDto sourceWallet =
                walletService.get(source);

        WalletDto destinationWallet =
                walletService.get(destination);

        assertThat(sourceWallet.balance())
                .isEqualByComparingTo("600");

        assertThat(destinationWallet.balance())
                .isEqualByComparingTo("400");
    }

    @Test
    void should_not_credit_twice_with_same_idempotency_key() {

        UUID walletId =
                walletService.openWallet(userId);

        String key =
                UUID.randomUUID().toString();

        walletService.credit(
                walletId,
                1000,
                key
        );

        assertThatThrownBy(() ->
                walletService.credit(
                        walletId,
                        1000,
                        key
                )
        )
                .isInstanceOf(
                        CompletionException.class
                );

        WalletDto wallet =
                walletService.get(walletId);

        assertThat(wallet.balance())
                .isEqualByComparingTo("1000");
    }

    @Test
    void should_execute_operations_with_different_idempotency_keys() {

        UUID walletId =
                walletService.openWallet(userId);

        walletService.credit(
                walletId,
                1000,
                UUID.randomUUID().toString()
        );

        walletService.credit(
                walletId,
                500,
                UUID.randomUUID().toString()
        );

        WalletDto wallet =
                walletService.get(walletId);

        assertThat(wallet.balance())
                .isEqualByComparingTo("1500");
    }

    @Test
    void should_not_transfer_twice_with_same_idempotency_key() {

        UUID source =
                walletService.openWallet(sourceUserId);

        UUID destination =
                walletService.openWallet(destinationUserId);

        walletService.credit(
                source,
                1000,
                UUID.randomUUID().toString()
        );

        String key =
                UUID.randomUUID().toString();

        walletService.transfer(
                source,
                destination,
                400,
                key
        );

        assertThatThrownBy(() ->
                walletService.transfer(
                        source,
                        destination,
                        400,
                        key
                )
        )
                .isInstanceOf(
                        CompletionException.class
                );

        WalletDto sourceWallet =
                walletService.get(source);

        WalletDto destinationWallet =
                walletService.get(destination);

        assertThat(sourceWallet.balance())
                .isEqualByComparingTo("600");

        assertThat(destinationWallet.balance())
                .isEqualByComparingTo("400");
    }

    @Test
    void should_reject_debit_when_balance_is_insufficient() {

        UUID walletId =
                walletService.openWallet(userId);

        walletService.credit(
                walletId,
                500,
                UUID.randomUUID().toString()
        );

        assertThatThrownBy(() ->
                walletService.debit(
                        walletId,
                        1000,
                        UUID.randomUUID().toString()
                )
        )
                .isInstanceOf(
                        CompletionException.class
                );

        WalletDto wallet =
                walletService.get(walletId);

        assertThat(wallet.balance())
                .isEqualByComparingTo("500");
    }

    @Test
    void should_not_change_balances_when_transfer_fails() {

        UUID source =
                walletService.openWallet(sourceUserId);

        UUID destination =
                walletService.openWallet(destinationUserId);

        walletService.credit(
                source,
                500,
                UUID.randomUUID().toString()
        );

        assertThatThrownBy(() ->
                walletService.transfer(
                        source,
                        destination,
                        1000,
                        UUID.randomUUID().toString()
                )
        )
                .isInstanceOf(
                        CompletionException.class
                );

        WalletDto sourceWallet =
                walletService.get(source);

        WalletDto destinationWallet =
                walletService.get(destination);

        assertThat(sourceWallet.balance())
                .isEqualByComparingTo("500");

        assertThat(destinationWallet.balance())
                .isEqualByComparingTo("0");
    }
}
