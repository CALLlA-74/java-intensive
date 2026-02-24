package userapp.internal.domain.user.dao.postgresql;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import userapp.internal.adapters.postgresql.PSQLSessionFactory;
import userapp.internal.domain.exceptions.DomainException;
import userapp.internal.domain.user.dto.CreateUser;
import userapp.internal.domain.user.dto.UpdateUser;
import userapp.internal.domain.user.model.User;
import userapp.internal.domain.user.service.IUserDAO;

public class PostgreSQLStorage implements IUserDAO {

    @Override
    public void create(CreateUser req) throws DomainException {
        try (Session s = PSQLSessionFactory.getSessionFactory().openSession()) {
            Transaction t = (Transaction) s.beginTransaction();
            s.persist(UserStorage.create(req));
            t.commit();
        } catch (Exception e) {
            throw new DomainException(e.getMessage());
        }
    }

    @Override
    public User get(String id) throws DomainException {
        try (Session s = PSQLSessionFactory.getSessionFactory().openSession()) {
            return getUserStorage(s, id).toDomain();
        } catch (Exception e) {
            throw new DomainException(e.getMessage());
        }
    }

    private UserStorage getUserStorage(Session s, String id) throws HibernateException {
        return s.get(UserStorage.class, id);
    }

    @Override
    public void update(UpdateUser req) throws DomainException {
        try (Session s = PSQLSessionFactory.getSessionFactory().openSession()) {
            UserStorage u = getUserStorage(s, req.getId()).update(req);;
            
            Transaction t = (Transaction) s.beginTransaction();
            s.merge(u);
            t.commit();
        } catch (Exception e) {
            throw new DomainException(e.getMessage());
        }
    }

    @Override
    public void removeByID(String id) throws DomainException {
        try (Session s = PSQLSessionFactory.getSessionFactory().openSession()) {
            Transaction t = (Transaction) s.beginTransaction();
            s.remove(new UserStorage().setId(id));
            t.commit();
        } catch (Exception e) {
            throw new DomainException(e.getMessage());
        }
    }

    @Override
    public List<User> getList(int limit, int offset) throws DomainException {
        List<UserStorage> l = null;
        try (Session s = PSQLSessionFactory.getSessionFactory().openSession()) {
            l = s.createQuery("FROM USERS", UserStorage.class)
                .setFirstResult(offset).setMaxResults(limit).getResultList();
        } catch (Exception e) {
            throw new DomainException(e.getMessage());
        }

        if (l != null) {
            List<User> u = new ArrayList(l.size());
            for (UserStorage us : l) {
                u.add(us.toDomain());
            }
            return u;
        }
        return null;
    }
}