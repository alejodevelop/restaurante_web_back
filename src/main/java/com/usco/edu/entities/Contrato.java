package com.usco.edu.entities;

import java.io.Serializable;
import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Contrato implements Serializable {

	private int codigo;
	private TipoContrato tipoContrato;
	private Date fechaInicial;
	private Date fechaFinal;
	private int valorContrato;
	private int subsidioDesayuno;
	private int subsidioAlmuerzo;
	private int subsidioCena;
	private int pagoEstudianteDesayuno;
	private int pagoEstudianteAlmuerzo;
	private int pagoEstudianteCena;
	private int cantidadDesayunos;
	private int cantidadAlmuerzos;
	private int cantidadCenas;
	private Dependencia dependencia;
	private int estado;

	private static final long serialVersionUID = 1L;
}
