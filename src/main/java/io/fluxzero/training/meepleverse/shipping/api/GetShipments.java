package io.fluxzero.training.meepleverse.shipping.api;

import io.fluxzero.sdk.Fluxzero;
import io.fluxzero.sdk.tracking.handling.HandleQuery;
import io.fluxzero.sdk.tracking.handling.Request;
import io.fluxzero.training.meepleverse.shipping.api.model.Shipment;

import java.util.List;

public record GetShipments() implements Request<List<Shipment>> {

    @HandleQuery
    List<Shipment> handle() {
        return Fluxzero.search(Shipment.class).fetchAll();
    }

}
