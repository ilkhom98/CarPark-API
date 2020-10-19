package com.carpark.demo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findByDriver(Driver driver);
    List<Vehicle> findByDriverId(Long driverId);
    Optional<Vehicle> findByPlateNum(String plateNum);
}
