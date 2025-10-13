# GuessGame 0.0.2

Juego educativo de adivinanzas entre 1 y 10 orientado a mostrar buenas prácticas en Java puro.
Incluye ejemplos de arquitectura con inyección de dependencias, concurrencia segura y pruebas automatizadas.

## Requisitos
- Java 25 (`java --version` debe reportar `openjdk 25`).
- Maven 3.9 o superior.
- Consola con soporte para ejecutar scripts PowerShell o Bash.

## Instalación rápida
1. Clona o copia el proyecto en tu máquina local.
2. Abre una terminal en el directorio `guessgame/`.
3. Compila y ejecuta la batería de pruebas:
   ```bash
   mvn clean test
   ```
   El informe confirma dos casos de uso: éxito tras varios intentos e identificación de entrada inválida.

## Pruebas automatizadas
- La suite principal está en `src/test/java/com/sinensia/games/GuessGameTest.java` y valida tanto el flujo exitoso como la gestión de entradas inválidas simulando la consola con un `MockIO`.
- Para lanzar todas las pruebas desde cualquier shell compatible, ejecuta `mvn test` dentro de `guessgame/`. Maven dejará el informe en `target/surefire-reports/` y mostrará un resumen en la terminal.
- Si necesitas repetir únicamente un caso concreto, usa `mvn -Dtest=GuessGameTest#testJuegoAciertaEnElSegundoIntento test` (cambia el nombre tras `#` por el método deseado).
- En PowerShell recuerda anteponer `./` cuando uses scripts (`./mvnw` si activas el wrapper). El proceso debería finalizar con `BUILD SUCCESS`; cualquier otro estado indica fallos que hay que revisar en los reportes.
- Los testers manuales pueden ejecutar `java -cp target/classes com.sinensia.games.AppGame` tras compilar y validar visualmente los mensajes que reflejan los mismos escenarios cubiertos por las pruebas.

## Ejecutar el juego
Maven no trae configurado el plugin `exec` para este proyecto, por lo que conviene compilar y ejecutar utilizando el `classpath` generado.

1. Compila (esto genera las clases en `target/classes`):
   ```bash
   mvn clean compile
   ```
2. Lanza la variante deseada indicando el `main` correspondiente:

   - **Juego interactivo por consola (AppGame + Consola):**
     ```bash
     java -cp target/classes com.sinensia.games.AppGame
     ```

   - **Versión simple autosuficiente (TwrGame):**
     ```bash
     java -cp target/classes com.sinensia.games.TwrGame
     ```

   - **Simulación concurrente (ConcurrentGame):**
     ```bash
     java -cp target/classes com.sinensia.games.ConcurrentGame
     ```

   En PowerShell utiliza comillas simples (`'`) si añades argumentos adicionales.

> **Nota:** Es normal que aparezcan avisos sobre `java.lang.System::load` y `sun.misc.Unsafe` cuando Maven descarga dependencias. Son advertencias del runtime, no errores del proyecto.

## Patrones de diseño destacados
- **Strategy:** `GameIO` define la estrategia de entrada/salida; `Consola` es una implementación concreta.
- **Monitor Object / Thread-safe core:** `GuessGame` encapsula el estado con bloqueos internos y `AtomicBoolean`.
- **Template Method (informal):** `AppGame` y `TwrGame` fijan la secuencia de pasos del juego, delegando variaciones.
- **Executor:** `ConcurrentGame` utiliza `ExecutorService` para manejar jugadores concurrentes.
- **Utilidades compartidas:** `GameRandom` y `GameLogger` concentran la generación de aleatorios y el logging, sin recurrir a singletons tradicionales.
- **Test Double (Mock):** `GuessGameTest` introduce un mock manual de `GameIO` para aislar la lógica del juego.

## Buenas prácticas de logging y configuración
- `GameLogger` encapsula el acceso a `java.util.logging` e ilustra cómo registrar eventos/errores sin exponer datos sensibles.
- Las excepciones se guardan para el personal desarrollador (`LOGGER.error("BUG: …", e)`), mientras que al usuario final se le ofrecen mensajes bien formados.
- `GameRandom` demuestra la reutilización de recursos compartidos; usar singletons para configuración global es posible, pero valora alternativas como inyección de dependencias o lectura desde ficheros/variables de entorno para mejorar testabilidad y seguridad.

## Generar documentación Javadoc
Los comentarios didácticos están listos para producir la documentación HTML:
```bash
mvn javadoc:javadoc
```
El resultado se guardará en `target/reports/apidocs/index.html`. Abre ese archivo en un navegador para consultar la guía.

## Estructura del proyecto
- `src/main/java/com/sinensia/games/` — Código principal del juego y demos.
- `src/test/java/com/sinensia/games/` — Pruebas automatizadas con JUnit 5.
- `pom.xml` — Configuración Maven (Java 25, plugins y dependencias).

## Próximos pasos sugeridos
- Añadir más casos de prueba (por ejemplo, agotamiento de vidas).
- Internacionalización de los mensajes para soportar varios idiomas.
- Crear una interfaz gráfica reutilizando la misma lógica central (`GuessGame` + `GameIO`).

¡Disfruta aprendiendo y experimentando con GuessGame!
