package ir.av.dws.core.application.usecases.wallet;

import ir.av.dws.wallet.core.application.ports.inbound.base.UseCaseResult;
import ir.av.dws.wallet.core.application.ports.inbound.wallet.DebitMoneyRequest;
import ir.av.dws.wallet.core.application.ports.outbound.eventpublisher.EventPublisher;
import ir.av.dws.wallet.core.application.ports.outbound.repository.wallet.WalletRepository;
import ir.av.dws.wallet.core.application.usecases.wallet.DebitMoneyUseCase;
import ir.av.dws.wallet.core.application.usecases.wallet.exception.WalletNotFoundException;
import ir.av.dws.wallet.core.domain.shared.exception.InsufficientBalanceException;
import ir.av.dws.wallet.core.domain.shared.vo.Money;
import ir.av.dws.wallet.core.domain.wallet.entity.Wallet;
import ir.av.dws.wallet.core.domain.wallet.event.DebitMoneyEvent;
import ir.av.dws.wallet.core.domain.wallet.vo.WalletId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DebitMoneyUseCaseTest {

    @Mock
    private WalletRepository repository;

    @Mock
    private EventPublisher eventPublisher;

    private DebitMoneyUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DebitMoneyUseCase(repository, eventPublisher);
    }

    @Test
    void shouldDebitMoneyFromWallet() {
        UUID walletId = UUID.randomUUID();

        Wallet wallet = Wallet.open("Ali");
        Wallet credited = wallet.deposit(
                Money.of(new BigDecimal("500"))
        );

        when(repository.findForUpdate(new WalletId(walletId)))
                .thenReturn(java.util.Optional.of(credited));

        DebitMoneyRequest request =
                new DebitMoneyRequest(
                        walletId,
                        new BigDecimal("100")
                );

        UseCaseResult<Void> result = useCase.execute(request);

        verify(repository).findForUpdate(
                new WalletId(walletId)
        );

        verify(repository).updateBalance(
                argThat(updated ->
                        updated.balance().equals(
                                Money.of(new BigDecimal("400"))
                        )
                )
        );

        assertNotNull(result);
    }

    @Test
    void shouldThrowWhenWalletDoesNotExist() {
        UUID walletId = UUID.randomUUID();

        when(repository.findForUpdate(new WalletId(walletId)))
                .thenReturn(java.util.Optional.empty());

        DebitMoneyRequest request =
                new DebitMoneyRequest(
                        walletId,
                        new BigDecimal("100")
                );

        assertThrows(
                WalletNotFoundException.class,
                () -> useCase.execute(request)
        );

        verify(repository).findForUpdate(
                new WalletId(walletId)
        );

        verify(repository, never()).updateBalance(any());
    }

    @Test
    void shouldThrowWhenBalanceIsInsufficient() {
        UUID walletId = UUID.randomUUID();

        Wallet wallet = Wallet.open("Ali");

        Wallet credited = wallet.deposit(
                Money.of(new BigDecimal("100"))
        );

        when(repository.findForUpdate(new WalletId(walletId)))
                .thenReturn(java.util.Optional.of(credited));

        DebitMoneyRequest request =
                new DebitMoneyRequest(
                        walletId,
                        new BigDecimal("101")
                );

        assertThrows(
                InsufficientBalanceException.class,
                () -> useCase.execute(request)
        );

        verify(repository).findForUpdate(
                new WalletId(walletId)
        );

        verify(repository, never()).updateBalance(any());
    }

    @Test
    void shouldDebitEntireBalance() {
        UUID walletId = UUID.randomUUID();

        Wallet wallet = Wallet.open("Ali");

        Wallet credited = wallet.deposit(
                Money.of(new BigDecimal("100"))
        );

        when(repository.findForUpdate(new WalletId(walletId)))
                .thenReturn(java.util.Optional.of(credited));

        DebitMoneyRequest request =
                new DebitMoneyRequest(
                        walletId,
                        new BigDecimal("100")
                );

        useCase.execute(request);

        verify(repository).updateBalance(
                argThat(updated ->
                        updated.balance().equals(Money.zero())
                )
        );
    }

    @Test
    void shouldThrowWhenAmountIsZero() {
        UUID walletId = UUID.randomUUID();

        Wallet wallet = Wallet.open("Ali");

        when(repository.findForUpdate(new WalletId(walletId)))
                .thenReturn(java.util.Optional.of(wallet));

        DebitMoneyRequest request =
                new DebitMoneyRequest(
                        walletId,
                        BigDecimal.ZERO
                );

        assertThrows(
                RuntimeException.class,
                () -> useCase.execute(request)
        );

        verify(repository, never()).updateBalance(any());
    }

    @Test
    void shouldThrowWhenAmountIsNegative() {
        UUID walletId = UUID.randomUUID();

        Wallet wallet = Wallet.open("Ali");

        when(repository.findForUpdate(new WalletId(walletId)))
                .thenReturn(java.util.Optional.of(wallet));

        DebitMoneyRequest request =
                new DebitMoneyRequest(
                        walletId,
                        new BigDecimal("-100")
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(request)
        );

        verify(repository, never()).updateBalance(any());
    }

    @Test
    void shouldReturnDebitDomainEvent() {
        UUID walletId = UUID.randomUUID();

        Wallet wallet = Wallet.open("Ali");

        Wallet credited = wallet.deposit(
                Money.of(new BigDecimal("500"))
        );

        when(repository.findForUpdate(new WalletId(walletId)))
                .thenReturn(java.util.Optional.of(credited));

        DebitMoneyRequest request =
                new DebitMoneyRequest(
                        walletId,
                        new BigDecimal("100")
                );

        UseCaseResult<Void> result = useCase.execute(request);

        assertNotNull(result);
        assertNotNull(result.events());
        assertEquals(1, result.events().size());

        assertInstanceOf(
                DebitMoneyEvent.class,
                result.events().getFirst()
        );
    }

    @Test
    void shouldUpdateRepositoryWithWithdrawnWallet() {
        UUID walletId = UUID.randomUUID();

        Wallet wallet = Wallet.open("Ali");

        Wallet credited = wallet.deposit(
                Money.of(new BigDecimal("500"))
        );

        when(repository.findForUpdate(new WalletId(walletId)))
                .thenReturn(java.util.Optional.of(credited));

        DebitMoneyRequest request =
                new DebitMoneyRequest(
                        walletId,
                        new BigDecimal("150")
                );

        useCase.execute(request);

        verify(repository).updateBalance(
                argThat(updated ->
                        updated.balance().equals(
                                Money.of(new BigDecimal("350"))
                        )
                )
        );
    }

    @Test
    void shouldFindWalletForUpdateBeforeUpdatingBalance() {
        UUID walletId = UUID.randomUUID();

        Wallet wallet = Wallet.open("Ali");

        Wallet credited = wallet.deposit(
                Money.of(new BigDecimal("500"))
        );

        when(repository.findForUpdate(new WalletId(walletId)))
                .thenReturn(java.util.Optional.of(credited));

        DebitMoneyRequest request =
                new DebitMoneyRequest(
                        walletId,
                        new BigDecimal("100")
                );

        useCase.execute(request);

        var inOrder = inOrder(repository);

        inOrder.verify(repository)
                .findForUpdate(new WalletId(walletId));

        inOrder.verify(repository)
                .updateBalance(any(Wallet.class));
    }

    @Test
    void shouldNotUpdateRepositoryWhenDebitFails() {
        UUID walletId = UUID.randomUUID();

        Wallet wallet = Wallet.open("Ali");

        Wallet credited = wallet.deposit(
                Money.of(new BigDecimal("100"))
        );

        when(repository.findForUpdate(new WalletId(walletId)))
                .thenReturn(java.util.Optional.of(credited));

        DebitMoneyRequest request =
                new DebitMoneyRequest(
                        walletId,
                        new BigDecimal("200")
                );

        assertThrows(
                InsufficientBalanceException.class,
                () -> useCase.execute(request)
        );

        verify(repository, never()).updateBalance(any());
    }
}