package fr.natsystem.projet.batch.Partitioner;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DvfPartitioner implements Partitioner {
  private final JdbcTemplate jdbcTemplate;

  @Override
  public Map<String, ExecutionContext> partition(int gridSize) {

    List<String> codes =
        jdbcTemplate.queryForList(
            """
                SELECT DISTINCT code_commune
                FROM dvf_staging
                ORDER BY code_commune
                """,
            String.class);

    Map<String, ExecutionContext> partitions = new LinkedHashMap<>();

    for (String codeInsee : codes) {
      ExecutionContext context = new ExecutionContext();
      context.putString("codeInsee", codeInsee);
      partitions.put("partition-" + codeInsee, context);
    }

    return partitions;
  }
}
