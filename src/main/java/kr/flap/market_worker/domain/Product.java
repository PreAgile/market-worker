package kr.flap.market_worker.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity
@Table(name = "products")
public class Product extends BaseTimeEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private BigInteger id;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "storage_id")
  public Storage storage;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seller_id")
  public Seller seller;

  @Transient
  private static AtomicInteger numberIncrease;

  @Column(name = "product_no")
  private BigInteger productNo;

  @Size(max = 100, message = "짧은 설명은 100자를 초과할 수 없습니다.")
  @Column(name = "short_description")
  public String shortDescription;

  @Column(name = "expiration_date")
  public LocalDate expirationDate;

  @Column(name = "main_image_url")
  public String mainImageUrl;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
  public List<ProductImage> productImages = new ArrayList<>();

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
  public List<SubProduct> subProducts = new ArrayList<>();

  private void setProductNo() {
    Number productNo = numberIncrease == null ? numberIncrease = new AtomicInteger(0) : numberIncrease.incrementAndGet();
    this.productNo = BigInteger.valueOf(productNo.intValue());
  }

  @Builder
  public Product(Storage storage, Seller seller, String shortDescription, LocalDate expirationDate, String mainImageUrl) {
    this.storage = storage;
    this.seller = seller;
    this.shortDescription = shortDescription;
    this.expirationDate = expirationDate;
    this.mainImageUrl = mainImageUrl;
    this.setProductNo();
  }
}
