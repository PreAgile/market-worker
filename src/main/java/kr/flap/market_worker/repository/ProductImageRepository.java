package kr.flap.market_worker.repository;

import kr.flap.market_worker.domain.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, BigInteger> {
}
