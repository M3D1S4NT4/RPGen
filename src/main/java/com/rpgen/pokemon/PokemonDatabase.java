package com.rpgen.pokemon;

import java.util.*;
import java.util.concurrent.*;
import java.net.http.*;
import java.net.URI;
import java.time.Duration;
import com.google.gson.*;

public class PokemonDatabase {
    private static final String POKE_API_BASE_URL = "https://pokeapi.co/api/v2";
    private static final int BATCH_SIZE = 10;
    private static final Set<Pokemon> pokemonSet = Collections.synchronizedSet(new TreeSet<>((p1, p2) -> 
        Integer.compare(Integer.parseInt(p1.getId()), Integer.parseInt(p2.getId()))
    ));
    private static final Set<String> loadedPokemonIds = Collections.synchronizedSet(new HashSet<>());
    private static int currentOffset = 0;
    private static boolean hasMore = true;
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .version(HttpClient.Version.HTTP_1_1)
        .build();
    private static final Gson gson = new Gson();
    private static final int TOTAL_POKEMON = 1302;
    private static final Semaphore requestSemaphore = new Semaphore(10); // Aumentado a 20 peticiones simultáneas
    private static final ExecutorService pokemonExecutor = Executors.newFixedThreadPool(2); // Pool para procesar Pokémon

