package com.jamiekettenhofen.inventory_project_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.boot.autoconfigure.domain.EntityScan;
// import org.springframework.context.annotation.ComponentScan;
// import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
// @EnableJpaRepositories("com.jamiekettenhofen.inventory_project_backend.*")
// @ComponentScan(basePackages = { "com.jamiekettenhofen.inventory_project_backend.*" })
// @EntityScan("com.jamiekettenhofen.inventory_project_backend.*")   
public class InventoryProjectBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryProjectBackendApplication.class, args);
	}

}
