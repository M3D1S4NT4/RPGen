package com.rpgen;

import com.rpgen.web.BattleServer;
import com.rpgen.pokemon.PokemonServer;
import com.rpgen.web.PokemonBattleServer;
import spark.Spark;

public class Main {
    public static void main(String[] args) {
        // Configurar el puerto
        Spark.port(4567);
        
        // Configurar el directorio de archivos estáticos
        Spark.staticFiles.location("/public");
        
        // Inicializar los servidores
        BattleServer battleServer = new BattleServer();
        battleServer.init();
        
        PokemonServer pokemonServer = new PokemonServer();
        pokemonServer.init();
        
        PokemonBattleServer pokemonBattleServer = new PokemonBattleServer();
        pokemonBattleServer.init();
        
        // Configurar manejo de errores
        Spark.exception(Exception.class, (exception, request, response) -> {
            exception.printStackTrace();
            response.status(500);
            response.body("Error interno del servidor: " + exception.getMessage());
        });
        
        // Configurar manejo de rutas no encontradas
        Spark.notFound((request, response) -> {
            response.status(404);
            return "Ruta no encontrada: " + request.pathInfo();
        });
        
        System.out.println("Servidor iniciado en http://localhost:4567");
    }
} 