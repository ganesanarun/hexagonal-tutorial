package hexagonal.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;


@Entity
@Getter
@Setter
@Table(name = "orders")
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @Column(name = "cost", nullable = false)
    private BigDecimal cost;

    @OneToOne()
    @JoinColumn(name = "cartId")
    private Cart cart;

    @OneToOne()
    @JoinColumn(name = "promotionId")
    private Promotion promotion;

    public Order(BigDecimal cost, Cart cart, Promotion promotion) {
        this.cost = cost;
        this.cart = cart;
        this.promotion = promotion;
    }
}
