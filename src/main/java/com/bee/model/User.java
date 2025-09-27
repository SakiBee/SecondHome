package com.bee.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String mobileNumber;
    private String email;
    private String city;
    private String postCode;
    private String password;
    private String image;
    private String role;
    private Boolean isEnable;

    private Boolean accountNonLocked;
    private Integer failedAttempt;
    private Date lockTime;

    private String resetToken;


}
