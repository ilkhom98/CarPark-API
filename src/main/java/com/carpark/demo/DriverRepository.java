package com.carpark.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByLicenseNum(String licence_num);
    Optional<Driver> findByCategory(Category category);

    List<Driver> findByOrderByNameAsc();
    List<Driver> findByOrderByNameDesc();

    @Query(value = "SELECT driver.* FROM driver LEFT JOIN vehicle ON driver.id = vehicle.driver_id " +
            "WHERE driver_id IS NULL",nativeQuery=true)
    List<Driver> noCars();
}
