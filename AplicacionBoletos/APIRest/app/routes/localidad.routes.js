import express from "express";
import { 
  crearLocalidad, 
  listarLocalidades,
  actualizarPrecios,
  actualizarPrecioLocalidad,
  listarLocalidadesConInventario,
  actualizarLocalidad
} from "../controllers/localidad.controller.js";
import { verificarToken, esAdmin } from "../middlewares/authJwt.js";

const router = express.Router();

router.post("/", verificarToken, esAdmin, crearLocalidad);
router.get("/", verificarToken, listarLocalidades);
router.get("/con-inventario", verificarToken, listarLocalidadesConInventario);
router.put("/actualizar-precios", verificarToken, esAdmin, actualizarPrecios);
router.put("/:id/precio", verificarToken, esAdmin, actualizarPrecioLocalidad);
router.put("/:id", verificarToken, esAdmin, actualizarLocalidad); // <-- nuevo endpoint

export default router;