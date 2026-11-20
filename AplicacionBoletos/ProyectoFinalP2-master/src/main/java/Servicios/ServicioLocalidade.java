package Servicios;

import Guardar.SessionManager;
import Modelos.Localidad;
import Modelos.LocalidadConInventario;
import Modelos.PrecioLocalidad;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.entity.EntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author josth
 */
public class ServicioLocalidade {

    private static final String BASE_URL = "http://localhost:8081/api/localidades";
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private final SessionManager sessionManager;

    public ServicioLocalidade() {
        this.sessionManager = SessionManager.getInstance();
    }

    // ==================== MÉTODOS PÚBLICOS ====================

    public List<Localidad> getLocalidades() throws Exception {
        verificarSesion();
        HttpGet request = new HttpGet(BASE_URL);
        String responseBody = ejecutarPeticionConAutenticacion(request);
        return parsearLista(responseBody, new TypeReference<List<Localidad>>() {});
    }

    public List<LocalidadConInventario> getLocalidadesConInventario(int idPartido) throws Exception {
        verificarSesion();
        String url = BASE_URL + "/con-inventario?id_partido=" + idPartido;
        HttpGet request = new HttpGet(url);
        String responseBody = ejecutarPeticionConAutenticacion(request);
        return parsearLista(responseBody, new TypeReference<List<LocalidadConInventario>>() {});
    }

    public Localidad createLocalidad(Localidad localidad) throws Exception {
        verificarSesion();
        HttpPost request = new HttpPost(BASE_URL);
        String json = JSON_MAPPER.writeValueAsString(localidad);
        request.setEntity(crearEntidadJson(json));
        String responseBody = ejecutarPeticionConAutenticacion(request);
        return JSON_MAPPER.readValue(responseBody, Localidad.class);
    }
// En ServicioLocalidade.java
public Localidad updateLocalidad(int id, Localidad localidad) throws Exception {
    verificarSesion();
    HttpPut request = new HttpPut(BASE_URL + "/" + id);
    String json = JSON_MAPPER.writeValueAsString(localidad);
    request.setEntity(crearEntidadJson(json));
    String responseBody = ejecutarPeticionConAutenticacion(request);
    return JSON_MAPPER.readValue(responseBody, Localidad.class);
}
    /**
     * Actualiza el precio de una localidad para un partido específico.
     */
    public void actualizarPrecioLocalidad(int idLocalidad, int idPartido, double nuevoPrecio) throws Exception {
        verificarSesion();
        String url = BASE_URL + "/" + idLocalidad + "/precio";
        HttpPut request = new HttpPut(url);
        String json = String.format(java.util.Locale.US, "{\"id_partido\": %d, \"precio\": %.2f}", idPartido, nuevoPrecio);
        request.setEntity(crearEntidadJson(json));
        ejecutarPeticionConAutenticacion(request);
    }

    /**
     * Actualiza los precios de varias localidades para un partido específico.
     */
    public void actualizarPrecios(int idPartido, List<PrecioLocalidad> precios) throws Exception {
        verificarSesion();
        String url = BASE_URL + "/actualizar-precios";
        HttpPut request = new HttpPut(url);
        Map<String, Object> body = new HashMap<>();
        body.put("id_partido", idPartido);
        body.put("precios", precios);
        String json = JSON_MAPPER.writeValueAsString(body);
        request.setEntity(crearEntidadJson(json));
        ejecutarPeticionConAutenticacion(request);
    }

    // ==================== MÉTODOS PRIVADOS ====================

    private void verificarSesion() throws Exception {
        if (!sessionManager.isSessionActiva()) {
            throw new Exception("Debe iniciar sesión primero.");
        }
        String token = sessionManager.getAuthToken();
        if (token == null || token.isEmpty()) {
            throw new Exception("Token de autenticación inválido.");
        }
        if (sessionManager.isTokenExpirado()) {
            throw new Exception("La sesión ha expirado. Inicie sesión nuevamente.");
        }
    }

    private String ejecutarPeticionConAutenticacion(Object request) throws Exception {
        if (!(request instanceof HttpGet) && !(request instanceof HttpPost) && !(request instanceof HttpPut)) {
            throw new IllegalArgumentException("Tipo de petición no soportado");
        }

        ((org.apache.hc.client5.http.classic.methods.HttpUriRequestBase) request)
                .setHeader("Authorization", "Bearer " + sessionManager.getAuthToken());

        try (CloseableHttpClient cliente = HttpClients.createDefault();
             ClassicHttpResponse response = (ClassicHttpResponse) cliente.execute(
                     (org.apache.hc.client5.http.classic.methods.HttpUriRequestBase) request)) {

            int statusCode = response.getCode();
            String responseBody = EntityUtils.toString(response.getEntity());

            System.out.println("Código de respuesta: " + statusCode);
            if (responseBody != null && !responseBody.isEmpty()) {
                System.out.println("Cuerpo: " + responseBody);
            }

            if (statusCode >= 400) {
                String mensaje = String.format("Error en la API (código %d): %s", statusCode, responseBody);
                throw new Exception(mensaje);
            }

            return responseBody;
        } catch (IOException e) {
            throw new Exception("Error de comunicación con el servidor: " + e.getMessage(), e);
        }
    }

    private org.apache.hc.core5.http.HttpEntity crearEntidadJson(String json) {
        return EntityBuilder.create()
                .setText(json)
                .setContentType(ContentType.APPLICATION_JSON)
                .build();
    }

    private <T> T parsearLista(String responseBody, TypeReference<T> typeRef) throws Exception {
        try {
            return JSON_MAPPER.readValue(responseBody, typeRef);
        } catch (IOException e) {
            throw new Exception("Error al procesar la respuesta del servidor: " + e.getMessage(), e);
        }
    }
}