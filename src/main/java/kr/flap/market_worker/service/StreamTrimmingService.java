package kr.flap.market_worker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class StreamTrimmingService {

  private static final Logger log = LoggerFactory.getLogger(StreamTrimmingService.class);

  @Autowired
  private RedisTemplate<String, String> redisTemplate;

  @Value("${redis.stream.key}")
  private String streamKey;

  // TTL을 1분(60초)로 설정
  private final long ttlInMillis = 60 * 1000;

  // 스케줄러를 사용하여 주기적으로 트리밍 작업 수행
  @Scheduled(fixedRate = 60000) // 1분마다 실행
  public void checkAndExpireMessages() {
    try {
      StreamOperations<String, String, String> opsForStream = redisTemplate.opsForStream();
      long currentTime = Instant.now().toEpochMilli();

      // 스트림에서 모든 메시지를 조회
      List<MapRecord<String, String, String>> records = opsForStream.range(streamKey, Range.unbounded());

      for (MapRecord<String, String, String> record : records) {
        String timestampStr = record.getValue().get("timestamp");

        if (timestampStr != null) {
          long messageTime = Long.parseLong(timestampStr);
          if (currentTime - messageTime > ttlInMillis) {
            // 만료된 메시지 삭제
            opsForStream.delete(streamKey, record.getId());
            log.info("Deleted expired message from stream '{}': {}", streamKey, record.getId());
          }
        }
      }
    } catch (Exception e) {
      log.error("Failed to check and expire messages for stream '{}': {}", streamKey, e.getMessage());
    }
  }
}
