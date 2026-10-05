package ir.av.dws.wallet.core.application.usecases.wallet;

import ir.av.dws.wallet.core.application.ports.inbound.wallet.OpenWalletRequest;
import ir.av.dws.wallet.core.application.ports.inbound.wallet.response.WalletCreatedResponse;
import ir.av.dws.wallet.core.application.ports.inbound.base.UseCaseResult;
import ir.av.dws.wallet.core.application.ports.outbound.eventpublisher.EventPublisher;
import ir.av.dws.wallet.core.application.ports.outbound.repository.wallet.WalletRepository;
import ir.av.dws.wallet.core.application.usecases.base.UseCase;
import ir.av.dws.wallet.core.domain.shared.event.DomainEvent;
import ir.av.dws.wallet.core.domain.wallet.entity.Wallet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OpenWalletUseCase implements UseCase<OpenWalletRequest, WalletCreatedResponse> {

    private final WalletRepository repository;
    private final EventPublisher eventPublisher;

    public OpenWalletUseCase(WalletRepository repository,
                             EventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public UseCaseResult<WalletCreatedResponse> execute(OpenWalletRequest request) {
        Wallet wallet = Wallet.open(request.name());

        List<DomainEvent<?>> domainEvents = wallet.pullDomainEvents();

        eventPublisher.publishAll(domainEvents);

        repository.save(wallet);

        WalletCreatedResponse response = new WalletCreatedResponse(wallet.getId().value(), wallet.name());

        return new UseCaseResult<>(response, domainEvents);
    }
}
