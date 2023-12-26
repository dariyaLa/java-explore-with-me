//package ru.practicum;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.boot.test.web.server.LocalServerPort;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import ru.practicum.model.ErrorResponse;
//
//import java.net.URI;
//import java.net.URISyntaxException;
//
//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.equalTo;
//
//@Slf4j
//@ExtendWith({MockitoExtension.class, SpringExtension.class})
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@RequiredArgsConstructor
//public class ExceptionHandleTest {
//
//    @Autowired
//    private TestRestTemplate restTemplate;
//
//    @LocalServerPort
//    int randomServerPort;
//
//    @Autowired
//    private StatsServiceImpl service;
//
//    @Test
//    void notFoundParameterStartDate() throws URISyntaxException {
//        final String baseUrl = "http://localhost:" + randomServerPort + "/stats";
//        URI uri = new URI(baseUrl);
//
//        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
//                uri,
//                HttpMethod.GET,
//                HttpEntity.EMPTY,
//                ErrorResponse.class);
//        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
//        assertThat(response.getBody().getMessageError(), equalTo("В запросе ожидается параметр start"));
//    }
//}
