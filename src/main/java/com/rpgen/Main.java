package com.rpgen;

import static spark.Spark.*;
import com.rpgen.web.BattleServer;
import com.rpgen.pokemon.PokemonServer;

public class Main {
    public static void main(String[] args) {
        // Configurar Spark una sola vez
        port(4567);
        staticFiles.location("/public");

        // Inicializar servidores
        BattleServer battleServer = new BattleServer();
        PokemonServer pokemonServer = new PokemonServer();
        
        // Iniciar servidores
        battleServer.init();
        pokemonServer.init();
    }
} 