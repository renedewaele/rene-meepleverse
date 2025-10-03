package io.fluxzero.training.meepleverse.shipping;

import io.fluxzero.sdk.Fluxzero;
import io.fluxzero.sdk.tracking.handling.authentication.NoUserRequired;
import io.fluxzero.sdk.web.HandleGet;
import io.fluxzero.sdk.web.Path;
import io.fluxzero.sdk.web.QueryParam;
import io.fluxzero.training.meepleverse.catalog.api.SearchItems;
import io.fluxzero.training.meepleverse.catalog.api.model.Item;
import io.fluxzero.training.meepleverse.shipping.api.GetShipments;
import io.fluxzero.training.meepleverse.shipping.api.ShipmentSent;
import io.fluxzero.training.meepleverse.shipping.api.model.Shipment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Path("/shipments")
public class ShipmentsEndpoint {

    @HandleGet
    List<Shipment> getShipments() {
        return Fluxzero.queryAndWait(new GetShipments());
    }

}
