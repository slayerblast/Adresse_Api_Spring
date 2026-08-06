package fr.natsystem.projet.batch.step;


import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CodeInseePartitioner implements Partitioner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {

        List<String> codes = jdbcTemplate.queryForList(
                """
                SELECT DISTINCT code_insee
                FROM adresse_staging
                ORDER BY code_insee
                """,
                String.class
        );

        Map<String, ExecutionContext> partitions =
                new LinkedHashMap<>();

        for (String codeInsee : codes) {

            ExecutionContext context =
                    new ExecutionContext();

            context.putString(
                    "codeInsee",
                    codeInsee
            );

            partitions.put(
                    "partition-" + codeInsee,
                    context
            );
        }

        return partitions;
    }
}

