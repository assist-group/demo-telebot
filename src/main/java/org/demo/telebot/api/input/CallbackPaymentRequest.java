package org.demo.telebot.api.input;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CallbackPaymentRequest {
    @JsonProperty("order")
    private String order;
    @JsonProperty("chat_id")
    private Long chatId;
    @JsonProperty("payment")
    private Boolean isPayment;

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public Boolean isPayment() {
        return isPayment;
    }

    public void setPayment(boolean payment) {
        isPayment = payment;
    }

    @Override
    public String toString() {
        return "CallbackPaymentRequest{" +
                "order='" + order + '\'' +
                ", chatId=" + chatId +
                ", isPayment=" + isPayment +
                '}';
    }
}
