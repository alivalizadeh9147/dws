package ir.av.dws.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "ir.av.dws.user")
@EnableJpaRepositories(basePackages = "ir.av.dws.user")
@EntityScan(basePackages = "ir.av.dws.user")
public class UserSpringBootApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserSpringBootApplication.class, args);
    }
}
