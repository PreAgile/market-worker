package kr.flap.market_worker.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class StreamTrimmingService {

  private static final Logger log = LoggerFactory.getLogger(StreamTrimmingService.class);

  @Autowired
  private StringRedisTemplate redisTemplate;

  @Value("${redis.stream.key}")
  private String streamKey;

  // 설정한 최대 메시지 수 임계값
  private final long maxMessageCount = 2000L;

  // 스케줄러를 사용하여 주기적으로 트리밍 작업 수행
  @Scheduled(fixedRate = 5000) // 5초마다 실행
  public void trimStream() {
    try {
      // 현재 스트림의 크기 확인
      Long streamLength = redisTemplate.opsForStream().size(streamKey);

      // 스트림 길이가 임계값을 초과할 경우 트리밍 수행
      if (streamLength != null && streamLength > maxMessageCount) {
        // 트리밍 전의 스트림 길이를 로그에 기록
        log.info("Trimming stream '{}' - current length: {}", streamKey, streamLength);

        // 스트림을 트리밍
        Long trimmedLength = redisTemplate.opsForStream().trim(streamKey, maxMessageCount);

        // 트리밍 후의 스트림 길이를 로그에 기록
        log.info("Stream '{}' trimmed to max {} entries. New length: {}", streamKey, maxMessageCount, trimmedLength);
      } else {
        // 스트림 길이가 임계값 이하일 경우
        log.info("Stream '{}' current length is {}, no trimming needed", streamKey, streamLength);
      }

    } catch (Exception e) {
      log.error("Failed to trim stream '{}': {}", streamKey, e.getMessage());
    }
  }
}
