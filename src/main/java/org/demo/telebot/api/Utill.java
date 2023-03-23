package org.demo.telebot.api;

import org.demo.telebot.api.error.CallbackServerException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

public class Utill {

    private static final ObjectMapper mapper = new ObjectMapper();
    public static  <T> T readRequest(InputStream is, Class<T> tClass) {
        try {
            return mapper.readValue(is, tClass);
        } catch (IOException e) {
            throw new CallbackServerException(e);
        }
    }

    public static <T> byte[] writeResponse(T response) {
        try {
            return mapper.writeValueAsBytes(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
