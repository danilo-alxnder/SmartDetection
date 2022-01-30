package com.example.project_mod8;

public class User {
    private String userID;
    private String currentRoom;

    public User(String userID, String currentRoom) {
        this.userID = userID;
        this.currentRoom = currentRoom;
    }

    public User(String userID) {
        this.userID = userID;
    }

    public User() {
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(String currentRoom) {
        this.currentRoom = currentRoom;
    }
}
