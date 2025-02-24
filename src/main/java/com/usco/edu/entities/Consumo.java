package com.usco.edu.entities;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Consumo implements Serializable {

	private int codigo;
	private Persona persona;
	private Venta venta;
	private TipoServicio tipoServicio;
	private Contrato contrato;
	private Dependencia dependencia;
	private int estado;
	private Date fecha;
	private Time hora;

	private static final long serialVersionUID = 1L;
}
