package com.carpark.demo;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;


@RestController
public class CarParkController {

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;

    CarParkController(DriverRepository repository, VehicleRepository vehicleRepository) {
        this.driverRepository = repository;
        this.vehicleRepository = vehicleRepository;
    }

    ResponseEntity<?> generateCustomResponse(boolean success,String message, HttpStatus status){
        return new ResponseEntity<>(new CustomResponse(success,message),status);
    }

    /**
     * Return list of drivers.
     *
     * Method: GET
     * URL :  localhost:8080/drivers
     *
     * Optional parameter “sort” can be applied to get drivers sorted by name.
     * It can take values “asc” for ascending order and “desc” for descending one.
     * For other values of sort and if sort is not used will return unsorted list.
     *
     * Example : GET localhost:8080/drivers?sort=asc will return drivers with names sorted in ascending order
     *
     * Return :
     * Status 200 with list of drivers entities
     */
    @GetMapping(value = "/drivers", produces = "application/json")
    List<Driver> allDrivers(@RequestParam(required = false) String sort) {

        if(sort!=null && sort.equals("asc")){
            return driverRepository.findByOrderByNameAsc();
        }else if(sort!=null && sort.equals("desc")){
            return driverRepository.findByOrderByNameDesc();
        }else{
            return driverRepository.findAll();
        }
    }

