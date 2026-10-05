package ir.av.dws.core.domain.wallet.entity;

import ir.av.dws.wallet.core.domain.wallet.entity.Wallet;
import ir.av.dws.wallet.core.domain.wallet.event.WalletOpenedEvent;
import ir.av.dws.wallet.core.domain.wallet.event.DebitMoneyEvent;
import ir.av.dws.wallet.core.domain.wallet.event.DepositedMoneyEvent;
import ir.av.dws.wallet.core.domain.wallet.exception.InvalidWalletOperationException;
import ir.av.dws.wallet.core.domain.shared.event.DomainEvent;
import ir.av.dws.wallet.core.domain.shared.exception.InsufficientBalanceException;
import ir.av.dws.wallet.core.domain.shared.vo.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WalletTest {

    private UUID userId = UUID.randomUUID();

    @Test
    void shouldOpenWalletWithZeroBalance() {

        Wallet wallet = Wallet.open(userId);

        assertNotNull(wallet.getId());
        assertEquals(userId, wallet.userId());
        assertEquals(Money.zero(), wallet.balance());
    }

    @Test
    void shouldGenerateWalletOpenedEventWhenWalletIsOpened() {

        Wallet wallet = Wallet.open(userId);

        List<DomainEvent<?>> events =
                wallet.pullDomainEvents();

        assertEquals(1, events.size());

        assertInstanceOf(
                WalletOpenedEvent.class,
                events.getFirst()
        );
    }

    @Test
    void shouldDepositMoney() {

        Wallet wallet = Wallet.open(userId);

        Wallet deposited = wallet.deposit(
                Money.of(new BigDecimal("100"))
        );

        assertEquals(
                Money.of(new BigDecimal("0")),
                wallet.balance()
        );

        assertEquals(
                Money.of(new BigDecimal("100")),
                deposited.balance()
        );
    }

    @Test
    void shouldGenerateDepositedMoneyEvent() {

        Wallet wallet = Wallet.open(userId);

        Wallet deposited = wallet.deposit(
                Money.of(new BigDecimal("100"))
        );

        List<DomainEvent<?>> events =
                deposited.pullDomainEvents();

        assertEquals(1, events.size());

        assertInstanceOf(
                DepositedMoneyEvent.class,
                events.getFirst()
        );
    }

    @Test
    void shouldNotAllowZeroDeposit() {

        Wallet wallet = Wallet.open(userId);

        assertThrows(
                InvalidWalletOperationException.class,
                () -> wallet.deposit(Money.zero())
        );
    }

    @Test
    void shouldNotAllowNullDeposit() {

        Wallet wallet = Wallet.open(userId);

        assertThrows(
                NullPointerException.class,
                () -> wallet.deposit(null)
        );
    }

    @Test
    void shouldDebitMoney() {

        Wallet wallet = Wallet.open(userId);

        Wallet deposited = wallet.deposit(
                Money.of(new BigDecimal("100"))
        );

        Wallet debited = deposited.debit(
                Money.of(new BigDecimal("40"))
        );

        assertEquals(
                Money.of(new BigDecimal("100")),
                deposited.balance()
        );

        assertEquals(
                Money.of(new BigDecimal("60")),
                debited.balance()
        );
    }

    @Test
    void shouldNotDebitMoreThanBalance() {

        Wallet wallet = Wallet.open(userId);

        Wallet deposited = wallet.deposit(
                Money.of(new BigDecimal("100"))
        );

        assertThrows(
                InsufficientBalanceException.class,
                () -> deposited.debit(
                        Money.of(new BigDecimal("101"))
                )
        );
    }

    @Test
    void shouldKeepBalanceUnchangedWhenDebitFails() {

        Wallet wallet = Wallet.open(userId);

        Wallet deposited = wallet.deposit(
                Money.of(new BigDecimal("100"))
        );

        assertThrows(
                InsufficientBalanceException.class,
                () -> deposited.debit(
                        Money.of(new BigDecimal("101"))
                )
        );

        assertEquals(
                Money.of(new BigDecimal("100")),
                deposited.balance()
        );
    }

    @Test
    void shouldDebitEntireBalance() {

        Wallet wallet = Wallet.open(userId);

        Wallet deposited = wallet.deposit(
                Money.of(new BigDecimal("100"))
        );

        Wallet debited = deposited.debit(
                Money.of(new BigDecimal("100"))
        );

        assertEquals(
                Money.zero(),
                debited.balance()
        );
    }

    @Test
    void shouldNotAllowZeroDebit() {

        Wallet wallet = Wallet.open(userId);

        assertThrows(
                InvalidWalletOperationException.class,
                () -> wallet.debit(Money.zero())
        );
    }

    @Test
    void shouldGenerateDebitMoneyEvent() {

        Wallet wallet = Wallet.open(userId);

        Wallet deposited = wallet.deposit(
                Money.of(new BigDecimal("100"))
        );

        Wallet debited = deposited.debit(
                Money.of(new BigDecimal("40"))
        );

        List<DomainEvent<?>> events =
                debited.pullDomainEvents();

        assertEquals(1, events.size());

        assertInstanceOf(
                DebitMoneyEvent.class,
                events.getFirst()
        );
    }
}
