package io.fluxzero.training.meepleverse.shipping;

import io.fluxzero.sdk.common.serialization.DeserializingMessage;
import io.fluxzero.sdk.tracking.Consumer;
import io.fluxzero.sdk.tracking.handling.HandleDocument;
import io.fluxzero.training.meepleverse.shipping.api.model.Shipment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Consumer(name = "ShipmentDocument", minIndex = 0)
@Slf4j
public class ShipmentDocumentHandler {

    @HandleDocument
    Shipment handle(Shipment in, DeserializingMessage m) {
        log.info("Changed Shipment: {} (revision: {}, original revision: {})",
                          m.getMessageId(), m.getSerializedObject().getRevision(),
                          m.getSerializedObject().getOriginalRevision());
        return in;
    }
}
