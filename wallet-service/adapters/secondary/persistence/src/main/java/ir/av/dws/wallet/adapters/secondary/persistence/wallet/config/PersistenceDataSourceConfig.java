package ir.av.dws.wallet.adapters.secondary.persistence.wallet.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan
@EnableJpaRepositories(
        basePackages = "ir.av.dws.wallet"
)
public class PersistenceDataSourceConfig {
}