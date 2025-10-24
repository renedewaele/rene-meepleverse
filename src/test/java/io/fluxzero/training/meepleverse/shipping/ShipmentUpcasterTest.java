package io.fluxzero.training.meepleverse.shipping;

import io.fluxzero.common.api.search.constraints.MatchConstraint;
import io.fluxzero.sdk.test.TestFixture;
import io.fluxzero.training.meepleverse.shipping.api.ShipmentSent;
import io.fluxzero.training.meepleverse.shipping.api.model.Shipment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ShipmentUpcasterTest {

    final TestFixture testFixture = TestFixture.create().registerCasters(ShipmentUpcaster.class);

    @Test
    void upcastShipmentSentRev0() {
        testFixture.whenUpcasting("/shipping/shipment-sent-rev0.json")
                .<ShipmentSent>expectResult(r -> r.details().itemIds().size() == 1);
    }

    @Test
    void upcastShipmentRev0() {
        testFixture.whenUpcasting("/shipping/shipment-rev0.json")
                .<Shipment>expectResult(r -> r.details().itemIds().size() == 1);
    }

    @Nested
    class ShipmentDocumentHandlerTests {
        @BeforeEach
        void setUp() {
            testFixture.registerHandlers(new ShipmentDocumentHandler());
        }

        @Test
        void persistShipmentRev0 () {
            testFixture.givenDocument("/shipping/shipment-rev0.json")
                    .whenSearching(Shipment.class, MatchConstraint.match("ark-nova", "details/itemIds"))
                    .expectResult(r -> r.getFirst().details().itemIds().size() == 1);
        }
    }
}