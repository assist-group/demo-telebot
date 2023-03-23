package org.demo.telebot.domain;

import org.demo.telebot.domain.error.ChatIdIsNotFound;
import org.demo.telebot.domain.port.IDataAccess;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class DishMenuInterceptor {

    private final Map<Long, DishMenu> session = new HashMap<>();
    private final IDataAccess dataBase;

    private final Consumer<SendMessage> sendMessage;

    public DishMenuInterceptor(final IDataAccess dataBase, final Consumer<SendMessage> sendMessage) {
        this.dataBase = dataBase;
        this.sendMessage = sendMessage;
    }

    public SendMessage handle(Long chatId, Message message) {
        return session
                // создать если отсутствует
                .computeIfAbsent(chatId, id -> new DishMenu(id, dataBase, sendMessage))
                .handle(message);
    }

    public void firePaymentCallback(final Payment payment) throws ChatIdIsNotFound {
        DishMenu menu = session.get(payment.getChatId());
        if(menu == null) {
            throw new ChatIdIsNotFound("Chat ID=" + payment.getChatId() + " is not found");
        }
        menu.firePaymentCallback(payment);
    }
}
