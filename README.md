# Основа для телеграм бота на WebHook.
Язык Java. На базе технологии openfeign.
____
## Оглавление
[Этап первый - Подготовка](#Этап-первый---Подготовка)  
* [Необходимые модули](#Необходимые-модули)  
* [Создание host-туннель](#Создание-host-туннеля)    
* [Получение token бота](#Получение-token-бота)  

[Этап второй - Создание бинов и конфигураций](#Этап-второй---Создание-бинов-и-конфигураций)
* [1. Main класс и аннотация @EnableFeignClients](#1-Main-класс-и-аннотация-EnableFeignClients)  
* [2. Бин класса SecurityFilterChain](#2-Бин-класса-SecurityFilterChain)  
* [3. Интерфейс FeignClient](#3-Интерфейс-FeignClient)  
* [4. record SetWebhookRequest(String url)](#4-record-SetWebhookRequestString-url)  
* [5. Controller для обработки входящих сообщений (Update)](#5-Controller-для-обработки-входящих-сообщений-Update)  
* [6. Кастомный Update](#6-Кастомный-Update)  

[Этап третий - Обработка Update и отправка SendMessage](#Этап-третий---Обработка-Update-и-отправка-SendMessage)  
* [1. Получение тела JSON и преобразование в Java объект](#1-Получение-тела-JSON-и-преобразование-в-Java-объект)  
* [2. Определение типа прибывшего сообщения (Update)](#2-Определение-типа-прибывшего-сообщения-Update)  
* [3. Отправка сообщения в чат (SendMessage)](#3-Отправка-сообщения-в-чат-SendMessage)  
* [4. Отправка кнопок (SendMessage)](#4-Отправка-кнопок-SendMessage)  
* [5. Отправка клавиатуры (SendMessage)](#5-Отправка-клавиатуры-SendMessage)  


____
## Этап первый - Подготовка

### Необходимые модули
#### 1. implementation 'org.springframework.boot:spring-boot-starter-web' 
Модуль Spring Boot для создания веб-приложений со встроенной поддержкой REST API. (@RestController). 

#### 2. implementation 'org.springframework.cloud:spring-cloud-starter-openfeign:5.0.0-M2'
Модуль декларативного клиента веб-сервисов интегрированного в систему Spring Boot. Позволяет упростить написание веб сервиса. 

#### 3. Implementation 'org.springframework.boot:spring-boot-starter-security'
Модуль для интеграции Spring Security в приложение Spring Boot. Упрощает конфигурирование и настройку функций безопасности, связанных с фреймворком Spring Security. Добавляет Автоконфигурацию для Аутентификации и Авторизации. Настраивает доступ к различным URL нашего сервера.

#### 4. implementation("org.telegram:telegrambots:6.9.7.1")
Модель для использования компонентов telegrambots. 
Нужны будут получаемые Update и отправляемые SendMessage. Это библиотека опциональна. 
Объекты этих классов могут быть созданы вручную. 
Главное, чтобы объекты этих классов собирались и разбирались в JSON который, прикладывается как тело URL ссылки отправляемой и получаемой от телеграм сервера.


[:arrow_up:Оглавление](#Оглавление)

### Создание host-туннеля
Для создания простого host-туннеля можно воспользоваться программой Visual Studio Code (VS Code). 
В вкладках с терминалами выбираем вкладку PORTS. Нажимаем Forward a Port. 
В поле ввода указываем порт 8080 и вкачаем его. В графе Forwarded Address указана ссылка на наш host-туннель. 
В графе Visibility состояние туннеля необходимо переключить с private на public.

<details> 
  <summary>Иллюстрация
</summary>

![Иллюстрация к проекту](https://github.com/GeorgiyIsaev/spring.demo.telegramBot/blob/master/Image/tunnel_host.jpg)
</details> 

[:arrow_up:Оглавление](#Оглавление)

### Получение token бота
Находим @BotFather в телеграм. С помощью меню @BotFather или команды (/newbot) создаем нового бота следуя всем инструкциям. Необходимо сначала указать его название, а затем username_bot. Username_bot должен оканчивается суффиксом bot. 

Команда /mybots покажет всех доступных ботов. Выбрав нужный бот через меню, можно запросить API Token вашего бота.

<details> 
  <summary>Иллюстрация
</summary>

![Иллюстрация к проекту](https://github.com/GeorgiyIsaev/spring.demo.telegramBot/blob/master/Image/bot_token.jpg)
</details> 

[:arrow_up:Оглавление](#Оглавление)


____
## Этап второй - Создание бинов и конфигураций
### 1 Main класс и аннотация @EnableFeignClients

На main класс необходимо повесить аннотацию @EnableFeignClients. Это включит Feign в проекте.

```java
@EnableFeignClients
@SpringBootApplication
public class TelegramBotApplication {
    public static void main(String[] args) {
       SpringApplication.run(TelegramBotApplication.class, args);
    }
}
```

[:arrow_up:Оглавление](#Оглавление)

### 2 Бин класса SecurityFilterChain

Необходимо создать конфигурационный класс, создающий бин класса SecurityFilterChain. 
В конфигурациях необходимо указать путь, на который разрешено отправляет сообщения. 
Например ("/v1/api/telegram/**").

```java
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v1/api/telegram/**"
                        ).permitAll()
                ).build();

    }
}
```

[:arrow_up:Оглавление](#Оглавление)

### 3 Интерфейс FeignClient
Необходимо создать интерфейс, который будет отправлять наши сообщения на сервер телеграм.


```java
@FeignClient(name = "telegram", url = " https://api.telegram.org/bot<bot_token>")
public interface TelegramFeignClient {

        @PostMapping("/setWebhook")
        String setWebhook(SetWebhookRequest request);


        @PostMapping("/sendMessage")
        String sendMessage(SendMessage sendMessage);
}
```

* Метод String setWebhook(SetWebhookRequest request); позволит отправить на телеграмм сервер Url содержащий информацию о нашем host тоннеле.
* Метод String sendMessage(SendMessage sendMessage); позволит отправлять объекты SendMessage содержащие текст, кнопки, картинки или иные допустимы объекты в чат бота.


[:arrow_up:Оглавление](#Оглавление)

### 4 record SetWebhookRequest(String url)
Необходимо создать record который будет содержать url нашего host туннеля на который телеграм бот будет прислать объекты Update.
```java
public record SetWebhookRequest(String url) {
}
```

[:arrow_up:Оглавление](#Оглавление)

### 5 Controller для обработки входящих сообщений (Update)
Необходимо создать Rest Controller, который будет получать от бота JSON объекты и оборачивать их в объекты классов Java указанных нами. 

```java
@RestController
@RequestMapping("/v1/api/telegram")
public class TelegramUpdateController {

    private final String urlServer = "<host_tunnel>v1/api/telegram/";
    private final TelegramFeignClient telegramFeignClient; 

    @PostConstruct
    public void init() {
        SetWebhookRequest request = new SetWebhookRequest(urlServer);
        //SetWebhookRequest request = new SetWebhookRequest("");
        String response = telegramFeignClient.setWebhook(request);
        System.out.println(response);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handle(HttpMessageNotReadableException e) {
        System.out.println("{400 Bad Request: " + e.getMessage() + "}");
    }

    @PostMapping("/")
    public void postMethodName(@RequestBody Update update) {
        System.out.println("Update print: " + update);
    }
}
```

В случаи если возникает ошибка отправки url сервера, его необходимо сбросить. Для этого необходимо отправить пустой url SetWebhookRequest request = new SetWebhookRequest("");   Затем снова отправить url нашего сервера.

[:arrow_up:Оглавление](#Оглавление)

### 6 Кастомный Update
Если мы хотим отказаться от библиотеки org.telegram:telegrambots мы можем создать свой собственный Update в виде record.

```java
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record Update(

        @JsonProperty("update_id")
        Long updateId,

        @JsonProperty("message")
        Message message,

        @JsonProperty("callback_query")
        CallbackQuery callbackQuery
){}
```

* Аннотация @JsonInclude(JsonInclude.Include.NON_NULL) позволит игнорировать null-поля при сериализации и сформировать объект с незаполненными полями. Например, при получении текстового сообщения CallbackQuery бедует равно null. А при нажатии кнопки уже Message будет равно null.
* Аннотация @JsonIgnoreProperties(ignoreUnknown = true) разрешает нам проигнорировать  поля из текст JSON которые нам не нужны.
* Аннотация @JsonProperty("update_id") нужна что бы указать сериализатору названия полей JSON которые должны быть помещены в это поле.
* Все поля, которые содержатся в объект Update можно посмотреть в документации
https://core.telegram.org/bots/api#update
* Примеры JSON объектов, отправляемых как Update для разных типов сообщений, можно посмотреть в конце инструкции по веб-хукам.
https://core.telegram.org/bots/webhooks#the-verbose-version


[:arrow_up:Оглавление](#Оглавление)


____
## Этап третий - Обработка Update и отправка SendMessage

### 1 Получение тела JSON и преобразование в Java объект
1. При получении JSON объекта его необходимо преобразовать в объект Java класс.
Это можно сделать с помощью аннотации @RequestBody осуществляющее чтение и десериализацию поступившего тела. 

```java
@PostMapping("/")
public void postMethod(@RequestBody Update update){}
```

[:arrow_up:Оглавление](#Оглавление)

### 2 Определение типа прибывшего сообщения (Update)

Телеграм бот присылает JSON объект с различным заполнением для каждого типа отправленных сообщений (текст, нажатие кнопки, картинка, голосовое сообщение). Вид JSON объекта для каждого типа сообщений можно посмотреть в конце по веб-хукам
https://core.telegram.org/bots/webhooks#the-verbose-version

Перед началом обработки Update необходимо выяснить тип прибывшего сообщения. Для этого необходимо проверить существования этого типа поля проверкой на null

```java
if (update.message() != null) {
    System.out.println("Сообщение " + update.message());
    String chatId = update.message().chat().chatId().toString();
    String text = update.message().text();
}
else if (update.callbackQuery() != null) {
    String chatId = update.callbackQuery().message().chat().chatId().toString();
    String text = update.callbackQuery().data();
}
```

Если используется Update из библиотеки телеграм бота необходимо использовать метод с префиксом has. Обращение к несуществующему поля update с помощью метода get вызовет исключение.

```java
if(update.hasMessage()){
    String chatId = update.getMessage().getChatId().toString();
    String text = update.getMessage().getText();
}
else if(update.hasCallbackQuery()){
    String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
    String dataButton = update.getCallbackQuery().getData();
}
```

[:arrow_up:Оглавление](#Оглавление)

### 3 Отправка сообщения в чат (SendMessage)
Для отправки сообщения в чат телеграм необходимо создать JSON тело формат которого соответствует объекту SendMessage.

В документации к телеграм боту можно посмотреть все поля объекта SendMessage. В графе Required указаны какие поля JSON объекта являются обязательными, а какие опциональными.   
https://core.telegram.org/bots/api#sendmessage

Для SendMessage с текстом обязательными являются ид чата и сам текст сообщения. ИД чата необходимо извлечь из Update.

Для создания объекта можно использовать встроенный билдер из библиотеки telegrambots.

```java
String chatId = update.getMessage().getChatId().toString();
String text = update.getMessage().getText();
SendMessage sendMessage = SendMessage.builder().chatId(chatId).text(text).build();
String request = telegramFeignClient.sendMessage(sendMessage);
```

[:arrow_up:Оглавление](#Оглавление)

### 4 Отправка кнопок (SendMessage)
Для отправки кнопок их сначала необходимо создать в виде двумерной коллекции. Где вложенная коллекция — это кнопки, расположенные в одну строку. А внешняя коллекция список строк с кнопками. Созданную коллекцию необходимо приложить к SendMessage. В text() указывается текст, который увидит пользователь. В callbackData() указывается текст который мы получим в update при нажатии кнопки.
```java
List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
buttons.add(
        Arrays.asList(
                InlineKeyboardButton.builder()
                        .text("Кнопка 1")
                        .callbackData("button1")
                        .build(),
                InlineKeyboardButton.builder()
                        .text("Кнопка 2")
                        .callbackData("button2")
                        .build()));

buttons.add(
        Arrays.asList(
                InlineKeyboardButton.builder()
                        .text("Кнопка 3")
                        .callbackData("button3")
                        .build()
               ));


SendMessage sendMessage = SendMessage.builder().chatId(chatID).text("Кнопки")
        .replyMarkup(InlineKeyboardMarkup.builder()
                .keyboard(buttons).build()).build();
String request = telegramFeignClient.sendMessage(sendMessage);
```


[:arrow_up:Оглавление](#Оглавление)

### 5 Отправка клавиатуры (SendMessage)
Отправка встроенной клавиатуры осуществляется аналогичным способ. Встроенная клавиатура не являются кнопкой. Она отправляет в чат текст который указан в ней.
```java
List<KeyboardRow> keyboardRows = new ArrayList<>();
KeyboardButton keyboardButton1 = new KeyboardButton("Справа");
KeyboardButton keyboardButton2 = new KeyboardButton("Слева");
KeyboardButton keyboardButton3 = new KeyboardButton("Нижняя линия");

KeyboardRow keyboardRow1;
keyboardRow1 = new KeyboardRow(List.of(keyboardButton1,keyboardButton2));
keyboardRows.add(keyboardRow1);
keyboardRows.add(new KeyboardRow(List.of(keyboardButton3)));

ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(keyboardRows);

SendMessage sendMessage = SendMessage.builder().chatId(chatID).text("Клава").build();
sendMessage.setReplyMarkup(replyKeyboardMarkup);
String request = telegramFeignClient.sendMessage(sendMessage);
```

[:arrow_up:Оглавление](#Оглавление)
