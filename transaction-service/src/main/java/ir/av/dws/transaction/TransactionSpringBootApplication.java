package ir.av.dws.transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "ir.av.dws.transaction")
public class TransactionSpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionSpringBootApplication.class, args);
    }
}
