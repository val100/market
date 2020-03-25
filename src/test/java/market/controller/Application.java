package market.controller;

import market.SecurityConfig;
import market.ServletConfig;
import market.domain.UserAccount;
import market.exception.EmailExistsException;
import market.service.UserAccountService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({ServletConfig.class, SecurityConfig.class})
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public UserAccountService userAccountService() {
		return new UserAccountService() {

			@Override
			public UserAccount findByEmail(String email) {
				return null;
			}

			@Override
			public UserAccount create(UserAccount userAccount) throws EmailExistsException {
				return null;
			}
		};
	}
}
