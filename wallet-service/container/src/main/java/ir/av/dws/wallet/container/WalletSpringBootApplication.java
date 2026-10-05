package ir.av.dws.wallet.container;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = "ir.av.dws.wallet")
@ComponentScan(basePackages = "ir.av.dws.wallet")
@EntityScan(basePackages = "ir.av.dws.wallet")
public class WalletSpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(WalletSpringBootApplication.class, args);
    }
}
