package ir.av.dws.wallet.core.application.usecases.wallet;

import ir.av.dws.wallet.core.application.ports.inbound.base.UseCaseResult;
import ir.av.dws.wallet.core.application.ports.inbound.wallet.TransferMoneyRequest;
import ir.av.dws.wallet.core.application.ports.outbound.eventpublisher.EventPublisher;
import ir.av.dws.wallet.core.application.ports.outbound.repository.wallet.WalletRepository;
import ir.av.dws.wallet.core.application.usecases.base.UseCase;
import ir.av.dws.wallet.core.application.usecases.wallet.exception.WalletNotFoundException;
import ir.av.dws.wallet.core.domain.shared.event.DomainEvent;
import ir.av.dws.wallet.core.domain.shared.vo.Money;
import ir.av.dws.wallet.core.domain.wallet.entity.Wallet;
import ir.av.dws.wallet.core.domain.wallet.event.TransferredMoneyEvent;
import ir.av.dws.wallet.core.domain.wallet.exception.InvalidWalletOperationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TransferMoneyUseCase implements UseCase<TransferMoneyRequest, Void> {

    private final WalletRepository repository;
    private final EventPublisher eventPublisher;

    public TransferMoneyUseCase(WalletRepository repository,
                                EventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public UseCaseResult<Void> execute(TransferMoneyRequest request) {
        if (request.sourceUserId().equals(request.destinationUserId())) {
            throw new InvalidWalletOperationException(
                    "Source and destination wallets must be different"
            );
        }

        Money amount = Money.of(request.amount());

        List<UUID> lockOrder = Stream
                .of(request.sourceUserId(), request.destinationUserId())
                .sorted(Comparator.naturalOrder())
                .toList();

        Map<UUID, Wallet> wallets =
                repository.findAllForUpdate(lockOrder)
                        .stream()
                        .collect(Collectors.toMap(
                                Wallet::userId,
                                Function.identity()
                        ));

        Wallet source = Optional
                .ofNullable(wallets.get(request.sourceUserId()))
                .orElseThrow(() ->
                        new WalletNotFoundException(
                                "Source wallet not found"
                        )
                );

        Wallet destination = Optional
                .ofNullable(wallets.get(request.destinationUserId()))
                .orElseThrow(() ->
                        new WalletNotFoundException(
                                "Destination not found"
                        )
                );

        Wallet withdrawn = source.debit(amount);

        Wallet deposited = destination.deposit(amount);

        repository.updateBalance(withdrawn);
        repository.updateBalance(deposited);

        List<DomainEvent<?>> events = new ArrayList<>();
        TransferredMoneyEvent.Payload payload = new TransferredMoneyEvent.Payload(
                source.getId().value(), source.userId(), destination.getId().value(), destination.userId(), amount.amount().toPlainString()
        );
        TransferredMoneyEvent debitMoneyEvent = new TransferredMoneyEvent(
                UUID.randomUUID(), Instant.now(), payload);

        events.addAll(withdrawn.pullDomainEvents());
        events.addAll(deposited.pullDomainEvents());
        events.add(debitMoneyEvent);

        eventPublisher.publishAll(events);

        return UseCaseResult.of(events);
    }
}
