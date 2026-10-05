package ir.av.dws.wallet.core.application.ports.inbound.base;

import ir.av.dws.wallet.core.domain.shared.event.DomainEvent;

import java.util.List;

public record UseCaseResult<T>(T data,
                               List<DomainEvent<?>> events) {

    public static <T> UseCaseResult<T> of(List<DomainEvent<?>> events) {
        return new UseCaseResult<>(null, events);
    }
}
