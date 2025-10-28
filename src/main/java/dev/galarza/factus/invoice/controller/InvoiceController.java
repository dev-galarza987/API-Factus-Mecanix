package dev.galarza.factus.invoice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/")
public class InvoiceController {
    @GetMapping
    public String index() {
        return "Galarza Techcorp";
    }
}
