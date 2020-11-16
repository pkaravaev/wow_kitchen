package com.foodtech.back.controller.hello;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class HelloAppController {

    @RequestMapping("/app")
    public String helloSecured() {
        return "Hello Secured World!";
    }

    @RequestMapping("/app/public")
    public String helloUnsecured() {
        return "Hello Unsecured World!";
    }
}
