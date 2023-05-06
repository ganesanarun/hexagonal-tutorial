package hexagonal.representations;


import java.util.UUID;

public record CartWithPromotion(UUID cartId, String promotionCode) {
}
