package hexagonal.representations;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


public sealed interface OrderCreationRepresentation {
    record OrderRepresentation(UUID orderId, BigDecimal cost) implements OrderCreationRepresentation {
    }

    record CartIsNotActive(ErrorCode errorCode) implements OrderCreationRepresentation {
    }


    record InvalidPromotion(ErrorCode errorCode, List<String> validPromotions) implements OrderCreationRepresentation {
    }
}