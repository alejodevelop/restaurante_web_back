package com.usco.edu.dao.daoImpl;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.usco.edu.dao.IConsumoDao;
import com.usco.edu.entities.Consumo;
import com.usco.edu.entities.Contrato;
import com.usco.edu.entities.Dependencia;
import com.usco.edu.entities.HorarioServicio;
import com.usco.edu.entities.Persona;
import com.usco.edu.entities.Qr;
import com.usco.edu.entities.TipoServicio;
import com.usco.edu.entities.Venta;
import com.usco.edu.resultSetExtractor.ConsumoSetExtractor;
import com.usco.edu.service.IContratoService;
import com.usco.edu.service.IHorarioServicioService;
import com.usco.edu.service.IVentaService;
import com.usco.edu.service.IWebParametroService;

@Repository
public class ConsumoDaoImpl implements IConsumoDao {


	@Autowired
	@Qualifier("JDBCTemplateConsulta")
	public JdbcTemplate jdbcTemplate;

	@Autowired
	@Qualifier("JDBCTemplateEjecucion")
	public JdbcTemplate jdbcTemplateEjecucion;
	
	@Autowired
	private IWebParametroService webParametroService;
	
	@Autowired
	private IContratoService contratoService;
	
	@Autowired
	private IHorarioServicioService horarioServicioService;
	
	@Autowired
	private IVentaService ventaService;
	
    // Añadir método para obtener DataSource de JdbcTemplateEjecucion
    private DataSource getDataSourceFromJdbcTemplate() {
        return jdbcTemplateEjecucion.getDataSource();
    }

	@Override
	public List<Consumo> obtenerConsumoByPerCodigo(String userdb, int codigoPersona, int codigoContrato) {
		String sql = "SELECT * FROM "
				+ "sibusco.restaurante_consumo rcn "
				+ "INNER JOIN dbo.persona p ON "
				+ "	p.per_codigo = rcn.per_codigo "
				+ "INNER JOIN sibusco.restaurante_tipo_servicio rts ON "
				+ "	rts.rts_codigo = rcn.rts_codigo "
				+ "INNER JOIN sibusco.restaurante_venta rv ON "
				+ "	rv.rve_codigo = rcn.rve_codigo "
				+ "INNER JOIN sibusco.restaurante_contrato rc ON "
				+ "	rc.rco_codigo = rcn.rco_codigo "
				+ "INNER JOIN sibusco.restaurante_tipo_contrato rtc ON "
				+ "	rtc.rtc_codigo = rc.rtc_codigo "
				+ "INNER JOIN dbo.uaa u ON "
				+ "	u.uaa_codigo = rcn.uaa_codigo "
				+ "WHERE "
				+ "	rcn.per_codigo = ? "
				+ "	AND rcn.rco_codigo = ? "
				+ " AND rcn.rcn_fecha = CONVERT(DATE, GETDATE()) " //Validacion fecha del dia de hoy! 
				+ "	AND rcn.rcn_estado = 1;";
		return jdbcTemplate.query(sql, new Object[]{codigoPersona, codigoContrato}, new ConsumoSetExtractor());
	}
	
	@Override
	public int obtenerConsumosDiarios(int tipoServicio, int codigoContrato) {
	    
	    String sql = "SELECT COUNT(*) AS cantidad_registros " +
	            "FROM sibusco.restaurante_consumo rc " +
	            "LEFT JOIN sibusco.restaurante_grupo_gabu rgg ON rc.per_codigo = rgg.per_codigo " +
	            "WHERE rc.rts_codigo = ? " +
	            "AND rc.rco_codigo = ? " +
	            "AND rc.rcn_estado = 1 " +
	            "AND rc.rcn_fecha = CONVERT(DATE, GETDATE()) " +
	            "AND (rgg.per_codigo IS NULL OR " +
	            "     rgg.rgg_estado = 0 OR " +  
	            "     (rgg.rgg_vigencia < CONVERT(DATE, GETDATE()) " +
	            "     AND NOT EXISTS (" +
	            "         SELECT 1 FROM sibusco.restaurante_grupo_gabu rgg2 " +
	            "         WHERE rgg2.per_codigo = rgg.per_codigo " +
	            "         AND rgg2.rgg_vigencia >= CONVERT(DATE, GETDATE())" +
	            "     ))" +
	            ");";

	    return jdbcTemplate.queryForObject(sql, new Object[]{tipoServicio, codigoContrato}, Integer.class);
	}
	
