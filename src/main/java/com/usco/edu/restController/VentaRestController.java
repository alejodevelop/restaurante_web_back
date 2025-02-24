package com.usco.edu.restController;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.usco.edu.entities.Venta;
import com.usco.edu.service.IVentaService;

@RestController
@RequestMapping(path = "venta")
public class VentaRestController {

	@Autowired
	private IVentaService ventaService;

	@GetMapping(path = "ejemplo")
	public boolean ejemplo() {

		return true;
	}

	@GetMapping(path = "obtener-venta/{username}/{codigoPersona}/{codigoContrato}")
	public List<Venta> obtenerVentasByPerCodigo(@PathVariable String username, @PathVariable int codigoPersona,
			@PathVariable int codigoContrato) {
		return ventaService.obtenerVentasByPerCodigo(username, codigoPersona, codigoContrato);
	}

	@GetMapping(path = "obtener-ventas-diarias-ordinarias/{codigoTipoServicio}/{CodigoContrato}")
	public int obtenerVentasDiarias(@PathVariable int codigoTipoServicio, @PathVariable int CodigoContrato) {
		return ventaService.obtenerVentasDiariasOrdinarias(codigoTipoServicio, CodigoContrato);
	}
	
	@GetMapping(path = "obtener-ventas-diarias-gabus/{codigoTipoServicio}/{CodigoContrato}")
	public int obtenerVentasDiariasGabus(@PathVariable int codigoTipoServicio, @PathVariable int CodigoContrato) {
		return ventaService.obtenerVentasDiariasGabus(codigoTipoServicio, CodigoContrato);
	}

	@PostMapping(path = "crear-ventas/{username}")
	public int registrarVenta(@PathVariable String username, @RequestBody List<Venta> ventas) {
		return ventaService.registrarVentas(username, ventas);
	}
	
	@PostMapping(path = "cargue-informacion/{username}")
	public List<Long> cargueInformacion(@PathVariable String username, 
            @RequestParam("jsonData") MultipartFile jsonData) throws JsonParseException, JsonMappingException, IOException {
		
		 List<Venta> ventas_json = ventaService.convertirJsonAVentas(jsonData);
		return ventaService.cargarVentas(username, ventas_json);
	}

	@PutMapping(path = "actualizar-venta/{username}")
	public int actualizarVenta(@PathVariable String username, @RequestBody Venta venta) {
		return ventaService.actualizarVenta(username, venta);
	}
	
	@PutMapping(path = "eliminar-venta/{username}")
	public int eliminarVenta(@PathVariable String username, @RequestBody Venta venta) {
		return ventaService.eliminarVenta(username, venta);
	}
}
