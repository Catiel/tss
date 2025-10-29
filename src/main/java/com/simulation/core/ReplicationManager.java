package com.simulation.core; // Declaración del paquete que contiene las clases principales (core) de la simulación

import com.simulation.config.SimulationParameters; // Importa la clase SimulationParameters del paquete config para acceder a los parámetros de configuración
import com.simulation.statistics.Statistics; // Importa la clase Statistics del paquete statistics para manejar las estadísticas de cada réplica
import javafx.application.Platform; // Importa la clase Platform de JavaFX para ejecutar código en el hilo de la interfaz gráfica
import javafx.concurrent.Task; // Importa la clase Task de JavaFX para tareas concurrentes (aunque no se usa actualmente en el código)

import java.util.ArrayList; // Importa la clase ArrayList para usar listas dinámicas
import java.util.List; // Importa la interfaz List para trabajar con colecciones tipo lista
import java.util.function.Consumer; // Importa la interfaz funcional Consumer para callbacks que aceptan un parámetro

/** // Inicio del comentario Javadoc de la clase
 * Gestor de réplicas de simulación // Descripción de la clase
 * Ejecuta múltiples corridas con diferentes semillas aleatorias // Funcionalidad principal de la clase
 */ // Fin del comentario Javadoc
public class ReplicationManager { // Declaración de la clase pública ReplicationManager que gestiona la ejecución de múltiples réplicas de la simulación
    private SimulationParameters baseParameters; // Variable privada que almacena los parámetros base de configuración que serán usados para todas las réplicas
    private int numReplications; // Variable privada que almacena el número total de réplicas a ejecutar
    private List<Statistics> replicationResults; // Variable privada que almacena una lista con las estadísticas resultantes de cada réplica ejecutada
    private boolean running; // Variable privada booleana que indica si actualmente se están ejecutando réplicas
    private Thread executionThread; // Variable privada que almacena la referencia al hilo de ejecución donde se ejecutan las réplicas

    public ReplicationManager(SimulationParameters baseParameters, int numReplications) { // Constructor público que inicializa el gestor de réplicas recibiendo parámetros base y número de réplicas como parámetros
        this.baseParameters = baseParameters; // Asigna los parámetros base recibidos a la variable de instancia baseParameters
        this.numReplications = numReplications; // Asigna el número de réplicas recibido a la variable de instancia numReplications
        this.replicationResults = new ArrayList<>(); // Inicializa la lista de resultados como un nuevo ArrayList vacío
        this.running = false; // Inicializa el estado de ejecución como falso indicando que no hay réplicas en ejecución
    } // Cierre del constructor ReplicationManager

