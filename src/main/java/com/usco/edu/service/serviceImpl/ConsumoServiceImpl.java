package com.usco.edu.service.serviceImpl;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.usco.edu.dao.IConsumoDao;
import com.usco.edu.entities.Consumo;
import com.usco.edu.entities.Qr;
import com.usco.edu.service.IConsumoService;

@Service
public class ConsumoServiceImpl implements IConsumoService {

	@Autowired
	private IConsumoDao ConsumoDao;

	@Override
	public List<Consumo> obtenerConsumoByPerCodigo(String userdb, int codigoPersona, int codigoContrato) {
		return ConsumoDao.obtenerConsumoByPerCodigo(userdb, codigoPersona, codigoContrato);
	}
	
	@Override
	public int obtenerConsumosDiarios(int tipoServicio, int codigoContrato) {
		return ConsumoDao.obtenerConsumosDiarios(tipoServicio, codigoContrato);
	}

	@Override
	public int obtenerConsumosDiariosGabus(int tipoServicio, int codigoContrato) {
		return ConsumoDao.obtenerConsumosDiariosGabus(tipoServicio, codigoContrato);
	}
	
	@Override
	public int registrarConsumo(String username, int uaaCodigo, Qr qr) {
		return ConsumoDao.registrarConsumo(username, uaaCodigo, qr);
	}

	@Override
	public int actualizarConsumo(String userdb, Consumo consumo) {
		return ConsumoDao.actualizarConsumo(userdb, consumo);
	}

	@Override
	public List<Long> cargarConsumos(String userdb, List<Consumo> consumos) {
		return ConsumoDao.cargarConsumos(userdb, consumos);
	}

	@Override
	public List<Consumo> convertirJsonAConsumos(MultipartFile jsonData)
			throws JsonParseException, JsonMappingException, IOException {
		return ConsumoDao.convertirJsonAConsumos(jsonData);
	}
	
}
