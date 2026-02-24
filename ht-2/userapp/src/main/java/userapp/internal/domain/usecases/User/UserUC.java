package userapp.internal.domain.usecases.User;

import java.util.List;

import userapp.internal.controllers.cli.IUserUC;
import userapp.internal.domain.exceptions.DomainException;
import userapp.internal.domain.exceptions.Excepts;
import userapp.internal.domain.usecases.User.dto.CreateUserInput;
import userapp.internal.domain.usecases.User.dto.UpdateUserInput;
import userapp.internal.domain.user.dto.CreateUser;
import userapp.internal.domain.user.dto.UpdateUser;
import userapp.internal.domain.user.model.User;

public class UserUC implements IUserUC {
    private IUserService userService;
    private IDateTime clock;
    private IUUIDGenerator generator;

    public UserUC(IUserService userService, IDateTime clock, IUUIDGenerator generator) {
        this.userService = userService;
        this.clock = clock;
        this.generator = generator;
    }

    public User createUser(CreateUserInput req) throws DomainException {
        return userService.createUser(
            new CreateUser(
                generator.genUUIDString(),
                req.getName(),
                req.getEmail(),
                req.getAge(),
                clock.now()
            )
        );
    }

    public void updateUser(UpdateUserInput req) throws DomainException {
        try {
            if (userService.getUser(req.getId()) == null) {
                throw Excepts.notFountException;
            }
        } catch (DomainException exception) {
            throw exception;
        }

        userService.updateUser(
            new UpdateUser(
                req.getId(),
                req.getName(),
                req.getEmail(),
                req.getAge()
            )
        );
    }

    public void removeUser(String id) throws DomainException {
        userService.removeUser(id);
    }

    public List<User> pagination(int page, int size) throws DomainException {
        page = Math.max(0, page);
        size = Math.max(1, size);
        return userService.getList(page, size);
    }
}
