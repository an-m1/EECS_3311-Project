package ca.yorku.eecs;

import java.io.IOException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.v1.Record;

import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import static org.neo4j.driver.v1.Values.parameters;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class Movie implements HttpHandler {
	
	public Movie() {
		
	}
	
	public void handle(HttpExchange r) {
        try {
            if (r.getRequestMethod().equals("PUT")) {
                handlePut(r);
            } else if (r.getRequestMethod().equals("GET")) {
                handleGet(r);
            } else {
                r.sendResponseHeaders(404, -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	public void handlePut(HttpExchange r) throws IOException, JSONException {
        
		String body = DBConnect.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);
        int statusCode = 0;
        String name;
        String movieId;
  
        if (deserialized.has("name"))
            name = deserialized.getString("name");
        else
            name = "";
        if (deserialized.has("movieId"))
        	movieId = deserialized.getString("movieId");
        else
        	movieId = "";
        
        System.out.println("INPUTS: name: " + name + " movieId: " + movieId);
        
        try (Session session = DBConnect.driver.session()) {
            if (name.isEmpty() || movieId.isEmpty()) {
                statusCode = 400; // BAD REQUEST
            } else {
                // Check if the movie already exists
                StatementResult result = session.run("MATCH (m:Movie {movieId:$movieId}) RETURN m", parameters("movieId", movieId));
                if (result.hasNext()) {
                    // Movie already exists, handle edge case
                    statusCode = 400; // BAD REQUEST
                } else {
                    // Create a new movie node
                    session.run("CREATE (m:Movie {name:$name, movieId:$movieId});", parameters("name", name, "movieId", movieId));
                    System.out.println("The Neo4j transaction ran");
                    statusCode = 200; // OK
                }
            }
        } catch (Exception e) {
            System.err.println("Caught Exception: " + e.getMessage());
            statusCode = 500; // INTERNAL SERVER ERROR
        }
        r.sendResponseHeaders(statusCode, -1);
    }

    public void handleGet(HttpExchange r) throws IOException, JSONException {
        String body = DBConnect.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);
        int statusCode = 0;
        String movieId;
  
        if (deserialized.has("movieId"))
        	movieId = deserialized.getString("movieId");
        else
        	movieId = "";
        
        System.out.println("INPUT: movieId: " + movieId);
        
        try (Session session = DBConnect.driver.session()) {
            if (movieId.isEmpty()) {
                statusCode = 400; // BAD REQUEST
            } else {
                // Check if the movie exists
                StatementResult result = session.run("MATCH (m:Movie {movieId:$movieId}) RETURN m.name AS name, m.movieId AS movieId", parameters("movieId", movieId));
                if (result.hasNext()) {
                    Record record = result.next();
                    JSONObject response = new JSONObject();
                    response.put("name", record.get("name").asString());
                    response.put("movieId", record.get("movieId").asString());

                    // Retrieve the actors who acted in this movie
                    StatementResult actorsResult = session.run("MATCH (a:Actor)-[:ACTED_IN]->(m:Movie {movieId:$movieId}) RETURN a.actorId AS actorId", parameters("movieId", movieId));
                    Set<String> actors = new HashSet<>();
                    while (actorsResult.hasNext()) {
                        Record actorRecord = actorsResult.next();
                        actors.add(actorRecord.get("actorId").asString());
                    }
                    response.put("actors", actors);
                    String jsonResponse = response.toString();
                    r.sendResponseHeaders(200, jsonResponse.length());
                    r.getResponseBody().write(jsonResponse.getBytes());
                    r.getResponseBody().close();
                } else {
                    statusCode = 404; // NOT FOUND
                }
            }
        } catch (Exception e) {
            System.err.println("Caught Exception: " + e.getMessage());
            statusCode = 500; // INTERNAL SERVER ERROR
        }
        r.sendResponseHeaders(statusCode, -1);
    }
}
//this is a test
