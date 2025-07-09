# RPGen-Core: Un Motor de Combate por Turnos Genérico

**RPGen-Core** es un framework robusto y extensible para crear sistemas de combate por turnos, desarrollado en **Java** con **Spark Framework**. Proporciona la lógica fundamental para gestionar entidades, acciones y el flujo de una batalla, permitiendo a los desarrolladores centrarse en las mecánicas específicas de su juego.

Este repositorio contiene el **núcleo** del sistema. Para ver ejemplos de cómo se puede extender, visita nuestros casos de uso.

## ✨ Características Principales

  - **Motor de Batalla Flexible**: Un sistema `BattleEngine` que gestiona el orden de los turnos, la ejecución de acciones y las condiciones de victoria/derrota.
  - **Sistema de Entidades**: Define `Entity` como la base para cualquier personaje o criatura, con atributos esenciales como vida, ataque y defensa.
  - **Acciones y Comandos**: Utiliza el patrón de diseño *Command* para encapsular las acciones (`GameAction`) que las entidades pueden realizar, como ataques o habilidades especiales.
  - **API RESTful**: Expone una API sencilla para gestionar personajes y controlar las batallas desde cualquier cliente web.
  - **Interfaz Web de Ejemplo**: Incluye una página `index.html` simple para probar la funcionalidad básica del motor de combate.
  - **Persistencia de Datos**: Guarda y carga el estado de los personajes en un archivo `characters.json`.

-----

## 🛠️ Cómo Funciona

El sistema se divide en varios componentes clave:

  - **`BattleServer`**: Es el corazón de la aplicación web. Utiliza Spark para levantar un servidor y exponer los endpoints de la API. Se encarga de recibir las peticiones del cliente, traducirlas a operaciones del motor de batalla y devolver los resultados.
  - **`BattleEngine`**: Es una clase abstracta que contiene la lógica principal del combate por turnos. Gestiona dos equipos de entidades, una cola de comandos para el turno actual y notifica a los *listeners* sobre los eventos de la batalla.
  - **`Entity` y `BaseEntity`**: `Entity` es una interfaz que define las propiedades y métodos básicos de un combatiente. `BaseEntity` es una implementación abstracta que proporciona la funcionalidad común, como recibir daño (`takeDamage`), curarse (`heal`) y verificar si la entidad sigue viva (`isAlive`).
  - **`GameAction` y `PredefinedActions`**: La interfaz `GameAction` define una acción que una entidad puede ejecutar. La clase `PredefinedActions` ofrece ejemplos de acciones listas para usar como `BasicAttackAction`, `StrongAttackAction` y `ShieldAction`, que pueden servir como base para acciones más complejas.

-----

## 🚀 Instalación y Uso

### Requisitos

  - Java 17 o superior
  - Maven 3.6+

### Ejecutar el Núcleo

Para probar la aplicación base, sigue estos pasos:

1.  Clona el repositorio.
2.  Desde la raíz del proyecto, ejecuta el siguiente comando para iniciar el servidor:
    ```bash
    mvn spring-boot:run
    ```
3.  Abre `http://localhost:4567` en tu navegador para ver la interfaz de ejemplo.

### Instalar como Dependencia

Para usar **RPGen-Core** en tu propio proyecto, necesitas instalarlo en tu repositorio local de Maven:

```bash
mvn install
```

Una vez instalado, puedes añadirlo como dependencia en el `pom.xml` de tu proyecto.

-----

## 🧩 Cómo Extender RPGen-Core

Este núcleo está diseñado para ser la base de sistemas de combate más complejos. Aquí te explicamos cómo puedes extenderlo para crear tu propio juego:

1.  **Crea tu proyecto Maven**: Inicia un nuevo proyecto en Java con Maven.

2.  **Añade RPGen-Core como dependencia**: En tu archivo `pom.xml`, añade la dependencia que instalaste en el paso anterior.

    ```xml
    <dependency>
        <groupId>com.rpgen</groupId>
        <artifactId>rpgen-core</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
    ```

3.  **Crea tus propias Entidades**: Define las entidades de tu juego extendiendo `BaseEntity`. Aquí puedes añadir atributos y lógicas específicas.

    *Ejemplo: una entidad `Pokemon`.*

    ```java
    public class Pokemon extends BaseEntity {
        private String type;
        // Constructores, getters, setters y otros métodos...

        public Pokemon(String name, int maxHealth, int attack, int defense, String type) {
            super(UUID.randomUUID().toString(), name, maxHealth, attack, defense, 0);
            this.type = type;
        }
    }
    ```

4.  **Define Acciones Personalizadas**: Implementa la interfaz `GameAction` para crear las habilidades y movimientos únicos de tu juego.

    *Ejemplo: un ataque tipo `Agua`.*

    ```java
    public class WaterGun extends AbstractGameAction {
        public WaterGun(String id) {
            super(id, "Water Gun", 0, "Lanza un chorro de agua.");
        }

        @Override
        public void execute(Entity source, Entity target) {
            // Lógica de daño personalizada, considerando tipos, etc.
            int damage = source.getAttack() * 2; // Ejemplo simple
            target.takeDamage(damage);
        }
    }
    ```

5.  **Configura tu propio Servidor**: Crea una clase principal que inicialice un `BattleServer` (o una versión extendida del mismo) para gestionar la lógica de tu juego, asociando las nuevas entidades y acciones a los endpoints de la API.

-----

## 🎲 Casos de Uso (Extensiones)

Para ver cómo se puede aplicar **RPGen-Core** en la práctica, hemos desarrollado dos extensiones completas como ejemplo:

  - **[RPGen-pokemon](https://github.com/M3D1S4NT4/RPGen-pokemon)**: Una implementación completa del sistema de combate de Pokémon, con sus tipos, habilidades y mecánicas únicas.
  - **[RPGen-chrono](https://github.com/M3D1S4NT4/RPGen-chrono)**: Una adaptación del sistema de combate del clásico JRPG *Chrono Trigger*.

-----

## 💻 Tecnologías Utilizadas

### Backend

  - **Java 17**
  - **Spark Framework**: Para crear la API REST de forma ligera.
  - **Spring Boot**: Para la gestión del ciclo de vida y la configuración de la aplicación.
  - **Gson**: Para la serialización y deserialización de datos JSON.
  - **Logback**: Para el registro de eventos y errores.

### Frontend

  - **HTML5** y **CSS3**
  - **JavaScript (ES6+)**: Para la lógica del cliente y la comunicación con la API.
