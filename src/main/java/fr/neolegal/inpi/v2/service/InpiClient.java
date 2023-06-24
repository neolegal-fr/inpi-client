package fr.neolegal.inpi.v2.service;

import static java.util.Objects.*;
import static java.util.stream.Collectors.*;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import fr.neolegal.inpi.v2.model.Acte;
import fr.neolegal.inpi.v2.model.Attachments;
import fr.neolegal.inpi.v2.model.Companies;
import fr.neolegal.inpi.v2.model.Company;
import fr.neolegal.inpi.v2.model.Credentials;
import fr.neolegal.inpi.v2.model.LoginResponse;
import fr.neolegal.inpi.v2.model.Pouvoir;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Client pour l'API INPI V2
 * La documentation de l'API est disponible ici :
 * https://www.inpi.fr/sites/default/files/documentation%20technique%20API%20formalit%C3%A9s_v13.pdf
 * Pour les actes :
 * https://www.inpi.fr/sites/default/files/documentation%20technique%20API%20Actes%20v1.1.pdf
 */
public class InpiClient {

    DecodedJWT jwt;
    final String api;
    final String username;
    final String password;

    final RestTemplate restTemplate;

    public InpiClient(
            String username,
            String password) {
        this("https://registre-national-entreprises.inpi.fr/api/", username, password);
    }

    public InpiClient(
            String apiUrl,
            String username,
            String password) {
        this.api = apiUrl.endsWith("/") ? apiUrl : apiUrl + "/";
        this.username = username;
        this.password = password;
        this.restTemplate = new RestTemplate();
    }

    public void login() {
        if (isAuthenticated()) {
            return;
        }
        jwt = null;
        Credentials credentials = new Credentials(username, password);
        String endPoint = api + "sso/login";

        LoginResponse response = restTemplate
                .exchange(endPoint, HttpMethod.POST, new HttpEntity<>(credentials), LoginResponse.class)
                .getBody();

        jwt = JWT.decode(response.token());
    }

    HttpHeaders builHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwt.getToken());
        return headers;
    }

    public Companies findByDenomination(String denomination) {
        login();
        HttpEntity<String> entity = new HttpEntity<>(builHeaders());

        String endPoint = UriComponentsBuilder
                .fromHttpUrl(api + "companies")
                .queryParam("companyName", "{companyName}")
                .encode()
                .toUriString();

        return restTemplate.exchange(endPoint, HttpMethod.GET, entity, Companies.class, denomination).getBody();
    }

    public Optional<Company> findBySiren(String siren) {
        try {
            return Optional.of(findCompany(siren, null));
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            throw e;
        }
    }

    private Company findCompany(String siren, LocalDate date) {
        login();

        HttpEntity<String> entity = new HttpEntity<>(builHeaders());

        // UriComponentsBuilder ne peut pas être utilisé à cause du format du paramètres
        // "siren[]" qui est escapé par toURIString
        String endPoint = String.format("%scompanies/%s", api, siren);

        // LE 26/04/2023, la racherche par date doit être désactivée. Suite à une maj de
        // l'INPI, le paramètre date déclenche une erreur 500 côté serveur
        // if (nonNull(date)) {
        // endPoint = String.format("%s?date=%s", endPoint, date.toString());
        // }

        return restTemplate.exchange(endPoint, HttpMethod.GET, entity, Company.class).getBody();
    }

    public Attachments findAttachmentsBySiren(String siren) {
        login();

        HttpEntity<String> entity = new HttpEntity<>(builHeaders());

        // UriComponentsBuilder ne peut pas être utilisé à cause du format du paramètres
        // "siren[]" qui est escapé par toURIString
        String endPoint = String.format("%scompanies/%s/attachments", api, siren);

        Attachments attachments = restTemplate.exchange(endPoint, HttpMethod.GET, entity, Attachments.class).getBody();

        return attachments;
    }

    public void logout() {
        jwt = null;
    }

    public boolean isAuthenticated() {
        return nonNull(jwt) && jwt.getExpiresAt().after(new Date());
    }

    List<Acte> filterDepotStatuts(Attachments attachments) {
        return attachments.getActes().stream().filter(Acte::isPublicationStatuts).collect(toList());
    }

    public Set<Acte> findStatuts(String siren) {
        Attachments attachments = findAttachmentsBySiren(siren);
        List<Acte> depotsStatuts = filterDepotStatuts(attachments);
        /**
         * Les requêtes historiques auprès de l'INPI ne fonctionnant pas en date du
         * 26/04/2023, on ne crée que les derniers statuts
         */
        // return depotsStatuts.stream().flatMap(acte ->
        // retrieveStatuts(acte).stream()).collect(toSet());
        Comparator<Acte> comparator = Comparator.comparing(Acte::getDateDepot);
        return depotsStatuts.stream().max(comparator).stream().collect(Collectors.toSet());
    }

    Collection<Pouvoir> retrieveRepresentants(Acte acte) {
        // Pour obtenir des détails sur la société, on récupère son état au moment du
        // dépôt des statuts
        Company company = findCompany(acte.getSiren(), acte.getDateDepot());
        return company
                .getFormality()
                .getContent()
                .getPersonneMorale()
                .getComposition()
                .getPouvoirs();
    }
}
