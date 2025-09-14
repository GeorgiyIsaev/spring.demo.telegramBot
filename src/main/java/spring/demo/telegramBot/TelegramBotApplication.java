package spring.demo.telegramBot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;


@EnableFeignClients
@SpringBootApplication
public class TelegramBotApplication {
	public static void main(String[] args) {
		SpringApplication.run(TelegramBotApplication.class, args);
	}

}