	@Override
	public int obtenerConsumosDiariosGabus(int tipoServicio, int codigoContrato) {
		String sql = "WITH VigenciasValidas AS ( " +
		        "    SELECT rgg.per_codigo, MAX(rgg.rgg_vigencia) AS rgg_vigencia " +
		        "    FROM sibusco.restaurante_grupo_gabu rgg " +
		        "    WHERE rgg.rgg_vigencia >= CONVERT(DATE, GETDATE()) " +
		        "    AND rgg.rgg_estado = 1 " +
		        "    GROUP BY rgg.per_codigo " +
		        ") " +
		        "SELECT COUNT(*) AS cantidad_registros " +
		        "FROM sibusco.restaurante_consumo rc " +
		        "INNER JOIN VigenciasValidas vv ON rc.per_codigo = vv.per_codigo " +
		        "WHERE rc.rco_codigo = ? " +
		        "AND rc.rts_codigo = ? " +
		        "AND rc.rcn_estado = 1 " +
		        "AND rc.rcn_fecha = CONVERT(DATE, GETDATE())";
		
		return jdbcTemplate.queryForObject(sql, new Object[]{codigoContrato,tipoServicio}, Integer.class);
	}
	
	private String[] extraerDatosQr(Qr qr) {
		String[] arregloDeDatos = obtenerArrayDatos(qr.getMensajeEncriptado());
		
		return arregloDeDatos;
		
	}
	

