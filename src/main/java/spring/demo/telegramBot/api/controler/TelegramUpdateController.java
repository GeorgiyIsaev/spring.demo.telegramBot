package spring.demo.telegramBot.api.controler;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import spring.demo.telegramBot.api.client.SetWebhookRequest;
import spring.demo.telegramBot.api.client.TelegramFeignClient;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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
        System.out.println("{400 Bad Request from Telegram " + e.getMessage() + "}");
    }

    @PostMapping("/")
    public void postMethodName(@RequestBody spring.demo.telegramBot.api.data.update.Update update) {
        System.out.println("Update print " + update);

        if (update.message() != null) {
            System.out.println("Сообщение " + update.message());
            String chatId = update.message().chat().chatId().toString();
            String text = update.message().text();

            String requestSendButton = sendButton(chatId, "button01", text, "button03");
            System.out.println("Request: " + requestSendButton);

            String requestKeyBoard = sendKeyBoard(chatId, text);
            System.out.println("Request: " + requestKeyBoard);

        }
        if (update.callbackQuery() != null) {
            System.out.println("Кнопка " + update.callbackQuery());
            String chatId = update.callbackQuery().message().chat().chatId().toString();
            String text = "data(): " + update.callbackQuery().data();

            SendMessage sendMessage = SendMessage.builder().chatId(chatId).text(text).build();
            String requestMessage = telegramFeignClient.sendMessage(sendMessage);
            System.out.println("requestMessage: " + requestMessage);
        }
    }

    public String sendButton(String chatID, String button1, String button2, String button3){
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        buttons.add(
                Arrays.asList(
                        InlineKeyboardButton.builder()
                                .text("Кнопка 1")
                                .callbackData(button1)
                                .build(),
                        InlineKeyboardButton.builder()
                                .text(button2)
                                .callbackData(button2)
                                .build()));

        buttons.add(
                Arrays.asList(
                        InlineKeyboardButton.builder()
                                .text("Кнопка 3")
                                .callbackData(button3)
                                .build()
                       ));


        SendMessage sendMessage = SendMessage.builder().chatId(chatID).text("Кнопки")
                .replyMarkup(InlineKeyboardMarkup.builder()
                        .keyboard(buttons).build()).build();

       return telegramFeignClient.sendMessage(sendMessage);
    }

    public String sendKeyBoard(String chatID, String text){
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardButton keyboardButton1 = new KeyboardButton("Пока");
        KeyboardButton keyboardButton2 = new KeyboardButton(text);
        KeyboardButton keyboardButton3 = new KeyboardButton("Нижняя линия");

        KeyboardRow keyboardRow1;
        keyboardRow1 = new KeyboardRow(List.of(keyboardButton1,keyboardButton2));
        keyboardRows.add(keyboardRow1);
        keyboardRows.add(new KeyboardRow(List.of(keyboardButton3)));

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(keyboardRows);

        SendMessage sendMessage = SendMessage.builder().chatId(chatID).text("Клава").build();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        return telegramFeignClient.sendMessage(sendMessage);
    }

}