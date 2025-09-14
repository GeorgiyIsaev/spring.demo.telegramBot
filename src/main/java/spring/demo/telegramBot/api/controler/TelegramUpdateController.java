package spring.demo.telegramBot.api.controler;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import spring.demo.telegramBot.api.client.SetWebhookRequest;
import spring.demo.telegramBot.api.client.TelegramFeignClient;
import spring.demo.telegramBot.api.data.Update;


@RestController
@RequestMapping("/v1/api/telegram")
public class TelegramUpdateController {

    private final String urlServer;

    private final TelegramFeignClient telegramFeignClient;

    public TelegramUpdateController(
            TelegramFeignClient telegramFeignClient
            ,@Value("${telegram.urlServer}") String urlServer){

        this.telegramFeignClient = telegramFeignClient;
        this.urlServer = urlServer;
    }

    @PostConstruct
    public void init(){
        SetWebhookRequest request = new SetWebhookRequest(urlServer);
        //SetWebhookRequest request = new SetWebhookRequest("");
        String response = telegramFeignClient.setWebhook(request);
        System.out.println(response);

    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handle(HttpMessageNotReadableException e) {
       // logger.error("400 Bad Request from Telegram, {}", e);
        System.out.println("{BAD_REQUEST " + e.getMessage() + "}");
    }



    @PostMapping("/")
    public void postMethodName(@RequestBody Update update) {
       // logger.info("Update received: {}", update);
        System.out.println("Update" + update.updateId() + update.message().text());
    }


}
