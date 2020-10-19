package com.carpark.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication  implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DemoApplication.class);

    @Autowired private DriverRepository driverRepository;
    @Autowired private VehicleRepository vehicleRepository;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(String... args) {

        log.info("StartApplication...");

        //Test DataBase population
        driverRepository.save(new Driver("Artur","AD9948454",Category.MOTORCYCLE));
        driverRepository.save(new Driver("Ibrat","AB9483729",Category.BUS));
        driverRepository.save(new Driver("Ilkhom","BC2954748",Category.CAR));
        vehicleRepository.save(new Vehicle("01N877LA", Category.CAR, driverRepository.getOne(3L)));
        vehicleRepository.save(new Vehicle("01454GTA", Category.BUS ));

    }
}