    /** // Inicio del comentario Javadoc del método
     * Ejecuta todas las réplicas de forma asíncrona // Descripción del método
     * @param progressCallback Callback para actualizar progreso (replicación actual) // Documentación del primer parámetro
     * @param completionCallback Callback al completar todas las réplicas // Documentación del segundo parámetro
     */ // Fin del comentario Javadoc
    public void runReplications(Consumer<Integer> progressCallback, Runnable completionCallback) { // Método público que ejecuta todas las réplicas de forma asíncrona recibiendo un callback de progreso y uno de completación como parámetros
        if (running) { // Condición que verifica si ya hay réplicas en ejecución
            System.out.println("Ya hay réplicas en ejecución"); // Imprime un mensaje de advertencia en la consola indicando que ya hay una ejecución en curso
            return; // Finaliza el método prematuramente para evitar múltiples ejecuciones simultáneas
        } // Cierre del bloque condicional if

        running = true; // Establece el estado de ejecución como verdadero indicando que las réplicas comenzaron a ejecutarse
        replicationResults.clear(); // Limpia la lista de resultados eliminando cualquier resultado de ejecuciones anteriores

        executionThread = new Thread(() -> { // Crea un nuevo hilo de ejecución usando una expresión lambda para ejecutar las réplicas en segundo plano
            try { // Bloque try para capturar y manejar excepciones que puedan ocurrir durante la ejecución de las réplicas
                for (int i = 0; i < numReplications && running; i++) { // Bucle for que itera desde 0 hasta el número de réplicas mientras el estado running sea verdadero
                    final int replicationNum = i + 1; // Declara una variable final que almacena el número de réplica actual (base 1) para usar en el lambda

                    // Crear parámetros con semilla única para esta réplica
                    SimulationParameters repParams = createReplicationParameters(replicationNum); // Crea parámetros de simulación específicos para esta réplica llamando al método auxiliar con el número de réplica

                    // Crear y ejecutar engine para esta réplica
                    SimulationEngine engine = new SimulationEngine(repParams); // Crea una nueva instancia del motor de simulación con los parámetros específicos de esta réplica
                    engine.setSimulationSpeed(999999); // Máxima velocidad para réplicas // Establece la velocidad de simulación al máximo (999999) para que las réplicas se ejecuten lo más rápido posible sin animación
                    engine.initialize(); // Inicializa el motor de simulación preparando todas las estructuras y componentes necesarios

                    // Ejecutar simulación
                    engine.run(); // Ejecuta la simulación completa hasta que finalice el tiempo configurado

                    // Guardar resultados
                    Statistics stats = engine.getStatistics(); // Obtiene las estadísticas generadas por el motor de simulación después de completar la ejecución
                    replicationResults.add(stats); // Agrega las estadísticas obtenidas a la lista de resultados de réplicas

                    // Actualizar progreso en el hilo de JavaFX
                    Platform.runLater(() -> progressCallback.accept(replicationNum)); // Ejecuta el callback de progreso en el hilo de JavaFX pasando el número de réplica completada para actualizar la interfaz

                    // Pequeña pausa para permitir actualizaciones de UI
                    try { // Bloque try interno para capturar interrupciones del hilo durante la pausa
                        Thread.sleep(50); // Pausa la ejecución del hilo por 50 milisegundos para permitir que la interfaz gráfica se actualice
                    } catch (InterruptedException e) { // Captura la excepción InterruptedException si el hilo es interrumpido durante el sleep
                        Thread.currentThread().interrupt(); // Restablece el estado de interrupción del hilo actual
                        break; // Sale del bucle for prematuramente si el hilo fue interrumpido
                    } // Cierre del bloque catch interno
                } // Cierre del bucle for

                running = false; // Establece el estado de ejecución como falso indicando que todas las réplicas terminaron de ejecutarse

                // Notificar completación en el hilo de JavaFX
                Platform.runLater(completionCallback); // Ejecuta el callback de completación en el hilo de JavaFX para notificar que todas las réplicas finalizaron

            } catch (Exception e) { // Captura cualquier excepción general que pueda ocurrir durante la ejecución de las réplicas
                e.printStackTrace(); // Imprime la traza completa de la excepción en la consola para propósitos de depuración
                running = false; // Establece el estado de ejecución como falso indicando que la ejecución terminó por un error
                Platform.runLater(completionCallback); // Ejecuta el callback de completación en el hilo de JavaFX incluso si hubo un error
            } // Cierre del bloque catch externo
        }); // Cierre de la expresión lambda y del constructor del Thread

        executionThread.setDaemon(true); // Configura el hilo como daemon para que no impida que la aplicación termine si es el único hilo restante
        executionThread.start(); // Inicia la ejecución del hilo para comenzar a procesar las réplicas de forma asíncrona
    } // Cierre del método runReplications

