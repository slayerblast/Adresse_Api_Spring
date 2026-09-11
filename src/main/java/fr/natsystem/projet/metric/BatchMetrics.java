package fr.natsystem.projet.metric;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class BatchMetrics {
  private static final int TIME = 1_000_000;
  private long readTimeNs;
  private long processorTimeNs;
  private long writeTimeNs;

  public synchronized void addReadTime(long ns) {
    readTimeNs += ns;
  }

  public synchronized void addProcessorTime(long ns) {
    processorTimeNs += ns;
  }

  public synchronized void addWriteTime(long ns) {
    writeTimeNs += ns;
  }

  public synchronized long consumeReadMs() {
    long ms = readTimeNs / TIME;
    readTimeNs = 0;
    return ms;
  }

  public synchronized long consumeProcessorMs() {
    long ms = processorTimeNs / TIME;
    processorTimeNs = 0;
    return ms;
  }

  public synchronized long consumeWriteMs() {
    long ms = writeTimeNs / TIME;
    writeTimeNs = 0;
    return ms;
  }
}
