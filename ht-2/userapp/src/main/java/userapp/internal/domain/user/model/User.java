package userapp.internal.domain.user.model;

import java.time.LocalDateTime;

public class User {
    private String id;
    private String name;
    private String email; 
    private Integer age;
    private LocalDateTime createdAt;

    public User(String id, String name, String email, Integer age, LocalDateTime createdAt){
        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean Equals(Object obj) {
        if (obj instanceof User) {
            User u = (User) obj;
            return u.getName().equals(getName()) &&
                u.getId().equals(getId()) &&
                u.getEmail().equals(getEmail()) &&
                u.getAge().equals(getAge()) &&
                u.getCreatedAt().equals(getCreatedAt());
        }
        return false;
    }
}
