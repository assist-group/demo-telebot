package org.demo.telebot.dao;

import org.demo.telebot.domain.Dish;
import org.demo.telebot.domain.port.IDataAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataBase implements IDataAccess {

    private static final Logger log = LoggerFactory.getLogger(DataBase.class);
    private static final DataBase instance = new DataBase();
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/demodb";
    private static final String DB_USER = "botuser";
    private static final String DB_PASSWORD = "qwe123";

    private final Connection connection;

    private DataBase()
    {
        try {
            this.connection = getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        log.debug("The connect to database is set: {}", DB_URL);
    }

    public static DataBase getInstance() {
        return instance;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
    @Override
    public List<Dish> getDishes() {
        List<Dish> list = new ArrayList<>();

        try(Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery("select * from dish");
            while (rs.next()) {
                Dish dish = new Dish();
                dish.setId(rs.getLong("id"));
                dish.setType(rs.getString("type"));
                dish.setName(rs.getString("name"));
                dish.setDescription(rs.getString("description"));
                dish.setPrice(rs.getString("price"));
                list.add(dish);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
}
