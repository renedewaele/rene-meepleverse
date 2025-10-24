package io.fluxzero.training.meepleverse.orders;

import io.fluxzero.common.api.search.BulkUpdate;
import io.fluxzero.common.api.search.bulkupdate.DeleteDocument;
import io.fluxzero.common.api.search.bulkupdate.IndexDocument;
import io.fluxzero.sdk.Fluxzero;
import io.fluxzero.sdk.modeling.Entity;
import io.fluxzero.sdk.tracking.Consumer;
import io.fluxzero.sdk.tracking.handling.HandleEvent;
import io.fluxzero.training.meepleverse.orders.api.model.Order;
import io.fluxzero.training.meepleverse.orders.api.model.OrderId;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static io.fluxzero.sdk.Fluxzero.loadAggregate;
import static io.fluxzero.sdk.common.serialization.DeserializingMessage.computeForBatchIfAbsent;
import static io.fluxzero.sdk.common.serialization.DeserializingMessage.whenBatchCompletes;

@Component
@Consumer(name = "OrderReplayer", minIndex = 0L, maxIndexExclusive = OrderReplayer.MAX_INDEX)
public class OrderReplayer {
    static final long MAX_INDEX = 115427613081600000L; // 2025-10-24T06:00:00Z,

    Set<OrderId> handledOrders = ConcurrentHashMap.newKeySet();

    @HandleEvent
    void handle(Order order) {
        if (handledOrders.add(order.orderId())) {
            computeForBatchIfAbsent("OrderReplayer", k -> {
                Set<OrderId> orderIds = new HashSet<>();
                whenBatchCompletes(e -> storeDocuments(orderIds));
                return orderIds;
            }).add(order.orderId());
        }
    }

    void storeDocuments(Set<OrderId> orderIds) {
        List<BulkUpdate> documents = orderIds.stream()
                .flatMap(orderId -> {
                    Entity<Order> orderEntity = loadAggregate(orderId);
                    if (orderEntity.lastEventIndex() > MAX_INDEX) {
                        return Stream.empty();
                    }
                    return Stream.of(createDocuments(orderEntity.get(), orderId));
                }).toList();
        Fluxzero.get().documentStore().bulkUpdate(documents).join();
    }

    BulkUpdate createDocuments(Order order, OrderId orderId) {
        return order == null
                ? DeleteDocument.builder().id(orderId.toString()).collection("Order").build()
                : IndexDocument.builder().id(orderId.toString()).collection("Order").object(order).build();
    }
}
