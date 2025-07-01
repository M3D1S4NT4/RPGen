package com.rpgen.pokemon;

import java.util.*;
import java.net.http.*;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.*;
import com.google.gson.*;

public class ItemDatabase {
    private static final String POKE_API_BASE_URL = "https://pokeapi.co/api/v2";
    private static final List<String> RELEVANT_CATEGORIES = List.of(
        "stat-boosts", "held-items", "choice", "bad-held-items", "species-specific", "type-enhancement", "type-protection", "in-a-pinch", "picky-healing", "plates"
    );
    private static final Map<String, HeldItem> items = new ConcurrentHashMap<>();
    private static final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .version(HttpClient.Version.HTTP_1_1)
        .build();
    private static final Gson gson = new Gson();
    private static final ExecutorService itemExecutor = Executors.newFixedThreadPool(2);
    private static volatile boolean initialized = false;
    private static final List<String> PREFERRED_VERSIONS = List.of(
        "scarlet-violet", "sword-shield", "brilliant-diamond-shining-pearl", "lets-go-pikachu-lets-go-eevee", "ultra-sun-ultra-moon", "sun-moon", "omega-ruby-alpha-sapphire", "x-y", "black-2-white-2", "black-white", "heartgold-soulsilver", "platinum", "diamond-pearl", "firered-leafgreen", "emerald", "ruby-sapphire"
    );
    
    // Mapeo manual de efectos para objetos competitivos
    private static final Map<String, Map<String, Object>> MANUAL_EFFECTS = new ConcurrentHashMap<>();
    static {
        // Efectos manuales conocidos (Choice, Life Orb, etc.)
        MANUAL_EFFECTS.put("choice-scarf", Map.of(
            "statModifiers", Map.of("speed", 1.5),
            "onlyOneMove", true
        ));
        MANUAL_EFFECTS.put("choice-band", Map.of(
            "statModifiers", Map.of("attack", 1.5),
            "onlyOneMove", true
        ));
        MANUAL_EFFECTS.put("choice-specs", Map.of(
            "statModifiers", Map.of("special_attack", 1.5),
            "onlyOneMove", true
        ));
        MANUAL_EFFECTS.put("life-orb", Map.of(
            "statModifiers", Map.of("power", 1.3),
            "recoilPercent", 0.1
        ));
        MANUAL_EFFECTS.put("leftovers", Map.of(
            "hpRecoveryPercent", 0.0625
        ));
        MANUAL_EFFECTS.put("black-sludge", Map.of(
            "hpRecoveryPercent", 0.0625,
            "poisonOnly", true
        ));
        MANUAL_EFFECTS.put("rocky-helmet", Map.of(
            "counterDamagePercent", 0.16
        ));
        MANUAL_EFFECTS.put("assault-vest", Map.of(
            "statModifiers", Map.of("special_defense", 1.5),
            "onlyAttackMoves", true
        ));
        MANUAL_EFFECTS.put("eviolite", Map.of(
            "statModifiers", Map.of("defense", 1.5, "special_defense", 1.5),
            "onlyIfCanEvolve", true
        ));
        MANUAL_EFFECTS.put("focus-sash", Map.of(
            "surviveFatal", true
        ));
        MANUAL_EFFECTS.put("expert-belt", Map.of(
            "statModifiers", Map.of("super_effective_power", 1.2)
        ));
        MANUAL_EFFECTS.put("muscle-band", Map.of(
            "statModifiers", Map.of("physical_power", 1.1)
        ));
        MANUAL_EFFECTS.put("wise-glasses", Map.of(
            "statModifiers", Map.of("special_power", 1.1)
        ));
        MANUAL_EFFECTS.put("quick-powder", Map.of(
            "statModifiers", Map.of("speed", 2.0),
            "onlyDitto", true
        ));
        MANUAL_EFFECTS.put("light-ball", Map.of(
            "statModifiers", Map.of("attack", 2.0, "special_attack", 2.0),
            "onlyPikachu", true
        ));
        MANUAL_EFFECTS.put("thick-club", Map.of(
            "statModifiers", Map.of("attack", 2.0),
            "onlyCuboneMarowak", true
        ));
        MANUAL_EFFECTS.put("deep-sea-scale", Map.of(
            "statModifiers", Map.of("special_defense", 2.0),
            "onlyClamperl", true
        ));
        MANUAL_EFFECTS.put("deep-sea-tooth", Map.of(
            "statModifiers", Map.of("special_attack", 2.0),
            "onlyClamperl", true
        ));
        MANUAL_EFFECTS.put("metal-powder", Map.of(
            "statModifiers", Map.of("defense", 1.5),
            "onlyDitto", true
        ));
        MANUAL_EFFECTS.put("soul-dew", Map.of(
            "statModifiers", Map.of("special_attack", 1.5, "special_defense", 1.5),
            "onlyLatiosLatias", true
        ));
        MANUAL_EFFECTS.put("adamant-orb", Map.of(
            "statModifiers", Map.of("special_attack", 1.2, "special_defense", 1.2),
            "onlyDialga", true
        ));
        MANUAL_EFFECTS.put("lustrous-orb", Map.of(
            "statModifiers", Map.of("special_attack", 1.2, "special_defense", 1.2),
            "onlyPalkia", true
        ));
        MANUAL_EFFECTS.put("griseous-orb", Map.of(
            "statModifiers", Map.of("special_attack", 1.2, "special_defense", 1.2),
            "onlyGiratina", true
        ));
    }
    
