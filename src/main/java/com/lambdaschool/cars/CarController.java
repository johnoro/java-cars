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
      .filter(car -> tester.test(car)) // case-insensitive check for being the same
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
    send("Data loaded"); // should probably be loading

    return repository.saveAll(cars);
  }

  @DeleteMapping("delete/{id}")
  public Car deleteCar(@PathVariable Long id) {
    Car car = get(id);

    repository.deleteById(id);

    send(id + " Data deleted");

    return car;
  }
}
