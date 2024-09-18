package com.hospital.consultoriocitas.model;

import jakarta.persistence.Entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Consultorio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int numeroConsultorio;
    private int piso;

    @OneToMany(mappedBy = "consultorio")
    private List<Cita> citas;

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getNumeroConsultorio() { return numeroConsultorio; }
    public void setNumeroConsultorio(int numeroConsultorio) { this.numeroConsultorio = numeroConsultorio; }
    public int getPiso() { return piso; }
    public void setPiso(int piso) { this.piso = piso; }
}
