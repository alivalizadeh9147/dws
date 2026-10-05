package ir.av.dws.wallet.core.application.usecases.wallet;

import ir.av.dws.wallet.core.application.ports.inbound.base.UseCaseResult;
import ir.av.dws.wallet.core.application.ports.inbound.wallet.CreditMoneyRequest;
import ir.av.dws.wallet.core.application.ports.outbound.eventpublisher.EventPublisher;
import ir.av.dws.wallet.core.application.ports.outbound.repository.wallet.WalletRepository;
import ir.av.dws.wallet.core.application.usecases.base.UseCase;
import ir.av.dws.wallet.core.application.usecases.wallet.exception.WalletNotFoundException;
import ir.av.dws.wallet.core.domain.shared.event.DomainEvent;
import ir.av.dws.wallet.core.domain.shared.vo.Money;
import ir.av.dws.wallet.core.domain.wallet.entity.Wallet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CreditMoneyUseCase implements UseCase<CreditMoneyRequest, Void> {

    private final WalletRepository repository;
    private final EventPublisher eventPublisher;

    public CreditMoneyUseCase(WalletRepository repository,
                              EventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public UseCaseResult<Void> execute(CreditMoneyRequest request) {
        Wallet wallet = repository.findForUpdateByUserId(
                request.userId()
        ).orElseThrow(() ->
                new WalletNotFoundException(
                        "Wallet not found"
                )
        );

        Wallet credited = wallet.deposit(
                Money.of(request.amount())
        );

        repository.updateBalance(credited);

        List<DomainEvent<?>> domainEvents = credited.pullDomainEvents();

        eventPublisher.publishAll(domainEvents);

        return UseCaseResult.of(domainEvents);
    }
}
