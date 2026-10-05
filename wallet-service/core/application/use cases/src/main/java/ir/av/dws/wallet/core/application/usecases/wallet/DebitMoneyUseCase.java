package ir.av.dws.wallet.core.application.usecases.wallet;

import ir.av.dws.wallet.core.application.ports.inbound.wallet.DebitMoneyRequest;
import ir.av.dws.wallet.core.application.ports.inbound.base.UseCaseResult;
import ir.av.dws.wallet.core.application.ports.outbound.eventpublisher.EventPublisher;
import ir.av.dws.wallet.core.application.ports.outbound.repository.wallet.WalletRepository;
import ir.av.dws.wallet.core.application.usecases.wallet.exception.WalletNotFoundException;
import ir.av.dws.wallet.core.application.usecases.base.UseCase;
import ir.av.dws.wallet.core.domain.shared.event.DomainEvent;
import ir.av.dws.wallet.core.domain.wallet.entity.Wallet;
import ir.av.dws.wallet.core.domain.wallet.vo.WalletId;
import ir.av.dws.wallet.core.domain.shared.vo.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DebitMoneyUseCase implements UseCase<DebitMoneyRequest, Void> {

    private final WalletRepository repository;
    private final EventPublisher eventPublisher;

    public DebitMoneyUseCase(WalletRepository repository,
                             EventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public UseCaseResult<Void> execute(DebitMoneyRequest request) {
        Wallet wallet = repository.findForUpdate(
                new WalletId(request.walletId())
        ).orElseThrow(() ->
                new WalletNotFoundException(
                        "Wallet with id '%s' not found"
                                .formatted(request.walletId())
                )
        );

        Wallet withdrawn = wallet.debit(
                Money.of(request.amount())
        );

        repository.updateBalance(withdrawn);

        List<DomainEvent<?>> domainEvents = withdrawn.pullDomainEvents();
        eventPublisher.publishAll(domainEvents);

        return UseCaseResult.of(domainEvents);
    }
}
