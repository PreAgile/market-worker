package kr.flap.market_worker.domain;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity
@Table(name = "sellers")
public class Seller extends BaseTimeEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private BigInteger id;

  @Column(nullable = false, length = 100)
  public String name;

  @OneToMany(mappedBy = "seller", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  public List<Product> products = new ArrayList<>();

  @Builder
  public Seller(String name) {
    this.name = name;
  }
}