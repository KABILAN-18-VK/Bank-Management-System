package com.bank.entity;
import jakarta.persistence.*;

@Entity
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false,unique = true)
    private String username;

    @Column(nullable = false,unique = true)
    private String emailID;

    @Column(nullable = false)
    private String password;


    @Column(name = "full_name",nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String role;

    public User(String username, String emailID,String password,String fullName,String role){
        this.username=username;
        this.emailID=emailID;
        this.password=password;
        this.fullName=fullName;
        this.role=role;
    }

    public Long getId(){
        return id;
    }

    public String getUsername(){
        return username;
    }

    public void setUsername(String username){
        this.username=username;
    }

    public String getEmail() {
        return emailID;
    }

    public void setEmail(String emailID) {
        this.emailID = emailID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullName(){
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

}