    /**
     * Add driver specified in request body
     *
     * Method: POST
     * URL: localhost:8080/drivers
     *
     * Request Body:
     * {
     *     "name": "Ilkhom Rakhimov",
     *     "licenseNum": "AD2992332"
     *     "category": "CAR"
     * }
     *
     * Parameters:
     * name – driver`s name
     * licenseNum – number of license
     *              should be a string of format 2 letters followed by 7 digits
     *              which is not belong to any other driver eg. AB1234567
     * category – category of vehicle driver`s license allow
     *            Should be one of : “MOTORCYCLE”, “CAR”, “TRUCK”, “BUS”, “TRAILER”
     *
     * Return:
     *
     * Status 200 and driver entity in body on success
     * Status 400 on failure
     *
     * You can check if license belong to anybody with “Check License Presence”
     * */
    @PostMapping(value = "/drivers", produces = "application/json")
    ResponseEntity<?> newDriver(@Valid @RequestBody Driver newDriver) {
        boolean sameLicenceFound = driverRepository.findByLicenseNum(newDriver.getLicenseNum()).isPresent();
        if(!sameLicenceFound){
            return new ResponseEntity<>(driverRepository.save(newDriver), HttpStatus.OK);
        }else{
            return generateCustomResponse(false,
                    "There is a driver with same Licence number",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    /**
     * Check if there any driver with license specified
     *
     * Method: GET
     * URL: localhost:8080/drivers/licenses/{licenseNum}
     *
     * {licenseNum} is a number of license you want to check
     *
     * Return:
     *
     * Status 200 if such license number exists
     * Status 400 if such license number doesn`t exists
     * */
    @GetMapping(value = "/drivers/licenses/{licenseNum}", produces = "application/json")
    ResponseEntity<?> checkLicensePresent(@PathVariable String licenseNum){
        boolean isLicenseNumPresent = driverRepository.findByLicenseNum(licenseNum).isPresent();
        return new ResponseEntity<>(isLicenseNumPresent ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    /**
     * Return driver with id specified
     *
     * Method: GET
     * URL: localhost:8080/drivers/{id}
     *
     * {id} is an id of driver you want to get
     *
     * Return:
     *
     * Status 200 and driver entity in response body if driver with such id exists
     * Status 400 if driver with such id doesn’t exists
     * */
    @GetMapping(value = "/drivers/{driverId}", produces = "application/json")
    ResponseEntity<?> driverById(@PathVariable Long driverId) {
        Optional<Driver> foundDriver = driverRepository.findById(driverId);
        if(foundDriver.isPresent()){
            return  new ResponseEntity<>(foundDriver,HttpStatus.OK);
        }else{
            return generateCustomResponse(false,"Driver not found", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Delete driver with id specified
     *
     * Method: DELETE
     * URL: localhost:8080/drivers/{id}
     *
     * {id} is an id of driver you want to delete
     *
     * Return:
     *
     * Status 200 if driver was successfully deleted
     * Status 400 with description in body if driver with such id doesn’t exists or couldn`t be deleted,as it first should be unassigned from all cars
     *
     * */
    @DeleteMapping(value = "/drivers/{driverId}", produces = "application/json")
    ResponseEntity<?> deleteDriver(@PathVariable Long driverId) {
        try{
            driverRepository.deleteById(driverId);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (EmptyResultDataAccessException e){
            return generateCustomResponse(false,"No driver with such id", HttpStatus.BAD_REQUEST);
        }catch (Exception e){
            return generateCustomResponse(false,"Driver is assigned to car", HttpStatus.BAD_REQUEST);
        }

    }

    /**
     * Edit driver with id specified
     *
     * Method: PUT
     * URL: localhost:8080/drivers/{id}
     *
     * {id} is an id of driver you want to edit
     *
     * Request body example:
     * {
     *     "name": "Ilkhom Rakhimov",
     *     "licenseNum": "AD2992332"
     *     "category": "CAR"
     * }
     *
     * You should include only parameters you need to change
     *
     * Return:
     *
     * Status 200 and driver record, if driver was successfully edited
     * Status 400 with description in body
     * */
    @PutMapping(value = "/drivers/{driverId}", produces = "application/json")
    ResponseEntity<?> driverEdit(@Valid @RequestBody Driver driverEdits, @PathVariable Long driverId) {
        try {
            Optional<Driver> editedDriver = driverRepository.findById(driverId)
                    .map(driver -> {
                        if (driverEdits.getName() != null)
                            driver.setName(driverEdits.getName());
                        if (driverEdits.getLicenseNum() != null)
                            driver.setLicenseNum(driverEdits.getLicenseNum());
                        if (driverEdits.getCategory() != null)
                            driver.setCategory(driverEdits.getCategory());
                        return driverRepository.save(driver);
                    });

            if(editedDriver.isPresent()){
                return new ResponseEntity<>(editedDriver,HttpStatus.OK);
            }else{
                return generateCustomResponse(false,"No driver with such ID", HttpStatus.BAD_REQUEST);
            }

        }catch (DataIntegrityViolationException e){
            return generateCustomResponse(false,"Such license number already exists", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Return all vehicles assigned to driver with id specified
     *
     * Method: GET
     * URL: localhost:8080/drivers/{id}/cars
     *
     * {id} is an id of driver
     *
     * Return:
     *
     * Status 200 and array(can be empty) of vehicle entity in response body if vehicle with such id exists
     * Status 400 if driver with such id doesn’t exists
     * */
    @GetMapping(value = "/drivers/{driverId}/cars", produces = "application/json")
    ResponseEntity<?> driversCars(@PathVariable Long driverId){
        if(driverRepository.findById(driverId).isPresent()){
            return new ResponseEntity<>(
                    vehicleRepository.findByDriverId(driverId),
                    HttpStatus.OK
            );
        }else{
            return generateCustomResponse(false,"No such driver", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Return list of drivers without vehicles
     *
     * Method: GET
     * URL: localhost:8080/drivers/withoutcar
     *
     * Return:
     *
     * Status 200 and array (can be empty) of drivers entity in response body
     * */
    @GetMapping(value = "/drivers/withoutcar", produces = "application/json")
    ResponseEntity<?> driversNoCar(){
        return new ResponseEntity<>(driverRepository.noCars(),HttpStatus.OK);
    }

    /**
     * Return list of vehicles.
     *
     * Method: GET
     * URL :  localhost:8080/vehicles
     *
     * Return :
     * Status 200 with list of drivers entities
     * */
    @GetMapping(value = "/vehicles", produces = "application/json")
    List<Vehicle> allCars() {
        return vehicleRepository.findAll();
    }

    /**
     * Return list of vehicles without driver.
     *
     * Method: GET
     * URL :  localhost:8080/vehicles/no-driver
     *
     * Return :
     * Status 200 with list of vehicle without driver
     * */
    @GetMapping(value = "/vehicles/no-driver", produces = "application/json")
    List<Vehicle> noDriver() {
        return vehicleRepository.findByDriver(null);
    }

    /**
     *Return vehicle with id specified
     *
     * Method: GET
     * URL: localhost:8080/drivers/{id}
     *
     * {id} is an id of driver you want to get
     *
     * Return:
     *
     * Status 200 and vehicle entity in response body if vehicle with such id exists
     * Status 400 if vehicle with such id doesn’t exists
     * */
    @GetMapping(value = "/vehicles/{vehicleId}",produces = "application/json")
    ResponseEntity<?> getVehicleById(@PathVariable Long vehicleId){
        Optional<Vehicle> vehicle = vehicleRepository.findById(vehicleId);
        if(vehicle.isPresent()){
            return new ResponseEntity<>(vehicle.get(), HttpStatus.OK);
        }else{
            return generateCustomResponse(false,"No such vehicle", HttpStatus.BAD_REQUEST);
        }

    }

    /**
     *Return list of drivers who can drive given vehicle.
     *
     * Method: GET
     * URL :  localhost:8080//vehicles/{vehicleId}/possible-drivers
     * {vehicleId} is an id of vehicle you interested in
     *
     * Return :
     * Status 200 with list of vehicle without driver
     * Status 400 if vehicle with such id doesn’t exists
     * */
    @GetMapping(value = "/vehicles/{vehicleId}/possible-drivers",produces = "application/json")
    ResponseEntity<?> possibleDrivers(@PathVariable Long vehicleId){
        Optional<Vehicle> vehicle = vehicleRepository.findById(vehicleId);
        if(vehicle.isPresent()){
            return new ResponseEntity<>(
                    driverRepository.findByCategory(vehicle.get().getCategory()),
                    HttpStatus.OK
            );
        }else{
            return generateCustomResponse(false,"No such vehicle",HttpStatus.BAD_REQUEST);
        }

    }

    /**
     *Unassign driver from vehicle with id specified
     *
     * Method: DELETE
     * URL: localhost:8080/vehicles/{vehicleId}/driver
     *
     * {vehicleId} is an id of vehicle you interested in
     *
     * Return:
     *
     * Status 200 if driver was successfully deleted
     * Status 400 with description in body if some error happend
     * */
    @DeleteMapping(value = "/vehicles/{vehicleId}/driver",produces = "application/json")
    ResponseEntity<?> deleteVehicleDriver(@PathVariable Long vehicleId){
        Optional<Vehicle> vehicle = vehicleRepository.findById(vehicleId);
        if(vehicle.isPresent()){
            if(vehicle.get().getDriver()!=null){
                Vehicle v = vehicle.get();
                v.setDriver(null);
                return new ResponseEntity<>(vehicleRepository.save(v),HttpStatus.OK);
            }else{
                return generateCustomResponse(false,"Vehicle does not have driver",HttpStatus.BAD_REQUEST);
            }
        }else{
            return generateCustomResponse(false,"No such vehicle",HttpStatus.BAD_REQUEST);
        }

    }

    /**
     *Add driver specified in request body to specified Vehicle
     *
     * Method: POST
     * URL: localhost:8080/vehicles/{vehicleId}/driver
     *
     * {vehicleId} is an id of vehicle you interested in
     *
     * Request Body:
     * {
     *     "id": 3
     * }
     *
     * Parameters:
     * id – driver`s id
     *
     * Return:
     *
     * Status 200 and vehicle entity in body on success
     * Status 400 on failure
     * */
    @PostMapping(value = "/vehicles/{vehicleId}/driver",produces = "application/json")
    ResponseEntity<?> assignDriver(@Valid @RequestBody Driver newDriver, @PathVariable Long vehicleId){

        Optional<Vehicle> vehicle = vehicleRepository.findById(vehicleId);
        if(vehicle.isPresent()){
            if(newDriver!=null){
                Optional<Driver> driver = driverRepository.findById(newDriver.getId());
                if(driver.isPresent()){
                    if(driver.get().getCategory()==vehicle.get().getCategory()){
                        vehicle.get().setDriver(driver.get());
                        return new ResponseEntity<>(vehicleRepository.save(vehicle.get()),HttpStatus.OK);
                    }else{
                        return generateCustomResponse(false,"Driver not allowed to drive this vehicle",HttpStatus.BAD_REQUEST);
                    }
                }else{
                    return generateCustomResponse(false,"No Such driver",HttpStatus.BAD_REQUEST);
                }
            }else{
                return generateCustomResponse(false,"Body should contain driver",HttpStatus.BAD_REQUEST);
            }
        }else{
            return generateCustomResponse(false,"No such vehicle",HttpStatus.BAD_REQUEST);
        }

    }

    /**
     *Delete vehicle with id specified
     *
     * Method: DELETE
     * URL: localhost:8080/vehicles/{vehicleId}
     *
     * {vehicleId} is an id of driver you want to delete
     *
     * Return:
     *
     * Status 200 if vehicle was successfully deleted
     * Status 400 with description in body if error occurs
     * */
    @DeleteMapping(value = "/vehicles/{vehicleId}", produces = "application/json")
    ResponseEntity<?> deleteVehicle(@PathVariable Long vehicleId) {
        try{
            driverRepository.deleteById(vehicleId);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (EmptyResultDataAccessException e){
            return generateCustomResponse(false,"No vehicle with such id", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     *Add vehicle specified in request body
     *
     * Method: POST
     * URL: localhost:8080/vehicles
     *
     * Request Body:
     * {
     *     "plateNum": "01N89LA",
     *     "category": "CARS",
     *     "driver": {
     *         "id": 3
     *     }
     * }
     *
     * Parameters:
     * plateNum – Car plate number in format “01N888LA” or “01555AAA”
     * category – category of vehicle – Should be one of : “MOTORCYCLE”, “CAR”, “TRUCK”, “BUS”, “TRAILER”
     * driver – driver to assign – null if shouldn’t be assigned or entity with id parametr
     *
     * Return:
     *
     * Status 200 and vehicle entity in body on success
     * Status 400 with description in body on failure
     * */
    @PostMapping(value = "/vehicles")
    ResponseEntity<?> newVehicle(@Valid @RequestBody Vehicle newVehicle) {
        boolean samePlateFound = vehicleRepository.findByPlateNum(newVehicle.getPlateNum()).isPresent();
        if(!samePlateFound){
            if(newVehicle.getDriver()!=null){
                Optional<Driver> driver = driverRepository.findById(newVehicle.getDriver().getId());
                if(driver.isPresent()){
                    if(driver.get().getCategory()==newVehicle.getCategory()){
                        newVehicle.setDriver(driver.get());
                        return new ResponseEntity<>(
                                vehicleRepository.save(newVehicle),
                                HttpStatus.OK
                        );
                    }else{
                        return generateCustomResponse(false,"Driver not allowed to drive this vehicle",HttpStatus.BAD_REQUEST);
                    }
                }else{
                    return generateCustomResponse(false,"There is no such driver", HttpStatus.BAD_REQUEST);
                }
            }else{
                return new ResponseEntity<>(vehicleRepository.save(newVehicle), HttpStatus.OK);
            }
        }else{
            return generateCustomResponse(false,"There is a car with same Plate number",HttpStatus.BAD_REQUEST);
        }

    }

    /**
     *Edit vehicle with id specified
     *
     * Method: PUT
     * URL: localhost:8080/vehicles/{vehicleId}
     *
     * {vehicleId} is an id of vehicle you want to edit
     *
     * Request body:
     * {
     *     "plateNum": "01N89LA",
     *     "category": "CARS",
     *     "driver": {
     *         "id": 3
     *     }
     * }
     *
     * Parameters:
     * plateNum – Car plate number in format “01N888LA” or “01555AAA”
     * category – category of vehicle – Should be one of : “MOTORCYCLE”, “CAR”, “TRUCK”, “BUS”, “TRAILER”
     * driver – driver to assign – null if shouldn’t be assigned or entity with id parametr
     *
     * All parameters are optional
     *
     * Return:
     *
     * Status 200 and vehicle record, if vehicle was successfully edited
     * Status 400 with description in body
     * */
    @PutMapping(value = "/vehicles/{vehicleId}", produces = "application/json")
    ResponseEntity<?> vehicleEdit(@Valid @RequestBody Vehicle vehicleEdits, @PathVariable Long vehicleId) {
        Optional<Vehicle> vehicle = vehicleRepository.findById(vehicleId);

        if(vehicle.isPresent()){
            Vehicle editedVehicle=vehicle.get();

            if(vehicleEdits.getPlateNum()!=null){
                boolean samePlateFound = vehicleRepository.findByPlateNum(vehicleEdits.getPlateNum()).isPresent();
                if(!samePlateFound){
                    editedVehicle.setPlateNum(vehicleEdits.getPlateNum());
                }else {
                    return generateCustomResponse(false,"Car with same plate number exists", HttpStatus.BAD_REQUEST);
                }
            }

            if(vehicleEdits.getCategory()!=null){
                editedVehicle.setCategory(vehicleEdits.getCategory());
            }

            if(vehicleEdits.getDriver()!=null){
                Optional<Driver> driver = driverRepository.findById(vehicleEdits.getDriver().getId());
                if(driver.isPresent()){
                    editedVehicle.setDriver(driver.get());
                }else{
                    return generateCustomResponse(false,"There is no such driver", HttpStatus.BAD_REQUEST);
                }
                editedVehicle.setCategory(vehicleEdits.getCategory());
            }

            if(editedVehicle.getCategory()!=editedVehicle.getDriver().getCategory()){
                return generateCustomResponse(false,"Driver not allowed to drive this vehicle", HttpStatus.BAD_REQUEST);
            }else{
                return new ResponseEntity<>(vehicleRepository.save(editedVehicle),HttpStatus.OK);
            }

        }else{
            return generateCustomResponse(false,"There is no Vehicle with such id", HttpStatus.BAD_REQUEST);
        }
    }

}
