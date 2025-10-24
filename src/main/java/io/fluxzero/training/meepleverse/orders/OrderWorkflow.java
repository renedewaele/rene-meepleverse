package io.fluxzero.training.meepleverse.orders;

import io.fluxzero.sdk.Fluxzero;
import io.fluxzero.sdk.modeling.EntityId;
import io.fluxzero.sdk.tracking.Consumer;
import io.fluxzero.sdk.tracking.handling.Association;
import io.fluxzero.sdk.tracking.handling.HandleEvent;
import io.fluxzero.sdk.tracking.handling.Stateful;
import io.fluxzero.training.meepleverse.catalog.api.model.ItemId;
import io.fluxzero.training.meepleverse.orders.api.AbortOrder;
import io.fluxzero.training.meepleverse.orders.api.PlaceOrder;
import io.fluxzero.training.meepleverse.orders.api.model.Order;
import io.fluxzero.training.meepleverse.orders.api.model.OrderId;
import io.fluxzero.training.meepleverse.payments.api.PaymentAccepted;
import io.fluxzero.training.meepleverse.payments.api.PaymentRejected;
import io.fluxzero.training.meepleverse.payments.api.ValidatePayment;
import io.fluxzero.training.meepleverse.payments.api.model.PaymentId;
import io.fluxzero.training.meepleverse.shipping.api.SendShipment;
import io.fluxzero.training.meepleverse.shipping.api.ShipmentSent;
import io.fluxzero.training.meepleverse.shipping.api.ShippingFailed;
import io.fluxzero.training.meepleverse.shipping.api.model.ShipmentDetails;
import io.fluxzero.training.meepleverse.shipping.api.model.ShipmentId;
import io.fluxzero.training.meepleverse.supplier.BackorderFailed;
import io.fluxzero.training.meepleverse.supplier.BackorderItems;
import io.fluxzero.training.meepleverse.supplier.BackorderedItemArrived;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

import static io.fluxzero.sdk.Fluxzero.sendAndForgetCommand;

@Stateful
@Value
@Builder(toBuilder = true)
@Slf4j
@Consumer(name = "OrderWorkflow", ignoreSegment = true)
public class OrderWorkflow {

    @EntityId
    @Association
    OrderId orderId;
    @Association
    PaymentId paymentId;
    boolean paid;
    @Singular
    Set<ItemId> arrivedItems;

    @HandleEvent
    static OrderWorkflow create(PlaceOrder event) {
        sendAndForgetCommand(new ValidatePayment(event.details().paymentId(), event.orderId().toString()));
        return OrderWorkflow.builder().orderId(event.orderId()).paymentId(event.details().paymentId()).build();
    }

    @HandleEvent
    OrderWorkflow handle(PaymentAccepted event) {
        var order = Fluxzero.loadEntityValue(orderId);
        sendAndForgetCommand(new BackorderItems(order.details().itemIds(), orderId,
                                                order.details().addressee().fullName()));
        return toBuilder().paid(true).build();
    }

    @HandleEvent
    OrderWorkflow handle(BackorderedItemArrived event) {
        var workflow = toBuilder().arrivedItem(event.itemId()).build();
        Order order = Fluxzero.loadEntityValue(orderId);
        if (workflow.arrivedItems.containsAll(order.details().itemIds())) {
            Fluxzero.sendAndForgetCommand(new SendShipment(Fluxzero.generateId(ShipmentId.class),
                                                           new ShipmentDetails(order.details().itemIds(), order.orderId(),
                                                                           order.details().addressee())));
        }
        return workflow;
    }

    @HandleEvent
    @Association("details/orderId")
    OrderWorkflow handle(ShipmentSent event) {
        log.info("Order {} completed successfully", orderId);
        return null;
    }

    @HandleEvent
    OrderWorkflow handle(AbortOrder event) {
        log.info("Order {} was aborted", orderId);
        return null;
    }

    /*
        Failures
     */

    @HandleEvent
    void handle(PaymentRejected event) {
        Fluxzero.sendAndForgetCommand(new AbortOrder(orderId, "Payment rejected"));
    }

    @HandleEvent
    void handle(BackorderFailed event) {
        Fluxzero.sendAndForgetCommand(new AbortOrder(orderId, "Backorder failed"));
    }

    @HandleEvent
    void handle(ShippingFailed event) {
        Fluxzero.sendAndForgetCommand(new AbortOrder(orderId, "Shipping failed"));
    }

}
