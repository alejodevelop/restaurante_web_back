package com.usco.edu.dao.daoImpl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.usco.edu.dao.IVentaDao;
import com.usco.edu.entities.Venta;
import com.usco.edu.resultSetExtractor.VentaSetExtractor;

@Repository
public class VentaDaoImpl implements IVentaDao {

	@Autowired
	@Qualifier("JDBCTemplateConsulta")
	public JdbcTemplate jdbcTemplate;

	@Autowired
	@Qualifier("JDBCTemplateEjecucion")
	public JdbcTemplate jdbcTemplateEjecucion;
	
	
    // Añadir método para obtener DataSource de JdbcTemplateEjecucion
    private DataSource getDataSourceFromJdbcTemplate() {
        return jdbcTemplateEjecucion.getDataSource();
    }

	@Override
	public List<Venta> obtenerVentasByPerCodigo(String userdb, int codigoPersona, int codigoContrato) {
		String sql = "SELECT * FROM sibusco.restaurante_venta rev "
				+ "INNER JOIN dbo.persona p ON p.per_codigo = rev.per_codigo "
				+ "INNER JOIN sibusco.restaurante_tipo_servicio rts ON rts.rts_codigo = rev.rts_codigo "
				+ "INNER JOIN sibusco.restaurante_contrato rc ON rc.rco_codigo = rev.rco_codigo "
				+ "INNER JOIN sibusco.restaurante_tipo_contrato rtc ON rtc.rtc_codigo = rc.rtc_codigo "
				+ "INNER JOIN dbo.uaa u ON u.uaa_codigo = rev.uaa_codigo " + "WHERE rev.per_codigo = ? " 
				+ " " + "AND rev.rco_codigo = ? AND rev.rve_estado = 1 " 
				+ "AND rev.rve_fecha = CONVERT(DATE, GETDATE()) " + " AND rev.rve_eliminado = 1 " //rev.rve_fecha = CONVERT(DATE, GETDATE()) valida fecha dia de hoy
				+ "ORDER BY rev.rve_fecha DESC";
		return jdbcTemplate.query(sql, new Object[]{codigoPersona, codigoContrato}, new VentaSetExtractor());
	}
	
	@Override
	public int obtenerVentasDiariasOrdinarias(int tipoServicio, int codigoContrato) {
		String sql = "SELECT COUNT(*) AS cantidad_registros FROM sibusco.restaurante_venta rv "
				+ "LEFT JOIN sibusco.restaurante_grupo_gabu rgg ON rv.per_codigo = rgg.per_codigo and rgg.rgg_estado = 1 and rgg.rgg_vigencia > (select p.per_fecha_inicio from dbo.periodo p where CONVERT(DATE, GETDATE()) BETWEEN p.per_fecha_inicio and p.per_fecha_fin) "
				+ "WHERE rv.rts_codigo = ? AND rv.rve_eliminado = 1 AND rv.rco_codigo = ? AND rv.rve_fecha = CONVERT(DATE, GETDATE()) AND rgg.per_codigo IS NULL;";

	    int cantidadRegistros = jdbcTemplate.queryForObject(sql, new Object[]{tipoServicio, codigoContrato}, Integer.class);

	    return cantidadRegistros;
	}
	
