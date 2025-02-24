package com.usco.edu.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.usco.edu.entities.Venta;

public interface IVentaService {

	public List<Venta> obtenerVentasByPerCodigo(String userdb, int codigo, int codigoContrato);
	
	public int obtenerVentasDiariasOrdinarias(int tipoServicio, int codigoContrato);
	
	public int obtenerVentasDiariasGabus(int tipoServicio, int codigoContrato);

	public int registrarVentas(String userdb, List<Venta> ventas);

	public List<Long> cargarVentas(String userdb, List<Venta> ventas);
	
	public List<Venta> convertirJsonAVentas(MultipartFile jsonData) throws JsonParseException, JsonMappingException, IOException;

    public int actualizarVenta(String userdb, Venta venta);
    
    public int eliminarVenta(String userdb, Venta venta);
}
