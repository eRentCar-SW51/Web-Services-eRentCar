package com.acme.webserviceserentcar.car.service;

import com.acme.webserviceserentcar.car.domain.model.entity.Car;
import com.acme.webserviceserentcar.car.domain.persistence.CarRepository;
import com.acme.webserviceserentcar.car.domain.service.CarService;
import com.acme.webserviceserentcar.shared.exception.ResourceNotFoundException;
import com.acme.webserviceserentcar.shared.exception.ResourceValidationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;

@Service
public class CarServiceImpl implements CarService {
    private static final String ENTITY = "Car";
    private final CarRepository carRepository;
    private final Validator validator;


    public CarServiceImpl(CarRepository carRepository, Validator validator) {
        this.carRepository = carRepository;
        this.validator = validator;
    }

    @Override
    public List<Car> getAll() {
        return carRepository.findAll();
    }

    @Override
    public Page<Car> getAll(Pageable pageable) {
        return carRepository.findAll(pageable);
    }

    @Override
    public Car getById(Long carId) {
        return carRepository.findById(carId).orElseThrow(() -> new ResourceNotFoundException(ENTITY, carId));
    }

    @Override
    public Car create(Car request) {
        Set<ConstraintViolation<Car>> violations = validator.validate(request);

        if (!violations.isEmpty())
            throw new ResourceValidationException(ENTITY, violations);

        return carRepository.save(request);
    }

    @Override
    public Car update(Long carId, Car request) {
        Set<ConstraintViolation<Car>> violations = validator.validate(request);

        if (!violations.isEmpty())
            throw new ResourceValidationException(ENTITY, violations);
        return carRepository.findById(carId).map(car ->
                carRepository.save(car.withAddress(request.getAddress())
                        .withYear(request.getYear())
                        .withMileage(request.getMileage())
                        .withSeating(request.getSeating())
                        .withManual(request.isManual())
                        .withCarValueInDollars(request.getCarValueInDollars())
                        .withExtraInformation(request.getExtraInformation())
                        .withRentAmountDay(request.getRentAmountDay())
                        .withCategory(request.getCategory()))
        ).orElseThrow(() -> new ResourceNotFoundException(ENTITY, carId));
    }

    @Override
    public Car updateRate(Long carId, int rate) {
        Car car = carRepository.findById(carId).orElseThrow(() -> new ResourceNotFoundException(ENTITY, carId));

        if (car.getRate() == 0) {
            car.setRate(rate);
        }
        else {
            car.setRate((car.getRate() + rate) / 2);
        }

        carRepository.save(car);

        return car;
    }

    @Override
    public ResponseEntity<?> delete(Long carId) {
        return carRepository.findById(carId).map(car -> {
            carRepository.delete(car);
            return ResponseEntity.ok().build();
        } ).orElseThrow(() -> new ResourceNotFoundException(ENTITY, carId));
    }
}
