package userapp.internal.domain.usecases.User;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import com.github.javafaker.Faker;

import userapp.internal.domain.usecases.User.dto.CreateUserInput;
import userapp.internal.domain.user.dao.postgresql.PostgreSQLStorage;
import userapp.internal.domain.user.dao.postgresql.UserStorage;
import userapp.internal.domain.user.model.User;
import userapp.internal.domain.user.service.UserService;
import userapp.lib.DateTime;
import userapp.lib.UUIDGenerator;

public class UserUCIntegrationTest {
    @SuppressWarnings("resource")
    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("user")
            .withPassword("password");

    private static SessionFactory sessionFactory;

    UserUC uc;
    Faker f;
    Map<String, User> users;
    private int numOfUsers = 15;
    private int pageNum = 2;
    private int pageSize = 3;

    @BeforeAll
    static void setup() {
        Properties props = new Properties();
        props.put("hibernate.connection.url", postgres.getJdbcUrl());
        props.put("hibernate.connection.username", postgres.getUsername());
        props.put("hibernate.connection.password", postgres.getPassword());
        props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        props.put("hibernate.hbm2ddl.auto", "create-drop");

        Configuration cfg = new Configuration();
        cfg.setProperties(props);
        cfg.addAnnotatedClass(UserStorage.class);

        sessionFactory = cfg.buildSessionFactory();
    }

    @AfterAll
    static void tearDown() {
        if (sessionFactory != null) sessionFactory.close();
        if (postgres != null) postgres.close();
    }

    @BeforeAll
    public void init() {
        uc = new UserUC(
            new UserService(new PostgreSQLStorage()),
            new DateTime(),
            new UUIDGenerator()
        );
        f = new Faker();
        users = new HashMap<>();
    }

    @Test 
    public void testAll() {
        // 1. create [numOfUsers] random users
        for (int i = 0; i < numOfUsers; i++) {
            assertDoesNotThrow(() -> {
                CreateUserInput req = CreateUserInput.generate(f);
                User u = uc.createUser(req);
                assertEquals(req.getName(), u.getName());
                assertEquals(req.getEmail(), u.getEmail());
                assertEquals(req.getAge(), u.getAge());
                users.put(u.getId(), u);
            });
        }

        // 2. get all users
        assertDoesNotThrow(() -> {
            List<User> all = uc.pagination(0, users.size());
            assertNotNull(all);
            assertEquals(all.size(), users.size());

            for (User u : all) {
                assertNotNull(users.get(u.getId()));
            }
        });

        // 3. delete one user
        assertDoesNotThrow(() -> {
            User removed = users.entrySet().iterator().next().getValue();
            users.remove(removed.getId());
            uc.removeUser(removed.getId());
        });
        assertDoesNotThrow(() -> {
            List<User> all = uc.pagination(0, users.size());
            assertNotNull(all);
            assertEquals(all.size(), users.size());

            for (User u : all) {
                assertNotNull(users.get(u.getId()));
            }
        });

        // 4. update any username
        assertDoesNotThrow(() -> {
            User t = users.entrySet().iterator().next().getValue();
            User updated = new User(
                t.getId(),
                f.name().toString(),
                t.getEmail(),
                t.getAge(),
                t.getCreatedAt()
            );
            
            users.put(updated.getId(), updated);
            uc.removeUser(updated.getId());
        });
        assertDoesNotThrow(() -> {
            List<User> all = uc.pagination(0, users.size());
            assertNotNull(all);
            assertEquals(all.size(), users.size());

            for (User u : all) {
                assertNotNull(users.get(u.getId()));
                assertEquals(users.get(u.getId()), u);
            }
        });

        // 5. check pagination
        assertDoesNotThrow(() -> {
            List<User> all = uc.pagination(0, users.size());
            assertNotNull(all);
            assertEquals(all.size(), users.size());

            List<User> anotherPage = uc.pagination(pageNum, pageSize);
            assertEquals(pageSize, anotherPage.size());

            int i = (pageNum - 1) * pageSize;
            for (User u : anotherPage) {
                assertEquals(u, all.get(i++));
            }
        });
    }
}
