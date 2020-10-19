package com.carpark.demo;
import javax.persistence.*;
import javax.validation.constraints.Pattern;
import java.util.Objects;



@Entity
class Driver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    @Pattern(regexp = "[A-Z]{2}\\d{7}") //licence number format is 2 letters followed by 7 digits
    private String licenseNum;

    private Category category;


    public Driver() { }

    public Driver( String name, String licenseNum,Category category) {
        this.name = name;
        this.licenseNum = licenseNum;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLicenseNum() {
        return licenseNum;
    }

    public Category getCategory() {
        return category;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLicenseNum(String licenseNum) {
        this.licenseNum = licenseNum;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.name, this.licenseNum, this.category);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "Driver: Id = '" + this.id
                + "' Name = '" + this.name
                + "' Licence number = '" + this.licenseNum
                + "' Category = '" + this.category
                + "'";
    }
}
