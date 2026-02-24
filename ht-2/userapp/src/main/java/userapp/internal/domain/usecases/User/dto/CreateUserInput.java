package userapp.internal.domain.usecases.User.dto;

import com.github.javafaker.Faker;

public class CreateUserInput {
    private String name;
    private String email; 
    private Integer age;

    public CreateUserInput(String name, String email, Integer age){
        this.name = name;
        this.email = email;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Integer getAge() {
        return age;
    }

    public static CreateUserInput generate(Faker faker) {
        return new CreateUserInput(
            faker.name().name(),
            faker.internet().emailAddress(),
            Integer.valueOf(faker.number().numberBetween(20, 35))
        );
    }
}
