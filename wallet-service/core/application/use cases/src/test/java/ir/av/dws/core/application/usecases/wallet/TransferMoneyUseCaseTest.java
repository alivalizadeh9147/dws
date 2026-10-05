package ir.av.dws.core.application.usecases.wallet;

import ir.av.dws.wallet.core.application.ports.inbound.wallet.TransferMoneyRequest;
import ir.av.dws.wallet.core.application.ports.inbound.base.UseCaseResult;
import ir.av.dws.wallet.core.application.ports.outbound.eventpublisher.EventPublisher;
import ir.av.dws.wallet.core.application.ports.outbound.repository.wallet.WalletRepository;
import ir.av.dws.wallet.core.application.usecases.wallet.TransferMoneyUseCase;
import ir.av.dws.wallet.core.application.usecases.wallet.exception.WalletNotFoundException;
import ir.av.dws.wallet.core.domain.wallet.entity.Wallet;
import ir.av.dws.wallet.core.domain.wallet.exception.InvalidWalletOperationException;
import ir.av.dws.wallet.core.domain.wallet.vo.WalletId;
import ir.av.dws.wallet.core.domain.shared.exception.InsufficientBalanceException;
import ir.av.dws.wallet.core.domain.shared.vo.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferMoneyUseCaseTest {

    @Mock
    private WalletRepository repository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private TransferMoneyUseCase useCase;

    UUID userId = UUID.randomUUID();
    UUID sourceUserId = UUID.randomUUID();
    UUID destinationUserId = UUID.randomUUID();

    @Test
    void should_transfer_money_successfully() {
        Wallet source = Wallet.open(sourceUserId)
                .deposit(Money.of(BigDecimal.valueOf(500)));

        Wallet destination = Wallet.open(destinationUserId);

        UUID sourceId = source.userId();
        UUID destinationId = destination.userId();

        when(repository.findAllForUpdate(anyList()))
                .thenReturn(List.of(source, destination));
        mock(EventPublisher.class);
        TransferMoneyRequest request = new TransferMoneyRequest(
                sourceId,
                destinationId,
                BigDecimal.valueOf(100)
        );

        UseCaseResult<Void> result = useCase.execute(request);

        assertThat(result).isNotNull();
        assertThat(result.events()).hasSize(3);

        ArgumentCaptor<Wallet> walletCaptor =
                ArgumentCaptor.forClass(Wallet.class);

        verify(repository, times(2))
                .updateBalance(walletCaptor.capture());

        List<Wallet> updatedWallets = walletCaptor.getAllValues();

        Wallet updatedSource = updatedWallets.stream()
                .filter(wallet -> wallet.userId().equals(sourceId))
                .findFirst()
                .orElseThrow();

        Wallet updatedDestination = updatedWallets.stream()
                .filter(wallet -> wallet.userId().equals(destinationId))
                .findFirst()
                .orElseThrow();

        assertThat(updatedSource.balance())
                .isEqualTo(Money.of(BigDecimal.valueOf(400)));

        assertThat(updatedDestination.balance())
                .isEqualTo(Money.of(BigDecimal.valueOf(100)));
    }

    @Test
    void should_reject_transfer_between_same_wallet() {
        Wallet wallet = Wallet.open(userId);

        UUID walletId = wallet.getId().value();

        TransferMoneyRequest request = new TransferMoneyRequest(
                walletId,
                walletId,
                BigDecimal.valueOf(100)
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InvalidWalletOperationException.class)
                .hasMessage("Source and destination wallets must be different");

        verifyNoInteractions(repository);
    }

    @Test
    void should_throw_when_source_wallet_not_found() {
        UUID sourceId = UUID.randomUUID();
        UUID destinationId = UUID.randomUUID();

        when(repository.findAllForUpdate(anyList()))
                .thenReturn(List.of());

        TransferMoneyRequest request = new TransferMoneyRequest(
                sourceId,
                destinationId,
                BigDecimal.valueOf(100)
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(WalletNotFoundException.class)
                .hasMessageContaining("Source wallet");

        verify(repository).findAllForUpdate(anyList());
        verify(repository, never()).updateBalance(any());
    }

    @Test
    void should_throw_when_destination_wallet_not_found() {
        Wallet source = Wallet.open(sourceUserId);

        UUID sourceId = source.userId();
        UUID destinationId = UUID.randomUUID();

        when(repository.findAllForUpdate(anyList()))
                .thenReturn(List.of(source));

        TransferMoneyRequest request = new TransferMoneyRequest(
                sourceId,
                destinationId,
                BigDecimal.valueOf(100)
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(WalletNotFoundException.class);

        verify(repository).findAllForUpdate(anyList());
        verify(repository, never()).updateBalance(any());
    }

    @Test
    void should_throw_when_source_has_insufficient_balance() {
        Wallet source = Wallet.open(sourceUserId);
        Wallet destination = Wallet.open(destinationUserId);

        UUID sourceId = source.userId();
        UUID destinationId = destination.userId();

        when(repository.findAllForUpdate(anyList()))
                .thenReturn(List.of(source, destination));

        TransferMoneyRequest request = new TransferMoneyRequest(
                sourceId,
                destinationId,
                BigDecimal.valueOf(100)
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InsufficientBalanceException.class);

        verify(repository).findAllForUpdate(anyList());
        verify(repository, never()).updateBalance(any());
    }

    @Test
    void should_lock_wallets_in_deterministic_order() {
        Wallet source = Wallet.open(sourceUserId);
        Wallet destination = Wallet.open(destinationUserId);

        UUID sourceId = source.userId();
        UUID destinationId = destination.userId();

        when(repository.findAllForUpdate(anyList()))
                .thenReturn(List.of(source, destination));

        TransferMoneyRequest request = new TransferMoneyRequest(
                sourceId,
                destinationId,
                BigDecimal.valueOf(100)
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InsufficientBalanceException.class);

        ArgumentCaptor<List<UUID>> captor =
                ArgumentCaptor.forClass(List.class);

        verify(repository).findAllForUpdate(captor.capture());

        List<UUID> lockedIds = captor.getValue();

        assertThat(lockedIds)
                .containsExactly(
                        lockedIds.get(0),
                        lockedIds.get(1)
                );

        assertThat(lockedIds.get(0))
                .isLessThan(lockedIds.get(1));
    }

    @Test
    void should_update_both_wallets_exactly_once() {
        Wallet source = Wallet.open(sourceUserId)
                .deposit(Money.of(BigDecimal.valueOf(1000)));

        Wallet destination = Wallet.open(destinationUserId);

        when(repository.findAllForUpdate(anyList()))
                .thenReturn(List.of(source, destination));

        TransferMoneyRequest request = new TransferMoneyRequest(
                source.userId(),
                destination.userId(),
                BigDecimal.valueOf(250)
        );

        useCase.execute(request);

        verify(repository, times(2))
                .updateBalance(any(Wallet.class));
    }

    @Test
    void should_return_two_domain_events_after_successful_transfer() {
        Wallet source = Wallet.open(sourceUserId)
                .deposit(Money.of(BigDecimal.valueOf(500)));

        Wallet destination = Wallet.open(destinationUserId);

        when(repository.findAllForUpdate(anyList()))
                .thenReturn(List.of(source, destination));

        TransferMoneyRequest request = new TransferMoneyRequest(
                source.userId(),
                destination.userId(),
                BigDecimal.valueOf(100)
        );

        UseCaseResult<Void> result = useCase.execute(request);

        assertThat(result.events())
                .hasSize(3);

        assertThat(result.events())
                .allSatisfy(event -> assertThat(event).isNotNull());
    }

    @Test
    void should_not_update_any_wallet_when_debit_fails() {
        Wallet source = Wallet.open(sourceUserId);
        Wallet destination = Wallet.open(destinationUserId);

        when(repository.findAllForUpdate(anyList()))
                .thenReturn(List.of(source, destination));

        TransferMoneyRequest request = new TransferMoneyRequest(
                source.userId(),
                destination.userId(),
                BigDecimal.valueOf(100)
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InsufficientBalanceException.class);

        verify(repository, never())
                .updateBalance(any(Wallet.class));
    }

    @Test
    void should_find_all_wallets_for_update_before_modifying_them() {
        Wallet source = Wallet.open(sourceUserId)
                .deposit(Money.of(BigDecimal.valueOf(500)));

        Wallet destination = Wallet.open(destinationUserId);

        when(repository.findAllForUpdate(anyList()))
                .thenReturn(List.of(source, destination));

        TransferMoneyRequest request = new TransferMoneyRequest(
                source.userId(),
                destination.userId(),
                BigDecimal.valueOf(100)
        );

        useCase.execute(request);

        InOrder inOrder = inOrder(repository);

        inOrder.verify(repository)
                .findAllForUpdate(anyList());

        inOrder.verify(repository, times(2))
                .updateBalance(any(Wallet.class));
    }
}