package com.usco.edu.dao;

import java.util.List;

import com.usco.edu.entities.Consumo;
import com.usco.edu.entities.Qr;

public interface IConsumoDao {

	public List<Consumo> obtenerConsumoByPerCodigo(String userdb, int codigoPersona, int codigoContrato);
	
	public int obtenerConsumosDiarios(int tipoServicio, int codigoContrato);
	
	public int obtenerConsumosDiariosGabus(int tipoServicio, int codigoContrato);

	public int registrarConsumo(String username, int uaaCodigo, Qr qr);
	
	public List<Long> cargarConsumos(String userdb, List<Consumo> consumos);

	public int actualizarConsumo(String userdb, Consumo consumo);
}
