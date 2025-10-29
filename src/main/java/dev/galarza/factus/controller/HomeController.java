package dev.galarza.factus.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
@Tag(name = "Home", description = "Endpoint de bienvenida")
public class HomeController {

    @GetMapping
    @Operation(summary = "Página de bienvenida")
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("aplicacion", "Factus - Sistema de Facturación Electrónica");
        response.put("version", "1.0");
        response.put("empresa", "Galarza TechCorp");
        response.put("documentacion", "/swagger-ui.html");
        response.put("impuestos", Map.of(
                "IVA", "13%",
                "IT", "3%"
        ));
        response.put("endpoints", Map.of(
                "auth", "/api/auth",
                "clients", "/api/clients",
                "invoices", "/api/invoices",
                "swagger", "/swagger-ui.html"
        ));

        return ResponseEntity.ok(response);
    }
}