    /** // Inicio del comentario Javadoc del método
     * Crea parámetros específicos para una réplica // Descripción del método
     */ // Fin del comentario Javadoc
    private SimulationParameters createReplicationParameters(int replicationNumber) { // Método privado que crea y retorna parámetros de simulación únicos para cada réplica recibiendo el número de réplica como parámetro
        SimulationParameters params = new SimulationParameters(); // Crea una nueva instancia de SimulationParameters con valores por defecto

        // Copiar parámetros base
        params.setSimulationDurationMinutes(baseParameters.getSimulationDurationMinutes()); // Copia la duración de la simulación desde los parámetros base a los nuevos parámetros

        // Usar semilla diferente para cada réplica
        long baseSeed = baseParameters.getBaseRandomSeed(); // Obtiene la semilla aleatoria base desde los parámetros base
        params.setBaseRandomSeed(baseSeed + replicationNumber * 1000); // Establece una semilla única para esta réplica sumando el número de réplica multiplicado por 1000 a la semilla base

        return params; // Retorna los parámetros configurados específicamente para esta réplica
    } // Cierre del método createReplicationParameters

    /** // Inicio del comentario Javadoc del método
     * Detiene la ejecución de réplicas // Descripción del método
     */ // Fin del comentario Javadoc
    public void stop() { // Método público que detiene la ejecución de réplicas en curso sin recibir parámetros
        running = false; // Establece el estado de ejecución como falso para señalar al bucle que debe detenerse
        if (executionThread != null && executionThread.isAlive()) { // Condición que verifica si el hilo de ejecución existe y está actualmente en ejecución
            executionThread.interrupt(); // Interrumpe el hilo de ejecución enviando una señal de interrupción
        } // Cierre del bloque condicional if
    } // Cierre del método stop

    /** // Inicio del comentario Javadoc del método
     * Obtiene los resultados de todas las réplicas // Descripción del método
     */ // Fin del comentario Javadoc
    public List<Statistics> getReplicationResults() { // Método público que retorna una copia de la lista de resultados de todas las réplicas ejecutadas
        return new ArrayList<>(replicationResults); // Retorna una nueva instancia de ArrayList con una copia de los resultados para evitar modificaciones externas
    } // Cierre del método getReplicationResults

    /** // Inicio del comentario Javadoc del método
     * Calcula estadísticas agregadas de todas las réplicas // Descripción del método
     */ // Fin del comentario Javadoc
    public AggregatedStatistics getAggregatedStatistics() { // Método público que calcula y retorna estadísticas agregadas de todas las réplicas ejecutadas
        if (replicationResults.isEmpty()) { // Condición que verifica si la lista de resultados está vacía (no hay réplicas ejecutadas)
            return null; // Retorna null si no hay resultados para agregar
        } // Cierre del bloque condicional if

        return new AggregatedStatistics(replicationResults); // Retorna una nueva instancia de AggregatedStatistics pasando la lista de resultados para calcular promedios e intervalos de confianza
    } // Cierre del método getAggregatedStatistics

    public boolean isRunning() { // Método público getter que retorna si actualmente hay réplicas en ejecución de tipo boolean
        return running; // Retorna el valor de la variable running
    } // Cierre del método isRunning

    public int getNumReplications() { // Método público getter que retorna el número total de réplicas configuradas de tipo int
        return numReplications; // Retorna el valor de la variable numReplications
    } // Cierre del método getNumReplications

    public int getCompletedReplications() { // Método público que retorna el número de réplicas completadas hasta el momento de tipo int
        return replicationResults.size(); // Retorna el tamaño de la lista de resultados que representa el número de réplicas completadas
    } // Cierre del método getCompletedReplications

    /** // Inicio del comentario Javadoc de la clase interna
     * Clase interna para estadísticas agregadas // Descripción de la clase interna
     */ // Fin del comentario Javadoc
    public static class AggregatedStatistics { // Declaración de clase estática pública anidada AggregatedStatistics que calcula estadísticas agregadas de múltiples réplicas
        private List<Statistics> allStats; // Variable privada que almacena la lista de todas las estadísticas individuales de cada réplica

        // Promedios
        private double avgTotalArrivals; // Variable privada que almacena el promedio del total de arribos entre todas las réplicas
        private double avgTotalExits; // Variable privada que almacena el promedio del total de salidas entre todas las réplicas
        private double avgThroughput; // Variable privada que almacena el promedio del throughput (piezas por hora) entre todas las réplicas
        private double avgAverageSystemTime; // Variable privada que almacena el promedio del tiempo promedio en el sistema entre todas las réplicas

