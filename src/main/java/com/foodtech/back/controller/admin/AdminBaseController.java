package com.foodtech.back.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AdminBaseController {

    @RequestMapping(path = "/")
    public String adminHomePage() {
        return "admin";
    }

    @GetMapping("favicon.ico")
    @ResponseBody
    public void returnNoFavicon() { }
}