	@Override
	public int registrarConsumo(String username, int uaaCodigo, Qr qr) {

	    String[] datosQr = extraerDatosQr(qr);

	    if (datosQr == null) {
	        return -5;
	    }

	    int percodigo = Integer.parseInt(datosQr[0]);
	    String fechaHoraCliente = datosQr[1];

	    Boolean isQRValido = this.validarQR(fechaHoraCliente);

	    if (!isQRValido) {
	        return -2;
	    }

	    String[] fechaHoraActualFormateada = this.ObtenerFechaHoraActualFormateada();

	    String fechaFormateada = fechaHoraActualFormateada[0];
	    String horaFormateada = fechaHoraActualFormateada[1];

	    List<Contrato> contratoVigente = this.obtenerContratoVigente(username, uaaCodigo);

	    if (contratoVigente.isEmpty()) {
	        return -3;
	    }

	    List<HorarioServicio> horarioServicio = this.horarioServicioService.obtenerHorarioServicio(username, uaaCodigo);

	    if (horarioServicio.isEmpty()) {
	        return -6;
	    }

	    int tipoServicioActual = obtenerTipoServicioActual(uaaCodigo);

	    if (tipoServicioActual == -8) {
	        return -8;
	    }

	    List<Venta> ventaMasReciente = this.ventaService.obtenerVentasByPerCodigo(username, percodigo,
	            contratoVigente.get(0).getCodigo());

	    if (ventaMasReciente.isEmpty()) {
	        return -4;
	    }

	    Boolean isFechaVentaVigente = this.validarFechaTiqueteVenta(ventaMasReciente.get(0));

	    if (!isFechaVentaVigente) {
	        return -7;
	    }

        String sql = "IF NOT EXISTS ( "
                + "SELECT 1 "
                + "FROM sibusco.restaurante_consumo "
                + "WHERE per_codigo = ? "
                + "AND rts_codigo = ? "
                + "AND rcn_fecha = CONVERT(DATE, GETDATE()) "
                + ") "
                + "BEGIN "
                + "INSERT INTO sibusco.restaurante_consumo "
                + "(per_codigo, rve_codigo, rts_codigo, rco_codigo, uaa_codigo, rcn_estado, rcn_fecha, rcn_hora) "
                + "VALUES(?, ?, ?, ?, ?, ?, ?, ?) "
                + "END";
	    
	    try (Connection connection = getDataSourceFromJdbcTemplate().getConnection();
	             PreparedStatement pstmt = connection.prepareStatement(sql)){

	        connection.setAutoCommit(false); // desactiva el auto-commit para manejar transacciones manualmente

	        // ahora creo un consumo y asocio la venta al consumo creado, acto seguido
	        // desactivo dicha venta
	        Consumo consumo = this.crearConsumo(ventaMasReciente.get(0), contratoVigente, uaaCodigo, fechaFormateada,
	                horaFormateada, percodigo, tipoServicioActual, username);


	        pstmt.setInt(1, percodigo);
	        pstmt.setInt(2, tipoServicioActual);
	        pstmt.setLong(3, consumo.getPersona().getCodigo());
	        pstmt.setInt(4, consumo.getVenta().getCodigo());
	        pstmt.setInt(5, consumo.getTipoServicio().getCodigo());
	        pstmt.setInt(6, consumo.getContrato().getCodigo());
	        pstmt.setInt(7, consumo.getDependencia().getCodigo());
	        pstmt.setInt(8, consumo.getEstado());
	        pstmt.setDate(9, consumo.getFecha());
	        pstmt.setTime(10, consumo.getHora());

	        int respuestaCreacionConsumo = pstmt.executeUpdate();

	        if (respuestaCreacionConsumo > 0) {
	            this.desactivarTiqueteVenta(respuestaCreacionConsumo, ventaMasReciente.get(0), username);
	            connection.commit(); // si todo va bien, haz commit de la transacción
	            return percodigo;
	        } else {
	            connection.rollback(); // deshacer la transacción en caso de error
	            return 0;
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	        return -1000;
	    }
	}

	
	private Boolean validarFechaTiqueteVenta(Venta venta) {
	    
	    LocalDate fechaActual = LocalDate.now();
        
        Date fechaTiquete = venta.getFecha(); 
        LocalDate fechaTiqueteLocalDate = new java.sql.Date(fechaTiquete.getTime()).toLocalDate();
		
		return fechaTiqueteLocalDate.isEqual(fechaActual);
	}
	
	
	private int desactivarTiqueteVenta(int resConsumo, Venta ventaRealizada, String username) {
		
		if (resConsumo > 0) {
			Venta ventaEnviar = new Venta();
			ventaEnviar.setCodigo(ventaRealizada.getCodigo());
			ventaEnviar.setEstado(0);
			this.ventaService.actualizarVenta(username, ventaEnviar);
			return 1;
		} else {
			return 0;
		}
	}

	
	private Consumo crearConsumo(Venta ventaReciente, List<Contrato> contratoVigente, int uaa, String fechaFormateada,
			String horaFormateada, int estudiante, int tipoServicioActual, String username
			) throws ParseException {
		
		SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");

		if (ventaReciente != null) {

			Consumo consumo = new Consumo();

			Contrato contrato = new Contrato();
			contrato.setCodigo(contratoVigente.get(0).getCodigo());
			consumo.setContrato(contrato);
			Dependencia dependencia = new Dependencia();
			dependencia.setCodigo(uaa);
			consumo.setDependencia(dependencia);
			consumo.setEstado(1);

			Date fechaEnviar = formatoFecha.parse(fechaFormateada);
			java.sql.Date sqlDate = new java.sql.Date(fechaEnviar.getTime());
			consumo.setFecha(sqlDate);
			Date date = formatoHora.parse(horaFormateada);
			long milisegundos = date.getTime();
			Time horaEnviar = new Time(milisegundos);
			consumo.setHora(horaEnviar);

			Persona persona = new Persona();
			persona.setCodigo(Long.valueOf(estudiante));
			consumo.setPersona(persona);
			TipoServicio tipoServicio = new TipoServicio();
			tipoServicio.setCodigo(tipoServicioActual);
			consumo.setTipoServicio(tipoServicio);
			Venta venta = new Venta();
			venta.setCodigo(ventaReciente.getCodigo());
			consumo.setVenta(venta);

			return consumo;

		} else {
			return null;
		}
	}
	
	private int obtenerTipoServicioActual(int uaa) {
		
		String tipoServicio = this.horarioServicioService.obtenerTipoServicioActual(uaa).getTipoServicio().getNombre();
		int codigoTipoServicio = 0;

		switch (tipoServicio.toLowerCase().trim()) {
		    case "desayuno":
		    	codigoTipoServicio = 1;
		        break;
		    case "almuerzo":
		    	codigoTipoServicio = 2;
		        break;
		    case "cena":
		    	codigoTipoServicio = 3;
		        break;
		    default:
		    	codigoTipoServicio = -8;
		        break;
		}

		return codigoTipoServicio;

	}
	
	private List<Contrato> obtenerContratoVigente(String username, int uaaCodigo){
		
		String[] fechaHoraActualFormateada = this.ObtenerFechaHoraActualFormateada(); 

		String fechaFormateada = fechaHoraActualFormateada[0];
		
	    List<Contrato> contratoVigente = this.contratoService.obtenerContratosByVigencia(username, uaaCodigo,
				fechaFormateada);
		
		return contratoVigente;
		
	}
	
	
	private Boolean validarQR(String fechaHoraCliente) {

		try {
			
			int validezQR = Integer.parseInt(this.webParametroService.obtenerWebParametro().get(1).getWebValor());

			// Obtener la fecha y hora actual del servidor
			Date currentFechaHora = new Date();

			// Convertir la fecha del cliente a objeto Date
			// Date fechaCliente = new Date(fechaHoraCliente);

			SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date fechaCliente;

			try {
				fechaCliente = formato.parse(fechaHoraCliente);
			} catch (ParseException e) {
				e.printStackTrace();
				return false;
			}

			// Calcular la fecha y hora límite
			Date fechaLimite = new Date(fechaCliente.getTime() + validezQR * 1000);

			// Comparar si la fecha del cliente es anterior a la fecha límite

			boolean isQRValido = currentFechaHora.before(fechaLimite);
			
			return isQRValido;
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}
	
	// UTILS
	
	private String[] obtenerArrayDatos(String stringEncriptado) {
		try {
			// Desencriptar el mensaje encriptado
			BigInteger biginteger = new BigInteger(stringEncriptado);
			String decryptedString = new String(biginteger.toByteArray());

			// String textoDesencriptado = customRSAService.decrypt(new
			// BigInteger(stringEncriptado)).toString();
			String[] arregloDeDatos = decryptedString.split(",");
			return arregloDeDatos;
		} catch (Exception e) {
			return null;
		}
	}
	
	private String[] ObtenerFechaHoraActualFormateada() {

		try {
			
			SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");
			
			// Obtener la fecha y hora actual del servidor
			Date fechaHoraActual = new Date();

			// Formatear la fecha y hora como una cadena
			String fechaFormateada = formatoFecha.format(fechaHoraActual);
			String horaFormateada = formatoHora.format(fechaHoraActual);

			// ("fecha formateada");
			// (fechaFormateada);
			return new String[] { fechaFormateada, horaFormateada };

		} catch (Error e) {
			return null;
		}
	}
	
	

	@Override
	public int actualizarConsumo(String userdb, Consumo consumo) {

	    String sql = "UPDATE sibusco.restaurante_consumo "
	            + "SET rcn_estado = ? "
	            + "WHERE rcn_codigo = ?";

	    try (Connection connection = getDataSourceFromJdbcTemplate().getConnection();
	             PreparedStatement pstmt = connection.prepareStatement(sql)){
	    	
	        connection.setAutoCommit(false); // Desactivar auto-commit para manejar transacciones manualmente

	        // Asignar los parámetros
	        pstmt.setInt(1, consumo.getEstado());
	        pstmt.setInt(2, consumo.getCodigo());

	        // Ejecutar la actualización
	        int affectedRows = pstmt.executeUpdate();

	        if (affectedRows > 0) {
	            connection.commit(); // Hacer commit de la transacción si todo fue exitoso
	            return affectedRows;
	        } else {
	            connection.rollback(); // Hacer rollback si no se actualiza ninguna fila
	            return 0;
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	        return 0;
	    }
	}

	
	private void cerrarConexion(Connection con) {
		if (con == null)
			return;

		try {
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public List<Long> cargarConsumos(String userdb, List<Consumo> consumos) {
	    List<Long> registrosErrados = new ArrayList<>();

	    String sql = 
	        "DECLARE @perCodigo INT; " +
	        "DECLARE @codigoVenta INT; " +
	        "SET @perCodigo = (SELECT e.per_codigo FROM estudiante e WHERE e.est_codigo = ?); " + // Placeholder 1
	        "IF @perCodigo IS NULL " +
	        "BEGIN " +
	        "    SET @perCodigo = (SELECT p.per_codigo FROM persona p WHERE p.per_identificacion = ?); " + // Placeholder 2
	        "END " +
	        "SET @codigoVenta = ( " +
	        "    SELECT rev.rve_codigo " +
	        "    FROM sibusco.restaurante_venta rev " +
	        "    INNER JOIN dbo.persona p ON p.per_codigo = rev.per_codigo " +
	        "    INNER JOIN sibusco.restaurante_tipo_servicio rts ON rts.rts_codigo = rev.rts_codigo " +
	        "    INNER JOIN sibusco.restaurante_contrato rc ON rc.rco_codigo = rev.rco_codigo " +
	        "    INNER JOIN sibusco.restaurante_tipo_contrato rtc ON rtc.rtc_codigo = rc.rtc_codigo " +
	        "    INNER JOIN dbo.uaa u ON u.uaa_codigo = rev.uaa_codigo " +
	        "    WHERE rev.per_codigo = @perCodigo AND rev.rco_codigo = ? AND rev.rve_fecha = ? " + // Placeholder 3, 4
	        "    AND rev.rts_codigo = ? AND rev.uaa_codigo = ? AND rev.rve_estado = 1 " + // Placeholder 5, 6
	        "    ); " +
	        "IF @perCodigo IS NOT NULL " +
	        "BEGIN " +
	        "      IF @codigoVenta IS NOT NULL " +
	        "      BEGIN " +
	        "           UPDATE sibusco.restaurante_venta " +
	        "           SET rve_estado = 0 " +
	        "           WHERE rve_codigo = @codigoVenta; " +
	        "           INSERT INTO sibusco.restaurante_consumo " +
	        "           (per_codigo, rve_codigo, rts_codigo, rco_codigo, uaa_codigo, rcn_estado, rcn_fecha, rcn_hora) " +
	        "           VALUES (@perCodigo, @codigoVenta, ?, ?, ?, ?, ?, ?) " + // Placeholder 7, 8, 9, 10
	        "      END " +
	        "END;";

	    try (Connection connection = getDataSourceFromJdbcTemplate().getConnection();
	             PreparedStatement pstmt = connection.prepareStatement(sql)) {

	        connection.setAutoCommit(false); // Desactivar auto-commit para manejar transacciones manualmente

	        // Ejecutar el batch de actualizaciones
	        for (Consumo consumo : consumos) {
	            // Establecemos los valores para los placeholders
	            pstmt.setString(1, consumo.getPersona().getIdentificacion()); // estudiante
	            pstmt.setString(2, consumo.getPersona().getIdentificacion()); // persona
	            pstmt.setInt(3, consumo.getContrato().getCodigo()); // contrato (rco_codigo)
	            pstmt.setDate(4, consumo.getFecha()); // fecha (rve_fecha)
	            pstmt.setInt(5, consumo.getTipoServicio().getCodigo()); // tipo de servicio (rts_codigo)
	            pstmt.setInt(6, consumo.getDependencia().getCodigo()); // dependencia (uaa_codigo)
	            pstmt.setInt(7, consumo.getTipoServicio().getCodigo()); // tipo de servicio (rts_codigo en restaurante_consumo)
	            pstmt.setInt(8, consumo.getContrato().getCodigo()); // contrato (rco_codigo en restaurante_consumo)
	            pstmt.setInt(9, consumo.getDependencia().getCodigo()); // dependencia (uaa_codigo en restaurante_consumo)
	            pstmt.setInt(10, consumo.getEstado()); // estado del consumo
	            pstmt.setDate(11, consumo.getFecha()); // fecha del consumo (rcn_fecha)
	            pstmt.setTime(12, consumo.getHora()); // hora del consumo (rcn_hora)

	            pstmt.addBatch();
	        }

	        int[] updateCounts = pstmt.executeBatch();

	        // Contar inserciones insatisfactorias
	        int totalUnsuccessful = 0;
	        int totalSuccessful = 0;

	        for (int i = 0; i < updateCounts.length; i++) {
	            int count = updateCounts[i];

	            if (count < 0) {
	                totalUnsuccessful++;
	                registrosErrados.add(Long.parseLong(consumos.get(i).getPersona().getIdentificacion()));
	            } else {
	                totalSuccessful++;
	            }
	        }

	        connection.commit(); // Hacer commit de la transacción si todo fue exitoso
	        return registrosErrados;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null; // Devuelve null si ocurre una excepción
	    }
	}
	
	@Override
	public List<Consumo> convertirJsonAConsumos(MultipartFile jsonData) throws JsonParseException, JsonMappingException, IOException {
		  ObjectMapper objectMapper = new ObjectMapper();
	      return Arrays.asList(objectMapper.readValue(jsonData.getInputStream(), Consumo[].class));
	}



}
