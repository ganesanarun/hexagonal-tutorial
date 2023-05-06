package hexagonal;

import hexagonal.entities.Cart;
import hexagonal.entities.Promotion;
import hexagonal.representations.CartWithPromotion;
import org.jeasy.random.EasyRandom;

import java.util.UUID;

public class TestDataGenerator {

    private static final EasyRandom easyRandom = new EasyRandom();

    public static CartWithPromotion cartWithPromotion() {
        return new CartWithPromotion(UUID.randomUUID(), easyRandom.nextObject(String.class));
    }

    public static Cart.CartBuilder cart() {
        return easyRandom.nextObject(Cart.CartBuilder.class);
    }

    public static Promotion.PromotionBuilder promotion() {
        return easyRandom.nextObject(Promotion.PromotionBuilder.class);
    }
}
