package com.rpgen.pokemon.web;

import static spark.Spark.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rpgen.pokemon.data.PokemonDatabase;
import com.rpgen.pokemon.entity.Pokemon;

import java.util.*;

public class PokemonServer {
    private final Gson gson;
    private static boolean initialized = false;

    public PokemonServer() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void init() {
        if (initialized) {
            return;
        }
        initialized = true;

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
            response.header("Access-Control-Allow-Headers", "Content-Type");
            response.type("application/json");
        });

        // Inicializar la base de datos de Pokémon
        PokemonDatabase.initialize();

        // Obtener todos los Pokémon
        get("/api/pokemon", (req, res) -> {
            try {
                return gson.toJson(PokemonDatabase.getAllPokemon());
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of(
                    "status", "error",
                    "message", "Error al obtener Pokémon: " + e.getMessage()
                ));
            }
        });

        // Cargar el siguiente lote de Pokémon
        get("/api/pokemon/load-more", (req, res) -> {
            try {
                PokemonDatabase.loadNextBatch();
                Map<String, Object> response = new HashMap<>();
                response.put("pokemon", PokemonDatabase.getAllPokemon());
                response.put("hasMore", PokemonDatabase.hasMorePokemon());
                return gson.toJson(response);
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of(
                    "status", "error",
                    "message", "Error al cargar más Pokémon: " + e.getMessage()
                ));
            }
        });

        // Buscar Pokémon por nombre o tipo
        get("/api/pokemon/search", (req, res) -> {
            try {
                String query = req.queryParams("q");
                if (query == null || query.trim().isEmpty()) {
                    return gson.toJson(PokemonDatabase.getAllPokemon());
                }
                return gson.toJson(PokemonDatabase.searchPokemon(query));
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of(
                    "status", "error",
                    "message", "Error al buscar Pokémon: " + e.getMessage()
                ));
            }
        });

        // Obtener un Pokémon específico por ID
        get("/api/pokemon/:id", (req, res) -> {
            try {
                String id = req.params(":id");
                Pokemon pokemon = PokemonDatabase.getPokemon(id);
                
                if (pokemon == null) {
                    res.status(404);
                    return gson.toJson(Map.of(
                        "status", "error",
                        "message", "Pokémon no encontrado"
                    ));
                }
                
                return gson.toJson(pokemon);
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of(
                    "status", "error",
                    "message", "Error al obtener Pokémon: " + e.getMessage()
                ));
            }
        });

        // Manejar errores
        exception(Exception.class, (e, req, res) -> {
            res.status(500);
            res.body(gson.toJson(Map.of(
                "status", "error",
                "message", "Error interno del servidor: " + e.getMessage()
            )));
        });
    }
} 