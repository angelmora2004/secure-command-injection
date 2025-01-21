package com.ciberseguridad.api.Controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PingController {
    
    @PostMapping("/ping")
    public ResponseEntity<String> ping(@RequestBody Map<String, String> request) {
        String ip = request.get("ip");

        // Validar que la entrada sea una dirección IP
        if (!ip.matches("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b")) {
            return ResponseEntity.badRequest().body("Entrada inválida: No es una IP válida.");
        }

        try {
            // Ejecutar el comando ping de forma segura
            ProcessBuilder processBuilder = new ProcessBuilder(
                System.getProperty("os.name").toLowerCase().contains("win") ? "ping" : "/bin/ping",
                System.getProperty("os.name").toLowerCase().contains("win") ? "-n" : "-c",
                "4", ip);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                     .body("Error ejecutando el comando ping.");
            }

            return ResponseEntity.ok(output.toString());
        } catch (Exception e) {
            // Registrar errores en el servidor
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error procesando la solicitud: " + e.getMessage());
        }
    }
}
