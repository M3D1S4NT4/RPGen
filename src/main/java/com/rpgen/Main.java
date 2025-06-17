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
        System.out.println("Iniciando servidores...");
        
        PokemonBattleServer pokemonBattleServer = new PokemonBattleServer();
        pokemonBattleServer.init();
        System.out.println("Servidor de batalla de Pokémon iniciado");
        
        BattleServer battleServer = new BattleServer();
        battleServer.init();
        System.out.println("Servidor de batalla general iniciado");
        
        PokemonServer pokemonServer = new PokemonServer();
        pokemonServer.init();
        System.out.println("Servidor de Pokémon iniciado");
        
        // Configurar manejo de errores
        Spark.exception(Exception.class, (exception, request, response) -> {
            System.err.println("Error en el servidor: " + exception.getMessage());
            exception.printStackTrace();
            response.status(500);
            response.body("Error interno del servidor: " + exception.getMessage());
        });
        
        // Configurar manejo de rutas no encontradas
        Spark.notFound((request, response) -> {
            System.err.println("Ruta no encontrada: " + request.pathInfo());
            response.status(404);
            return "Ruta no encontrada: " + request.pathInfo();
        });
        
        System.out.println("Servidor iniciado en http://localhost:4567");
    }
} 