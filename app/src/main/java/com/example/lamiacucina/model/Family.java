package com.example.lamiacucina.model;

public class Family {
    String ID;
    String Name;
    String Role;
    String FamilyId;
    String Email;
    String isAccountCreated;

    public Family() {
    }

    public Family(String ID, String name, String role, String familyId, String email, String isAccountCreated) {
        this.ID = ID;
        Name = name;
        Role = role;
        FamilyId = familyId;
        Email = email;
        this.isAccountCreated = isAccountCreated;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getRole() {
        return Role;
    }

    public void setRole(String role) {
        Role = role;
    }

    public String getFamilyId() {
        return FamilyId;
    }

    public void setFamilyId(String familyId) {
        FamilyId = familyId;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getIsAccountCreated() {
        return isAccountCreated;
    }

    public void setIsAccountCreated(String isAccountCreated) {
        this.isAccountCreated = isAccountCreated;
    }
}
