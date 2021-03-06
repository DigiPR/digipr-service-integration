/*
 * Copyright (c) 2018. University of Applied Sciences and Arts Northwestern Switzerland FHNW.
 * All rights reserved.
 */

package rocks.process.integration.inventory.stream.listener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rocks.process.integration.inventory.business.domain.OrderItem;
import rocks.process.integration.inventory.business.domain.PackingSlip;
import rocks.process.integration.inventory.business.service.InventoryService;
import rocks.process.integration.inventory.stream.message.EventMessage;
import rocks.process.integration.inventory.stream.message.OrderMessage;

import java.util.List;

@Component
@EnableBinding({Sink.class, Source.class})
public class MessageEventListener {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    @Output(Source.OUTPUT)
    private MessageChannel messageChannel;

    private static Logger logger = LoggerFactory.getLogger(MessageEventListener.class);

    @StreamListener(target = Sink.INPUT,
            condition="headers['type']=='FetchGoods'")
    @Transactional
    public void payment(@Payload EventMessage<OrderMessage> eventMessage) throws Exception {
        OrderMessage orderMessage = eventMessage.getPayload();
        logger.info("Payload received: "+orderMessage.toString());
        List<OrderItem> orderItems = objectMapper.convertValue(orderMessage.getItems(), new TypeReference<List<OrderItem>>() {});
        PackingSlip packingSlip = inventoryService.fetchGoods(Long.valueOf(orderMessage.getCustomerId()), orderMessage.getOrderId(), orderItems);
        orderMessage.setPackingSlipId(packingSlip.getPackingSlipId());
        orderMessage.setStatus("GoodsFetched");
        send(new EventMessage<>("ShipGoods", orderMessage));
    }

    public void send(EventMessage<OrderMessage> eventMessage) {
        messageChannel.send(MessageBuilder.withPayload(eventMessage).setHeader("type", eventMessage.getType()).build());
    }

    @StreamListener(target = Sink.INPUT)
    public void defaultListener() {}
}