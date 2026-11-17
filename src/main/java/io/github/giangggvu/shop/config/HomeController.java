package io.github.giangggvu.shop.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Visiting the bare domain opens the interactive API docs instead of a 401. */
@Controller
class HomeController {

    @GetMapping("/")
    String home() {
        return "redirect:/swagger-ui.html";
    }
}
