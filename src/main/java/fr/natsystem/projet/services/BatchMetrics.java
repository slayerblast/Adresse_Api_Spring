package fr.natsystem.projet.services;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class BatchMetrics {

    private long processorTimeNs;

    public synchronized void addProcessorTime(long ns) {
        processorTimeNs += ns;
    }

    public synchronized long consumeProcessorMs() {
        long ms = processorTimeNs / 1_000_000;
        processorTimeNs = 0;
        return ms;
    }
}