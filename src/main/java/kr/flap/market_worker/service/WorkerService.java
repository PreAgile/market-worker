package kr.flap.market_worker.service;

import kr.flap.market_worker.domain.Product;
import kr.flap.market_worker.domain.ProductImage;
import kr.flap.market_worker.dto.ImageUploadResponse;
import kr.flap.market_worker.repository.ProductImageRepository;
import kr.flap.market_worker.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkerService implements StreamListener<String, MapRecord<String, String, String>> {

  private final NaverCloudService naverCloudService;
  private final ProductRepository productRepository;
  private final ProductImageRepository productImageRepository;

  @Override
  public void onMessage(MapRecord<String, String, String> record) {
    try {
      String productId = record.getValue().get("productId");
      String encodedFile = record.getValue().get("encodedFile");
      byte[] decodedBytes = Base64.getDecoder().decode(encodedFile);
      ByteArrayInputStream inputStream = new ByteArrayInputStream(decodedBytes);

      ImageUploadResponse response = naverCloudService.uploadImage(inputStream, "uploadedImage.png", decodedBytes.length, "image/png");

      // 제품 업데이트 로직 호출
      updateProductWithImages(new BigInteger(productId), response);

    } catch (Exception e) {
      log.error("Failed to process image upload request", e);
    }
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updateProductWithImages(BigInteger productId, ImageUploadResponse imageUploadResponse) {
    Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

    String mainImageUrl = imageUploadResponse.getObjectUrl();
    product.setMainImageUrl(mainImageUrl);
    productRepository.save(product);

    ProductImage productImage = ProductImage.builder()
            .product(product)
            .imageUrl(imageUploadResponse.getObjectUrl())
            .eTag(imageUploadResponse.getETag())
            .build();

    productImageRepository.save(productImage);

    log.info("Product and related entities saved successfully for product id: {}", productId);
  }
}
