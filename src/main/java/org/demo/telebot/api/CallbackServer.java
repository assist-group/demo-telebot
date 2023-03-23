package org.demo.telebot.api;

import org.demo.telebot.api.error.CallbackServerException;
import org.demo.telebot.api.input.CallbackPaymentRequest;
import org.demo.telebot.api.output.CallbackPaymentResponse;
import org.demo.telebot.domain.DishMenuInterceptor;
import org.demo.telebot.domain.Payment;
import org.demo.telebot.domain.error.ChatIdIsNotFound;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class CallbackServer {
    private static final Logger log = LoggerFactory.getLogger(CallbackServer.class);
    private static final int SERVER_PORT = 8000;
    private static final String API_PAYMENT_CALLBACK = "/api/payment";

    private final DishMenuInterceptor dishMenuInterceptor;

    public CallbackServer(final DishMenuInterceptor dishMenuInterceptor) {
        this.dishMenuInterceptor = dishMenuInterceptor;

        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress(SERVER_PORT), 0);
        } catch (IOException e) {
            throw new CallbackServerException(e);
        }
        server.createContext(API_PAYMENT_CALLBACK, postPaymentHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        log.debug("The callback server is running on port: {}", SERVER_PORT);
    }

    private HttpHandler postPaymentHandler() {
        return exchange -> {
            if(Constants.HttpMethod.POST.equals(exchange.getRequestMethod())) {
                CallbackPaymentRequest request;
                try {
                    request = Utill.readRequest(exchange.getRequestBody(), CallbackPaymentRequest.class);
                } catch (CallbackServerException ex) {
                    sendErrorResponse(exchange, HttpStatus.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
                    return;
                }

                log.debug("Received the request: {}", request);
                if(!isValidPaymentRequest(request)) {
                    sendErrorResponse(exchange, HttpStatus.SC_BAD_REQUEST, "The callback payment request is not valid");
                    return;
                }

                try {
                    dishMenuInterceptor.firePaymentCallback(convertToPayment(request));
                } catch (ChatIdIsNotFound ex) {
                    sendErrorResponse(exchange, HttpStatus.SC_NOT_FOUND, ex.getMessage());
                    return;
                }

                CallbackPaymentResponse response = new CallbackPaymentResponse();
                response.setMessage("success");
                byte[] body = Utill.writeResponse(response);
                sendResponse(exchange,
                        HttpStatus.SC_CREATED,
                        Map.of(Constants.MediaType.CONTENT_TYPE, Constants.MediaType.APPLICATION_JSON),
                        body);
            } else {
                exchange.sendResponseHeaders(HttpStatus.SC_METHOD_NOT_ALLOWED, -1);
            }
            exchange.close();
        };
    }

    private boolean isValidPaymentRequest(final CallbackPaymentRequest request) {
        return request.isPayment() != null
                && request.getChatId() != null
                && request.getOrder() != null;
    }

    private void sendResponse(final HttpExchange exchange, int status, Map<String, String> headers, byte[] body) throws IOException {
        headers.forEach((k, v) -> exchange.getResponseHeaders().set(k, v));
        exchange.sendResponseHeaders(status, body.length);
        OutputStream output = exchange.getResponseBody();
        output.write(body);
        output.flush();
    }

    private void sendErrorResponse(final HttpExchange exchange, int status, String message) throws IOException {
        sendResponse(exchange, status, new HashMap<>(), message.getBytes(StandardCharsets.UTF_8));
    }

    private Payment convertToPayment(CallbackPaymentRequest request) {
        Payment payment = new Payment();
        payment.setOrder(request.getOrder());
        payment.setChatId(request.getChatId());
        payment.setPayment(request.isPayment());

        return payment;
    }
}
