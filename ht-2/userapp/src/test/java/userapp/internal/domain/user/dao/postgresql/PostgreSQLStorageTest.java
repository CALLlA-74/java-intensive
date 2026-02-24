package userapp.internal.domain.user.dao.postgresql;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.github.javafaker.Faker;

import userapp.internal.domain.exceptions.DomainException;
import userapp.internal.domain.user.dto.CreateUser;
import userapp.internal.domain.user.dto.UpdateUser;
import userapp.internal.domain.user.model.User;
import userapp.lib.DateTime;
import userapp.lib.UUIDGenerator;

@Testcontainers
public class PostgreSQLStorageTest {
    @SuppressWarnings("resource")
    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("user")
            .withPassword("password");

    private static SessionFactory sessionFactory;

    PostgreSQLStorage storage;
    UUIDGenerator g;
    DateTime dt;
    Faker f;

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

    @BeforeEach
    public void init() {
        storage = new PostgreSQLStorage();
        g = new UUIDGenerator();
        dt = new DateTime();
        f = new Faker();
    }

    @AfterAll
    static void tearDown() {
        if (sessionFactory != null) sessionFactory.close();
        if (postgres != null) postgres.close();
    }

    @Test
    public void createAndGetTest() {
        CreateUser req = makeCreationReq();
        testCreation(req);
    }

    private CreateUser makeCreationReq() {
        return new CreateUser(
            g.genUUIDString(),
            f.name().toString(),
            f.internet().emailAddress(),
            Integer.valueOf(f.number().numberBetween(20, 35)),
            dt.now()
        );
    }

    private void testCreation(CreateUser req) {
        assertDoesNotThrow(() -> storage.create(req));
        assertDoesNotThrow(() -> {
            User u = storage.get(req.getId());

            assertEquals(req.getId(), u.getId());
            assertEquals(req.getName(), u.getName());
            assertEquals(req.getEmail(), u.getEmail());
            assertEquals(req.getAge(), u.getAge());
            assertEquals(req.getCreatedAt(), u.getCreatedAt());
        });
    }

    @Test
    public void updateTest() {
        CreateUser req = makeCreationReq();
        testCreation(req);

        UpdateUser updReq = new UpdateUser(
            req.getId(), 
            f.name().toString(),
            req.getEmail(),
            req.getAge()
        );
        
        assertDoesNotThrow(() -> storage.update(updReq));
        assertDoesNotThrow(() -> {
            User u = storage.get(updReq.getId());
            assertEquals(updReq.getName(), u.getName());
            assertEquals(updReq.getEmail(), u.getEmail());
            assertEquals(updReq.getAge(), u.getAge());
            assertEquals(req.getCreatedAt(), u.getCreatedAt());
        });
    }
    
    @Test
    public void removeByIDTest() {
        CreateUser req = makeCreationReq();
        testCreation(req);
        assertDoesNotThrow(() -> storage.removeByID(""));
        assertThrows(DomainException.class, () -> storage.get(req.getId()));
    }
    
    @Test
    public void getListTest() {
        for (int i = 0; i < 10; i++) {
            testCreation(makeCreationReq());
        }
        assertDoesNotThrow(() -> {
            List<User> all = storage.getList(2, 0);
            assertTrue(all.size() <= 2);
        });
    }
}
