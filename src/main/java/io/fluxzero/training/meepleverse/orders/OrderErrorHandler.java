package io.fluxzero.training.meepleverse.orders;

import io.fluxzero.common.MessageType;
import io.fluxzero.sdk.Fluxzero;
import io.fluxzero.sdk.common.exception.TechnicalException;
import io.fluxzero.sdk.tracking.Consumer;
import io.fluxzero.sdk.tracking.handling.HandleError;
import io.fluxzero.sdk.tracking.handling.Trigger;
import io.fluxzero.training.meepleverse.payments.api.ValidatePayment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Consumer(name = "order-error-handler", minIndex = 0)
@Slf4j
public class OrderErrorHandler {

    @HandleError(allowedClasses = TechnicalException.class)
    void handle(@Trigger(messageType = MessageType.COMMAND) ValidatePayment command) {
        log.info("Retrying payment validation for order {}", command.getReference());
        Fluxzero.sendAndForgetCommand(command);
    }

}
