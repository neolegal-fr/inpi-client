package fr.neolegal.inpi.v2.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import fr.neolegal.inpi.v2.dto.Companies;
import fr.neolegal.inpi.v2.dto.Company;
import fr.neolegal.inpi.v2.dto.LoginResponse;

public class InpiClientTest {

    ObjectMapper mapper = configureMapper();

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

    @Test
    void parseFindCompanyResponse() throws IOException {
        // Arrange - Création du mock RestTemplate
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        
        // Création d'un JWT valide pour simuler l'authentification
        String token = JWT.create()
                .withExpiresAt(Date.from(Instant.now().plusSeconds(3600)))
                .sign(Algorithm.none());
        LoginResponse loginResponse = new LoginResponse(token);
        
        // Mock de la réponse de login
        when(mockRestTemplate.exchange(
                eq("https://registre-national-entreprises.inpi.fr/api/sso/login"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(LoginResponse.class)))
            .thenReturn(ResponseEntity.ok(loginResponse));
        
        // Chargement de la réponse JSON depuis le fichier de test
        String json = getResourceFileAsString("company-IRRIPROS.json");
        Company expectedCompany = mapper.readValue(json, Company.class);
        
        // Mock de la réponse findBySiren
        when(mockRestTemplate.exchange(
                eq("https://registre-national-entreprises.inpi.fr/api/companies/378012603"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Company.class)))
            .thenReturn(ResponseEntity.ok(expectedCompany));
        
        // Création du client avec le mock
        InpiClient clientWithMock = new InpiClient(
                "https://registre-national-entreprises.inpi.fr/api/",
                "testuser",
                "testpass",
                mockRestTemplate);
        
        // Act
        Company actual = clientWithMock.findBySiren("378012603").orElseThrow();
        
        // Assert - Vérification des données principales de Company
        assertEquals("63a9c6632e71a8e787026863", actual.getId());
        assertEquals(1, actual.getNombreRepresentantsActifs());
        assertEquals(0, actual.getNombreEtablissementsOuverts());
        
        // Vérification de la formality
        assertEquals("378012603", actual.getFormality().getSiren());
        assertEquals("5499", actual.getFormality().getFormeJuridique());
        assertEquals("M", actual.getFormality().getTypePersonne());
        assertEquals(false, actual.getFormality().isDiffusionCommerciale());
        
        // Vérification de l'identité
        assertEquals("IRRIPROS", actual.getFormality().getContent().getPersonneMorale().getIdentite().getEntreprise().getDenomination());
        assertEquals("4673A", actual.getFormality().getContent().getPersonneMorale().getIdentite().getEntreprise().getCodeApe());
        
        // Vérification de l'établissement principal
        assertEquals("37801260300015", actual.getFormality().getContent().getPersonneMorale().getEtablissementPrincipal().getDescriptionEtablissement().getSiret());
        
        // Vérification de la composition (représentants)
        assertEquals(1, actual.getFormality().getContent().getPersonneMorale().getComposition().getPouvoirs().size());
        assertEquals("MARTINET", actual.getFormality().getContent().getPersonneMorale().getComposition().getPouvoirs().stream().findFirst().get().getIndividu().getDescriptionPersonne().getNom());
        
        // Vérification que les méthodes mockées ont été appelées
        verify(mockRestTemplate).exchange(
                eq("https://registre-national-entreprises.inpi.fr/api/sso/login"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(LoginResponse.class));
        verify(mockRestTemplate).exchange(
                eq("https://registre-national-entreprises.inpi.fr/api/companies/378012603"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Company.class));
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

    @Test
    void testDownloadActeToDirectory(@TempDir Path tempDir) throws IOException {
        // Arrange - Création du mock RestTemplate
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        
        // Création d'un JWT valide pour simuler l'authentification
        String token = JWT.create()
                .withExpiresAt(Date.from(Instant.now().plusSeconds(3600)))
                .sign(Algorithm.none());
        LoginResponse loginResponse = new LoginResponse(token);
        
        // Mock de la réponse de login
        when(mockRestTemplate.exchange(
                eq("https://registre-national-entreprises.inpi.fr/api/sso/login"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(LoginResponse.class)))
            .thenReturn(ResponseEntity.ok(loginResponse));
        
        // Contenu PDF simulé
        byte[] pdfContent = "PDF_CONTENT_TEST".getBytes();
        
        // Mock de la réponse de téléchargement
        when(mockRestTemplate.exchange(
                eq("https://registre-national-entreprises.inpi.fr/api/actes/123456/download"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(byte[].class)))
            .thenReturn(ResponseEntity.ok(pdfContent));
        
        // Création du client avec le mock
        InpiClient clientWithMock = new InpiClient(
                "https://registre-national-entreprises.inpi.fr/api/",
                "testuser",
                "testpass",
                mockRestTemplate);
        
        // Act
        clientWithMock.downloadActe("123456", tempDir);
        
        // Assert
        Path expectedFile = tempDir.resolve("123456.pdf");
        assertTrue(Files.exists(expectedFile), "Le fichier PDF devrait avoir été créé");
        byte[] actualContent = Files.readAllBytes(expectedFile);
        assertEquals("PDF_CONTENT_TEST", new String(actualContent), "Le contenu du fichier devrait correspondre");
        
        // Vérification que les méthodes mockées ont été appelées
        verify(mockRestTemplate).exchange(
                eq("https://registre-national-entreprises.inpi.fr/api/sso/login"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(LoginResponse.class));
        verify(mockRestTemplate).exchange(
                eq("https://registre-national-entreprises.inpi.fr/api/actes/123456/download"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(byte[].class));
    }

    @Test
    void testDownloadActeToSpecificFile(@TempDir Path tempDir) throws IOException {
        // Arrange
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        
        String token = JWT.create()
                .withExpiresAt(Date.from(Instant.now().plusSeconds(3600)))
                .sign(Algorithm.none());
        LoginResponse loginResponse = new LoginResponse(token);
        
        when(mockRestTemplate.exchange(
                eq("https://registre-national-entreprises.inpi.fr/api/sso/login"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(LoginResponse.class)))
            .thenReturn(ResponseEntity.ok(loginResponse));
        
        byte[] pdfContent = "SPECIFIC_FILE_CONTENT".getBytes();
        
        when(mockRestTemplate.exchange(
                eq("https://registre-national-entreprises.inpi.fr/api/actes/789/download"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(byte[].class)))
            .thenReturn(ResponseEntity.ok(pdfContent));
        
        InpiClient clientWithMock = new InpiClient(
                "https://registre-national-entreprises.inpi.fr/api/",
                "testuser",
                "testpass",
                mockRestTemplate);
        
        Path targetFile = tempDir.resolve("custom-name.pdf");
        
        // Act
        clientWithMock.downloadActe("789", targetFile);
        
        // Assert
        assertTrue(Files.exists(targetFile), "Le fichier spécifique devrait avoir été créé");
        byte[] actualContent = Files.readAllBytes(targetFile);
        assertEquals("SPECIFIC_FILE_CONTENT", new String(actualContent));
    }
}
