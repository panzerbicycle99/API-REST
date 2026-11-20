import bcrypt from "bcryptjs";
import { Usuario } from "../models/index.models.js";

export const crearUsuario = async (req, res) => {
  try {
    const { nombre_usuario, contrasena, nombre_completo, rol } = req.body;

    if (!nombre_usuario || !contrasena || !nombre_completo) {
      return res.status(400).json({ message: "Faltan datos obligatorios del usuario" });
    }

    const existente = await Usuario.findOne({ where: { nombre_usuario } });
    if (existente) {
      return res.status(400).json({ message: "El nombre de usuario ya existe" });
    }

    const hash = await bcrypt.hash(contrasena, 10);

    const nuevo = await Usuario.create({
      nombre_usuario,
      contrasena: hash, 
      nombre_completo,
      rol: rol || "vendedor",
    });

    const { contrasena: _omitida, ...usuarioSinHash } = nuevo.toJSON();
    res.json(usuarioSinHash);
  } catch (err) {
    res.status(500).json({ message: "Error al crear usuario", error: err.message });
  }
};

export const listarUsuarios = async (req, res) => {
  try {
    const usuarios = await Usuario.findAll({ attributes: { exclude: ["contrasena"] } });
    res.json(usuarios);
  } catch (err) {
    res.status(500).json({ message: "Error al listar usuarios" });
  }
};

// ACTUALIZAR USUARIO COMPLETO CON PUT
export const actualizarUsuario = async (req, res) => {
  try {
    const { id_usuario } = req.params;
    const { nombre_usuario, nombre_completo, rol, estado, contrasena } = req.body;

    const usuario = await Usuario.findByPk(id_usuario);
    
    if (!usuario) {
      return res.status(404).json({ message: "Usuario no encontrado" });
    }

    // Verificar que no sea auto-eliminación si se está desactivando
    if (estado === false && parseInt(id_usuario) === req.userId) {
      return res.status(400).json({ 
        message: "No puedes desactivar tu propio usuario" 
      });
    }

    // Verificar si el nombre de usuario ya existe (excluyendo el actual)
    if (nombre_usuario && nombre_usuario !== usuario.nombre_usuario) {
      const usuarioExistente = await Usuario.findOne({ 
        where: { nombre_usuario } 
      });
      if (usuarioExistente) {
        return res.status(400).json({ 
          message: "El nombre de usuario ya está en uso" 
        });
      }
    }

    // Actualizar todos los campos
    await usuario.update({
      nombre_usuario: nombre_usuario || usuario.nombre_usuario,
      nombre_completo: nombre_completo || usuario.nombre_completo,
      rol: rol || usuario.rol,
      estado: estado !== undefined ? estado : usuario.estado
    });

    // Si se envió una contraseña nueva se guarda cifrada (el formulario de administración la envía)
    if (contrasena) {
      await usuario.update({ contrasena: await bcrypt.hash(contrasena, 10) });
    }

    // No retornar la contraseña
    const { contrasena: _, ...usuarioActualizado } = usuario.toJSON();

    res.json({ 
      message: "Usuario actualizado exitosamente",
      usuario: usuarioActualizado
    });
  } catch (err) {
    res.status(500).json({ 
      message: "Error al actualizar el usuario", 
      error: err.message 
    });
  }
};

// OPCIONAL: Obtener usuario por ID
export const obtenerUsuarioPorId = async (req, res) => {
  try {
    const { id_usuario } = req.params;

    const usuario = await Usuario.findByPk(id_usuario, {
      attributes: { exclude: ['contrasena'] }
    });
    
    if (!usuario) {
      return res.status(404).json({ message: "Usuario no encontrado" });
    }

    res.json(usuario);
  } catch (err) {
    res.status(500).json({ 
      message: "Error al obtener el usuario", 
      error: err.message 
    });
  }
};