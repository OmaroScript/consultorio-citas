package com.hospital.consultoriocitas.controller;

import com.hospital.consultoriocitas.model.Cita;
import com.hospital.consultoriocitas.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    @Autowired
    private CitaService citaService;

    @PostMapping("/nueva")
    public ResponseEntity<?> agendarCita(@RequestBody Cita cita) {
        try {
            return ResponseEntity.ok(citaService.agendarCita(cita));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/editar/{id}")
    public ResponseEntity<?> editarCita(@PathVariable Long id, @RequestBody Cita citaActualizada) {
        try {
            return ResponseEntity.ok(citaService.editarCita(id, citaActualizada));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/cancelar/{id}")
    public ResponseEntity<?> cancelarCita(@PathVariable Long id) {
        try {
            citaService.cancelarCita(id);
            return ResponseEntity.ok("Cita cancelada");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Nuevo: Obtener citas de un doctor en una fecha específica
    @GetMapping("/doctor/{doctorId}/fecha")
    public ResponseEntity<?> obtenerCitasPorDoctorYFecha(@PathVariable Long doctorId, @RequestParam("fecha") String fechaStr) {
        try {
            // Parsear la fecha recibida como String a LocalDateTime
            LocalDateTime fecha = LocalDateTime.parse(fechaStr + "T00:00:00");

            // Obtener las citas del doctor en esa fecha
            List<Cita> citas = citaService.consultarCitas(doctorId, fecha);
            return ResponseEntity.ok(citas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al obtener citas: " + e.getMessage());
        }
    }

    // Nuevo: Obtener todas las citas (opcional, si quieres listar todas)
    @GetMapping("/todas")
    public ResponseEntity<?> obtenerTodasLasCitas() {
        try {
            List<Cita> citas = citaService.obtenerTodasLasCitas();
            return ResponseEntity.ok(citas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al obtener todas las citas: " + e.getMessage());
        }
    }
}
