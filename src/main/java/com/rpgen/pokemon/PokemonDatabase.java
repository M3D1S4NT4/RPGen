package com.rpgen.pokemon;

import java.util.*;
import java.util.concurrent.*;
import java.net.http.*;
import java.net.URI;
import com.google.gson.*;

public class PokemonDatabase {
    private static final String POKE_API_BASE_URL = "https://pokeapi.co/api/v2";
    private static final int BATCH_SIZE = 100;
    private static final List<Pokemon> pokemonList = new ArrayList<>();
    private static int currentOffset = 0;
    private static boolean hasMore = true;
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();
    private static final int TOTAL_POKEMON = 1302; // Primera generación

    public static void initialize() {
        // Cargar el primer lote
        loadNextBatch();
        
        scheduler.scheduleAtFixedRate(() -> {
            if (hasMore) {
                loadNextBatch();
            }
        }, 0, 500, TimeUnit.MICROSECONDS);
    }

    public static void loadNextBatch() {
        if (!hasMore) return;

        try {
            String url = POKE_API_BASE_URL + "/pokemon?offset=" + currentOffset + "&limit=" + BATCH_SIZE;
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
            JsonArray results = jsonResponse.getAsJsonArray("results");

            // Crear una lista temporal para mantener el orden
            List<Pokemon> batchPokemon = new ArrayList<>();
            CountDownLatch latch = new CountDownLatch(results.size());

            for (JsonElement element : results) {
                JsonObject pokemon = element.getAsJsonObject();
                String pokemonUrl = pokemon.get("url").getAsString();
                
                // Cargar detalles de cada Pokémon en paralelo
                CompletableFuture.runAsync(() -> {
                    try {
                        Pokemon p = loadPokemonDetails(pokemonUrl);
                        if (p != null) {
                            synchronized (batchPokemon) {
                                batchPokemon.add(p);
                            }
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // Esperar a que todos los Pokémon del lote se carguen
            latch.await();

            // Ordenar el lote por ID y añadirlo a la lista principal
            synchronized (pokemonList) {
                batchPokemon.sort(Comparator.comparingInt(p -> Integer.parseInt(p.getId())));
                pokemonList.addAll(batchPokemon);
                // Ordenar la lista completa por ID
                pokemonList.sort(Comparator.comparingInt(p -> Integer.parseInt(p.getId())));
            }

            currentOffset += BATCH_SIZE;
            hasMore = currentOffset < TOTAL_POKEMON;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Pokemon loadPokemonDetails(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject pokemonData = gson.fromJson(response.body(), JsonObject.class);

            String id = pokemonData.get("id").getAsString();
            String name = pokemonData.get("name").getAsString();
            
            // Cargar estadísticas base
            JsonArray statsArray = pokemonData.getAsJsonArray("stats");
            int maxHealth = statsArray.get(0).getAsJsonObject().get("base_stat").getAsInt() * 2;
            int attack = statsArray.get(1).getAsJsonObject().get("base_stat").getAsInt();
            int defense = statsArray.get(2).getAsJsonObject().get("base_stat").getAsInt();
            int specialAttack = statsArray.get(3).getAsJsonObject().get("base_stat").getAsInt();
            int specialDefense = statsArray.get(4).getAsJsonObject().get("base_stat").getAsInt();
            int speed = statsArray.get(5).getAsJsonObject().get("base_stat").getAsInt();

            // Cargar tipos
            List<String> types = new ArrayList<>();
            JsonArray typesArray = pokemonData.getAsJsonArray("types");
            for (JsonElement typeElement : typesArray) {
                String type = typeElement.getAsJsonObject()
                    .getAsJsonObject("type")
                    .get("name").getAsString();
                types.add(type);
            }

            // Cargar imagen
            String imageUrl = pokemonData.getAsJsonObject("sprites")
                .getAsJsonObject("other")
                .getAsJsonObject("official-artwork")
                .get("front_default").getAsString();

            // Cargar movimientos
            List<Map<String, Object>> moves = new ArrayList<>();
            JsonArray movesArray = pokemonData.getAsJsonArray("moves");
            Set<String> addedMoveNames = new HashSet<>();
            
            // Obtener los primeros 4 movimientos con power
            for (JsonElement moveElement : movesArray) {
                if (moves.size() >= 4) break;
                
                JsonObject moveData = moveElement.getAsJsonObject();
                String moveUrl = moveData.getAsJsonObject("move").get("url").getAsString();
                
                try {
                    HttpRequest moveRequest = HttpRequest.newBuilder()
                        .uri(URI.create(moveUrl))
                        .GET()
                        .build();
                    
                    HttpResponse<String> moveResponse = httpClient.send(moveRequest, HttpResponse.BodyHandlers.ofString());
                    JsonObject moveDetails = gson.fromJson(moveResponse.body(), JsonObject.class);
                    
                    if (moveDetails.has("power") && !moveDetails.get("power").isJsonNull()) {
                        String moveName = moveDetails.get("name").getAsString();
                        if (!addedMoveNames.contains(moveName)) {
                            int power = moveDetails.get("power").getAsInt();
                            String moveType = moveDetails.getAsJsonObject("type").get("name").getAsString();
                            
                            Map<String, Object> move = new HashMap<>();
                            move.put("name", moveName);
                            move.put("power", power);
                            move.put("type", moveType);
                            moves.add(move);
                            addedMoveNames.add(moveName);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error al cargar movimiento: " + e.getMessage());
                    continue;
                }
            }

            // Si no hay suficientes movimientos, añadir movimientos por defecto
            while (moves.size() < 4) {
                Map<String, Object> defaultMove = new HashMap<>();
                defaultMove.put("name", "Ataque Rápido");
                defaultMove.put("power", 40);
                defaultMove.put("type", "normal");
                moves.add(defaultMove);
            }

            return new Pokemon(
                id,
                name,
                maxHealth,
                attack,
                defense,
                types,
                speed,
                specialAttack,
                specialDefense,
                imageUrl,
                moves
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Pokemon> getAllPokemon() {
        synchronized (pokemonList) {
            return new ArrayList<>(pokemonList);
        }
    }

    public static Pokemon getPokemon(String id) {
        synchronized (pokemonList) {
            return pokemonList.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
        }
    }

    public static List<Pokemon> searchPokemon(String query) {
        final String searchQuery = query.toLowerCase();
        synchronized (pokemonList) {
            return pokemonList.stream()
                .filter(p -> p.getName().toLowerCase().contains(searchQuery) ||
                           p.getTypes().stream().anyMatch(t -> t.toLowerCase().contains(searchQuery)))
                .toList();
        }
    }

    public static boolean hasMorePokemon() {
        return hasMore;
    }
} 