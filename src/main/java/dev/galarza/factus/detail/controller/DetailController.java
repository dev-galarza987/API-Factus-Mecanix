package dev.galarza.factus.detail.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/details")
@Tag(name = "Detalles", description = "API de detalles de factura (se gestionan a través de facturas)")
@SecurityRequirement(name = "bearerAuth")
public class DetailController {

    @GetMapping
    @Operation(summary = "Información sobre detalles")
    public ResponseEntity<String> info() {
        return ResponseEntity.ok("Los detalles se gestionan a través del endpoint de facturas");
    }
}