        // Desviaciones estándar
        private double stdTotalExits; // Variable privada que almacena la desviación estándar del total de salidas entre todas las réplicas
        private double stdThroughput; // Variable privada que almacena la desviación estándar del throughput entre todas las réplicas
        private double stdAverageSystemTime; // Variable privada que almacena la desviación estándar del tiempo promedio en el sistema entre todas las réplicas

        // Intervalos de confianza (95%)
        private double[] ciTotalExits; // Variable privada array que almacena el intervalo de confianza del 95% para el total de salidas [límite inferior, límite superior]
        private double[] ciThroughput; // Variable privada array que almacena el intervalo de confianza del 95% para el throughput [límite inferior, límite superior]
        private double[] ciAverageSystemTime; // Variable privada array que almacena el intervalo de confianza del 95% para el tiempo promedio en el sistema [límite inferior, límite superior]

        public AggregatedStatistics(List<Statistics> stats) { // Constructor público que inicializa las estadísticas agregadas recibiendo una lista de estadísticas individuales como parámetro
            this.allStats = stats; // Asigna la lista de estadísticas recibida a la variable de instancia allStats
            calculateStatistics(); // Llama al método privado para calcular todos los promedios, desviaciones e intervalos de confianza
        } // Cierre del constructor AggregatedStatistics

        private void calculateStatistics() { // Método privado que realiza todos los cálculos estadísticos (promedios, desviaciones estándar e intervalos de confianza)
            int n = allStats.size(); // Obtiene el número total de réplicas (tamaño de la lista de estadísticas)
            if (n == 0) return; // Si no hay estadísticas, sale del método prematuramente sin realizar cálculos

            // Calcular promedios
            double sumArrivals = 0; // Inicializa el acumulador de la suma de arribos en cero
            double sumExits = 0; // Inicializa el acumulador de la suma de salidas en cero
            double sumThroughput = 0; // Inicializa el acumulador de la suma de throughput en cero
            double sumSystemTime = 0; // Inicializa el acumulador de la suma de tiempos en el sistema en cero

            for (Statistics stat : allStats) { // Bucle for-each que itera sobre cada estadística individual en la lista
                sumArrivals += stat.getTotalArrivals(); // Acumula el total de arribos de esta réplica al acumulador
                sumExits += stat.getTotalExits(); // Acumula el total de salidas de esta réplica al acumulador
                sumThroughput += stat.getThroughput(); // Acumula el throughput de esta réplica al acumulador
                sumSystemTime += stat.getAverageSystemTime(); // Acumula el tiempo promedio en el sistema de esta réplica al acumulador
            } // Cierre del bucle for-each

            avgTotalArrivals = sumArrivals / n; // Calcula el promedio de arribos dividiendo la suma total entre el número de réplicas
            avgTotalExits = sumExits / n; // Calcula el promedio de salidas dividiendo la suma total entre el número de réplicas
            avgThroughput = sumThroughput / n; // Calcula el promedio de throughput dividiendo la suma total entre el número de réplicas
            avgAverageSystemTime = sumSystemTime / n; // Calcula el promedio del tiempo en el sistema dividiendo la suma total entre el número de réplicas

            // Calcular desviaciones estándar
            double sumSqExits = 0; // Inicializa el acumulador de la suma de cuadrados de las diferencias para salidas en cero
            double sumSqThroughput = 0; // Inicializa el acumulador de la suma de cuadrados de las diferencias para throughput en cero
            double sumSqSystemTime = 0; // Inicializa el acumulador de la suma de cuadrados de las diferencias para tiempo en el sistema en cero

            for (Statistics stat : allStats) { // Bucle for-each que itera nuevamente sobre cada estadística individual
                sumSqExits += Math.pow(stat.getTotalExits() - avgTotalExits, 2); // Calcula el cuadrado de la diferencia entre el valor individual y el promedio para salidas y lo acumula
                sumSqThroughput += Math.pow(stat.getThroughput() - avgThroughput, 2); // Calcula el cuadrado de la diferencia entre el valor individual y el promedio para throughput y lo acumula
                sumSqSystemTime += Math.pow(stat.getAverageSystemTime() - avgAverageSystemTime, 2); // Calcula el cuadrado de la diferencia entre el valor individual y el promedio para tiempo en sistema y lo acumula
            } // Cierre del bucle for-each

            stdTotalExits = Math.sqrt(sumSqExits / n); // Calcula la desviación estándar de salidas como la raíz cuadrada de la varianza (suma de cuadrados dividido por n)
            stdThroughput = Math.sqrt(sumSqThroughput / n); // Calcula la desviación estándar de throughput como la raíz cuadrada de la varianza
            stdAverageSystemTime = Math.sqrt(sumSqSystemTime / n); // Calcula la desviación estándar del tiempo en sistema como la raíz cuadrada de la varianza

            // Calcular intervalos de confianza del 95% (t-student para n pequeño)
            double tValue = getTValue(n); // Obtiene el valor t de Student correspondiente al nivel de confianza del 95% según el tamaño de muestra
            double marginExits = tValue * stdTotalExits / Math.sqrt(n); // Calcula el margen de error para salidas multiplicando el valor t por el error estándar (desviación estándar dividido por raíz de n)
            double marginThroughput = tValue * stdThroughput / Math.sqrt(n); // Calcula el margen de error para throughput usando la misma fórmula
            double marginSystemTime = tValue * stdAverageSystemTime / Math.sqrt(n); // Calcula el margen de error para tiempo en sistema usando la misma fórmula

            ciTotalExits = new double[]{avgTotalExits - marginExits, avgTotalExits + marginExits}; // Crea el intervalo de confianza para salidas restando y sumando el margen de error al promedio [límite inferior, límite superior]
            ciThroughput = new double[]{avgThroughput - marginThroughput, avgThroughput + marginThroughput}; // Crea el intervalo de confianza para throughput restando y sumando el margen de error al promedio
            ciAverageSystemTime = new double[]{avgAverageSystemTime - marginSystemTime, avgAverageSystemTime + marginSystemTime}; // Crea el intervalo de confianza para tiempo en sistema restando y sumando el margen de error al promedio
        } // Cierre del método calculateStatistics

