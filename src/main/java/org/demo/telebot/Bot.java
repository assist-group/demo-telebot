package org.demo.telebot;

import org.demo.telebot.api.CallbackServer;
import org.demo.telebot.dao.DataBase;
import org.demo.telebot.domain.DishMenuInterceptor;
import org.demo.telebot.domain.port.IDataAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.LongPollingBot;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.List;

public class Bot extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(Bot.class);

    // Inject
    private final IDataAccess dataBase = DataBase.getInstance();

    // Inject
    private final DishMenuInterceptor dishMenuInterceptor = new DishMenuInterceptor(dataBase, this::sendMsg);

    // Inject
    private final CallbackServer callbackServer = new CallbackServer(dishMenuInterceptor);

    /**
     * Метод для приема сообщений.
     *
     * @param update Содержит сообщение от пользователя.
     */
    @Override
    public void onUpdateReceived(Update update) {

        if(update.hasMessage()) {
            Long chatId = update.getMessage().getChatId();
            Message message = update.getMessage();
            String userName = message.getFrom().getUserName();

            SendMessage answer = dishMenuInterceptor.handle(chatId, message);
            log.debug("{}[QUESTION]: {}", userName, message.getText());
            log.debug("{}[ANSWER]: {}", userName, answer.getText());
            sendMsg(answer);
        } else if(update.hasCallbackQuery()) {
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            String data = update.getCallbackQuery().getData();
            SendMessage answer = new SendMessage();
            answer.setChatId(chatId);
            answer.setText(data);
            sendMsg(answer);
        }
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
    }

    /**
     * Метод для настройки сообщения и его отправки.
     *
     * @param sendMessage отправляемое сообщение.
     */
    public synchronized void sendMsg(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.debug("Exception: {}", e.toString());
        }
    }

    /**
     * Метод возвращает имя бота, указанное при регистрации.
     *
     * @return имя бота
     */
    @Override
    public String getBotUsername() {
        return "yourUsername";
    }

    /**
     * Метод возвращает token бота для связи с сервером Telegram
     *
     * @return token для бота
     */
    @Override
    public String getBotToken() {
        return "yourToken";
    }

    public static void main(String[] args) {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(Bot.getBot());
            log.info("Bot is started...");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private static LongPollingBot getBot() {
        return new Bot();
    }
}
