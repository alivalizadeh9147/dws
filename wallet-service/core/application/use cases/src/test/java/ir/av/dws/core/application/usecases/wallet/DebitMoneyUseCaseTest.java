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

    UUID uuid = UUID.randomUUID();
    @BeforeEach
    void setUp() {
        useCase = new DebitMoneyUseCase(repository, eventPublisher);
    }

    @Test
    void shouldDebitMoneyFromWallet() {
        Wallet wallet = Wallet.open(uuid);
        Wallet credited = wallet.deposit(
                Money.of(new BigDecimal("500"))
        );

        when(repository.findForUpdateByUserId(uuid))
                .thenReturn(java.util.Optional.of(credited));

        DebitMoneyRequest request =
                new DebitMoneyRequest(
                        uuid,
                        new BigDecimal("100")
                );

        UseCaseResult<Void> result = useCase.execute(request);

        verify(repository).findForUpdateByUserId(
                uuid
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
        when(repository.findForUpdateByUserId(uuid))
                .thenReturn(java.util.Optional.empty());

        DebitMoneyRequest request =
                new DebitMoneyRequest(
                        uuid,
                        new BigDecimal("100")
                );

        assertThrows(
                WalletNotFoundException.class,
                () -> useCase.execute(request)
        );

        verify(repository).findForUpdateByUserId(
                uuid
        );

        verify(repository, never()).updateBalance(any());
    }

    @Test
    void shouldThrowWhenBalanceIsInsufficient() {
        Wallet wallet = Wallet.open(uuid);

        Wallet credited = wallet.deposit(
                Money.of(new BigDecimal("100"))
        );

        when(repository.findForUpdateByUserId(uuid))
                .thenReturn(java.util.Optional.of(credited));

        DebitMoneyRequest request =
                new DebitMoneyRequest(
                        uuid,
                        new BigDecimal("101")
                );

        assertThrows(
                InsufficientBalanceException.class,
                () -> useCase.execute(request)
        );

        verify(repository).findForUpdateByUserId(
                uuid
        );

        verify(repository, never()).updateBalance(any());
    }

    @Test
    void shouldDebitEntireBalance() {
        Wallet wallet = Wallet.open(uuid);

        Wallet credited = wallet.deposit(
                Money.of(new BigDecimal("100"))
        );

        when(repository.findForUpdateByUserId(uuid))
                .thenReturn(java.util.Optional.of(credited));

        DebitMoneyRequest request =
                new DebitMoneyRequest(
                        uuid,
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
        Wallet wallet = Wallet.open(uuid);

        when(repository.findForUpdateByUserId(uuid))
                .thenReturn(java.util.Optional.of(wallet));

        DebitMoneyRequest request =
                new DebitMoneyRequest(
                        uuid,
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
        Wallet wallet = Wallet.open(uuid);

        when(repository.findForUpdateByUserId(uuid))
                .thenReturn(java.util.Optional.of(wallet));

        DebitMoneyRequest request =
                new DebitMoneyRequest(
                        uuid,
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
        Wallet wallet = Wallet.open(uuid);

        Wallet credited = wallet.deposit(
                Money.of(new BigDecimal("500"))
        );

        when(repository.findForUpdateByUserId(uuid))
                .thenReturn(java.util.Optional.of(credited));

        DebitMoneyRequest request =
                new DebitMoneyRequest(
                        uuid,
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
        Wallet wallet = Wallet.open(uuid);

        Wallet credited = wallet.deposit(
                Money.of(new BigDecimal("500"))
        );

        when(repository.findForUpdateByUserId(uuid))
                .thenReturn(java.util.Optional.of(credited));

        DebitMoneyRequest request =
                new DebitMoneyRequest(
                        uuid,
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
        Wallet wallet = Wallet.open(uuid);

        Wallet credited = wallet.deposit(
                Money.of(new BigDecimal("500"))
        );

        when(repository.findForUpdateByUserId(uuid))
                .thenReturn(java.util.Optional.of(credited));

        DebitMoneyRequest request =
                new DebitMoneyRequest(
                        uuid,
                        new BigDecimal("100")
                );

        useCase.execute(request);

        var inOrder = inOrder(repository);

        inOrder.verify(repository)
                .findForUpdateByUserId(uuid);

        inOrder.verify(repository)
                .updateBalance(any(Wallet.class));
    }

    @Test
    void shouldNotUpdateRepositoryWhenDebitFails() {
        Wallet wallet = Wallet.open(uuid);

        Wallet credited = wallet.deposit(
                Money.of(new BigDecimal("100"))
        );

        when(repository.findForUpdateByUserId(uuid))
                .thenReturn(java.util.Optional.of(credited));

        DebitMoneyRequest request =
                new DebitMoneyRequest(
                        uuid,
                        new BigDecimal("200")
                );

        assertThrows(
                InsufficientBalanceException.class,
                () -> useCase.execute(request)
        );

        verify(repository, never()).updateBalance(any());
    }
}