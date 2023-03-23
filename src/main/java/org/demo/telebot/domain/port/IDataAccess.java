package org.demo.telebot.domain.port;

import org.demo.telebot.domain.Dish;

import java.util.List;

public interface IDataAccess {
    List<Dish> getDishes();
}
