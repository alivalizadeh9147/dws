package ir.av.dws.core.application.usecases.wallet;

import ir.av.dws.wallet.core.application.ports.inbound.wallet.CreditMoneyRequest;
import ir.av.dws.wallet.core.application.ports.inbound.base.UseCaseResult;
import ir.av.dws.wallet.core.application.ports.outbound.eventpublisher.EventPublisher;
import ir.av.dws.wallet.core.application.ports.outbound.repository.wallet.WalletRepository;
import ir.av.dws.wallet.core.application.usecases.wallet.CreditMoneyUseCase;
import ir.av.dws.wallet.core.application.usecases.wallet.exception.WalletNotFoundException;
import ir.av.dws.wallet.core.domain.wallet.entity.Wallet;
import ir.av.dws.wallet.core.domain.wallet.event.DepositedMoneyEvent;
import ir.av.dws.wallet.core.domain.wallet.vo.WalletId;
import ir.av.dws.wallet.core.domain.shared.vo.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditMoneyUseCaseTest {

    @Mock
    private WalletRepository repository;

    @Mock
    private EventPublisher eventPublisher;

    private CreditMoneyUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreditMoneyUseCase(repository, eventPublisher);
    }

    @Test
    void shouldCreditMoneyToWallet() {
        UUID walletId = UUID.randomUUID();

        Wallet wallet = Wallet.open("Ali");

        when(repository.findForUpdate(new WalletId(walletId)))
                .thenReturn(Optional.of(wallet));

        CreditMoneyRequest request =
                new CreditMoneyRequest(
                        walletId,
                        new BigDecimal("100")
                );

        UseCaseResult<Void> result =
                useCase.execute(request);

        verify(repository).findForUpdate(
                new WalletId(walletId)
        );

        verify(repository).updateBalance(
                argThat(updated ->
                        updated.balance().equals(
                                Money.of(new BigDecimal("100"))
                        )
                )
        );

        assertNotNull(result);
    }

    @Test
    void shouldThrowWhenWalletDoesNotExist() {
        UUID walletId = UUID.randomUUID();

        when(repository.findForUpdate(new WalletId(walletId)))
                .thenReturn(Optional.empty());

        CreditMoneyRequest request =
                new CreditMoneyRequest(
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
    void shouldThrowWhenAmountIsZero() {
        UUID walletId = UUID.randomUUID();

        Wallet wallet = Wallet.open("Ali");

        when(repository.findForUpdate(new WalletId(walletId)))
                .thenReturn(Optional.of(wallet));

        CreditMoneyRequest request =
                new CreditMoneyRequest(
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
                .thenReturn(Optional.of(wallet));

        CreditMoneyRequest request =
                new CreditMoneyRequest(
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
    void shouldNotUpdateRepositoryWhenDomainOperationFails() {
        UUID walletId = UUID.randomUUID();

        Wallet wallet = Wallet.open("Ali");

        when(repository.findForUpdate(new WalletId(walletId)))
                .thenReturn(Optional.of(wallet));

        CreditMoneyRequest request =
                new CreditMoneyRequest(
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
    void shouldReturnDomainEvents() {
        UUID walletId = UUID.randomUUID();

        Wallet wallet = Wallet.open("Ali");

        when(repository.findForUpdate(new WalletId(walletId)))
                .thenReturn(Optional.of(wallet));

        CreditMoneyRequest request =
                new CreditMoneyRequest(
                        walletId,
                        new BigDecimal("100")
                );

        UseCaseResult<Void> result =
                useCase.execute(request);

        assertNotNull(result);
        assertNotNull(result.events());

        assertEquals(1, result.events().size());

        assertInstanceOf(
                DepositedMoneyEvent.class,
                result.events().getFirst()
        );
    }

    @Test
    void shouldUpdateRepositoryWithCreditedWallet() {
        UUID walletId = UUID.randomUUID();

        Wallet wallet = Wallet.open("Ali");

        when(repository.findForUpdate(new WalletId(walletId)))
                .thenReturn(Optional.of(wallet));

        CreditMoneyRequest request =
                new CreditMoneyRequest(
                        walletId,
                        new BigDecimal("250.75")
                );

        useCase.execute(request);

        verify(repository).updateBalance(
                argThat(updated ->
                        updated.getId().equals(wallet.getId())
                                && updated.balance().equals(
                                Money.of(new BigDecimal("250.75"))
                        )
                )
        );
    }

    @Test
    void shouldFindWalletForUpdateBeforeUpdatingBalance() {
        UUID walletId = UUID.randomUUID();

        Wallet wallet = Wallet.open("Ali");

        when(repository.findForUpdate(new WalletId(walletId)))
                .thenReturn(Optional.of(wallet));

        CreditMoneyRequest request =
                new CreditMoneyRequest(
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
}