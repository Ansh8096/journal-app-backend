package net.engineerAnsh.journalApp.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/public")
@Tag(name = "Public APIs", description = "Application status")
public class PublicController {

    @GetMapping("/health-check")
    @Operation(summary = "Application's current health status")
    public String healthCheckup() {
        return "ok";
    }
}
