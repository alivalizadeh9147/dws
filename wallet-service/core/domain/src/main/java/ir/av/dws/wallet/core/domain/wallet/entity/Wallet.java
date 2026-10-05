package ir.av.dws.wallet.core.domain.wallet.entity;

import ir.av.dws.wallet.core.domain.shared.entity.AggregateRoot;
import ir.av.dws.wallet.core.domain.shared.exception.InsufficientBalanceException;
import ir.av.dws.wallet.core.domain.shared.vo.Money;
import ir.av.dws.wallet.core.domain.wallet.event.DebitMoneyEvent;
import ir.av.dws.wallet.core.domain.wallet.event.DepositedMoneyEvent;
import ir.av.dws.wallet.core.domain.wallet.event.WalletOpenedEvent;
import ir.av.dws.wallet.core.domain.wallet.exception.InvalidWalletOperationException;
import ir.av.dws.wallet.core.domain.wallet.vo.WalletId;

import java.time.Instant;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

public final class Wallet extends AggregateRoot<WalletId> {

    private final Money balance;
    private final UUID userId;

    private Wallet(
            WalletId walletId,
            Money balance,
            UUID userId
    ) {
        super(walletId);

        this.balance = requireNonNull(
                balance,
                "Balance must not be null"
        );
        this.userId = requireNonNull(userId);
    }

    private static String requireValidName(String name) {
        requireNonNull(name, "Name must not be null");

        if (name.isBlank()) {
            throw new InvalidWalletOperationException(
                    "Name must not be blank"
            );
        }

        return name;
    }

    public static Wallet open(
            UUID userId
    ) {
        Wallet wallet = new Wallet(
                WalletId.open(),
                Money.zero(),
                userId
        );
        WalletOpenedEvent.Payload payload = new WalletOpenedEvent.Payload();
        WalletOpenedEvent walletOpenedEvent = new WalletOpenedEvent(
                UUID.randomUUID(), Instant.now(), payload);
        wallet.addEvent(walletOpenedEvent);
        return wallet;
    }

    public Wallet deposit(Money amount) {
        requirePositiveAmount(amount);

        Money add = balance.add(amount);

        Wallet build = copyBuilder()
                .balance(add)
                .build();
        DepositedMoneyEvent.Payload payload = new DepositedMoneyEvent.Payload(
                getId().value(), userId, amount.amount().toPlainString()
        );
        DepositedMoneyEvent depositedMoneyEvent = new DepositedMoneyEvent(
                UUID.randomUUID(), Instant.now(), payload);
        build.addEvent(depositedMoneyEvent);
        return build;
    }

    public Wallet debit(Money amount) {
        requirePositiveAmount(amount);

        if (balance.isLessThan(amount)) {
            throw new InsufficientBalanceException(
                    getId()
            );
        }

        Wallet build = copyBuilder()
                .balance(balance.subtract(amount))
                .build();

        DebitMoneyEvent.Payload payload = new DebitMoneyEvent.Payload(
                getId().value(), userId, amount.amount().toPlainString()
        );
        DebitMoneyEvent debitMoneyEvent = new DebitMoneyEvent(
                UUID.randomUUID(), Instant.now(), payload);
        build.addEvent(debitMoneyEvent);
        return build;
    }

    public Money balance() {
        return balance;
    }

    public UUID userId() {
        return userId;
    }

    private static void requirePositiveAmount(Money amount) {
        requireNonNull(
                amount,
                "Amount must not be null"
        );

        if (amount.isZero()) {
            throw new InvalidWalletOperationException(
                    "Amount must be greater than zero"
            );
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private WalletId id;
        private Money balance;
        private UUID userId;

        private Builder() {
        }

        public Builder id(WalletId walletId) {
            this.id = walletId;
            return this;
        }

        public Builder balance(Money balance) {
            this.balance = balance;
            return this;
        }

        public Builder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Wallet build() {
            return new Wallet(
                    id,
                    balance,
                    userId
            );
        }
    }

    private Builder copyBuilder() {
        return new Builder()
                .id(getId())
                .balance(balance)
                .userId(userId)
                ;
    }

    @Override
    public String toString() {
        return "Wallet{" +
                "balance=" + balance +
                ", userId='" + userId + '\'' +
                '}';
    }
}