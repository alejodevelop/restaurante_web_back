package com.usco.edu.service;

import java.util.List;

import com.usco.edu.entities.Contrato;

public interface IContratoService {

	public List<Contrato> obtenerContratos(String userdb);

	public List<Contrato> obtenerContrato(String userdb, int codigo);
	
	public List<Contrato> obtenerContratosByVigencia(String userdb, int codigoUaa, String fecha);

	public int crearContrato(String userdb, Contrato contrato);

	public int actualizarContrato(String userdb, Contrato contrato);
}
