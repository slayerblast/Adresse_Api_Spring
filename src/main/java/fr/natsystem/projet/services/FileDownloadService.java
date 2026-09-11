package fr.natsystem.projet.services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FileDownloadService {

  @Value("${spring.batch.gzFilePath}")
  private String gzFilePath;

  @Value("${spring.batch.csvFileLoc}")
  private String csvFileLoc;

  public String downloadAndUngzip(String url) {
    Path gzFile = Paths.get(gzFilePath);

    try (HttpClient client = HttpClient.newHttpClient()) {

      client.send(
          HttpRequest.newBuilder().uri(URI.create(url)).build(),
          HttpResponse.BodyHandlers.ofFile(gzFile));

      Path csvFile = Paths.get(csvFileLoc);

      try (var input = new GZIPInputStream(Files.newInputStream(gzFile));
          var output = Files.newOutputStream(csvFile)) {
        input.transferTo(output);
      }

      return csvFile.toAbsolutePath().toString();

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return "";
    } catch (IOException e) {
      return "";
    }
  }
}
