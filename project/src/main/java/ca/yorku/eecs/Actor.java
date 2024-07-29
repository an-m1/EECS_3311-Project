package ca.yorku.eecs;



import java.io.IOException;
import java.time.Instant;



import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.v1.Session;

import static org.neo4j.driver.v1.Values.parameters;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class Actor implements HttpHandler{
	
	public Actor() {
		
	}
	
	public void handle(HttpExchange r) {
        try {
            if (r.getRequestMethod().equals("GET")) {
                handlePost(r);
            }else{
                r.sendResponseHeaders(404, -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	public void handlePost(HttpExchange r) throws IOException, JSONException{
        
		String body = DBConnect.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);
        int statusCode = 0;
        String name;
        String actorId;
  
        
        if (deserialized.has("name"))
            name = deserialized.getString("name");
        else
            name = "";
        if (deserialized.has("actorId"))
        	actorId = deserialized.getString("actorId");
        else
        	actorId = "";
        
        System.out.println("INPUTS: name: "+name+" actorId: "+actorId);
        
        
        try (Session session = DBConnect.driver.session()) {
            
        	session.run("CREATE (a:Actor {name:$name, actorId:$actorId});", parameters("name", name, "actorId", actorId));
                
                System.out.println("The Neo4j transaction ran");
                statusCode = 200;
        } 
        
        catch( Exception e ) {
            System.err.println("Caught Exception: " + e.getMessage());
            statusCode = 500;
        }
        r.sendResponseHeaders(statusCode, -1);
    }

}
