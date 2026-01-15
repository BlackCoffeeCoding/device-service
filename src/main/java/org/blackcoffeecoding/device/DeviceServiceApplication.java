package org.blackcoffeecoding.device;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;

@SpringBootApplication(
        scanBasePackages = {"org.blackcoffeecoding.device"},
        exclude = {DataSourceAutoConfiguration.class}
)
@EnableHypermediaSupport(type = HypermediaType.HAL) // Включаем режим ссылок
public class DeviceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeviceServiceApplication.class, args);
    }
}