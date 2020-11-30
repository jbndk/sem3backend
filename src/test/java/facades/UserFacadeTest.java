package facades;

import dtos.UserDTO;
import entities.Favourite;
import entities.User;
import errorhandling.AlreadyExistsException;
import errorhandling.MissingInputException;
import errorhandling.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.hamcrest.Matchers;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
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
public class UserFacadeTest {

    private static EntityManagerFactory emf;
    private static UserFacade facade;
    private static User u1, u2, u3;
    private static Favourite f1, f2, f3;

    public UserFacadeTest() {

    }

    @BeforeAll
    public static void setUpClass() {
        emf = EMF_Creator.createEntityManagerFactoryForTest();
        facade = UserFacade.getUserFacade(emf);

        u1 = new User("user11", "test11");
        u2 = new User("user12", "test12");
        u3 = new User("user13", "test13");

        f1 = new Favourite("Belgium");
        f2 = new Favourite("France");
        f3 = new Favourite("Sweden");
        u1.addFavourite(f1);
        u1.addFavourite(f3);
        u2.addFavourite(f2);
        u3.addFavourite(f3);

        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();
            em.createNamedQuery("User.deleteAllRows").executeUpdate();

            em.persist(u1);
            em.persist(u2);
            em.persist(u3);

            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @AfterAll
    public static void tearDwonClass() {

    }

    @AfterEach
    public void tearDown() {

    }

    @Test
    public void testGetUserFacade() {
        EntityManagerFactory emf = null;
        UserFacade expectedResult = null;
        UserFacade result = UserFacade.getUserFacade(emf);
        assertNotEquals(expectedResult, result);
    }

    @Test
    public void testDeleteUser() throws NotFoundException {
        int expectedCount = 2;

        facade.deleteUser(u2.getUserName());
        List<UserDTO> allUsers = new ArrayList<>();
        allUsers = facade.getAllUsers();
        int actualCount = allUsers.size();

        assertEquals(expectedCount, actualCount);

    }

    @Test
    public void testGetAllUsers() {
        int expectedCount = 3;
        int actual = facade.getAllUsers().size();
        assertEquals(expectedCount, actual);
    }

    @Test
    public void testGetFavouritesByCount() {
        int actual = facade.getFavorites(u1.getUserName()).size();
        int exp = 2;
        assertEquals(exp, actual);

    }

    @Test
    public void testGetFavouritesByContent() {
        List<Favourite> favList = new ArrayList<Favourite>();
        favList = facade.getFavorites(u1.getUserName());

        assertThat(favList, hasItem(
                Matchers.<Favourite>hasProperty("countryName", is("Sweden"))));

    }

    @Test
    public void testAddFavourite() throws MissingInputException, AlreadyExistsException {
        facade.addFavourite("Poland", u2.getUserName());
        int actual = facade.getFavorites(u2.getUserName()).size();
        int exp = 2;
        assertEquals(exp,actual);
        
    }

}
