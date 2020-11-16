package com.foodtech.back.controller.hello;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloAdminController {

    @GetMapping(path = "/admin/hello")
    public String helloAdmin() {
        return "Hello Admin!";
    }

    @GetMapping(path = "/master/hello")
    public String helloMaster() {
        return "Hello Master!";
    }

    @GetMapping(path = "/admin/crypt")
    public String crypt() {
        return "crypt";
    }
}
