package com.ahmed.authservice.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class DemoController {

    private static final Logger log = LoggerFactory.getLogger(DemoController.class);

    @GetMapping("${api.prefix}/secure")
    public String secure() {
        log.info("HIT /api/secure");
        return "You are authenticated";
    }

    @GetMapping("${api.prefix}/user/hello")
    public String user() {
        return "Hello USER";
    }

    @GetMapping("${api.prefix}/admin/hello")
    public String admin() {
        return "Hello ADMIN";
    }
}