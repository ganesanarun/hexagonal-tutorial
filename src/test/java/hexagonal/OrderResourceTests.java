package hexagonal;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexagonal.entities.Cart;
import hexagonal.entities.Order;
import hexagonal.repositories.CartRepository;
import hexagonal.repositories.OrderRepository;
import hexagonal.repositories.PromotionRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;


import static hexagonal.TestDataGenerator.cart;
import static hexagonal.TestDataGenerator.cartWithPromotion;
import static hexagonal.TestDataGenerator.promotion;
import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderResource.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(OrderService.class)
class OrderResourceTests {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartRepository cartRepository;

    @MockBean
    private PromotionRepository promotionRepository;

    @MockBean
    private OrderRepository orderRepository;

    @Nested
    class OrderCreationTests {
        @Test
        void returnStatusOKWithOrderRepresentationWhenCartConvertedToOrder() throws Exception {
            var cartWithPromotion = cartWithPromotion();
            var cart = cart().id(cartWithPromotion.cartId()).isActive(true).build();
            final var promotion = promotion().code(cartWithPromotion().promotionCode()).build();
            final var orderId = UUID.randomUUID();
            when(cartRepository.findById(cartWithPromotion.cartId())).thenReturn(of(cart));
            when(promotionRepository.findByCodeContainingIgnoreCase(cartWithPromotion.promotionCode())).thenReturn(of(promotion));
            when(cartRepository.save(any(Cart.class))).thenAnswer(answer -> {
                final var savingCart = (Cart) answer.getArgument(0);
                assertThat(savingCart.getId()).isEqualTo(cart.getId());
                assertThat(savingCart.isActive()).isFalse();
                return savingCart;
            });
            when(orderRepository.save(any(Order.class))).thenAnswer(answer -> {
                final var newOrder = (Order) answer.getArgument(0);
                assertThat(newOrder.getCart().getId()).isEqualTo(cart.getId());
                newOrder.setId(orderId);
                return newOrder;
            });

            mockMvc
                    .perform(post("/orders")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(cartWithPromotion)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string(LOCATION, format("/orders/%s", orderId)));
        }

        @Test
        void return404WhenCartIsNotFound() throws Exception {
            var cartWithPromotion = cartWithPromotion();
            when(cartRepository.findById(cartWithPromotion.cartId())).thenReturn(empty());

            mockMvc
                    .perform(post("/orders")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(cartWithPromotion)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void return410WhenCartIsAlreadyUsed() throws Exception {
            var cartWithPromotion = cartWithPromotion();
            var cart = cart().id(cartWithPromotion.cartId()).isActive(false).build();
            when(cartRepository.findById(cartWithPromotion.cartId())).thenReturn(of(cart));

            mockMvc
                    .perform(post("/orders")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(cartWithPromotion)))
                    .andExpect(status().isGone());
        }

        @Test
        void return400WhenDiscountCodeIsInvalid() throws Exception {
            var cartWithPromotion = cartWithPromotion();
            var cart = cart().id(cartWithPromotion.cartId()).isActive(true).build();
            when(cartRepository.findById(cartWithPromotion.cartId())).thenReturn(of(cart));
            when(promotionRepository.findByCodeContainingIgnoreCase(cartWithPromotion.promotionCode())).thenReturn(empty());
            when(promotionRepository.findAll()).thenReturn(List.of());

            mockMvc
                    .perform(post("/orders")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(cartWithPromotion)))
                    .andExpect(status().isBadRequest());
        }
    }

}


