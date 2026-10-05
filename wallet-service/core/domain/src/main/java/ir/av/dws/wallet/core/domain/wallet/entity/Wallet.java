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
import java.util.Objects;
import java.util.UUID;

public final class Wallet extends AggregateRoot<WalletId> {

    private final Money balance;
    private final String name;

    private Wallet(
            WalletId walletId,
            Money balance,
            String name
    ) {
        super(walletId);

        this.balance = Objects.requireNonNull(
                balance,
                "Balance must not be null"
        );
        this.name = requireValidName(name);
    }

    private static String requireValidName(String name) {
        Objects.requireNonNull(name, "Name must not be null");

        if (name.isBlank()) {
            throw new InvalidWalletOperationException(
                    "Name must not be blank"
            );
        }

        return name;
    }

    public static Wallet open(
            String name
    ) {
        Wallet wallet = new Wallet(
                WalletId.open(),
                Money.zero(),
                name
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
                getId().value(), name, amount.amount().toPlainString()
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
                getId().value(), name, amount.amount().toPlainString()
        );
        DebitMoneyEvent debitMoneyEvent = new DebitMoneyEvent(
                UUID.randomUUID(), Instant.now(), payload);
        build.addEvent(debitMoneyEvent);
        return build;
    }

    public Money balance() {
        return balance;
    }

    public String name() {
        return name;
    }

    private static void requirePositiveAmount(Money amount) {
        Objects.requireNonNull(
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
        private String name;

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

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Wallet build() {
            return new Wallet(
                    id,
                    balance,
                    name
            );
        }
    }

    private Builder copyBuilder() {
        return new Builder()
                .id(getId())
                .balance(balance)
                .name(name)
                ;
    }

    @Override
    public String toString() {
        return "Wallet{" +
                "balance=" + balance +
                ", name='" + name + '\'' +
                '}';
    }
}