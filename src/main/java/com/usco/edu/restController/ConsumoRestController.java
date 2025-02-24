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
import com.usco.edu.entities.Consumo;
import com.usco.edu.entities.Qr;
import com.usco.edu.service.IConsumoService;
import com.usco.edu.service.serviceImpl.EncrypDecryptService;

@RestController
@RequestMapping(path = "consumo")
public class ConsumoRestController {

	@Autowired
	private IConsumoService consumoService;

	@Autowired
	EncrypDecryptService customRSAService;

	@GetMapping(path = "obtener-consumo/{username}/{codigoPersona}/{codigoContrato}")
	public List<Consumo> obtenerConsumoByPerCodigo(@PathVariable String username, @PathVariable int codigoPersona,
			@PathVariable int codigoContrato) {
		return consumoService.obtenerConsumoByPerCodigo(username, codigoPersona, codigoContrato);
	}

	@GetMapping(path = "obtener-consumos-diarios/{codigoTipoServicio}/{CodigoContrato}")
	public int obtenerConsumosDiarios(@PathVariable int codigoTipoServicio, @PathVariable int CodigoContrato) {
		return consumoService.obtenerConsumosDiarios(codigoTipoServicio, CodigoContrato);
	}
	
	@GetMapping(path = "obtener-consumos-diarios-gabus/{codigoTipoServicio}/{CodigoContrato}")
	public int obtenerConsumosDiariosGabus(@PathVariable int codigoTipoServicio, @PathVariable int CodigoContrato) {
		return consumoService.obtenerConsumosDiariosGabus(codigoTipoServicio, CodigoContrato);
	}

	@PutMapping(path = "actualizar-consumo/{username}")
	public int actualizarConsumo(@PathVariable String username, @RequestBody Consumo consumo) {
		return consumoService.actualizarConsumo(username, consumo);
	}

	@PostMapping(path = "cargue-informacion/{username}")
	public List<Long> cargueInformacion(@PathVariable String username, 
            @RequestParam("jsonData") MultipartFile jsonData) throws JsonParseException, JsonMappingException, IOException {
		List<Consumo> consumos_json = consumoService.convertirJsonAConsumos(jsonData);
		return consumoService.cargarConsumos(username, consumos_json);
	}

	@PutMapping(path = "validar-consumo/{username}/{uaaCodigo}")
	public int validarConsumo(@PathVariable String username, @PathVariable int uaaCodigo, @RequestBody Qr qr) {
		return consumoService.registrarConsumo(username, uaaCodigo, qr);
	}

}
