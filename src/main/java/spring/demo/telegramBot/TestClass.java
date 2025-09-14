package spring.demo.telegramBot;


import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:connectBot.properties")
public class TestClass {
    @Value("${app.token}")
    String token;
    @Value("${app.urlRequest}")
    String urlRequest;


    @PostConstruct
    public void init(){
        System.out.println("Токен: " + token + "; URL: " + urlRequest);
    }
}
