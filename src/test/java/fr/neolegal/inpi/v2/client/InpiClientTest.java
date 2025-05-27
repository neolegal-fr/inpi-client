package fr.neolegal.inpi.v2.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import fr.neolegal.inpi.v2.dto.Companies;
import fr.neolegal.inpi.v2.dto.Company;

public class InpiClientTest {

    ObjectMapper mapper = configureMapper();
    InpiClient client = new InpiClient("nicolas@riousset.com", "0000ShinyStar%%");

    @Test
    void parseCompanies() throws IOException {
        String json = getResourceFileAsString("companies.json");
        Companies actual = mapper.readValue(json, Companies.class);
        assertEquals(1, actual.size());
    }

    @Test
    void parseCompany() throws IOException {
        String json = getResourceFileAsString("company.json");
        Company actual = mapper.readValue(json, Company.class);
        assertEquals(1, actual.getFormality().getHistorique().size());
    }

    static String getResourceFileAsString(String fileName) throws IOException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(fileName)) {
            if (is == null)
                return null;
            try (InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader reader = new BufferedReader(isr)) {
                return reader.lines().collect(Collectors.joining());
            }
        }
    }

    private ObjectMapper configureMapper() {
        JavaTimeModule module = new JavaTimeModule();
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .registerModule(module);
    }

    // @Test
    public void downloadActe() throws IOException {
        client.downloadActe("63ded30ef1cd45aa6715ab8f", Path.of("c:/tmp/"));
        File actual = Path.of("c:/tmp/63ded30ef1cd45aa6715ab8f.pdf").toFile();
        assertTrue(actual.exists());
    }
}
