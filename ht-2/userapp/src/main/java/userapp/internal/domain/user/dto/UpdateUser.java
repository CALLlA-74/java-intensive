package userapp.internal.domain.user.dto;

import userapp.internal.domain.user.model.User;

public class UpdateUser {
    private String id;
    private String name;
    private String email; 
    private Integer age;

    public UpdateUser(String id, String name, String email, Integer age){
        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
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

    public boolean Equals(Object obj) {
        if (obj instanceof UpdateUser) {
            User u = (User) obj;
            return u.getName().equals(getName()) &&
                u.getId().equals(getId()) &&
                u.getEmail().equals(getEmail()) &&
                u.getAge().equals(getAge());
        }
        return false;
    }
}
