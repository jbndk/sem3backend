package rest;

import entities.Destination;
import entities.Favourite;
import entities.User;
import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import java.net.URI;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import utils.EMF_Creator;

/**
 *
 * @author am
 */
@Disabled
public class DestinationResourceTest {

    private static final int SERVER_PORT = 7777;
    private static final String SERVER_URL = "http://localhost/api";
    private static User u1, u2;
    private static Favourite f1, f2;
    private static Destination d1;

    static final URI BASE_URI = UriBuilder.fromUri(SERVER_URL).port(SERVER_PORT).build();
    private static HttpServer httpServer;
    private static EntityManagerFactory emf;

    static HttpServer startServer() {
        ResourceConfig rc = ResourceConfig.forApplication(new ApplicationConfig());
        return GrizzlyHttpServerFactory.createHttpServer(BASE_URI, rc);
    }

    @BeforeAll
    public static void setUpClass() {
        //This method must be called before you request the EntityManagerFactory
        EMF_Creator.startREST_TestWithDB();
        emf = EMF_Creator.createEntityManagerFactoryForTest();

        httpServer = startServer();
        //Setup RestAssured
        RestAssured.baseURI = SERVER_URL;
        RestAssured.port = SERVER_PORT;
        RestAssured.defaultParser = Parser.JSON;
        
        EntityManager em = emf.createEntityManager();
        u1 = new User("Hans", "test123");
        u2 = new User("Grethe", "test321");

        f1 = new Favourite("Iceland");
        f2 = new Favourite("Portugal");

        d1 = new Destination("Sweden", "SEK", "Stockholm", 9894888L);

        u1.addFavourite(f1);
        u1.addFavourite(f2);
        u2.addFavourite(f2);

        try {
            em.getTransaction().begin();
            //em.createQuery("delete from User").executeUpdate();
            em.createNamedQuery("User.deleteAllRows").executeUpdate();
            em.persist(u1);
            em.persist(u2);
            //em.persist(d1);

            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @AfterAll
    public static void closeTestServer() {
        //System.in.read();
        //Don't forget this, if you called its counterpart in @BeforeAll
        EMF_Creator.endREST_TestWithDB();
        httpServer.shutdownNow();
    }

  

    @Test
    public void contentType() {

        given().when().get("/destination/open" + u2.getUserName()).then().assertThat().contentType(ContentType.JSON);
    }

    @Test
    public void testGetFavourites() {
        given()
                .contentType("application/json")
                .get("/destination/open/favourites/" + u1.getUserName()).then()
                .assertThat()
                .statusCode(HttpStatus.OK_200.getStatusCode());

    }

    @Test
    public void testGetOpenDestination() {
        given()
                .contentType("application/json")
                .get("/destination/open/"+d1.getName()).then()
                .assertThat()
                .statusCode(HttpStatus.OK_200.getStatusCode());
    }
    
//    @Test
//    public void testSaveFavourite(){
//        given()
//                .contentType("application(json")
//                .get("/destination/open/"+d1.getName()+"/"+u2.getUserName()).then()
//                .assertThat()
//                .statusCode(HttpStatus.OK_200.getStatusCode());
//    }

}
