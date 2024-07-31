package kr.flap.market_worker.repository;

import kr.flap.market_worker.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;

public interface ProductRepository extends JpaRepository<Product, BigInteger> {
}