	@Override
	public int obtenerVentasDiariasGabus(int tipoServicio, int codigoContrato) {
		String sql = "SELECT COUNT(*) AS cantidad_registros FROM sibusco.restaurante_venta rv "
				+ "LEFT JOIN sibusco.restaurante_grupo_gabu rgg ON rv.per_codigo = rgg.per_codigo and rgg.rgg_estado = 1 and rgg.rgg_vigencia > (select p.per_fecha_inicio from dbo.periodo p where CONVERT(DATE, GETDATE()) BETWEEN p.per_fecha_inicio and p.per_fecha_fin) "
				+ "WHERE rv.rco_codigo = ? AND rv.rve_eliminado = 1 AND rv.rts_codigo = ? AND rv.rve_fecha = CONVERT(DATE, GETDATE()) AND rgg.per_codigo IS NOT NULL;";

		
		int cantidadRegistros = jdbcTemplate.queryForObject(sql,new Object[]{codigoContrato,tipoServicio}, Integer.class);
		
		return cantidadRegistros;
	}

	
	@Override
	public int registrarVentas(String userdb, List<Venta> ventas) {
	    int totalInserted = 0; // Inicializa el contador para el recuento total de inserciones

	    String sql = "DECLARE @cantidad_tiquetes INT; "
	            + "SELECT @cantidad_tiquetes = rhs_cantidad_tiquetes "
	            + "FROM sibusco.restaurante_horario_servicio rhs "
	            + "WHERE rhs.rhs_uaa_codigo = ? AND rhs.rts_codigo = ?; "
	            + "IF ( "
	            + "    SELECT COUNT(*) AS cantidad_registros "
	            + "    FROM sibusco.restaurante_venta rv "
	            + "    WHERE rv.per_codigo = ? "
	            + "    AND rv.rts_codigo = ? "
	            + "    AND rv.rve_estado = ? "
	            + "    AND rv.rve_fecha = CONVERT(DATE, GETDATE()) " //rev.rve_fecha = CONVERT(DATE, GETDATE()) valida fecha día de hoy
	            + ") < @cantidad_tiquetes "
	            + "BEGIN "
	            + "    INSERT INTO sibusco.restaurante_venta "
	            + "        (per_codigo, rts_codigo, rco_codigo, uaa_codigo, rve_estado, rve_fecha, rve_hora, rve_eliminado) "
	            + "    VALUES "
	            + "        (?, ?, ?, ?, ?, CONVERT(DATE, GETDATE()), CONVERT(TIME, GETDATE()), ?); "
	            + "    SELECT 1; "
	            + "END "
	            + "ELSE "
	            + "BEGIN "
	            + "    SELECT 0; "
	            + "END";

	    try (Connection connection = getDataSourceFromJdbcTemplate().getConnection();
	             PreparedStatement pstmt = connection.prepareStatement(sql)){
	        
	        connection.setAutoCommit(false); // Desactivar auto-commit para manejar transacciones manualmente

	        // Preparar el PreparedStatement con la conexión
	        
	        for (Venta venta : ventas) {
	            // Establecer los parámetros en el PreparedStatement
	            pstmt.setInt(1, venta.getDependencia().getCodigo());
	            pstmt.setInt(2, venta.getTipoServicio().getCodigo());
	            pstmt.setLong(3, venta.getPersona().getCodigo());
	            pstmt.setInt(4, venta.getTipoServicio().getCodigo());
	            pstmt.setInt(5, venta.getEstado());
	            pstmt.setLong(6, venta.getPersona().getCodigo());
	            pstmt.setInt(7, venta.getTipoServicio().getCodigo());
	            pstmt.setInt(8, venta.getContrato().getCodigo());
	            pstmt.setInt(9, venta.getDependencia().getCodigo());
	            pstmt.setInt(10, venta.getEstado());
	            pstmt.setInt(11, venta.getEliminado());

	            // Ejecutar la consulta preparada
	            int result = pstmt.executeUpdate();
	            totalInserted += result; // Agregar el resultado de la inserción al contador total
	        }

	        connection.commit(); // Hacer commit de la transacción si todo fue exitoso
	        return totalInserted;

	    } catch (SQLException e) {
	        e.printStackTrace();
	        return 0; // Retorna 0 en caso de excepción
	    }
	}