        private double getTValue(int n) { // Método privado que retorna el valor t de Student para un 95% de confianza según el tamaño de muestra recibido como parámetro
            // Aproximación de valores t-student para 95% de confianza
            if (n <= 2) return 12.706; // Retorna 12.706 si el tamaño de muestra es menor o igual a 2
            if (n <= 5) return 2.776; // Retorna 2.776 si el tamaño de muestra es menor o igual a 5
            if (n <= 10) return 2.228; // Retorna 2.228 si el tamaño de muestra es menor o igual a 10
            if (n <= 20) return 2.086; // Retorna 2.086 si el tamaño de muestra es menor o igual a 20
            if (n <= 30) return 2.042; // Retorna 2.042 si el tamaño de muestra es menor o igual a 30
            return 1.96; // Para n > 30, aproximar con distribución normal // Retorna 1.96 para muestras grandes (mayor a 30) usando la aproximación de distribución normal
        } // Cierre del método getTValue

        // Getters
        public double getAvgTotalArrivals() { // Método público getter que retorna el promedio del total de arribos de tipo double
            return avgTotalArrivals; // Retorna el valor de la variable avgTotalArrivals
        } // Cierre del método getAvgTotalArrivals

        public double getAvgTotalExits() { // Método público getter que retorna el promedio del total de salidas de tipo double
            return avgTotalExits; // Retorna el valor de la variable avgTotalExits
        } // Cierre del método getAvgTotalExits

        public double getAvgThroughput() { // Método público getter que retorna el promedio del throughput de tipo double
            return avgThroughput; // Retorna el valor de la variable avgThroughput
        } // Cierre del método getAvgThroughput

