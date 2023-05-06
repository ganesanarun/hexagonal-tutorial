package hexagonal;

import hexagonal.entities.Order;
import hexagonal.repositories.CartRepository;
import hexagonal.repositories.OrderRepository;
import hexagonal.repositories.PromotionRepository;
import hexagonal.representations.CartWithPromotion;
import hexagonal.representations.ErrorCode;
import hexagonal.representations.OrderCreationRepresentation;
import hexagonal.representations.OrderCreationRepresentation.CartIsNotActive;
import hexagonal.representations.OrderCreationRepresentation.InvalidPromotion;
import hexagonal.representations.OrderCreationRepresentation.OrderRepresentation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

import static hexagonal.representations.ErrorCode.CART_IS_NOT_ACTIVE;
import static java.lang.String.format;
import static org.springframework.http.HttpStatus.GONE;

@RestController
@AllArgsConstructor
public class OrderResource {

    private final OrderService orderService;
    private final CartRepository cartRepository;
    private final PromotionRepository promotionRepository;
    private final OrderRepository orderRepository;

    @PostMapping("/orders")
    public ResponseEntity<OrderCreationRepresentation> convertThis(@RequestBody CartWithPromotion cartDiscount) {
        var createOrderResult = orderService.from(cartDiscount.cartId(), cartDiscount.promotionCode(),
                cartRepository::findById,
                promotionRepository::findByCodeContainingIgnoreCase,
                cart -> promotionRepository.findAll(),
                orderRepository::save,
                cartRepository::save);
        return switch (createOrderResult) {
            case CreateResult.Success<Order> success ->
                    ResponseEntity.created(URI.create(format("/orders/%s", success.order().getId()))).body(new OrderRepresentation(success.order().getId(), success.order().getCost()));
            case CreateResult.InvalidDiscountCode<?> badResult ->
                    ResponseEntity.badRequest().body(new InvalidPromotion(ErrorCode.INVALID_PROMOTION, badResult.applicableDiscounts()));
            case CreateResult.CartNotFound<?> ignored -> ResponseEntity.notFound().build();
            case CreateResult.CartIsNotActive<?> cartIsNotActive ->
                    ResponseEntity.status(GONE).body(new CartIsNotActive(CART_IS_NOT_ACTIVE));
        };
    }
}
