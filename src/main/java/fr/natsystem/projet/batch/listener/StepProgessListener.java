package fr.natsystem.projet.batch.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StepProgessListener implements StepExecutionListener {
    @Override
    public void beforeStep(StepExecution s) {

    }

    @Override
    public ExitStatus afterStep(StepExecution s) {
        return s.getExitStatus();
    }

}