        public double getAvgAverageSystemTime() { // Método público getter que retorna el promedio del tiempo promedio en el sistema de tipo double
            return avgAverageSystemTime; // Retorna el valor de la variable avgAverageSystemTime
        } // Cierre del método getAvgAverageSystemTime

        public double getStdTotalExits() { // Método público getter que retorna la desviación estándar del total de salidas de tipo double
            return stdTotalExits; // Retorna el valor de la variable stdTotalExits
        } // Cierre del método getStdTotalExits

        public double getStdThroughput() { // Método público getter que retorna la desviación estándar del throughput de tipo double
            return stdThroughput; // Retorna el valor de la variable stdThroughput
        } // Cierre del método getStdThroughput

        public double getStdAverageSystemTime() { // Método público getter que retorna la desviación estándar del tiempo promedio en el sistema de tipo double
            return stdAverageSystemTime; // Retorna el valor de la variable stdAverageSystemTime
        } // Cierre del método getStdAverageSystemTime

        public double[] getCiTotalExits() { // Método público getter que retorna el intervalo de confianza del total de salidas de tipo array double
            return ciTotalExits; // Retorna el valor de la variable ciTotalExits
        } // Cierre del método getCiTotalExits

        public double[] getCiThroughput() { // Método público getter que retorna el intervalo de confianza del throughput de tipo array double
            return ciThroughput; // Retorna el valor de la variable ciThroughput
        } // Cierre del método getCiThroughput

        public double[] getCiAverageSystemTime() { // Método público getter que retorna el intervalo de confianza del tiempo promedio en el sistema de tipo array double
            return ciAverageSystemTime; // Retorna el valor de la variable ciAverageSystemTime
        } // Cierre del método getCiAverageSystemTime

        public List<Statistics> getAllStatistics() { // Método público getter que retorna la lista completa de estadísticas de todas las réplicas
            return allStats; // Retorna el valor de la variable allStats
        } // Cierre del método getAllStatistics

        @Override // Anotación que indica que este método sobrescribe el método toString de la clase Object
        public String toString() { // Método público que retorna una representación en texto de las estadísticas agregadas de tipo String
            return String.format( // Retorna una cadena formateada usando String.format con múltiples líneas
                "Estadísticas Agregadas (%d réplicas):\n" + // Línea de título con el número de réplicas
                "  Arribos promedio: %.2f\n" + // Línea que muestra el promedio de arribos con 2 decimales
                "  Salidas promedio: %.2f ± %.2f [IC 95%%: %.2f - %.2f]\n" + // Línea que muestra salidas promedio con desviación estándar e intervalo de confianza
                "  Throughput promedio: %.2f ± %.2f [IC 95%%: %.2f - %.2f] piezas/hora\n" + // Línea que muestra throughput promedio con desviación estándar e intervalo de confianza
                "  Tiempo en sistema promedio: %.2f ± %.2f [IC 95%%: %.2f - %.2f] min", // Línea que muestra tiempo en sistema promedio con desviación estándar e intervalo de confianza
                allStats.size(), // Argumento 1: número de réplicas (tamaño de la lista)
                avgTotalArrivals, // Argumento 2: promedio de arribos
                avgTotalExits, stdTotalExits, ciTotalExits[0], ciTotalExits[1], // Argumentos 3-6: promedio, desviación estándar y límites del intervalo de confianza de salidas
                avgThroughput, stdThroughput, ciThroughput[0], ciThroughput[1], // Argumentos 7-10: promedio, desviación estándar y límites del intervalo de confianza de throughput
                avgAverageSystemTime, stdAverageSystemTime, ciAverageSystemTime[0], ciAverageSystemTime[1] // Argumentos 11-14: promedio, desviación estándar y límites del intervalo de confianza de tiempo en sistema
            ); // Cierre del paréntesis de String.format
        } // Cierre del método toString
    } // Cierre de la clase AggregatedStatistics
} // Cierre de la clase ReplicationManager
