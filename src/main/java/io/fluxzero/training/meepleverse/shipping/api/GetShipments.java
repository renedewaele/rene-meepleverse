package io.fluxzero.training.meepleverse.shipping.api;

import io.fluxzero.sdk.Fluxzero;
import io.fluxzero.sdk.common.serialization.FilterContent;
import io.fluxzero.sdk.tracking.handling.HandleQuery;
import io.fluxzero.sdk.tracking.handling.Request;
import io.fluxzero.sdk.tracking.handling.authentication.RequiresUser;
import io.fluxzero.training.meepleverse.shipping.api.model.Shipment;

import java.util.List;

@RequiresUser
public record GetShipments() implements Request<List<Shipment>> {

    @HandleQuery
    @FilterContent
    List<Shipment> handle() {
        return Fluxzero.search(Shipment.class).fetchAll();
    }

}
