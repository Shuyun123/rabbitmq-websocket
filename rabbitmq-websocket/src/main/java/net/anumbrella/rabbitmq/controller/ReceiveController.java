package net.anumbrella.rabbitmq.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

/**
 * @author Anumbrella
 */
@Controller
public class ReceiveController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiveController.class);

    @MessageMapping("/client")
    public void all(String message) {
        LOGGER.info("*** 来自客户端的消息 ***:" + message);
    }
}
