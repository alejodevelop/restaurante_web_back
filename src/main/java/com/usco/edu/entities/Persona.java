package com.usco.edu.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "persona", schema = "dbo")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Persona implements Serializable{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "per_codigo", columnDefinition = "integer")
	private Long codigo;
	
	@Column(name = "per_nombre")
	private String nombre;

	@Column(name = "per_apellido")
	private String apellido;
	
	@Column(name = "per_identificacion")
	private String identificacion;
	
	@Column(name = "per_email_interno")
	private String emailInterno;

	public String getEmailInterno() {
		return emailInterno;
	}

	public void setEmailInterno(String emailInterno) {
		this.emailInterno = emailInterno;
	}

	public Long getCodigo() {
		return codigo;
	}

	public void setCodigo(Long codigo) {
		this.codigo = codigo;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getApellido() {
		return apellido;
	}

	public void setApellido(String apellido) {
		this.apellido = apellido;
	}

	public String getIdentificacion() {
		return identificacion;
	}

	public void setIdentificacion(String identificacion) {
		this.identificacion = identificacion;
	}
	
	private static final long serialVersionUID = 1L;

}