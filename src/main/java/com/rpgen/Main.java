package com.rpgen;

import static spark.Spark.*;
import com.rpgen.web.BattleServer;

public class Main {
    public static void main(String[] args) {
        // Configurar el puerto del servidor
        port(4567);
        
        // Configurar la ubicación de los archivos estáticos
        staticFileLocation("/public");
        
        // Inicializar el servidor de batalla
        BattleServer server = new BattleServer();
        server.init();
        
        // Configurar CORS
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Headers", "*");
            response.header("Access-Control-Allow-Methods", "*");
        });
    }
} 