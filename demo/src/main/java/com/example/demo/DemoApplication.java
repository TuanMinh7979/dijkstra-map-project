package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
// import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.example.demo.controller.MapController;


// @ComponentScan(basePackages = "com.example.demo")
@SpringBootApplication
// @EntityScan("com.example.demo.*")
// @EnableJpaRepositories("com.example.demo.model")
public class DemoApplication implements CommandLineRunner {

	@Autowired
	private MapController mapController;

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		mapController.hdleSetupGraph();
	}

}
