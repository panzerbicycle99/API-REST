import { Localidad, Inventario } from "../models/index.models.js";

export const crearLocalidad = async (req, res) => {
  try {
    const { nombre } = req.body;
    if (!nombre || !nombre.trim()) {
      return res.status(400).json({ mensaje: "El nombre de la localidad es requerido" });
    }
    const nueva = await Localidad.create({ nombre: nombre.trim() });
    res.json(nueva);
  } catch (err) {
    res.status(500).json({ mensaje: "Error al crear localidad", error: err.message });
  }
};

export const listarLocalidades = async (req, res) => {
  try {
    const localidades = await Localidad.findAll();
    res.json(localidades);
  } catch (err) {
    res.status(500).json({ mensaje: "Error al listar localidades", error: err.message });
  }
};

// Actualizar precios de varias localidades para un partido específico
export const actualizarPrecios = async (req, res) => {
  try {
    const { id_partido, precios } = req.body; 
    // precios: [{id_localidad: 1, precio: 100}, ...]
    
    if (!id_partido) {
      return res.status(400).json({ mensaje: "id_partido es requerido" });
    }

    const resultados = [];
    
    for (const item of precios) {
      const inventario = await Inventario.findOne({
        where: { id_localidad: item.id_localidad, id_partido }
      });
      
      if (inventario) {
        await inventario.update({ precio: item.precio });
        resultados.push({
          id_localidad: item.id_localidad,
          id_partido,
          precio: item.precio,
          estado: 'actualizado'
        });
      } else {
        resultados.push({
          id_localidad: item.id_localidad,
          id_partido,
          estado: 'no_encontrado'
        });
      }
    }
    
    res.json({ 
      mensaje: 'Precios actualizados correctamente',
      resultados 
    });
  } catch (error) {
    res.status(500).json({ mensaje: "Error al actualizar precios", error: error.message });
  }
};

// Actualizar precio de una localidad para un partido específico
export const actualizarPrecioLocalidad = async (req, res) => {
  try {
    const { id } = req.params; // id_localidad
    const { id_partido, precio } = req.body;
    
    if (!id_partido) {
      return res.status(400).json({ mensaje: "id_partido es requerido" });
    }

    const inventario = await Inventario.findOne({
      where: { id_localidad: id, id_partido }
    });

    if (!inventario) {
      return res.status(404).json({ mensaje: "Inventario no encontrado para esa localidad y partido" });
    }
    
    await inventario.update({ precio });
    res.json({ 
      mensaje: 'Precio actualizado correctamente',
      inventario 
    });
  } catch (error) {
    res.status(500).json({ mensaje: "Error al actualizar precio", error: error.message });
  }
};

// Listar localidades con inventario para un partido
export const listarLocalidadesConInventario = async (req, res) => {
  try {
    const { id_partido } = req.query;
    
    if (!id_partido) {
      return res.status(400).json({ mensaje: "ID del partido es requerido" });
    }

    const localidades = await Localidad.findAll({
      include: [
        {
          model: Inventario,
          attributes: ['cantidad_disponible', 'precio'], // incluimos precio
          where: { id_partido: id_partido },
          required: false
        }
      ]
    });
    const localidadesFormateadas = localidades.map(localidad => ({
      id_localidad: localidad.id_localidad,
      nombre: localidad.nombre,
      precio: localidad.Inventarios?.[0]?.precio || 0,
      disponibilidad: localidad.Inventarios?.[0]?.cantidad_disponible || 0
    }));

    res.json(localidadesFormateadas);
  } catch (err) {
    res.status(500).json({ mensaje: "Error al listar localidades", error: err.message });
  }
};

export const actualizarLocalidad = async (req, res) => {
  try {
    const { id } = req.params;
    const { nombre } = req.body;
    
    const localidad = await Localidad.findByPk(id);
    if (!localidad) {
      return res.status(404).json({ mensaje: "Localidad no encontrada" });
    }
    
    await localidad.update({ nombre });
    res.json(localidad); // Devuelve la localidad actualizada
  } catch (error) {
    res.status(500).json({ mensaje: "Error al actualizar localidad", error: error.message });
  }
};
