# PokéAPI DB

Database Pokémon con interfaccia Dark Mode e analisi avanzata delle statistiche.

## Funzionalità
* **Analisi Statistiche:**
    * Visualizza HP, ATK, DEF, SP.ATK, SP.DEF.
    * **Confronto Dinamico (ATK vs SP.ATK):**
        * Differenza > 30: Verde (Maggiore) / Rosso (Minore).
        * Differenza 10-30: Verdino chiaro / Giallino.
        * Differenza < 10: Entrambi verde chiaro.
* **Parsing Robusto:** Estrae correttamente i dati JSON anche quando l'ordine cambia.

## Esecuzione
Compila ed esegui il file principale:
1. `javac PokemonDarkDB.java`
2. `java PokemonDarkDB`