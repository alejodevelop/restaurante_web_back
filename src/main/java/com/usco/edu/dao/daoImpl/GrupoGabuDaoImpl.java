package com.usco.edu.dao.daoImpl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.usco.edu.dao.IGrupoGabuDao;
import com.usco.edu.entities.GrupoGabu;
import com.usco.edu.resultSetExtractor.GrupoGabuSetExtractor;
import com.usco.edu.resultSetExtractor.ReporteVentaSetExtractor;
import com.usco.edu.util.AuditoriaJdbcTemplate;

@Repository
public class GrupoGabuDaoImpl implements IGrupoGabuDao {

	@Autowired
	private AuditoriaJdbcTemplate jdbcComponent;

	@Autowired
	@Qualifier("JDBCTemplateConsulta")
	public JdbcTemplate jdbcTemplate;

	@Override
	public List<GrupoGabu> obtenerGrupoGabus(String userdb) {
		 
		String sql = "SELECT * FROM academia3000_alejandroc.sibusco.restaurante_grupo_gabu rgg "
				+ "INNER JOIN sibusco.restaurante_dias_beneficio rdb ON rgg.rgg_codigo = rdb.rgg_codigo "
				+ "INNER JOIN sibusco.restaurante_tipo_gabu rtg ON rgg.rtg_codigo = rtg.rtg_codigo "
				+ "INNER JOIN dbo.persona p on rgg.per_codigo = p.per_codigo "
				+ "INNER JOIN sibusco.restaurante_tipo_servicio rts ON rdb.rts_codigo = rts.rts_codigo "
				+ "INNER JOIN dbo.estudiante e ON rgg.est_codigo = e.est_codigo "
				+ "INNER JOIN dbo.programa pr ON e.pro_codigo = pr.pro_codigo "
				+ "INNER JOIN dbo.uaa u ON pr.uaa_codigo = u.uaa_codigo "
				+ "INNER JOIN dbo.dia d on rdb.dia_codigo = d.dia_codigo "
				+ "WHERE rgg.rgg_estado = 1;";

		return jdbcTemplate.query(sql, new GrupoGabuSetExtractor());
	}

	@Override
	public List<GrupoGabu> obtenerGrupoGabu(String userdb, int codigo) {
		
		String sql = "SELECT TOP 1 * "
				+ "FROM sibusco.restaurante_grupo_gabu rgg "
				+ "INNER JOIN sibusco.restaurante_dias_beneficio rdb ON rgg.rgg_codigo = rdb.rgg_codigo "
				+ "INNER JOIN sibusco.restaurante_tipo_gabu rtg ON rgg.rtg_codigo = rtg.rtg_codigo "
				+ "INNER JOIN dbo.persona p ON rgg.per_codigo = p.per_codigo "
				+ "INNER JOIN sibusco.restaurante_tipo_servicio rts ON rdb.rts_codigo = rts.rts_codigo "
				+ "INNER JOIN dbo.estudiante e ON rgg.est_codigo = e.est_codigo "
				+ "INNER JOIN dbo.programa pr ON e.pro_codigo = pr.pro_codigo "
				+ "INNER JOIN dbo.uaa u ON pr.uaa_codigo = u.uaa_codigo "
				+ "INNER JOIN dbo.dia d ON rdb.dia_codigo = d.dia_codigo "
				+ "WHERE rgg.per_codigo = ? AND GETDATE() <= rgg.rgg_vigencia "
				+ "AND rgg.rgg_estado = 1 "
				+ "ORDER BY rgg.rgg_vigencia DESC;";
		
		return jdbcTemplate.query(sql, new GrupoGabuSetExtractor(), codigo);
	}

	@Override
	public int crearGrupoGabu(String userdb, GrupoGabu grupoGabu) {
		DataSource dataSource = jdbcComponent.construirDataSourceDeUsuario(userdb);
		NamedParameterJdbcTemplate jdbc = jdbcComponent.construirTemplatenew(dataSource);

		KeyHolder keyHolder = new GeneratedKeyHolder();
		String sql = "INSERT INTO sibusco.restaurante_grupo_gabu "
				+ "(rtg_codigo, per_codigo, rgg_vigencia, rgg_estado, est_codigo) "
				+ "VALUES(:tipoGabu, :persona, :vigencia, :estado, :codigoEstudiante );";

		try {

			MapSqlParameterSource parameter = new MapSqlParameterSource();

			parameter.addValue("tipoGabu", grupoGabu.getTipoGabu().getCodigo());
			parameter.addValue("codigoEstudiante", grupoGabu.getCodigoEstudiante());
			parameter.addValue("persona", grupoGabu.getPersona().getCodigo());
			parameter.addValue("vigencia", grupoGabu.getVigencia());
			parameter.addValue("estado", grupoGabu.getEstado());

			jdbc.update(sql, parameter, keyHolder);
			return keyHolder.getKey().intValue();

		} catch (Exception e) {

			e.printStackTrace();
			return 0;
		} finally {
			try {
				cerrarConexion(dataSource.getConnection());
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public int actualizarGrupoGabu(String userdb, GrupoGabu grupoGabu) {
		DataSource dataSource = jdbcComponent.construirDataSourceDeUsuario(userdb);
		NamedParameterJdbcTemplate jdbc = jdbcComponent.construirTemplatenew(dataSource);

		String sql = "UPDATE sibusco.restaurante_grupo_gabu "
				+ "SET rtg_codigo=:tipoGabu, per_codigo=:persona, rgg_vigencia=:vigencia, rgg_estado=:estado "
				+ "WHERE rgg_codigo=:codigo";

		try {

			MapSqlParameterSource parameter = new MapSqlParameterSource();

			parameter.addValue("codigo", grupoGabu.getCodigo());
			parameter.addValue("tipoGabu", grupoGabu.getTipoGabu().getCodigo());
			parameter.addValue("persona", grupoGabu.getPersona().getCodigo());
			parameter.addValue("vigencia", grupoGabu.getVigencia());
			parameter.addValue("estado", grupoGabu.getEstado());

			return jdbc.update(sql, parameter);
		} catch (Exception e) {

			e.printStackTrace();
			return 0;
		} finally {
			try {
				cerrarConexion(dataSource.getConnection());
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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

}
