package com.example.demo.model;





import javax.persistence.*;


@Entity
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // private String firstName;
    // private String lastName;
    // private int age;

    // // Constructors, getters, setters, and other methods

    // // Constructors
    // public Place() {
    //     // Default constructor
    // }

    // public Place(String firstName, String lastName, int age) {
    //     this.firstName = firstName;
    //     this.lastName = lastName;
    //     this.age = age;
    // }

    // // Getters and setters
    // public Long getId() {
    //     return id;
    // }

    // public void setId(Long id) {
    //     this.id = id;
    // }

    // public String getFirstName() {
    //     return firstName;
    // }

    // public void setFirstName(String firstName) {
    //     this.firstName = firstName;
    // }

    // public String getLastName() {
    //     return lastName;
    // }

    // public void setLastName(String lastName) {
    //     this.lastName = lastName;
    // }

    // public int getAge() {
    //     return age;
    // }

    // public void setAge(int age) {
    //     this.age = age;
    // }

    // // Other methods, if needed
}




