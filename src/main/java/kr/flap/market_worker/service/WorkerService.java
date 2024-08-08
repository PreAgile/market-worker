package kr.flap.market_worker.service;

import jakarta.annotation.PostConstruct;
import kr.flap.market_worker.domain.Product;
import kr.flap.market_worker.domain.ProductImage;
import kr.flap.market_worker.dto.ImageUploadResponse;
import kr.flap.market_worker.repository.ProductImageRepository;
import kr.flap.market_worker.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.util.Base64;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkerService implements StreamListener<String, MapRecord<String, String, String>> {

  @Autowired
  private StringRedisTemplate redisTemplate;

  @Value("${redis.stream.key}")
  private String streamKey;

  @Value("${redis.stream.group}")
  private String groupName;

  private final NaverCloudService naverCloudService;
  private final ProductRepository productRepository;
  private final ProductImageRepository productImageRepository;

  @PostConstruct
  public void init() {
    try {
      Boolean streamExists = redisTemplate.hasKey(streamKey);

      if (Boolean.FALSE.equals(streamExists)) {
        redisTemplate.opsForStream().add(streamKey, Collections.singletonMap("init", "true"));
        log.info("Stream '{}' created", streamKey);
      }

      redisTemplate.opsForStream().createGroup(streamKey, ReadOffset.latest(), groupName);
      log.info("Group '{}' created for stream '{}'", groupName, streamKey);

    } catch (Exception e) {
      if (e.getMessage().contains("BUSYGROUP")) {
        log.warn("Group '{}' already exists for stream '{}'", groupName, streamKey);
      }
    }
  }

  @Override
  public void onMessage(MapRecord<String, String, String> record) {
    try {
      String productId = record.getValue().get("productId");
      String encodedFile = record.getValue().get("encodedFile");
      byte[] decodedBytes = Base64.getDecoder().decode(encodedFile);
      ByteArrayInputStream inputStream = new ByteArrayInputStream(decodedBytes);

      ImageUploadResponse response = naverCloudService.uploadImage(inputStream, "uploadedImage.png", decodedBytes.length, "image/png");

      updateProductWithImages(new BigInteger(productId), response);

    } catch (Exception e) {
      log.error("Failed to process image upload request", e);
    } finally {
      // 메시지 성공 여부와 관계없이 삭제
      redisTemplate.opsForStream().acknowledge(streamKey, groupName, record.getId());
      redisTemplate.opsForStream().delete(streamKey, record.getId());
      log.info("Processed and deleted message with ID: {}", record.getId());
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
