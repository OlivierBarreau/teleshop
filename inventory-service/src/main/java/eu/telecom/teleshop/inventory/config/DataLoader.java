package eu.telecom.teleshop.inventory.config;

import eu.telecom.teleshop.inventory.model.Product;
import eu.telecom.teleshop.inventory.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initProducts(ProductRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                repo.save(new Product("Phone", 10));
                repo.save(new Product("Laptop", 5));
                repo.save(new Product("Headphones", 20));
            }
        };
    }
}
