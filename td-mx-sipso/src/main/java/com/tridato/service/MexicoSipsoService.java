package com.tridato.service;

import com.tridato.service.api.SipsoApiResponse;
import com.tridato.service.constants.SipsoSelector;
import com.trudato.commons.api.ApiResponse;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/mexican-sipso")
public class MexicoSipsoService {

    private Environment environment;

    @Value("${trudato.mx.sipso.url.index}")
    private String index;

    @Autowired
    public MexicoSipsoService(Environment environment) {
        this.environment = environment;
    }

    @GetMapping("/health")
    public ResponseEntity health() {
        return ResponseEntity.ok().build();
    }

    @CrossOrigin(origins = { "http://trudata-live.s3-website.us-east-2.amazonaws.com", "http://localhost:3000"})
    @GetMapping(path = "/query/{curp}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> query(@PathVariable("curp") final String queriedCurp) throws IOException {

        final Document curpDocument = Jsoup.connect(String.format(index, queriedCurp)).get();
        return ResponseEntity.ok(new SipsoApiResponse()
                .withCurp(getTextFromSelector(curpDocument, SipsoSelector.CURP))
                .withLastName(getTextFromSelector(curpDocument, SipsoSelector.LAST_NAME))
                .withSecondLastName(getTextFromSelector(curpDocument, SipsoSelector.SECOND_LAST_NAME))
                .withName(getTextFromSelector(curpDocument, SipsoSelector.NAME))
                .withGender(getTextFromSelector(curpDocument, SipsoSelector.GENDER))
                .withBirthday(getTextFromSelector(curpDocument, SipsoSelector.BIRTHDAY))
                .withBirthdayEntity(getTextFromSelector(curpDocument, SipsoSelector.BIRTHDAY_ENTITY)));
    }

    @CrossOrigin(origins = {"http://trudata-live.s3-website.us-east-2.amazonaws.com", "http://localhost:3000"})
    @GetMapping("/query/{curp}/name/{name}")
    public ResponseEntity<ApiResponse> query(
            @PathVariable("curp") final String queriedCurp,
            @PathVariable("name") final String queriedName) throws IOException {

        final Document curpDocument = Jsoup.connect(String.format(index, queriedCurp)).get();
        final String name = curpDocument.select(environment.getProperty(SipsoSelector.NAME)).text();

        if(!queriedName.equals(name)) {
            return ResponseEntity.ok(new SipsoApiResponse());
        }

        return ResponseEntity.ok(new SipsoApiResponse()
                .withCurp(getTextFromSelector(curpDocument, SipsoSelector.CURP))
                .withLastName(getTextFromSelector(curpDocument, SipsoSelector.LAST_NAME))
                .withSecondLastName(getTextFromSelector(curpDocument, SipsoSelector.SECOND_LAST_NAME))
                .withName(getTextFromSelector(curpDocument, SipsoSelector.NAME))
                .withGender(getTextFromSelector(curpDocument, SipsoSelector.GENDER))
                .withBirthday(getTextFromSelector(curpDocument, SipsoSelector.BIRTHDAY))
                .withBirthdayEntity(getTextFromSelector(curpDocument, SipsoSelector.BIRTHDAY_ENTITY)));
    }

    private String getTextFromSelector(final Document document, final String selector) {

        return  document.select(environment.getProperty(selector)).text().trim();
    }
}