    static {
        initialize();
    }
    
    public static void initialize() {
        if (initialized) return;
        initialized = true;
        CompletableFuture.runAsync(ItemDatabase::loadAllRelevantItems, itemExecutor);
    }
    
    private static void loadAllRelevantItems() {
        try {
            for (String category : RELEVANT_CATEGORIES) {
                String url = POKE_API_BASE_URL + "/item-category/" + category + "/";
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                JsonObject json = gson.fromJson(response.body(), JsonObject.class);
                JsonArray itemsArray = json.getAsJsonArray("items");
                for (JsonElement itemElem : itemsArray) {
                    JsonObject itemObj = itemElem.getAsJsonObject();
                    String itemUrl = itemObj.get("url").getAsString();
                    CompletableFuture.runAsync(() -> loadItemDetails(itemUrl, category), itemExecutor);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void loadItemDetails(String url, String category) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject itemData = gson.fromJson(response.body(), JsonObject.class);
            String id = itemData.get("name").getAsString();
            String name = getLocalizedName(itemData, "es");
            String description = getLocalizedDescription(itemData, "es");
            String effect = getLocalizedEffect(itemData, "es");
            Map<String, Double> statModifiers = new HashMap<>();
            Map<String, Object> extraEffects = new HashMap<>();
            // Aplicar efectos automáticos
            applyAutoEffects(id, category, name);
            // Aplicar efectos manuales si existen
            if (MANUAL_EFFECTS.containsKey(id)) {
                Map<String, Object> manual = MANUAL_EFFECTS.get(id);
                if (manual.containsKey("statModifiers")) {
                    Map<String, Double> mods = (Map<String, Double>) manual.get("statModifiers");
                    statModifiers.putAll(mods);
                }
                for (Map.Entry<String, Object> entry : manual.entrySet()) {
                    if (!entry.getKey().equals("statModifiers")) {
                        extraEffects.put(entry.getKey(), entry.getValue());
                    }
                }
            }
            HeldItem item = new HeldItem(id, name, description, effect, statModifiers, category, extraEffects);
            items.put(id, item);
        } catch (Exception e) {
            // Silenciar errores individuales para no detener la carga global
        }
    }
    
    private static String getLocalizedName(JsonObject itemData, String lang) {
        JsonArray names = itemData.getAsJsonArray("names");
        for (JsonElement elem : names) {
            JsonObject obj = elem.getAsJsonObject();
            if (obj.getAsJsonObject("language").get("name").getAsString().equals(lang)) {
                return obj.get("name").getAsString();
            }
        }
        return itemData.get("name").getAsString();
    }
    
    private static String getLocalizedDescription(JsonObject itemData, String lang) {
        JsonArray flavorTexts = itemData.getAsJsonArray("flavor_text_entries");
        String best = null;
        int bestPriority = Integer.MAX_VALUE;
        String fallbackEn = null;
        String fallback = null;
        for (JsonElement elem : flavorTexts) {
            JsonObject obj = elem.getAsJsonObject();
            String language = obj.getAsJsonObject("language").get("name").getAsString();
            String text = obj.get("text").getAsString();
            String version = obj.has("version_group") ? obj.getAsJsonObject("version_group").get("name").getAsString() : "";
            if (language.equals(lang)) {
                int priority = PREFERRED_VERSIONS.indexOf(version);
                if (priority == -1) priority = Integer.MAX_VALUE - 1; // Si no está en la lista, menos preferido
                if (priority < bestPriority) {
                    best = text;
                    bestPriority = priority;
                }
            }
            if (language.equals("en") && fallbackEn == null) {
                fallbackEn = text;
            }
            if (fallback == null) fallback = text;
        }
        if (best != null) return best;
        if (fallbackEn != null) return fallbackEn;
        if (fallback != null) return fallback;
        return "Sin descripción disponible";
    }
    
    private static String getLocalizedEffect(JsonObject itemData, String lang) {
        JsonArray effectEntries = itemData.getAsJsonArray("effect_entries");
        String latest = null;
        String fallbackEn = null;
        String fallback = null;
        for (JsonElement elem : effectEntries) {
            JsonObject obj = elem.getAsJsonObject();
            String language = obj.getAsJsonObject("language").get("name").getAsString();
            String effect = obj.get("effect").getAsString();
            if (language.equals(lang) && latest == null) {
                latest = effect;
            }
            if (language.equals("en") && fallbackEn == null) {
                fallbackEn = effect;
            }
            if (fallback == null) fallback = effect;
        }
        if (latest != null) return latest;
        if (fallbackEn != null) return fallbackEn;
        if (fallback != null) return fallback;
        return "Sin efecto disponible";
    }
    
    public static HeldItem getItem(String id) {
        return items.get(id.toLowerCase());
    }
    
    public static List<HeldItem> getAllItems() {
        return new ArrayList<>(items.values());
    }
    
    public static HeldItem getRandomItem() {
        List<HeldItem> itemList = new ArrayList<>(items.values());
        if (itemList.isEmpty()) return null;
        return itemList.get(new Random().nextInt(itemList.size()));
    }
    
    public static Map<String, Object> getAllItemsAsMap() {
        Map<String, Object> result = new HashMap<>();
        System.out.println("Obteniendo todos los objetos...");
        System.out.println("Número de objetos: " + items.size());
        for (HeldItem item : items.values()) {
            System.out.println("Añadiendo objeto: " + item.getId() + " - " + item.getName());
            result.put(item.getId(), item.toMap());
        }
        System.out.println("Total de objetos en resultado: " + result.size());
        return result;
    }
    
    public static List<HeldItem> getItemsByCategory(String category) {
        return items.values().stream()
            .filter(item -> item.getCategory().equals(category))
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    // Mapeo automático de efectos genéricos por categoría y nombre
    private static void applyAutoEffects(String id, String category, String name) {
        // Plates y type-enhancing: boost 20% al tipo correspondiente
        if (category != null && (category.contains("plate") || category.contains("type-enhancement"))) {
            String type = id.replace("-plate", "").replace("-memory", "").replace("-incense", "").replace("-gem", "").replace("-boost", "");
            // Excepciones manuales
            if (id.equals("charcoal")) type = "fire";
            if (id.equals("mystic-water")) type = "water";
            if (id.equals("miracle-seed")) type = "grass";
            if (id.equals("black-belt")) type = "fighting";
            if (id.equals("poison-barb")) type = "poison";
            if (id.equals("soft-sand")) type = "ground";
            if (id.equals("sharp-beak")) type = "flying";
            if (id.equals("twisted-spoon")) type = "psychic";
            if (id.equals("silver-powder")) type = "bug";
            if (id.equals("hard-stone")) type = "rock";
            if (id.equals("spell-tag")) type = "ghost";
            if (id.equals("dragon-fang")) type = "dragon";
            if (id.equals("black-glasses")) type = "dark";
            if (id.equals("metal-coat")) type = "steel";
            if (id.equals("pixie-plate")) type = "fairy";
            if (id.equals("fairy-feather")) type = "fairy";
            if (id.equals("meadow-plate")) type = "grass";
            if (id.equals("flame-plate")) type = "fire";
            if (id.equals("fist-plate")) type = "fighting";
            if (id.equals("toxic-plate")) type = "poison";
            if (id.equals("earth-plate")) type = "ground";
            if (id.equals("sky-plate")) type = "flying";
            if (id.equals("mind-plate")) type = "psychic";
            if (id.equals("insect-plate")) type = "bug";
            if (id.equals("stone-plate")) type = "rock";
            if (id.equals("spooky-plate")) type = "ghost";
            if (id.equals("draco-plate")) type = "dragon";
            if (id.equals("dread-plate")) type = "dark";
            if (id.equals("iron-plate")) type = "steel";
            if (id.equals("zap-plate")) type = "electric";
            if (id.equals("icicle-plate")) type = "ice";
            if (id.equals("bug-memory")) type = "bug";
            if (id.equals("dark-memory")) type = "dark";
            if (id.equals("dragon-memory")) type = "dragon";
            if (id.equals("electric-memory")) type = "electric";
            if (id.equals("fairy-memory")) type = "fairy";
            if (id.equals("fighting-memory")) type = "fighting";
            if (id.equals("fire-memory")) type = "fire";
            if (id.equals("flying-memory")) type = "flying";
            if (id.equals("ghost-memory")) type = "ghost";
            if (id.equals("grass-memory")) type = "grass";
            if (id.equals("ground-memory")) type = "ground";
            if (id.equals("ice-memory")) type = "ice";
            if (id.equals("poison-memory")) type = "poison";
            if (id.equals("psychic-memory")) type = "psychic";
            if (id.equals("rock-memory")) type = "rock";
            if (id.equals("steel-memory")) type = "steel";
            if (id.equals("water-memory")) type = "water";
            if (type != null && !type.isEmpty()) {
                MANUAL_EFFECTS.putIfAbsent(id, Map.of(
                    "statModifiers", Map.of(type + "_power", 1.2)
                ));
            }
        }
        // Gems: boost de 50% al tipo correspondiente, un solo uso
        if (id.endsWith("-gem")) {
            String type = id.replace("-gem", "");
            MANUAL_EFFECTS.putIfAbsent(id, Map.of(
                "statModifiers", Map.of(type + "_power", 1.5),
                "singleUse", true
            ));
        }
        // Incenses: boost de 20% al tipo correspondiente si aplica
        if (id.endsWith("-incense")) {
            String type = id.replace("-incense", "");
            MANUAL_EFFECTS.putIfAbsent(id, Map.of(
                "statModifiers", Map.of(type + "_power", 1.2)
            ));
        }
        // Otros efectos genéricos por categoría
        if (category != null && category.contains("stat-boosts")) {
            // Placeholder: boost genérico
            MANUAL_EFFECTS.putIfAbsent(id, Map.of(
                "statModifiers", Map.of("attack", 1.1)
            ));
        }
        // Puedes añadir más reglas automáticas aquí para otras categorías
    }
} 