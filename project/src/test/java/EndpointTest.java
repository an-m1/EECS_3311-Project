package ca.yorku.eecs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class EndpointTest {

    private static HttpServer server;

    @BeforeClass
    public static void setUp() throws IOException {
        server = App.createServer();
        server.start();
        System.out.println("Test server started...");
    }

    @AfterClass
    public static void tearDown() {
        server.stop(0);
        System.out.println("Test server stopped...");
    }

    @Test
    public void addActorPass() throws IOException {
        // Prepare request
        String requestBody = new JSONObject()
                .put("name", "Tom Hanks")
                .put("actorId", "nm0000158")
                .toString();

        HttpExchange exchange = TestUtils.createMockHttpExchange("PUT", "/api/v1/addActor/", requestBody);
        
        // Handle request
        new Actor().handle(exchange);
        
        // Check database
        try (Session session = DBConnect.driver.session()) {
            StatementResult result = session.run("MATCH (a:Actor {actorId:$actorId}) RETURN a.name AS name, a.actorId AS actorId",
                parameters("actorId", "nm0000158"));
            
            assertTrue(result.hasNext());
            assertEquals("Tom Hanks", result.next().get("name").asString());
        }
        
        // Verify response
        assertEquals(200, exchange.getResponseCode());
    }

    @Test
    public void addActorFail() throws IOException {
        // Prepare request with missing actorId
        String requestBody = new JSONObject()
                .put("name", "Tom Hanks")
                .toString();

        HttpExchange exchange = TestUtils.createMockHttpExchange("PUT", "/api/v1/addActor/", requestBody);
        
        // Handle request
        new Actor().handle(exchange);
        
        // Verify response
        assertEquals(400, exchange.getResponseCode());
    }

    @Test
    public void getActorPass() throws IOException {
        // First, add the actor
        try (Session session = DBConnect.driver.session()) {
            session.run("CREATE (a:Actor {name:$name, actorId:$actorId})", parameters("name", "Leonardo DiCaprio", "actorId", "nm0000138"));
        }

        // Prepare request
        String requestBody = new JSONObject()
                .put("actorId", "nm0000138")
                .toString();

        HttpExchange exchange = TestUtils.createMockHttpExchange("GET", "/api/v1/getActor/", requestBody);
        
        // Handle request
        new Actor().handle(exchange);
        
        // Check response
        String response = TestUtils.getResponseBody(exchange);
        JSONObject jsonResponse = new JSONObject(response);
        
        assertEquals("Leonardo DiCaprio", jsonResponse.getString("name"));
        assertEquals("nm0000138", jsonResponse.getString("actorId"));
        
        // Verify response
        assertEquals(200, exchange.getResponseCode());
    }

    @Test
    public void getActorFail() throws IOException {
        // Prepare request with non-existing actorId
        String requestBody = new JSONObject()
                .put("actorId", "nm9999999")
                .toString();

        HttpExchange exchange = TestUtils.createMockHttpExchange("GET", "/api/v1/getActor/", requestBody);
        
        // Handle request
        new Actor().handle(exchange);
        
        // Verify response
        assertEquals(404, exchange.getResponseCode());
    }
}
