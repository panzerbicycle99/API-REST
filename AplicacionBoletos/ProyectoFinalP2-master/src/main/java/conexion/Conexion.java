package conexion;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.nio.file.Paths;

public class Conexion {
    static Properties config = new Properties();
    String hostname = null;
    String port = null;
    String database = null;
    String username = null;
    String password = null;

    public Conexion(){
        InputStream in;
        // Primero la ruta original; si no existe se busca junto a la aplicación
        String[] rutas = {"C:\\Config\\DBConfig.properties", "DBConfig.properties"};
        
        for(String path : rutas){
            if(!Files.exists(Paths.get(path))){
                continue;
            }
            try{
                in = Files.newInputStream(Paths.get(path));
                config.load(in);
                in.close();
                loadProperties();
                return;
            }catch(IOException e){
                System.out.println(e.getMessage());
            }
        }
        System.err.println("No se encontró el archivo DBConfig.properties (C:\\Config o carpeta de la aplicación)");
    }
    
    private void loadProperties(){
        this.hostname = config.getProperty("hostname");
        this.port = config.getProperty("port");
        this.database = config.getProperty("database");
        this.username = config.getProperty("username");
        this.password = config.getProperty("password");
    }
    
    public Connection getConnection() throws SQLException{
        Connection conn;
        if(hostname == null || port == null || database == null){
            throw new SQLException("No se pudo leer la configuración de la base de datos. "
                    + "Verifique que exista el archivo DBConfig.properties (C:\\Config\\DBConfig.properties) "
                    + "con hostname, port, database, username y password.");
        }
        String jdbcUrl = "jdbc:postgresql://"+this.hostname+":"+this.port+"/"+this.database;
        conn = DriverManager.getConnection(jdbcUrl, username, password);
        return conn;
    }
}
