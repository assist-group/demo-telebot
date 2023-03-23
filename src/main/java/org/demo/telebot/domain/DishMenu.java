package org.demo.telebot.domain;

import org.demo.telebot.domain.port.IDataAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Stateless
 * */

public class DishMenu {

    private static final Logger log = LoggerFactory.getLogger(DishMenu.class);

    private static final String START = "/start";

    private static final String SHOW_MENU = "Показать меню";

    private static final String YES = "Да";

    private static final String NO = "Нет";

    private static final String PAYMENT_BUTTON = "Оплатить";

    private static final String UNKNOWN_COMMAND = "Неизвестная команда";

    private static final String PAYMENT_URL = "https://payments.paysecure.ru/pay/order.cfm";

    private static final String MERCHANT_ID = "160090";

    private final List<Dish> dishList = new ArrayList<>();

    private final Long chatId;
    private final IDataAccess dataBase;

    private final Consumer<SendMessage> sendMessage;
    private String orderNumber;
    private Dish selectedDish;

    public DishMenu(Long chatId, IDataAccess dataBase, Consumer<SendMessage> sendMessage) {
        this.chatId = chatId;
        this.dataBase = dataBase;
        this.sendMessage = sendMessage;
    }

    public SendMessage handle(Message message) {
        String logTitle = "Selected option: {}";
        if(message.getText().equals(START)) {
            log.debug(logTitle, START);
             return showMainMenu();
        }

        if(message.getText().equals(SHOW_MENU)) {
            log.debug(logTitle, SHOW_MENU);
            return showDishesMenu();
        }

        Dish dish = findDish(message.getText());
        if(dish != null) {
            log.debug(logTitle, dish);
            selectedDish = dish;
            return showYesNoMenu(dish);
        }

        if (message.getText().equals(YES)) {
            log.debug(logTitle, YES);
            return showPaymentMenu();
        }

        if(message.getText().equals(NO)) {
            log.debug(logTitle, NO);
            return showDishesMenu();
        }

        return new SendMessage(String.valueOf(chatId), UNKNOWN_COMMAND);
    }

    public void firePaymentCallback(final Payment payment) {
        sendMessage.accept(prepareAnswer(
                payment.getChatId(),
                "Заказ оплачен! Приятного аппетита!",
                new ReplyKeyboardRemove(true)));
    }

    private SendMessage showMainMenu() {
        //Создаем объект будущей клавиатуры и выставляем нужные настройки
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true); //подгоняем размер
        replyKeyboardMarkup.setOneTimeKeyboard(true); //скрываем после использования

        //Создаем список с рядами кнопок
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        keyboardRows.add(createRow(SHOW_MENU));
        replyKeyboardMarkup.setKeyboard(keyboardRows);

        return prepareAnswer(chatId,"Что желаете?",replyKeyboardMarkup);
    }

    private SendMessage showDishesMenu() {
        //Создаем объект будущей клавиатуры и выставляем нужные настройки
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true); //подгоняем размер
        replyKeyboardMarkup.setOneTimeKeyboard(true); //скрываем после использования

        //Создаем список с рядами кнопок
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        // Группируем по типу и создаем панели
        dataBase.getDishes().stream()
                        .collect(Collectors.groupingBy(Dish::getType))
                .forEach((type, list) -> {
                    List<String> row = new ArrayList<>(list.size());
                    for(Dish dish : list) {
                        dishList.add(dish);
                        row.add(getFormattedDishName(dish));
                    }
                    keyboardRows.add(createRow(row.toArray(new String[0])));
                });

        replyKeyboardMarkup.setKeyboard(keyboardRows);

        return prepareAnswer(chatId,"Выберете блюдо", replyKeyboardMarkup);

    }

    private SendMessage showYesNoMenu(Dish dish)
    {
        //Создаем объект будущей клавиатуры и выставляем нужные настройки
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true); //подгоняем размер
        replyKeyboardMarkup.setOneTimeKeyboard(true); //скрываем после использования

        //Создаем список с рядами кнопок
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        keyboardRows.add(createRow(YES, NO));
        //добавляем лист с одним рядом кнопок в главный объект
        replyKeyboardMarkup.setKeyboard(keyboardRows);

        orderNumber = getOrderNumber();
        String text = "Подтверждаете?\n" +
                "Номер заказа: №" + orderNumber + "\n" +
                dish.getName() +
                "(" + dish.getDescription() + ") " +
                dish.getPrice();
        return prepareAnswer(chatId, text, replyKeyboardMarkup);
    }

    private SendMessage showPaymentMenu() {
        if(selectedDish == null) {
            return showDishesMenu();
        }
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton button = new InlineKeyboardButton(PAYMENT_BUTTON);
        button.setUrl(makeOrderUrl(makeParamsForOrderUrl()));

        List<InlineKeyboardButton> row = new ArrayList<>();
        List<List<InlineKeyboardButton>> rowList= new ArrayList<>();
        row.add(button);
        rowList.add(row);
        keyboardMarkup.setKeyboard(rowList);

        log.debug("ORDER FOR CHAT ID: {}", chatId);
        log.debug("ORDER: {}", orderNumber);
        // Вставить эту команду для имитации прихода уведомления оплаты об оплате в Assist
        log.debug("curl -X POST localhost:8000/api/payment -d '{\"order\":\"" + orderNumber + "\",\"chat_id\":\"" + chatId + "\",\"payment\":true}' -v; echo\n");

        return prepareAnswer(chatId, "Ссылка на оплату", keyboardMarkup);
    }

    private String makeOrderUrl(Map<String, String> params) {
        if(params.size() == 0 ) {
            return PAYMENT_URL;
        }
        StringBuilder builder = new StringBuilder(PAYMENT_URL);
        builder.append("?");
        params.forEach((k, v) -> builder.append(k).append("=").append(v).append("&"));
        return builder.substring(0, builder.length() - 1);
    }

    private Map<String, String> makeParamsForOrderUrl() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("orderamount", calculateDollarToRuble(selectedDish.getPrice()));
        map.put("merchant_id", MERCHANT_ID);
        map.put("ordernumber", orderNumber);

        return map;
    }

    private String getOrderNumber() {
        return Integer.toString(19000 +  new Random().nextInt(1000));
    }

    private String calculateDollarToRuble(String price) {
        BigDecimal dollar = BigDecimal.valueOf(77.01d);
        return String.valueOf(dollar.multiply(
                BigDecimal.valueOf(
                        Double.parseDouble(price.replace("$", "")))
        ).setScale(2, RoundingMode.HALF_UP).doubleValue());
    }

    private Dish findDish(String dishName) {
        return dishList.stream()
                .filter(dish -> dishName.equals(getFormattedDishName(dish)))
                .findFirst().orElse(null);
    }

    private String getFormattedDishName(final Dish dish) {
        return dish.getName() + " " + dish.getPrice();
    }

    private SendMessage prepareAnswer(Long chatId, String text, ReplyKeyboard keyboard) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(keyboard);

        return sendMessage;
    }

    private KeyboardRow createRow(String ... buttonNames) {
        //Создаем один ряд кнопок
        KeyboardRow keyboardRow = new KeyboardRow();
        //Добавляем кнопки
        Arrays.stream(buttonNames).forEach(name ->
                keyboardRow.add(new KeyboardButton(name)));
        return keyboardRow;
    }
}
