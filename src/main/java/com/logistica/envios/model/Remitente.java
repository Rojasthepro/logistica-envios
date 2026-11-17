package com.logistica.envios.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "remitente")
public class Remitente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRemitente;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120, message = "El nombre admite máximo 120 caracteres")
    @Column(nullable = false, length = 120)
    private String nombre;

    @NotBlank(message = "El documento es obligatorio")
    @Size(max = 20, message = "El documento admite máximo 20 caracteres")
    @Column(nullable = false, unique = true, length = 20)
    private String documento;

    @Pattern(regexp = "\\d{7,10}", message = "El teléfono debe ser numérico de 7 a 10 dígitos")
    @Column(length = 10)
    private String telefono;

    @Size(max = 150, message = "La dirección admite máximo 150 caracteres")
    @Column(length = 150)
    private String direccion;

    @Size(max = 60, message = "La ciudad admite máximo 60 caracteres")
    @Column(length = 60)
    private String ciudad;

    @NotNull(message = "El estado debe ser ACTIVO o INACTIVO")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoRemitente estado = EstadoRemitente.ACTIVO;

    public Long getIdRemitente() { return idRemitente; }
    public void setIdRemitente(Long idRemitente) { this.idRemitente = idRemitente; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public EstadoRemitente getEstado() { return estado; }
    public void setEstado(EstadoRemitente estado) { this.estado = estado; }
}
