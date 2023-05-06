package hexagonal;


import java.util.List;

public sealed interface CreateResult<T> {

    record Success<T>(T order) implements CreateResult<T> {
    }

    record CartNotFound<T>() implements CreateResult<T> {
    }

    record CartIsNotActive<T>() implements CreateResult<T> {
    }

    record InvalidDiscountCode<T>(List<String> applicableDiscounts) implements CreateResult<T> {
    }
}
