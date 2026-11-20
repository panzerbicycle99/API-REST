package Servicios;

import Guardar.SessionManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import Modelos.Inventario;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.entity.EntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;

import java.io.InputStream;
import java.util.List;

public class ServicioInventario {
    private static final String BASE_URL = "http://localhost:8081/api/inventario";
    private static final ObjectMapper mapper = new ObjectMapper();
    private final SessionManager sessionManager;

    public ServicioInventario() {
        this.sessionManager = SessionManager.getInstance();
    }
    
    public List<Inventario> getInventario() throws Exception {
        verificarSesion();
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(BASE_URL);
            request.setHeader("Authorization", "Bearer " + sessionManager.getAuthToken());
            
            ClassicHttpResponse response = (ClassicHttpResponse) client.execute(request);
            int statusCode = response.getCode();
            
            if (statusCode != 200) {
                throw new Exception("Error en la API: " + statusCode);
            }
            
            InputStream is = response.getEntity().getContent();
            
            try {
                return mapper.readValue(is, new TypeReference<List<Inventario>>() {});
            } catch (Exception e) {
                is = response.getEntity().getContent();
                Modelos.InventarioResponse wrapper = mapper.readValue(is, Modelos.InventarioResponse.class);
                if (wrapper != null && wrapper.getData() != null) {
                    return wrapper.getData();
                } else {
                    throw new Exception("Formato de respuesta no reconocido");
                }
            }
        }
    }
    
    public Inventario createInventario(Inventario i) throws Exception {
        verificarSesion();
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(BASE_URL);
            request.setHeader("Authorization", "Bearer " + sessionManager.getAuthToken());
            java.util.Map<String, Object> nuevo = new java.util.LinkedHashMap<>();
            nuevo.put("id_partido", i.getId_partido());
            nuevo.put("id_localidad", i.getId_localidad());
            nuevo.put("cantidad_total", i.getCantidad_total());
            nuevo.put("cantidad_disponible", i.getCantidad_disponible());
            nuevo.put("precio", i.getPrecio());
            String json = mapper.writeValueAsString(nuevo);
            request.setEntity(EntityBuilder.create()
                    .setText(json)
                    .setContentType(ContentType.APPLICATION_JSON)
                    .build());
            ClassicHttpResponse response = (ClassicHttpResponse) client.execute(request);
            int statusCode = response.getCode();
            
            if (statusCode != 200 && statusCode != 201) {
                throw new Exception("Error en la API: " + statusCode);
            }
            InputStream is = response.getEntity().getContent();
            return mapper.readValue(is, Inventario.class);
        }
    }
    
    public Inventario updateInventario(int id_inventario, Inventario i) throws Exception {
        verificarSesion();
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPut request = new HttpPut(BASE_URL + "/" + id_inventario);
            request.setHeader("Authorization", "Bearer " + sessionManager.getAuthToken());
            // Solo se envían los campos con valor: mandar ids/cantidades en 0 corrompía el registro
            java.util.Map<String, Object> cambios = new java.util.LinkedHashMap<>();
            cambios.put("precio", i.getPrecio());
            cambios.put("cantidad_total", i.getCantidad_total());
            if (i.getId_partido() > 0) cambios.put("id_partido", i.getId_partido());
            if (i.getId_localidad() > 0) cambios.put("id_localidad", i.getId_localidad());
            if (i.getCantidad_disponible() > 0) cambios.put("cantidad_disponible", i.getCantidad_disponible());
            String json = mapper.writeValueAsString(cambios);
            request.setEntity(EntityBuilder.create()
                    .setText(json)
                    .setContentType(ContentType.APPLICATION_JSON)
                    .build());
            ClassicHttpResponse response = (ClassicHttpResponse) client.execute(request);
            int statusCode = response.getCode();
            
            if (statusCode != 200) {
                throw new Exception("Error en la API: " + statusCode);
            }
            InputStream is = response.getEntity().getContent();
            return leerInventarioActualizado(is);
        }
    }
    
    public Inventario updatePrecioInventario(int id_inventario, double nuevoPrecio) throws Exception {
        verificarSesion();
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPut request = new HttpPut(BASE_URL + "/" + id_inventario + "/precio");
            request.setHeader("Authorization", "Bearer " + sessionManager.getAuthToken());        
            // Locale.US: con configuración regional en español "%.2f" produce coma y rompe el JSON
            String json = String.format(java.util.Locale.US, "{\"precio\": %.2f}", nuevoPrecio);
            request.setEntity(EntityBuilder.create()
                    .setText(json)
                    .setContentType(ContentType.APPLICATION_JSON)
                    .build());
            ClassicHttpResponse response = (ClassicHttpResponse) client.execute(request);
            int statusCode = response.getCode();
            if (statusCode != 200) {
                throw new Exception("Error en la API: " + statusCode);
            }
            InputStream is = response.getEntity().getContent();
            return leerInventarioActualizado(is);
        }
    }

    // La API responde { mensaje, inventario: {...} } al actualizar
    private Inventario leerInventarioActualizado(InputStream is) throws Exception {
        com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(is);
        if (node.has("inventario")) {
            return mapper.treeToValue(node.get("inventario"), Inventario.class);
        }
        return mapper.treeToValue(node, Inventario.class);
    }
    
    private void verificarSesion() throws Exception {
        if (!sessionManager.isSessionActiva()) {
            throw new Exception("Debe iniciar sesión primero");
        }
    }
}