package ir.av.dws.wallet.adapters.secondary.eventbus.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String WALLET_EVENTS_EXCHANGE =
            "wallet.events";

    public static final String BALANCE_INCREASED =
            "DEPOSITED_MONEY_EVENT";

    public static final String BALANCE_DECREASED =
            "DEBIT_MONEY_EVENT";

    public static final String MONEY_TRANSFERRED =
            "TRANSFERRED_MONEY_EVENT";


    @Bean
    public TopicExchange walletEventsExchange() {
        return new TopicExchange(
                WALLET_EVENTS_EXCHANGE,
                true,
                false
        );
    }


    @Bean
    public Queue balanceIncreasedQueue() {
        return QueueBuilder
                .durable("wallet.balance.increased")
                .build();
    }

    @Bean
    public Queue balanceDecreasedQueue() {
        return QueueBuilder
                .durable("wallet.balance.decreased")
                .build();
    }

    @Bean
    public Queue moneyTransferredQueue() {
        return QueueBuilder
                .durable("wallet.money.transferred")
                .build();
    }


    @Bean
    public Binding balanceIncreasedBinding(
            Queue balanceIncreasedQueue
    ) {
        return BindingBuilder
                .bind(balanceIncreasedQueue)
                .to(walletEventsExchange())
                .with(BALANCE_INCREASED);
    }

    @Bean
    public Binding balanceDecreasedBinding(
            Queue balanceDecreasedQueue
    ) {
        return BindingBuilder
                .bind(balanceDecreasedQueue)
                .to(walletEventsExchange())
                .with(BALANCE_DECREASED);
    }

    @Bean
    public Binding moneyTransferredBinding(
            Queue moneyTransferredQueue
    ) {
        return BindingBuilder
                .bind(moneyTransferredQueue)
                .to(walletEventsExchange())
                .with(MONEY_TRANSFERRED);
    }
}