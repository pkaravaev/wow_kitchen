package com.foodtech.back.config;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;

@Controller
@EnableScheduling
@Profile({"prod", "dev"})
public class SchedulingConfig {
}
