package hexagonal;

import hexagonal.entities.Cart;
import hexagonal.entities.Order;
import hexagonal.entities.Promotion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@Slf4j
@Service
public class OrderService {

    public CreateResult<Order> from(UUID cartId, String discountCode,
                                    Function<UUID, Optional<Cart>> cartFinder,
                                    Function<String, Optional<Promotion>> promotionFinder,
                                    Function<Cart, Iterable<Promotion>> applicablePromotionsFinder,
                                    Consumer<Order> saveThis,
                                    Consumer<Cart> updateThis) {
        final var maybeCart = cartFinder.apply(cartId);
        if (maybeCart.isEmpty()) {
            log.warn("Invalid cart {}", cartId);
            return new CreateResult.CartNotFound<>();
        }
        final var cart = maybeCart.get();

        if (!cart.isActive()) {
            log.warn("Cart {} already been used", cart.getId());
            return new CreateResult.CartIsNotActive<>();
        }

        final var maybeDiscount = promotionFinder.apply(discountCode);
        if (maybeDiscount.isEmpty()) {
            log.warn("Invalid discount code {} for cart {}", discountCode, cartId);
            return new CreateResult.InvalidDiscountCode<>(promotionCodesFrom(applicablePromotionsFinder.apply(cart)));
        }
        final var promotion = maybeDiscount.get();

        final var order = from(cart, promotion);
        cart.setActive(false);
        saveThis.accept(order);
        updateThis.accept(cart);
        return new CreateResult.Success<>(order);
    }

    private Order from(Cart cart, Promotion promotion) {
        final var totalCost = cart.getCost().multiply(BigDecimal.valueOf(cart.getCount()));
        final var costAfterDiscount = totalCost.subtract(promotion.getDiscount());
        return new Order(costAfterDiscount, cart, promotion);
    }

    private List<String> promotionCodesFrom(Iterable<Promotion> promotions) {
        return StreamSupport.stream(promotions.spliterator(), false).map(Promotion::getCode).toList();
    }
}
