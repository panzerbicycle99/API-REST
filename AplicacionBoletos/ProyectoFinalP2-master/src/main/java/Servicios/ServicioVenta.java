package Servicios;

import Guardar.SessionManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import Modelos.Venta;
import java.io.BufferedReader;
import java.io.IOException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.EntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class ServicioVenta {
    private static final String BASE_URL = "http://localhost:8081/api/ventas";
    private static final ObjectMapper mapper = new ObjectMapper();
    private final SessionManager sessionManager;

    public ServicioVenta() {
        this.sessionManager = SessionManager.getInstance();
    }
    
    public List<Venta> getVentas() throws Exception {
        verificarSesion();
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(BASE_URL);
            request.setHeader("Authorization", "Bearer " + sessionManager.getAuthToken());
            ClassicHttpResponse response = (ClassicHttpResponse) client.execute(request);
            String responseBody = convertInputStreamToString(response.getEntity().getContent());
            if (response.getCode() != 200) {
                throw new Exception("Error en la API (" + response.getCode() + "): " + extraerMensaje(responseBody));
            }
            return mapper.readValue(responseBody, new TypeReference<List<Venta>>() {});
        }
    }
    
    public Venta createVenta(Venta v) throws Exception {
        verificarSesion();
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(BASE_URL);
            request.setHeader("Authorization", "Bearer " + sessionManager.getAuthToken());
            String json = mapper.writeValueAsString(v);
            
            System.out.println("=== JSON ENVIADO ===");
            System.out.println(json);
            
            request.setEntity(EntityBuilder.create()
                    .setText(json)
                    .setContentType(ContentType.APPLICATION_JSON)
                    .build());
            
            ClassicHttpResponse response = (ClassicHttpResponse) client.execute(request);
            int statusCode = response.getCode();
            String responseBody = convertInputStreamToString(response.getEntity().getContent());
            System.out.println("=== RESPUESTA DEL API ===");
            System.out.println("Código: " + statusCode);
            System.out.println("Respuesta: " + responseBody);
            
            // Si la API rechazó la venta (ej. boletos insuficientes) NO se debe dar por exitosa
            if (statusCode != 200 && statusCode != 201) {
                throw new Exception(extraerMensaje(responseBody));
            }
            
            if (responseBody != null && !responseBody.isEmpty()) {
                return mapper.readValue(responseBody, Venta.class);
            }
            return new Venta();
        }
    }

    // Extrae el texto de error que devuelve la API ({ mensaje, error })
    private String extraerMensaje(String body) {
        try {
            com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(body);
            String mensaje = node.has("mensaje") ? node.get("mensaje").asText() : "";
            if (node.has("error")) {
                mensaje = mensaje.isEmpty() ? node.get("error").asText() : mensaje + ": " + node.get("error").asText();
            }
            if (mensaje.isEmpty() && node.has("message")) {
                mensaje = node.get("message").asText();
            }
            return mensaje.isEmpty() ? body : mensaje;
        } catch (Exception e) {
            return body;
        }
    }

private String convertInputStreamToString(InputStream is) throws IOException {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        return stringBuilder.toString();
    }
}
/*    
    public Venta updateVenta(int id, Venta v) throws Exception {
        verificarSesion();
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPut request = new HttpPut(BASE_URL + "/" + id);
            request.setHeader("Authorization", "Bearer " + sessionManager.getAuthToken());
            String json = mapper.writeValueAsString(v);
            request.setEntity(EntityBuilder.create()
                    .setText(json)
                    .setContentType(ContentType.APPLICATION_JSON)
                    .build());
            ClassicHttpResponse response = (ClassicHttpResponse) client.execute(request);
            InputStream is = response.getEntity().getContent();
            return mapper.readValue(is, Venta.class);
        }
    }
*/    
    private void verificarSesion() throws Exception {
        if (!sessionManager.isSessionActiva()) {
            throw new Exception("Debe iniciar sesión primero");
        }
    }
}