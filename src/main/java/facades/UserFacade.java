package facades;

import dtos.UserInfoDTO;
import entities.Favourite;
import entities.User;
import entities.UserInfo;
import errorhandling.AlreadyExistsException;
import errorhandling.MissingInputException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import security.errorhandling.AuthenticationException;

/**
 * @author lam@cphbusiness.dk
 */
public class UserFacade {

    private static EntityManagerFactory emf;
    private static UserFacade instance;

    private UserFacade() {
    }

    /**
     *
     * @param _emf
     * @return the instance of this facade.
     */
    public static UserFacade getUserFacade(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new UserFacade();
        }
        return instance;
    }

    public User getVeryfiedUser(String username, String password) throws AuthenticationException {
        EntityManager em = emf.createEntityManager();
        User user;
        try {
            user = em.find(User.class, username);
            if (user == null || !user.verifyPassword(password)) {
                throw new AuthenticationException("Invalid user name or password");
            }
        } finally {
            em.close();
        }
        return user;
    }
    
    public UserInfoDTO addUserInfo(UserInfoDTO userinfodto, String userName) throws MissingInputException  {
        
        if (userinfodto.getMail().length() == 0 || (userinfodto.getTelno().length() == 0)) {
            throw new MissingInputException("One or both values are missing");
        }   
        
        EntityManager em = emf.createEntityManager();
        
        try {
        
        User user = em.find(User.class, userName);
        
        UserInfo info = new UserInfo(userinfodto.getMail(), userinfodto.getTelno());
        
        info.setUser(user);
        
        em.getTransaction().begin();
            em.persist(info);
            em.getTransaction().commit();
        
         return new UserInfoDTO(info.getEmail(),info.getPhone());
        } finally {
            em.close();
        }
    }
    
    public List<Favourite> getFavorites(String user) {
        EntityManager em = emf.createEntityManager();
        
        TypedQuery<Favourite> query = em.createQuery("select f FROM Favourite f INNER JOIN f.users User WHERE User.userName = :user", Favourite.class);
        query.setParameter("user", user);
        List<Favourite> resultList = query.getResultList();
        return resultList;       
        
    }
    
        public String addFavourite(String country, String userName) throws MissingInputException, AlreadyExistsException  {
            
        String lowerCountry = country.toLowerCase();
        
        EntityManager em = emf.createEntityManager();
        
        if (lowerCountry.length() == 0 || (userName.length() == 0)) {
            throw new MissingInputException("One or both values are missing");
        } 
        
        Favourite favourite = new Favourite(lowerCountry);
        
        List<Favourite> usersFavorites = getFavorites(userName);
        
        boolean favoriteExists = usersFavorites.stream().anyMatch(o -> o.getCountryName().equals(lowerCountry));
        
        if (favoriteExists== true) {
            System.out.println(getFavorites(userName));
            throw new AlreadyExistsException("You have already saved the destination");
        } else {
            
            
            try {
                
                User user = em.find(User.class, userName);
                
                user.addFavourite(favourite);
                
                em.getTransaction().begin();
                em.persist(user);
                em.getTransaction().commit();
                
                return favourite.getCountryName();
            } finally {
                em.close();
            }
        }
        }

}
