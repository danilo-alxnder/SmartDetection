package com.example.project_mod8;

import java.util.ArrayList;
import java.util.List;

public class Users {
    private List<User> userList;

    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

    public Users(List<User> userList) {
        this.userList = userList;
    }

    public Users() {
        userList = new ArrayList<>();
    }

    public User isUser(String userID) {
        for(User user: userList) {
            if (user.getUserID().equals(userID)) {
                return user;
            }
        }
        return null;
    }
}
