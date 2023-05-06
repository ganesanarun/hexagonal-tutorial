package hexagonal.repositories;

import hexagonal.entities.Promotion;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromotionRepository extends CrudRepository<Promotion, Long> {
    Optional<Promotion> findByCodeContainingIgnoreCase(String code);
}
