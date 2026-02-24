package userapp.internal.controllers.cli;

import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import com.github.javafaker.Faker;

import userapp.internal.domain.exceptions.DomainException;
import userapp.internal.domain.usecases.User.dto.CreateUserInput;
import userapp.internal.domain.usecases.User.dto.UpdateUserInput;
import userapp.internal.domain.user.model.User;

public class ConsloleInterface {
    private IUserUC userUC;
    private List<User> users;
    private int currentPage = 1;
    private static final int pageSize = 5;
    private final Faker faker;

    public ConsloleInterface(IUserUC userUC) {
        this.userUC = userUC;
        faker = new Faker(Locale.US);
    }

    public void runMainMenu() {
        int cmd;
        try (Scanner s = new Scanner(System.in)) {
            while (true) {
                if (!flushOutAndPrintUsers()) {
                    exit();
                    return;
                }
                printMainMenu();

                cmd = s.nextInt();
                switch (cmd) {
                    case 0:
                        exit();
                        return;
                    case 1:
                        currentPage++;
                        break;
                    case 2:
                        currentPage--;
                        break;
                    case 3:
                        menuOfCreation(s);
                        break;
                    case 4:
                        menuOfUpdating(s);
                        break;
                    case 5:
                        menuOfRemoving(s);
                        break;
                    case 6:
                        menuOfRandomCreation(s);
                        break;
                }
            }
        }
    }

    private void printMainMenu() {
        StringBuilder b = new StringBuilder();
        String m = b
            .append("Выберете команду:\n")
            .append("0 -- выход\n")
            .append("1 -- следующая страница\n")
            .append("2 -- предыдущая страница\n")
            .append("3 -- добавить пользователя\n")
            .append("4 -- обновить пользователя\n")
            .append("5 -- удалить пользователя\n")
            .append("6 -- создать случайного пользователя\n")
            .toString();
        System.out.println(m);
    }

    private boolean flushOutAndPrintUsers() {
        try {
            users = userUC.pagination(currentPage, pageSize);
        } catch (DomainException e) {
            System.out.println(e.toString());
            System.out.println(e.getStackTrace());
            return false;
        }

        System.out.flush();
        int i = (currentPage - 1) * pageSize;
        for (User u : users) {
            System.out.printf("%d) имя: %s\n\temail: %s\n\tвозраст: %d, создан: %s",
                ++i,
                u.getName(),
                u.getEmail(),
                u.getAge().intValue(),
                u.getCreatedAt().toString()
            );
        }
        return true;
    }

    private void menuOfRandomCreation(Scanner s) {
        int num;
        while (true) {
            System.out.println("Введите количество создаваемых пользователей: ");
            num = s.nextInt();

            if (num > 0) {
                for (int i = 0; i < num; i++) {
                    try {
                        CreateUserInput req = CreateUserInput.generate(faker);
                        userUC.createUser(req);
                    } catch (DomainException e) {
                        // TODO add log
                    }
                }
                return;
            }
        }
    }

    private void menuOfCreation(Scanner s) {
        System.out.flush();

        CreateUserInput req = getUserParamCreation(s);
        if (req != null) {
            try {
                userUC.createUser(req);
            } catch (DomainException e) {
                // TODO add log
            }
        }
    }

    private void menuOfUpdating(Scanner s) {
        int userNum = chooseUser(s, "Введите номер пользователя для обновления: ");
        if (userNum <= 0 || userNum > users.size()) return;

        CreateUserInput params = getUserParamCreation(s);
        if (params != null) {
            try {
                userUC.updateUser(
                    new UpdateUserInput(
                        users.get(userNum).getId(),
                        params.getName(),
                        params.getEmail(),
                        params.getAge()
                    )
                );
            } catch (DomainException e) {
                // TODO add log
            }
        }
    }

    private CreateUserInput getUserParamCreation(Scanner s) {
        CreateUserInputBuilder builder = new CreateUserInputBuilder();

        String param = getStringParam(s, "Введите имя пользователя: ");
        if (param == null) {
            return null;
        } else {
            builder.setName(param);
        }

        param = getStringParam(s, "Введите email пользователя: ");
        if (param == null) {
            return null;
        } else {
            builder.setEmail(param);
        }

        Integer pInteger = getIntegerParam(s, "Введите возраст пользователя: ");
        if (pInteger == null) {
            return null;
        } else {
            builder.setAge(pInteger);
        }

        return builder.build();
    }

    private Integer getIntegerParam(Scanner s, String message) {
        flushOutAndPrintUsers();
        System.out.println("0 -- вернуться в главное меню");
        System.out.println(message);

        int inp = s.nextInt();
        if (inp == 0) {
            return null;
        }
        return Integer.valueOf(inp);
    }

    private String getStringParam(Scanner s, String message) {
        flushOutAndPrintUsers();
        System.out.println("0 -- вернуться в главное меню");
        System.out.println(message);
        String inp = s.nextLine();

        if (inp.equals("0")) {
            return null;
        }
        return inp;
    }

    private void menuOfRemoving(Scanner s) {
        int userNum = chooseUser(s, "Введите номер пользователя для удаления: ");
        if (userNum <= 0 || userNum > users.size()) return;

        try {
            userUC.removeUser(users.get(userNum).getId());
        } catch (DomainException e) {
            // TODO add log
        }
    }

    private int chooseUser(Scanner s, String message) {
        while (true) {
            flushOutAndPrintUsers();
            System.out.println("0 -- вернуться в главное меню");
            System.out.println(message);

            int num = s.nextInt();
            if (num == 0) {
                return 0;
            } else if (0 < num && num <= users.size()) {
                return num;
            }
        }
    }

    private void exit() {
        System.out.println("Bye!");
    }

    private class CreateUserInputBuilder {
        private String name, email;
        private Integer age;

        public CreateUserInputBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public CreateUserInputBuilder setEmail(String email) {
            this.email = email;
            return this;
        }

        public CreateUserInputBuilder setAge(Integer age) {
            this.age = age;
            return this;
        }

        public CreateUserInput build() {
            return new CreateUserInput(name, email, age);
        }
    }
}
