package userapp.internal.domain.usecases.User;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Locale;

import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import com.github.javafaker.Faker;

import userapp.internal.domain.exceptions.DomainException;
import userapp.internal.domain.usecases.User.dto.CreateUserInput;
import userapp.internal.domain.usecases.User.dto.UpdateUserInput;
import userapp.internal.domain.user.dto.UpdateUser;
import userapp.internal.domain.user.model.User;
import userapp.internal.domain.user.service.UserService;
import userapp.lib.DateTime;
import userapp.lib.UUIDGenerator;

public class UserUCTest {
    UserUC uc;
    UserService userServiceMock;
    UUIDGenerator gen;
    DateTime dt;
    Faker faker;

    @BeforeAll
    public void init() {
        dt = new DateTime();
        gen = new UUIDGenerator();
        faker = new Faker(Locale.US);
    }

    @BeforeEach
    public void setUp() {
        userServiceMock = Mockito.mock(UserService.class);
        uc = new UserUC(
            userServiceMock,
            dt,
            gen
        );
    }

    @Test
    public void createUserTest() {
        User user = new User(
                gen.genUUIDString(),
                faker.name().toString(), 
                faker.internet().emailAddress(),
                Integer.valueOf(faker.number().numberBetween(20, 35)),
                dt.now()
        );

        assertDoesNotThrow(() -> 
            Mockito.when(
                userServiceMock.createUser(Mockito.any())
            ).thenReturn(user)
        );

        assertDoesNotThrow(() -> {
            User gotUser = uc.createUser(
                new CreateUserInput(
                    user.getName(),
                    user.getEmail(),
                    user.getAge()
                )
            );

            assertNotNull(gotUser);
            assertNotNull(user);

            assertEquals(user, gotUser);
        });
    }

    @Test
    public void updateUserTest() {
        assertDoesNotThrow(() -> {
            UpdateUserInput req = new UpdateUserInput(
                gen.genUUIDString(),
                faker.name().toString(),
                faker.internet().emailAddress(),
                Integer.valueOf(faker.number().numberBetween(20, 35))
            );
            uc.updateUser(req);

            UpdateUser updReq = new UpdateUser(
                req.getId(),
                req.getName(),
                req.getEmail(),
                req.getAge()
            );
            Mockito.verify(userServiceMock).updateUser(updReq);
        });
    }

    @Test
    public void removeUserTest() {
        assertDoesNotThrow(() -> {
            uc.removeUser("id");
            Mockito.verify(userServiceMock).removeUser("id");
        });
    }

    @Test
    public void paginationTest() {
        try {
            uc.pagination(10, 10);
        } catch (DomainException e) {
            e.printStackTrace();
        } finally {
            assertDoesNotThrow(() -> Mockito.verify(userServiceMock).getList(10, 10));
        }

        try {
            uc.pagination(-10, -10);
        } catch (DomainException e) {
            e.printStackTrace();
        } finally {
            assertDoesNotThrow(() -> Mockito.verify(userServiceMock).getList(0, 1));
        }
    }
}
