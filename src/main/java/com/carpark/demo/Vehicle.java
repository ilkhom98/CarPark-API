package com.carpark.demo;

import javax.persistence.*;
import javax.validation.constraints.Pattern;
import java.util.Objects;


@Entity
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //plate number format should be either
    //2 digits, 1 letter, 3 digits, 2 letters. Example - "01N877LA"
    //5 digits followed by 3 letters. Example - "01454GTA"
    @Column(unique=true)
    @Pattern(regexp = "\\d{2}[A-Z]\\d{3}[A-Z]{2}|\\d{5}[A-Z]{3}")
    private String plateNum;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Category category;

    @ManyToOne
    private Driver driver;

    public Vehicle(){}

    public Vehicle(String plateNum, Category category) {
        this(plateNum,category,null);
    }

    public Vehicle(String plateNum, Category category, Driver driver) {
        this.plateNum = plateNum;
        this.category = category;
        this.driver = driver;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlateNum() {
        return plateNum;
    }

    public void setPlateNum(String plate_num) {
        this.plateNum = plate_num;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    @Override
    public int hashCode() {
        return  Objects.hash(this.id,this.plateNum,this.category,this.driver);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "Car: Id = '"+this.id+"' Plate_num = '"+this.plateNum +"' Category = '"+this.category+"' "+this.driver+"";
    }
}
