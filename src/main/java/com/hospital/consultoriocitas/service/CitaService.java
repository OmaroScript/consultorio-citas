package com.hospital.consultoriocitas.service;

import com.hospital.consultoriocitas.model.Cita;
import com.hospital.consultoriocitas.model.Doctor;
import com.hospital.consultoriocitas.model.Consultorio;
import com.hospital.consultoriocitas.repository.CitaRepository;
import com.hospital.consultoriocitas.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CitaService {

    @Autowired
    private CitaRepository citaRepository;
    @Autowired
    private DoctorRepository doctorRepository;

    /**
     * Agendar una nueva cita con las siguientes validaciones:
     * - No se puede agendar cita en el mismo consultorio a la misma hora.
     * - No se puede agendar cita para el mismo doctor a la misma hora.
     * - No se puede agendar cita para el mismo paciente en el mismo día con menos de 2 horas de diferencia.
     * - Un doctor no puede tener más de 8 citas en el día.
     */
    public Cita agendarCita(Cita cita) throws Exception {
        validarConsultorioDisponible(cita);
        validarDoctorDisponible(cita);
        validarPacienteDisponible(cita);
        validarCitasDiariasDoctor(cita);

        // Si pasa todas las validaciones, agendar la cita
        return citaRepository.save(cita);
    }

    /**
     * Cancelar una cita por ID.
     * - Si la cita no existe, se lanza una excepción.
     */
    public void cancelarCita(Long id) throws Exception {
        Cita cita = citaRepository.findById(id).orElseThrow(() -> new Exception("Cita no encontrada"));
        if (cita.getHorarioConsulta().isBefore(LocalDateTime.now())) {
            throw new Exception("No se puede cancelar una cita que ya pasó.");
        }
        citaRepository.delete(cita);
    }

    /**
     * Editar una cita existente.
     * - Aplica las mismas validaciones que agendar una nueva cita.
     * - Se asegura de que la nueva cita cumple con las reglas de negocio.
     */
    public Cita editarCita(Long id, Cita citaActualizada) throws Exception {
        Cita citaExistente = citaRepository.findById(id).orElseThrow(() -> new Exception("Cita no encontrada"));

        // Asegura que la cita no esté en el pasado
        if (citaExistente.getHorarioConsulta().isBefore(LocalDateTime.now())) {
            throw new Exception("No se puede editar una cita que ya ocurrió.");
        }

        // Realizamos las mismas validaciones de la nueva cita para la cita actualizada
        validarConsultorioDisponible(citaActualizada);
        validarDoctorDisponible(citaActualizada);
        validarPacienteDisponible(citaActualizada);
        validarCitasDiariasDoctor(citaActualizada);

        return citaRepository.save(citaActualizada);
    }

    // Método nuevo: Consultar citas de un doctor en una fecha específica
    public List<Cita> consultarCitas(Long doctorId, LocalDateTime fecha) throws Exception {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new Exception("Doctor no encontrado"));

        LocalDateTime inicioDia = fecha.toLocalDate().atStartOfDay();
        LocalDateTime finDia = inicioDia.plusDays(1);

        return citaRepository.findByDoctorAndHorarioConsultaBetween(doctor, inicioDia, finDia);
    }

    // Método nuevo: Obtener todas las citas
    public List<Cita> obtenerTodasLasCitas() {
        return citaRepository.findAll();
    }

    /**
     * Validación: No se puede agendar cita en el mismo consultorio a la misma hora.
     */
    private void validarConsultorioDisponible(Cita cita) throws Exception {
        List<Cita> citasConsultorio = citaRepository.findByConsultorioAndHorarioConsulta(
                cita.getConsultorio(), cita.getHorarioConsulta()
        );
        if (!citasConsultorio.isEmpty()) {
            throw new Exception("El consultorio ya está ocupado a la hora solicitada.");
        }
    }

    /**
     * Validación: No se puede agendar cita para el mismo doctor a la misma hora.
     */
    private void validarDoctorDisponible(Cita cita) throws Exception {
        List<Cita> citasDoctor = citaRepository.findByDoctorAndHorarioConsulta(
                cita.getDoctor(), cita.getHorarioConsulta()
        );
        if (!citasDoctor.isEmpty()) {
            throw new Exception("El doctor ya tiene una cita a la hora solicitada.");
        }
    }

    /**
     * Validación: No se puede agendar cita para el mismo paciente en el mismo día con menos de 2 horas de diferencia.
     */
    private void validarPacienteDisponible(Cita cita) throws Exception {
        LocalDateTime inicio = cita.getHorarioConsulta().minusHours(2);
        LocalDateTime fin = cita.getHorarioConsulta().plusHours(2);
        List<Cita> citasPaciente = citaRepository.findByNombrePacienteAndHorarioConsultaBetween(
                cita.getNombrePaciente(), inicio, fin
        );
        if (!citasPaciente.isEmpty()) {
            throw new Exception("El paciente ya tiene una cita cercana a la hora solicitada.");
        }
    }

    /**
     * Validación: Un doctor no puede tener más de 8 citas en el mismo día.
     */
    private void validarCitasDiariasDoctor(Cita cita) throws Exception {
        LocalDateTime inicioDia = cita.getHorarioConsulta().toLocalDate().atStartOfDay();
        LocalDateTime finDia = inicioDia.plusDays(1);
        List<Cita> citasDelDia = citaRepository.findByDoctorAndHorarioConsultaBetween(
                cita.getDoctor(), inicioDia, finDia
        );
        if (citasDelDia.size() >= 8) {
            throw new Exception("El doctor ya tiene 8 citas programadas para este día.");
        }
    }
}
