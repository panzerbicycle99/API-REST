import { Inventario, Partido, Localidad } from "../models/index.models.js";

// Crear inventario (por partido y localidad)
export const crearInventario = async (req, res) => {
  try {
    
    const { id_partido, id_localidad, cantidad_total, cantidad_disponible, precio } = req.body;

    const nuevo = await Inventario.create({
      id_partido,
      id_localidad,
      cantidad_total,
      cantidad_disponible,
      precio 
    });

    res.json(nuevo);
  } catch (err) {
    console.error(err);
    res.status(500).json({ mensaje: "Error al crear inventario" });
  }
};

// Listar inventarios con detalles de partido y localidad
export const listarInventario = async (req, res) => {
  try {
    const inventario = await Inventario.findAll({
      include: [Partido, Localidad],
    });
    res.json(inventario);
  } catch (err) {
    console.error(err);
    res.status(500).json({ mensaje: "Error al listar inventario" });
  }
};


// Actualizar precio de una localidad en un partido
export const actualizarPrecio = async (req, res) => {
  try {
    const { id_inventario } = req.params;
    const { precio } = req.body;

    const inventario = await Inventario.findByPk(id_inventario);
    if (!inventario) {
      return res.status(404).json({ mensaje: "Inventario no encontrado" });
    }

    inventario.precio = precio;
    await inventario.save();

    res.json({ mensaje: "Precio actualizado correctamente", inventario });
  } catch (err) {
    console.error(err);
    res.status(500).json({ mensaje: "Error al actualizar precio" });
  }
};


// Actualizar cualquier campo del inventario (precio, localidad, cantidad, etc.)
export const actualizarInventario = async (req, res) => {
  try {
    const { id_inventario } = req.params;
    const datos = req.body;

    const inventario = await Inventario.findByPk(id_inventario);
    if (!inventario) {
      return res.status(404).json({ mensaje: "Inventario no encontrado" });
    }

    // Solo se aceptan estos campos; nunca el id ni valores vacíos (0) que corrompían el registro
    const cambios = {};
    if (datos.precio !== undefined && datos.precio !== null) cambios.precio = datos.precio;
    if (Number(datos.id_partido) > 0) cambios.id_partido = Number(datos.id_partido);
    if (Number(datos.id_localidad) > 0) cambios.id_localidad = Number(datos.id_localidad);

    if (datos.cantidad_total !== undefined && datos.cantidad_total !== null) {
      const nuevoTotal = Number(datos.cantidad_total);
      const vendidos = inventario.cantidad_total - inventario.cantidad_disponible;
      if (nuevoTotal < vendidos) {
        return res.status(400).json({
          mensaje: `El total no puede ser menor a los boletos ya vendidos (${vendidos})`,
        });
      }
      cambios.cantidad_total = nuevoTotal;
      // el disponible se ajusta con la diferencia del total, descontando lo ya vendido
      cambios.cantidad_disponible =
        Number(datos.cantidad_disponible) > 0 ? Number(datos.cantidad_disponible) : nuevoTotal - vendidos;
    } else if (Number(datos.cantidad_disponible) > 0) {
      cambios.cantidad_disponible = Number(datos.cantidad_disponible);
    }

    await inventario.update(cambios);
    res.json({ mensaje: "Inventario actualizado correctamente", inventario });
  } catch (err) {
    console.error(err);
    res.status(500).json({ mensaje: "Error al actualizar inventario" });
  }
};
