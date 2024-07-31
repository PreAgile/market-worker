package kr.flap.market_worker.domain;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigInteger;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity
@Table(name = "storages")
public class Storage extends BaseTimeEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private BigInteger id;

  @Enumerated(EnumType.STRING)
  public StorageType type;

  @Builder
  public Storage(StorageType type) {
    this.type = type;
  }
}