    public static void initialize() {
        // Cargar el primer lote
        loadNextBatch();
        
        // Programar la carga del siguiente lote
        scheduler.scheduleAtFixedRate(() -> {
            if (hasMore) {
                loadNextBatch();
            }
        }, 0, 500, TimeUnit.MILLISECONDS); // Aumentado a 500ms entre lotes
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

            List<CompletableFuture<Pokemon>> pokemonFutures = new ArrayList<>();
            CountDownLatch latch = new CountDownLatch(results.size());

            for (JsonElement element : results) {
                JsonObject pokemon = element.getAsJsonObject();
                String pokemonUrl = pokemon.get("url").getAsString();
                String pokemonId = pokemonUrl.split("/")[6];
                
                if (loadedPokemonIds.contains(pokemonId)) {
                    latch.countDown();
                    continue;
                }
                
                CompletableFuture<Pokemon> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        return loadPokemonDetails(pokemonUrl);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    } finally {
                        latch.countDown();
                    }
                }, pokemonExecutor);
                
                pokemonFutures.add(future);
            }

            // Esperar a que todos los Pokémon del lote se carguen
            latch.await();

            // Recopilar los resultados
            for (CompletableFuture<Pokemon> future : pokemonFutures) {
                try {
                    Pokemon pokemon = future.get();
                    if (pokemon != null) {
                        synchronized (pokemonSet) {
                            pokemonSet.add(pokemon);
                            loadedPokemonIds.add(pokemon.getId());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            currentOffset += BATCH_SIZE;
            hasMore = currentOffset < TOTAL_POKEMON;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Pokemon loadPokemonDetails(String url) {
        int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                requestSemaphore.acquire();
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    JsonObject pokemonData = gson.fromJson(response.body(), JsonObject.class);

                    String id = pokemonData.get("id").getAsString();
                    if (loadedPokemonIds.contains(id)) {
                        return null;
                    }

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

                    // Cargar habilidades
                    List<Ability> abilities = new ArrayList<>();
                    JsonArray abilitiesArray = pokemonData.getAsJsonArray("abilities");
                    for (JsonElement abilityElement : abilitiesArray) {
                        JsonObject abilityData = abilityElement.getAsJsonObject();
                        JsonObject abilityInfo = abilityData.getAsJsonObject("ability");
                        String abilityUrl = abilityInfo.get("url").getAsString();
                        boolean isHidden = abilityData.get("is_hidden").getAsBoolean();
                        
                        try {
                            requestSemaphore.acquire();
                            try {
                                HttpRequest abilityRequest = HttpRequest.newBuilder()
                                    .uri(URI.create(abilityUrl))
                                    .GET()
                                    .build();
                                
                                HttpResponse<String> abilityResponse = httpClient.send(abilityRequest, HttpResponse.BodyHandlers.ofString());
                                JsonObject abilityDetails = gson.fromJson(abilityResponse.body(), JsonObject.class);
                                
                                String abilityId = abilityDetails.get("id").getAsString();
                                String abilityName = abilityDetails.get("name").getAsString();
                                
                                // Buscar descripción en español
                                String description = "Sin descripción disponible";
                                JsonArray flavorTextEntries = abilityDetails.getAsJsonArray("flavor_text_entries");
                                for (JsonElement entry : flavorTextEntries) {
                                    JsonObject flavorEntry = entry.getAsJsonObject();
                                    JsonObject language = flavorEntry.getAsJsonObject("language");
                                    if (language.get("name").getAsString().equals("es")) {
                                        description = flavorEntry.get("flavor_text").getAsString();
                                        break;
                                    }
                                }
                                
                                // Si no hay descripción en español, buscar en inglés
                                if (description.equals("Sin descripción disponible")) {
                                    for (JsonElement entry : flavorTextEntries) {
                                        JsonObject flavorEntry = entry.getAsJsonObject();
                                        JsonObject language = flavorEntry.getAsJsonObject("language");
                                        if (language.get("name").getAsString().equals("en")) {
                                            description = flavorEntry.get("flavor_text").getAsString();
                                            break;
                                        }
                                    }
                                }
                                
                                Ability ability = new Ability(abilityId, abilityName, description, "", isHidden);
                                abilities.add(ability);
                            } finally {
                                requestSemaphore.release();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // Cargar movimientos (todos los disponibles)
                    List<Map<String, Object>> moves = new ArrayList<>();
                    JsonArray movesArray = pokemonData.getAsJsonArray("moves");
                    Set<String> addedMoveNames = new HashSet<>();
                    
                    // Procesar todos los movimientos
                    ExecutorService moveExecutor = Executors.newFixedThreadPool(5);
                    
                    try {
                        List<CompletableFuture<Map<String, Object>>> moveFutures = new ArrayList<>();
                        
                        for (JsonElement moveElement : movesArray) {
                            JsonObject moveData = moveElement.getAsJsonObject();
                            JsonObject moveInfo = moveData.getAsJsonObject("move");
                            String moveUrl = moveInfo.get("url").getAsString();
                            
                            CompletableFuture<Map<String, Object>> moveFuture = CompletableFuture.supplyAsync(() -> {
                                try {
                                    requestSemaphore.acquire();
                                    try {
                                        HttpRequest moveRequest = HttpRequest.newBuilder()
                                            .uri(URI.create(moveUrl))
                                            .GET()
                                            .build();
                                        
                                        HttpResponse<String> moveResponse = httpClient.send(moveRequest, HttpResponse.BodyHandlers.ofString());
                                        JsonObject moveDetails = gson.fromJson(moveResponse.body(), JsonObject.class);
                                        
                                        String moveName = moveDetails.get("name").getAsString();
                                        if (!addedMoveNames.contains(moveName)) {
                                            Map<String, Object> move = new HashMap<>();
                                            move.put("name", moveName);
                                            
                                            JsonElement powerElement = moveDetails.get("power");
                                            int power = powerElement.isJsonNull() ? 0 : powerElement.getAsInt();
                                            move.put("power", power);
                                            
                                            String moveType = moveDetails.getAsJsonObject("type").get("name").getAsString();
                                            move.put("type", moveType);
                                            
                                            String category = moveDetails.getAsJsonObject("damage_class").get("name").getAsString();
                                            move.put("category", category);
                                            
                                            addedMoveNames.add(moveName);
                                            return move;
                                        }
                                    } finally {
                                        requestSemaphore.release();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }, moveExecutor);
                            
                            moveFutures.add(moveFuture);
                        }

                        // Esperar a que todos los movimientos se carguen
                        CompletableFuture.allOf(moveFutures.toArray(new CompletableFuture[0])).join();
                        
                        // Recopilar los resultados
                        for (CompletableFuture<Map<String, Object>> future : moveFutures) {
                            Map<String, Object> move = future.get();
                            if (move != null) {
                                moves.add(move);
                            }
                        }
                    } finally {
                        moveExecutor.shutdown();
                    }

                    // Ordenar movimientos por poder
                    moves.sort((a, b) -> Integer.compare(
                        (int) b.get("power"),
                        (int) a.get("power")
                    ));

                    // Si no hay movimientos, añadir movimientos por defecto
                    if (moves.isEmpty()) {
                        moves.add(createDefaultMove("Ataque Rápido", 40, "normal"));
                        moves.add(createDefaultMove("Placaje", 35, "normal"));
                        moves.add(createDefaultMove("Arañazo", 30, "normal"));
                        moves.add(createDefaultMove("Destructor", 45, "normal"));
                    }

                    Pokemon pokemon = new Pokemon(
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

                    // Configurar habilidades
                    pokemon.setAbilities(abilities);

                    List<Integer> defaultIndices = new ArrayList<>();
                    for (int i = 0; i < Math.min(4, moves.size()); i++) {
                        defaultIndices.add(i);
                    }
                    pokemon.setSelectedMoveIndices(defaultIndices);

                    return pokemon;
                } finally {
                    requestSemaphore.release();
                }
            } catch (Exception e) {
                e.printStackTrace();
                retryCount++;
            }
        }
        return null;
    }

    private static Map<String, Object> createDefaultMove(String name, int power, String type) {
        Map<String, Object> move = new HashMap<>();
        move.put("name", name);
        move.put("power", power);
        move.put("type", type);
        return move;
    }

    public static List<Pokemon> getAllPokemon() {
        synchronized (pokemonSet) {
            return new ArrayList<>(pokemonSet);
        }
    }

    public static Pokemon getPokemon(String id) {
        synchronized (pokemonSet) {
            System.out.println("Buscando Pokémon con ID: " + id);
            System.out.println("Total de Pokémon cargados: " + pokemonSet.size());
            Pokemon pokemon = pokemonSet.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
            if (pokemon != null) {
                System.out.println("Pokémon encontrado: " + pokemon.getName());
            } else {
                System.out.println("Pokémon no encontrado con ID: " + id);
            }
            return pokemon;
        }
    }

    public static List<Pokemon> searchPokemon(String query) {
        final String searchQuery = query.toLowerCase();
        synchronized (pokemonSet) {
            return pokemonSet.stream()
                .filter(p -> p.getName().toLowerCase().contains(searchQuery) ||
                           p.getTypes().stream().anyMatch(t -> t.toLowerCase().contains(searchQuery)))
                .toList();
        }
    }

    public static boolean hasMorePokemon() {
        return hasMore;
    }
} 