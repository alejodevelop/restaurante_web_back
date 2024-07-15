package com.usco.edu.dao;

import java.util.List;

import com.usco.edu.entities.HorarioServicio;

public interface IHorarioServicioDao {

	public List<HorarioServicio> obtenerHorariosServicio(String userdb);

	public List<HorarioServicio> obtenerHorarioServicio(String userdb, int codigo);

	public int crearHorarioServicio(String userdb, HorarioServicio horarioServicio);

	public int actualizarHorarioServicio(String userdb, HorarioServicio horarioServicio);
}
