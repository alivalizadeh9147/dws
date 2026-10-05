package ir.av.dws.core.application.usecases.wallet;

import ir.av.dws.wallet.core.application.ports.inbound.wallet.OpenWalletRequest;
import ir.av.dws.wallet.core.application.ports.inbound.wallet.response.WalletCreatedResponse;
import ir.av.dws.wallet.core.application.ports.inbound.base.UseCaseResult;
import ir.av.dws.wallet.core.application.ports.outbound.eventpublisher.EventPublisher;
import ir.av.dws.wallet.core.application.ports.outbound.repository.wallet.WalletRepository;
import ir.av.dws.wallet.core.application.usecases.wallet.OpenWalletUseCase;
import ir.av.dws.wallet.core.domain.wallet.entity.Wallet;
import ir.av.dws.wallet.core.domain.wallet.event.WalletOpenedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpenWalletUseCaseTest {

    @Mock
    private WalletRepository repository;

    @Mock
    private EventPublisher eventPublisher;

    private OpenWalletUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new OpenWalletUseCase(repository, eventPublisher);
    }

    @Test
    void shouldOpenWalletSuccessfully() {
        OpenWalletRequest request =
                new OpenWalletRequest("Ali");

        UseCaseResult<WalletCreatedResponse> result =
                useCase.execute(request);

        assertNotNull(result);
        assertNotNull(result.data());

        assertEquals(
                "Ali",
                result.data().name()
        );

        assertNotNull(
                result.data().walletId()
        );

        verify(repository).save(any(Wallet.class));
    }

    @Test
    void shouldSaveOpenedWallet() {
        OpenWalletRequest request =
                new OpenWalletRequest("Ali");

        useCase.execute(request);

        ArgumentCaptor<Wallet> captor =
                ArgumentCaptor.forClass(Wallet.class);

        verify(repository).save(captor.capture());

        Wallet savedWallet = captor.getValue();

        assertNotNull(savedWallet);
        assertNotNull(savedWallet.getId());

        assertEquals(
                "Ali",
                savedWallet.name()
        );

        assertEquals(
                0,
                savedWallet.balance().amount().signum()
        );
    }

    @Test
    void shouldReturnCreatedWalletId() {
        OpenWalletRequest request =
                new OpenWalletRequest("Ali");

        UseCaseResult<WalletCreatedResponse> result =
                useCase.execute(request);

        ArgumentCaptor<Wallet> captor =
                ArgumentCaptor.forClass(Wallet.class);

        verify(repository).save(captor.capture());

        Wallet savedWallet = captor.getValue();

        assertEquals(
                savedWallet.getId().value(),
                result.data().walletId()
        );
    }

    @Test
    void shouldReturnWalletName() {
        OpenWalletRequest request =
                new OpenWalletRequest("My Wallet");

        UseCaseResult<WalletCreatedResponse> result =
                useCase.execute(request);

        assertEquals(
                "My Wallet",
                result.data().name()
        );
    }

    @Test
    void shouldReturnWalletOpenedEvent() {
        OpenWalletRequest request =
                new OpenWalletRequest("Ali");

        UseCaseResult<WalletCreatedResponse> result =
                useCase.execute(request);

        assertNotNull(result.events());

        assertEquals(
                1,
                result.events().size()
        );

        assertInstanceOf(
                WalletOpenedEvent.class,
                result.events().getFirst()
        );
    }

    @Test
    void shouldNotReturnNullEvents() {
        OpenWalletRequest request =
                new OpenWalletRequest("Ali");

        UseCaseResult<WalletCreatedResponse> result =
                useCase.execute(request);

        assertNotNull(result.events());
    }

    @Test
    void shouldSaveOnlyOnce() {
        OpenWalletRequest request =
                new OpenWalletRequest("Ali");

        useCase.execute(request);

        verify(repository, times(1))
                .save(any(Wallet.class));
    }
}