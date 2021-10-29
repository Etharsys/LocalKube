package fr.uge.app.helloWorld;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@RequestMapping(value="/app")
public class HelloWorldApplication {

	public static void main(String[] args) {
		try {
			SpringApplication.run(HelloWorldApplication.class, args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(value="/hello")
	public String hello(){
		return "Hello World !";
	}

}
