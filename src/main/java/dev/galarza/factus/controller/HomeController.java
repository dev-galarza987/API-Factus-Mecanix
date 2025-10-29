package dev.galarza.factus.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/")
@Tag(name = "Home", description = "Endpoint de bienvenida")
public class HomeController {

    @GetMapping
    @Operation(summary = "Página de bienvenida con UI")
    public String home(Model model) {
        model.addAttribute("appName", "Factus");
        model.addAttribute("appDescription", "Sistema de Facturación Electrónica");
        model.addAttribute("version", "1.0");
        model.addAttribute("empresa", "Galarza TechCorp");
        model.addAttribute("iva", "13%");
        model.addAttribute("it", "3%");
        model.addAttribute("year", LocalDateTime.now().getYear());
        model.addAttribute("currentTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

        return "index";
    }
}



