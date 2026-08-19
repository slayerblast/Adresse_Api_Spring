package fr.natsystem.projet.model;

public record JobStatusResponse(
        Long jobExecutionId,
        String jobName,
        String status,
        String code,
        String nameCode,
        String exitCode
) {}
