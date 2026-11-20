// app/routes/reporte.routes.js
import express from "express";
import { ventasPorPartido, ventasPorVendedor } from "../controllers/reporte.controller.js";
import { verificarToken } from "../middlewares/authJwt.js";

const router = express.Router();

// Requieren autenticación 
router.get("/partido/:id_partido", verificarToken, ventasPorPartido);
router.get("/vendedor/:id_vendedor", verificarToken, ventasPorVendedor);

export default router;