	@Override
	public int actualizarVenta(String userdb, Venta venta) {
	    int result = 0;

	    String sql = "UPDATE sibusco.restaurante_venta "
	               + "SET rve_estado = ? "
	               + "WHERE rve_codigo = ?";

	    try (Connection connection = getDataSourceFromJdbcTemplate().getConnection();
	             PreparedStatement pstmt = connection.prepareStatement(sql)){
	        connection.setAutoCommit(false); // Desactivar auto-commit para manejar transacciones manualmente

	        // Establecer los parámetros en el PreparedStatement
	        pstmt.setInt(1, venta.getEstado());
	        pstmt.setInt(2, venta.getCodigo());

	        // Ejecutar la consulta preparada
	        result = pstmt.executeUpdate();

	        connection.commit(); // Hacer commit de la transacción si todo fue exitoso
	        return result;

	    } catch (SQLException e) {
	        e.printStackTrace();
	        return 0; // Retorna 0 en caso de excepción
	    }
	}

	
	@Override
	public int eliminarVenta(String userdb, Venta venta) {
	    int result = 0;

	    String sql = "UPDATE sibusco.restaurante_venta "
	               + "SET rve_estado = ?, rve_eliminado = ? "
	               + "WHERE rve_codigo = ?";

	    try (Connection connection = getDataSourceFromJdbcTemplate().getConnection();
	             PreparedStatement pstmt = connection.prepareStatement(sql)){

	        connection.setAutoCommit(false); // Desactivar auto-commit para manejar transacciones manualmente

	        // Establecer los parámetros en el PreparedStatement
	        pstmt.setInt(1, venta.getEstado());
	        pstmt.setInt(2, venta.getEliminado());
	        pstmt.setInt(3, venta.getCodigo());

	        // Ejecutar la consulta preparada
	        result = pstmt.executeUpdate();

	        connection.commit(); // Hacer commit de la transacción si todo fue exitoso
	        return result;

	    } catch (SQLException e) {
	        e.printStackTrace();
	        return 0; // Retorna 0 en caso de excepción
	    }
	}

	@Override
	public List<Long> cargarVentas(String userdb, List<Venta> ventas) {
	    List<Long> registrosErrados = new ArrayList<>();
	    int totalUnsuccessful = 0;
	    int totalSuccessful = 0;

	    String sql = "DECLARE @perCodigo INT; "
	               + "SET @perCodigo = (SELECT e.per_codigo FROM estudiante e WHERE e.est_codigo = ?); "
	               + "IF @perCodigo IS NULL "
	               + "BEGIN "
	               + "    SET @perCodigo = (SELECT p.per_codigo FROM persona p WHERE p.per_identificacion = ?); "
	               + "END; "
	               + "IF @perCodigo IS NOT NULL "
	               + "BEGIN "
	               + "    INSERT INTO sibusco.restaurante_venta "
	               + "        (per_codigo, rts_codigo, rco_codigo, uaa_codigo, rve_estado, rve_fecha, rve_hora) "
	               + "    VALUES "
	               + "        (@perCodigo, ?, ?, ?, ?, ?, ?); "
	               + "END";

	    try (Connection connection = getDataSourceFromJdbcTemplate().getConnection();
	         PreparedStatement pstmt = connection.prepareStatement(sql)) {
	        
	        connection.setAutoCommit(false); // Desactivar auto-commit para manejar transacciones manualmente

	        for (Venta venta : ventas) {
	            // Establecer los parámetros en el PreparedStatement
	            pstmt.setString(1, venta.getPersona().getIdentificacion()); // estudianteCodigo (para la primera subconsulta)
	            pstmt.setString(2, venta.getPersona().getIdentificacion()); // id (para la segunda subconsulta si la primera falla)
	            pstmt.setInt(3, venta.getTipoServicio().getCodigo()); // rts_codigo
	            pstmt.setInt(4, venta.getContrato().getCodigo()); // rco_codigo
	            pstmt.setInt(5, venta.getDependencia().getCodigo()); // uaa_codigo
	            pstmt.setInt(6, venta.getEstado()); // rve_estado
	            pstmt.setDate(7, venta.getFecha()); // rve_fecha
	            pstmt.setTime(8, venta.getHora()); // rve_hora

	            // Ejecutar la consulta preparada
	            int result = pstmt.executeUpdate();

	            if (result < 1) {
	                totalUnsuccessful++;
	                registrosErrados.add(Long.parseLong(venta.getPersona().getIdentificacion()));
	            } else {
	                totalSuccessful++;
	            }
	        }

	        connection.commit(); // Hacer commit de la transacción si todo fue exitoso

		    // Mostrar el conteo de inserciones exitosas y no exitosas
		    System.out.println("Total no exitosos: " + totalUnsuccessful);
		    System.out.println("Total exitosos: " + totalSuccessful);

		    return registrosErrados;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null; // Devuelve null si ocurre una excepción
	    }
	}

	@Override
	public List<Venta> convertirJsonAVentas(MultipartFile jsonData) throws JsonParseException, JsonMappingException, IOException {
		  ObjectMapper objectMapper = new ObjectMapper();
	      return Arrays.asList(objectMapper.readValue(jsonData.getInputStream(), Venta[].class));
	}



}
