package fr.natsystem.projet.services;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

    public String downloadAndUngzip(String url) throws Exception {

        try {
            Path gzFile = Paths.get(gzFilePath);

            HttpClient client = HttpClient.newHttpClient();

            client.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .build(),
                    HttpResponse.BodyHandlers.ofFile(gzFile)
            );

            String csvFile = csvFileLoc;

            try (
                    GZIPInputStream gis =
                            new GZIPInputStream(new FileInputStream(gzFile.toFile()));
                    FileOutputStream fos =
                            new FileOutputStream(csvFile)
            ) {
                gis.transferTo(fos);
            }

            return Paths.get(csvFile).toAbsolutePath().toString();
        }catch (Exception e){
            return "";
        }
    }
}
