package org.demo.telebot.domain;

public class Payment {
    private String order;

    private Long chatId;

    private boolean isPayment;

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

    public boolean isPayment() {
        return isPayment;
    }

    public void setPayment(boolean payment) {
        isPayment = payment;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "order='" + order + '\'' +
                ", chatId=" + chatId +
                ", isPayment=" + isPayment +
                '}';
    }
}
