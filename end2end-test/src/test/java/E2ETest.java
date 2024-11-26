import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.vertx.core.json.JsonObject;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.hamcrest.Matchers.*;

public class E2ETest {
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost:10000";  // Usa la variabile d'ambiente per il gateway
    }

    @Test
    public void testUserJourney() {
        // Step 1: Creare un utente
        String userId = "testUser123";
        Response createUserResponse = RestAssured.given()
            .contentType("application/json")
            .body(new JsonObject().put("userId", userId).encode())
            .post("/api/users")
            .then()
            .statusCode(200)
            .extract().response();
        
        String createdUserId = createUserResponse.jsonPath().getString("user.userId");
        // Step 2: Creare una bicicletta
        Response getBikeIds = RestAssured.given()
            .contentType("application/json")
            .get("/api/ebikes/ids")
            .then()
            .statusCode(200)
            .extract().response();

        String ebikeId = getBikeIds.jsonPath().getString("ebikes[0]");
        
        // // Step 3: Noleggiare una bicicletta
        Response getRideId = RestAssured.given()
            .contentType("application/json")
            .body(new JsonObject().put("userId", userId).put("ebikeId", ebikeId).encode())
            .post("/api/rides")
            .then()
            .statusCode(200)
            .extract().response();

        String rideId = getRideId.jsonPath().getString("ride.rideId");

        // // Step 4: Restituire la bicicletta
        RestAssured.given()
            .contentType("application/json")
            .body(new JsonObject().put("userId", userId).put("rideId", rideId).encode())
            .delete("/api/rides")
            .then()
            .statusCode(200);
    } 
}
