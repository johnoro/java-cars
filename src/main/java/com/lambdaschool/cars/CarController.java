package com.lambdaschool.cars;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("cars")
public class CarController {
  private final CarRepository repository;
  private final RabbitTemplate template;

  private Car get(Long id) {
    return repository.findById(id)
      .orElseThrow(() -> new CarNotFoundException(id));
  }

  private List<Car> filter(CheckCar tester) {
    return repository.findAll().stream()
      .filter(car -> tester.test(car))
      .collect(Collectors.toList());
  }

  private void send(String message) {
    CarLog messageLog = new CarLog(message);
    template.convertAndSend(CarsApplication.QUEUE_NAME, messageLog.toString());
  }

  public CarController(CarRepository repository, RabbitTemplate template) {
    this.repository = repository;
    this.template = template;
  }

  @GetMapping("id/{id}")
  public Car oneById(@PathVariable Long id) {
    return get(id);
  }

  @GetMapping("year/{year}")
  public List<Car> someByYear(@PathVariable int year) {
    return filter(car -> car.getYear() == year);
  }

  @GetMapping("brand/{brand}")
  public List<Car> someByMake(@PathVariable String brand) {
    send("search for " + brand);
                          // case-insensitive check for being the same
    return filter(car -> car.getBrand().compareToIgnoreCase(brand) == 0);
  }

  @PostMapping("upload")
  public List<Car> addCars(@RequestBody List<Car> cars) {
    List<Car> addedCars = repository.saveAll(cars);
    send("Data loaded");
    return addedCars;
  }

  @DeleteMapping("delete/{id}")
  public Car deleteCar(@PathVariable Long id) {
    Car car = get(id);
    repository.deleteById(id);
    send(id + " Data deleted");
    return car;
  }

  /* EXTRA / SELF-IMPOSED STRETCH */

  @GetMapping("")
  public List<Car> all() {
    return repository.findAll();
  }

  @GetMapping("model/{model}")
  public List<Car> someByModel(@PathVariable String model) {
    send("search for " + model);
    return filter(car -> car.getModel().compareToIgnoreCase(model) == 0);
  }

  @PutMapping("update/{id}")
  public Car updateCar(@PathVariable Long id, @RequestBody Car newCar) {
    Car updatedCar = get(id);

    updatedCar.setBrand(newCar.getBrand());
    updatedCar.setModel(newCar.getModel());
    updatedCar.setYear(newCar.getYear());

    send("updated " + id);
    return repository.save(updatedCar);
  }
}
