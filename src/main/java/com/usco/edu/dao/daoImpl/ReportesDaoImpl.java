package com.usco.edu.dao.daoImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.usco.edu.dao.IReportesDao;
import com.usco.edu.entities.ReporteVenta;
import com.usco.edu.resultSetExtractor.ReporteVentaSetExtractor;

@Repository
public class ReportesDaoImpl implements IReportesDao {


	@Autowired
	@Qualifier("JDBCTemplateConsulta")
	public JdbcTemplate jdbcTemplate;

	@Override
	public List<ReporteVenta> obtenerReporteVentas(int sede, String inicio, String fin) {

		String sql = "WITH VigenciaReciente AS ( " +
		        "    SELECT rgg.per_codigo, MAX(rgg.rgg_vigencia) AS rgg_vigencia, MAX(rgg.rgg_codigo) AS rgg_codigo_reciente " +
		        "    FROM sibusco.restaurante_grupo_gabu rgg " +
		        "    WHERE rgg.rgg_vigencia >= CONVERT(DATE, GETDATE()) " +
		        "    GROUP BY rgg.per_codigo " +
		        ") " +
		        "SELECT * FROM sibusco.restaurante_venta rv " +
		        "INNER JOIN dbo.persona p ON rv.per_codigo = p.per_codigo " +
		        "LEFT JOIN VigenciaReciente vr ON rv.per_codigo = vr.per_codigo " +
		        "LEFT JOIN sibusco.restaurante_grupo_gabu rgg ON rv.per_codigo = rgg.per_codigo AND rgg.rgg_vigencia = vr.rgg_vigencia AND rgg.rgg_codigo = vr.rgg_codigo_reciente " +
		        "LEFT JOIN sibusco.restaurante_tipo_gabu rtg ON rgg.rtg_codigo = rtg.rtg_codigo " +
		        "INNER JOIN sibusco.restaurante_tipo_servicio rts ON rv.rts_codigo = rts.rts_codigo " +
		        "INNER JOIN sibusco.restaurante_contrato rc ON rv.rco_codigo = rc.rco_codigo " +
		        "INNER JOIN sibusco.restaurante_tipo_contrato rtc ON rc.rtc_codigo = rtc.rtc_codigo " +
		        "INNER JOIN sibusco.restaurante_sede rs ON rv.uaa_codigo = rs.uaa_codigo " +
		        "WHERE rv.rve_eliminado != 0 " +
		        "AND rv.uaa_codigo = ? " +
		        "AND CONVERT(DATE, rv.rve_fecha) BETWEEN ? AND ? " +
		        "ORDER BY rv.rve_fecha DESC, rv.rts_codigo ASC;";

		
		return jdbcTemplate.query(sql, new ReporteVentaSetExtractor(), sede, inicio, fin);
		
	}
	
}