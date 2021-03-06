/*
 * Copyright (c) 2018. University of Applied Sciences and Arts Northwestern Switzerland FHNW.
 * All rights reserved.
 */

package rocks.process.integration.eshop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import rocks.process.integration.eshop.stream.message.EventMessage;
import rocks.process.integration.eshop.stream.message.OrderMessage;
import rocks.process.integration.eshop.stream.sender.MessageEventSender;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
public class CheckoutController {

    @Autowired
    private MessageEventSender eventSender;

    @GetMapping(path = "/checkout", produces = "text/plain")
    public ResponseEntity<String> getCheckout(){
        List<OrderMessage.OrderItem> orderItems = new ArrayList<>();
        orderItems.add(new OrderMessage.OrderItem("1", UUID.randomUUID().toString(), 15));
        OrderMessage orderMessage = new OrderMessage(UUID.randomUUID().toString(), "1", 2000D, 15, orderItems, null, null, null, "Order Placed");
        orderMessage.setStatus("OrderPlaced");
        eventSender.send(new EventMessage<>("RequestPayment", orderMessage));
        return ResponseEntity.ok(orderMessage.toString());
    }
}
