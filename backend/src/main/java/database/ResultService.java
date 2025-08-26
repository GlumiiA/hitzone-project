package database;

import entity.Result;
import entity.User;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;

@Stateless
public class ResultService {
    private static final EntityManager em = Persistence.createEntityManagerFactory("default").createEntityManager();

    public boolean saveResultToDb(Result result) {
        try {
            em.getTransaction().begin();
            em.persist(result);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            em.getTransaction().rollback();
            return false;
        }
    }

    public void clearResultsForUser(User user) {
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Result r WHERE r.user = :user")
                .setParameter("user", user)
                .executeUpdate();
        em.getTransaction().commit();
    }


}
