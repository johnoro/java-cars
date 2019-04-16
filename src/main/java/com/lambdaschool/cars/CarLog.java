package com.lambdaschool.cars;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CarLog implements Serializable {
  private final String message;
  private final String date;

  public CarLog(String message) {
    this.message = message;
    String format = "yyyy-MM-dd hh:mm:ss a";
    this.date = new SimpleDateFormat(format).format(new Date());
  }

  @Override
  public String toString() {
    return "CarLog{" +
      "message='" + message + '\'' +
      ", date='" + date + '\'' +
      '}';
  }
}
