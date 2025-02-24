package com.usco.edu.service.serviceImpl;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.usco.edu.dao.IVentaDao;
import com.usco.edu.entities.Venta;
import com.usco.edu.service.IVentaService;

@Service
public class VentaServiceImpl implements IVentaService {

	@Autowired
	private IVentaDao VentaDao;

	@Override
	public List<Venta> obtenerVentasByPerCodigo(String userdb, int codigoPersona, int codigoContrato) {
		return VentaDao.obtenerVentasByPerCodigo(userdb, codigoPersona, codigoContrato);
	}

	@Override
	public int obtenerVentasDiariasOrdinarias(int tipoServicio, int codigoContrato) {
		return VentaDao.obtenerVentasDiariasOrdinarias(tipoServicio, codigoContrato);
	}
	
	@Override
	public int obtenerVentasDiariasGabus(int tipoServicio, int codigoContrato) {
		return VentaDao.obtenerVentasDiariasGabus(tipoServicio, codigoContrato);
	}
	
	@Override
	public int registrarVentas(String userdb, List<Venta> ventas) {
		return VentaDao.registrarVentas(userdb, ventas);
	}

	@Override
	public int actualizarVenta(String userdb, Venta venta) {
		return VentaDao.actualizarVenta(userdb, venta);
	}

	@Override
	public List<Long> cargarVentas(String userdb, List<Venta> ventas) {
		return VentaDao.cargarVentas(userdb, ventas);
	}

	@Override
	public int eliminarVenta(String userdb, Venta venta) {
		return VentaDao.eliminarVenta(userdb, venta);
	}

	@Override
	public List<Venta> convertirJsonAVentas(MultipartFile jsonData) throws JsonParseException, JsonMappingException, IOException {
		return VentaDao.convertirJsonAVentas(jsonData);
	}

}
