package com.jtok.spring.examples.myecom.integration;

import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.awaitility.Awaitility;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
public class EcommerceSagaIT {

    public static final String DEPOT = "depot";
    public static final String ECOMMERCE = "ecommerce";
    public static final String PAYMENTS = "payments";
    public static final String ACTUATOR_HEALTH = "/actuator/health";
    public static final String USERID = "panks";

    @Container
    static DockerComposeContainer services =
            new DockerComposeContainer(
                    new File("src/main/docker/docker-compose.yml"))
                    .withEnv("DOCKER_BUILDKIT", "1")
                    .withEnv("ECOMMERCE_PASSWORD", "ecommerce")
                    .withEnv("PAYMENTS_PASSWORD", "payments")
                    .withEnv("DEPOT_PASSWORD", "depot")
                    .withEnv("POSTGRES_PASSWORD", "postgres")
                    .withLocalCompose(true)
                    .withOptions("--compatibility")
                    .withExposedService(DEPOT, 8080, Wait.forHttp(ACTUATOR_HEALTH))
                    .withExposedService(ECOMMERCE, 8080, Wait.forHttp(ACTUATOR_HEALTH))
                    .withExposedService(PAYMENTS, 8080, Wait.forHttp(ACTUATOR_HEALTH));

    @NotNull
    private URL depotEndpoint(String path) throws MalformedURLException {
        return urlForService(DEPOT, path);
    }

    @NotNull
    private URL ecommerceEndpoint(String path) throws MalformedURLException {
        return urlForService(ECOMMERCE, path);
    }

    @NotNull
    private URL paymentsEndpoint(String path) throws MalformedURLException {
        return urlForService(PAYMENTS, path);
    }

    @NotNull
    private URL urlForService(String service, String path) throws MalformedURLException {
        URL url = new URL("http",
                services.getServiceHost(service, 8080),
                services.getServicePort(service, 8080),
                path);
        log.info("Providing endpoint url for depot " + url.toString());
        return url;
    }

    @Test
    @Order(1)
    void articleDefinition() throws MalformedURLException {
        given().contentType(ContentType.JSON)
                .body("{\n" +
                        "  \"name\": \"scarpe-eleganti\",\n" +
                        "  \"description\": \"scarpe eleganti\"\n" +
                        "}").when()
                .post(depotEndpoint("/api/articles"))
                .then().statusCode(HttpStatus.SC_CREATED);

        given().contentType(ContentType.JSON)
                .body("{\n" +
                        "  \"name\": \"calze-seta\",\n" +
                        "  \"description\": \"calze di seta\"\n" +
                        "}\n").when()
                .post(depotEndpoint("/api/articles"))
                .then().statusCode(HttpStatus.SC_CREATED);

        JsonPath body = Awaitility.await().atMost(5, TimeUnit.SECONDS).until(
                () -> given().get(ecommerceEndpoint("/api/catalogArticles"))
                        .then().extract().body().jsonPath(), b -> b.getInt("_embedded.catalogArticles.size()") == 2
        );

        assertThat(body.getInt("_embedded.catalogArticles.find {it.name == 'scarpe-eleganti'}.quantity")).isEqualTo(0);
        assertThat(body.getInt("_embedded.catalogArticles.find {it.name == 'calze-seta'}.quantity")).isEqualTo(0);
    }

    @Test
    @Order(2)
    void operationHandling() throws MalformedURLException {

        given().contentType(ContentType.JSON)
                .body("[\n" +
                        "  {\n" +
                        "    \"article\": \"scarpe-eleganti\",\n" +
                        "    \"quantity\": 50\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"article\": \"calze-seta\",\n" +
                        "    \"quantity\": 250\n" +
                        "  }\n" +
                        "]").when()
                .post(depotEndpoint("/api/operations/place"))
                .then().statusCode(HttpStatus.SC_CREATED);

        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(
                () -> given().get(ecommerceEndpoint("/api/catalogUpdates"))
                        .then().extract().body().jsonPath(), b -> b.getInt("_embedded.catalogUpdates.size()") == 1
        );

        given().get(ecommerceEndpoint("/api/catalogArticles")).then()
                .body("_embedded.catalogArticles.find {it.name == 'scarpe-eleganti'}.quantity", equalTo(50))
                .body("_embedded.catalogArticles.find {it.name == 'calze-seta'}.quantity", equalTo(250));
    }

