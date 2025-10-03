package io.fluxzero.training.meepleverse.shipping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.fluxzero.sdk.common.serialization.casting.Upcast;
import org.springframework.stereotype.Component;

@Component
public class ShipmentUpcaster {

    @Upcast(type = "io.fluxzero.training.meepleverse.shipping.api.ShipmentSent", revision = 0)
    JsonNode upcastShipmentSent(ObjectNode payload) {
        return upcastShipmentDetails(payload);
    }

    @Upcast(type = "io.fluxzero.training.meepleverse.shipping.api.model.Shipment", revision = 0)
    JsonNode upcastShipment(ObjectNode payload) {
        return upcastShipmentDetails(payload);
    }

    ObjectNode upcastShipmentDetails(ObjectNode payload) {
        ObjectNode details = payload.withObject("details");
        JsonNode itemId = details.get("itemId");
        ArrayNode itemIds = details.withArray("itemIds");
        itemIds.add(itemId);
        details.remove("itemId");
        return payload;
    }

}