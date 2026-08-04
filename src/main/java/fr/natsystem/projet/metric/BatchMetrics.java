package fr.natsystem.projet.metric;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class BatchMetrics {

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
        long ms = readTimeNs / 1_000_000;
        readTimeNs = 0;
        return ms;
    }

    public synchronized long consumeProcessorMs() {
        long ms = processorTimeNs / 1_000_000;
        processorTimeNs = 0;
        return ms;
    }

    public synchronized long consumeWriteMs() {
        long ms = writeTimeNs / 1_000_000;
        writeTimeNs = 0;
        return ms;
    }
}