    @Test
    @Order(3)
    void paymentAccount() throws MalformedURLException {
        given().contentType(ContentType.JSON)
                .body("{\n" +
                        "  \"userId\": \"" + USERID + "\",\n" +
                        "  \"notes\": \"Panks account\"\n" +
                        "}\n").when()
                .post(paymentsEndpoint("/api/accounts"))
                .then().statusCode(HttpStatus.SC_CREATED);

        given().contentType(ContentType.JSON)
                .body("250").when()
                .post(paymentsEndpoint("/api/accounts/" + USERID + "/deposit"))
                .then().statusCode(HttpStatus.SC_CREATED);

        given().when()
                .get(paymentsEndpoint("/api/accounts/" + USERID + "/balance"))
                .then().statusCode(HttpStatus.SC_OK)
                .body("balance", equalTo(250.0F));

    }

    @Test
    @Order(4)
    void orderWorkflow() throws MalformedURLException {

        String granTotal = "123.56";

        //order creation on ecommerce service. The order is
        // in initial state  CREATED
        long orderId = given().contentType(ContentType.JSON)
                .body("{\n" +
                        "  \"customer\": \"panks\",\n" +
                        "  \"granTotal\": " + granTotal + ",\n" +
                        "  \"itemValues\": [\n" +
                        "    {\"article\": \"scarpe-eleganti\", \"quantity\": 1},\n" +
                        "    {\"article\": \"calze-seta\", \"quantity\": 2}\n" +
                        "  ]\n" +
                        "}").when()
                .post(ecommerceEndpoint("/api/order"))
                .then().statusCode(HttpStatus.SC_CREATED)
                .body("status", equalTo("CREATED"))
                .body("granTotal", equalTo(new BigDecimal(granTotal).floatValue()))
                .body("items.find {it.catalogArticle.name == 'scarpe-eleganti'}.quantity", equalTo(1))
                .body("items.find {it.catalogArticle.name == 'calze-seta'}.quantity", equalTo(2))
                .extract().body().jsonPath().getLong("id");

        //if articles stocks are present the order can pass in  APPROVED state and
        //the quantity is updated on the depot service and on the ecommerce catalog service
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(
                () -> given().get(ecommerceEndpoint("/api/orders/" + orderId))
                        .then().extract().body().jsonPath(), b -> b.getString("status").equals("APPROVED")
        );

        given().get(depotEndpoint("/api/articles"))
                .then().statusCode(HttpStatus.SC_OK)
                .body("_embedded.articles.find {it.name == 'scarpe-eleganti'}.stockQuantity", equalTo(49))
                .body("_embedded.articles.find {it.name == 'calze-seta'}.stockQuantity", equalTo(248));

        given().get(ecommerceEndpoint("/api/catalogArticles"))
                .then().statusCode(HttpStatus.SC_OK)
                .body("_embedded.catalogArticles.find {it.name == 'scarpe-eleganti'}.quantity", equalTo(49))
                .body("_embedded.catalogArticles.find {it.name == 'calze-seta'}.quantity", equalTo(248));

        // we proceed with checkout
        given().patch(ecommerceEndpoint("/api/order/" + orderId + "/checkout"))
                .then().statusCode(HttpStatus.SC_OK);

        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(
                () -> given().get(ecommerceEndpoint("/api/orders/" + orderId))
                        .then().extract().body().jsonPath(), b -> b.getString("status").equals("PAYED")
        );

        given().when()
                .get(paymentsEndpoint("/api/accounts/" + USERID + "/balance"))
                .then().statusCode(HttpStatus.SC_OK)
                .body("balance", equalTo(126.44F));
    }
}
