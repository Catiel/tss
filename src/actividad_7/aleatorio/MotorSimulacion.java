package actividad_7.aleatorio; // Declaración del paquete donde se encuentra la clase

import org.apache.commons.math3.distribution.NormalDistribution; // Importa la clase para distribuciones normales
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest; // Importa la clase para la prueba de Kolmogorov-Smirnov
import javax.swing.table.DefaultTableModel; // Importa DefaultTableModel para manipular datos de tablas
import java.util.Arrays; // Importa utilidades para arreglos
import java.util.Random; // Importa la clase Random para generación de números aleatorios

/**
 * Clase responsable de ejecutar las simulaciones y cálculos estadísticos
 */
public class MotorSimulacion { // Declaración de la clase MotorSimulacion

    // Variables para resultados de análisis
    private double promedio; // Promedio de los costos
    private double desviacion; // Desviación estándar de los costos
    private int tamanoRecomendado; // Tamaño recomendado de muestra
    private boolean esNormal; // Indica si la distribución es normal
    private double pValue; // Valor p de la prueba de normalidad
    private double valorAd; // Valor AD (simulado, para compatibilidad)

    // ========================== GETTERS ==========================
    public double getPromedio() { return promedio; } // Getter para promedio
    public double getDesviacion() { return desviacion; } // Getter para desviación
    public int getTamanoRecomendado() { return tamanoRecomendado; } // Getter para tamaño recomendado
    public boolean isEsNormal() { return esNormal; } // Getter para normalidad
    public double getPValue() { return pValue; } // Getter para valor p
    public double getValorAd() { return valorAd; } // Getter para valor AD

    /**
     * Ejecuta la simulación completa y llena la tabla con los resultados
     */
    public double[] generarSimulacionYllenarTabla(int dias, DefaultTableModel modeloTabla) { // Método para simular y llenar la tabla
        modeloTabla.setRowCount(0); // Limpiar tabla existente
        int inventarioFinal = 0; // Inventario inicial es cero al comenzar
        NormalDistribution dist = new NormalDistribution(Constantes.MEDIA_DEMANDA, Constantes.DESVIACION_DEMANDA); // Distribución normal para demanda
        Random random = new Random(); // Generador de números aleatorios
        double[] costosTotales = new double[dias]; // Arreglo para almacenar los costos totales

        // Simular cada día individualmente
        for (int dia = 1; dia <= dias; dia++) { // Recorre los días
            // Simular un día y obtener todos los resultados
            ModelosDeDatos.ResultadoSimulacion resultado = simularDia(inventarioFinal, dist, random); // Simula el día
            costosTotales[dia - 1] = resultado.costoTotal; // Guardar costo total (índice base 0)
            inventarioFinal = resultado.inventarioFinal; // El inventario final se convierte en inicial del siguiente día

            // Crear fila para mostrar en la tabla
            Object[] fila = {
                dia, // Día
                resultado.inventarioInicial, // Inventario inicial
                Constantes.POLITICA_PRODUCCION, // Política de producción
                resultado.totalDisponible, // Total disponible
                String.format("%.4f", resultado.rn), // Número aleatorio
                resultado.demanda, // Demanda
                resultado.ventas, // Ventas
                resultado.ventasPerdidas, // Ventas perdidas
                resultado.inventarioFinal, // Inventario final
                resultado.costoFaltante, // Costo faltante
                resultado.costoInventario, // Costo inventario
                resultado.costoTotal // Costo total
            };
            modeloTabla.addRow(fila); // Añade la fila a la tabla
        }
        return costosTotales; // Devuelve el arreglo de costos totales
    }

    /**
     * Simula las operaciones de un día específico
     */
    public ModelosDeDatos.ResultadoSimulacion simularDia(int inventarioFinalAnterior, NormalDistribution dist, Random random) { // Método para simular un día
        ModelosDeDatos.ResultadoSimulacion resultado = new ModelosDeDatos.ResultadoSimulacion(); // Crea el objeto resultado

        // Valores iniciales del día
        resultado.inventarioInicial = inventarioFinalAnterior; // Inventario inicial
        resultado.totalDisponible = resultado.inventarioInicial + Constantes.POLITICA_PRODUCCION; // Total disponible

        // Generación de demanda aleatoria
        resultado.rn = random.nextDouble(); // Número aleatorio
        resultado.demanda = (int) Math.round(dist.inverseCumulativeProbability(resultado.rn)); // Demanda generada

        // Cálculos de ventas y faltantes
        resultado.ventas = Math.min(resultado.demanda, resultado.totalDisponible); // Ventas efectivas
        resultado.ventasPerdidas = Math.max(0, resultado.demanda - resultado.ventas); // Ventas perdidas
        resultado.inventarioFinal = resultado.totalDisponible - resultado.ventas; // Inventario final

        // Cálculos de costos
        resultado.costoFaltante = resultado.ventasPerdidas * Constantes.COSTO_FALTANTE_UNITARIO; // Costo por faltante
        resultado.costoInventario = resultado.inventarioFinal * Constantes.COSTO_INVENTARIO_UNITARIO; // Costo de inventario
        resultado.costoTotal = resultado.costoFaltante + resultado.costoInventario; // Costo total

        return resultado; // Devuelve el resultado del día
    }

    /**
     * Calcula estadísticas descriptivas de un array de datos
     */
    public ModelosDeDatos.EstadisticasSimulacion calcularEstadisticas(double[] costosTotales) { // Método para calcular estadísticas descriptivas
        ModelosDeDatos.EstadisticasSimulacion stats = new ModelosDeDatos.EstadisticasSimulacion(); // Crea el objeto de estadísticas

        // Cálculos usando streams de Java 8 para eficiencia
        stats.suma = Arrays.stream(costosTotales).sum(); // Suma total
        stats.sumaCuadrados = Arrays.stream(costosTotales).map(x -> x * x).sum(); // Suma de cuadrados
        stats.promedio = stats.suma / costosTotales.length; // Promedio

        // Varianza usando fórmula: E[X²] - (E[X])²
        stats.varianza = (stats.sumaCuadrados / costosTotales.length) - (stats.promedio * stats.promedio); // Varianza
        stats.desviacion = Math.sqrt(stats.varianza); // Desviación estándar

        // Valores extremos
        stats.minimo = Arrays.stream(costosTotales).min().orElse(Double.NaN); // Mínimo
        stats.maximo = Arrays.stream(costosTotales).max().orElse(Double.NaN); // Máximo

        return stats; // Devuelve el objeto de estadísticas
    }

    /**
     * Ejecuta cálculos estadísticos y pruebas de normalidad sobre los datos
     */
    public void calcularEstadisticasYPruebas(double[] costosTotales) { // Método para calcular estadísticas y pruebas de normalidad
        // Calcular estadísticas descriptivas básicas
        ModelosDeDatos.EstadisticasSimulacion stats = calcularEstadisticas(costosTotales); // Calcula estadísticas
        promedio = stats.promedio; // Asigna promedio
        desviacion = stats.desviacion; // Asigna desviación

        // Prueba de normalidad Kolmogorov-Smirnov
        pValue = new KolmogorovSmirnovTest().kolmogorovSmirnovTest(
            new NormalDistribution(promedio, desviacion), costosTotales, false); // Calcula el valor p

        // Determinar si los datos siguen distribución normal (α = 0.05)
        esNormal = pValue > 0.05; // Es normal si p > 0.05

        if (esNormal) {
            // CASO NORMAL: Usar fórmula existente
            tamanoRecomendado = (int) Math.ceil(Math.pow((desviacion / Constantes.ERROR_PERMITIDO) * Constantes.VALOR_T, 2)); // Tamaño recomendado normal
        } else {
            // CASO NO NORMAL: Usar fórmula n = (1/α × s/ε)²
            double alfa = 0.05; // Nivel de significancia
            tamanoRecomendado = (int) Math.ceil(Math.pow((1.0/alfa) * (desviacion / Constantes.ERROR_PERMITIDO), 2)); // Tamaño recomendado no normal
        }

        valorAd = pValue; // Mantener compatibilidad (simulado)
    }

    /**
     * Calcula intervalos de confianza para réplicas (casos NO normales)
     */
    public double[] calcularIntervalosConfianzaNoNormal(double[] promediosReplicas) { // Método para calcular intervalos de confianza no normales
        int r = promediosReplicas.length; // Número de réplicas = 5
        double alfa = 0.05; // Nivel de significancia

        // Calcular promedio de las réplicas
        double sumaReplicas = Arrays.stream(promediosReplicas).sum(); // Suma de promedios
        double promedioReplicas = sumaReplicas / r; // Promedio de las réplicas

        // Calcular desviación estándar de las réplicas
        double sumaCuadrados = Arrays.stream(promediosReplicas)
            .map(x -> Math.pow(x - promedioReplicas, 2))
            .sum(); // Suma de cuadrados de las diferencias
        double desviacionReplicas = Math.sqrt(sumaCuadrados / (r - 1)); // Desviación estándar

        // Fórmula NO NORMAL: IC = x̄ ± s/√(rα/2)
        double denominador = Math.sqrt(r * alfa / 2); // Denominador de la fórmula
        double margenError = desviacionReplicas / denominador; // Margen de error

        double intervaloInferior = promedioReplicas - margenError; // Intervalo inferior
        double intervaloSuperior = promedioReplicas + margenError; // Intervalo superior

        return new double[]{intervaloInferior, intervaloSuperior}; // Devuelve los intervalos
    }

    /**
     * Simula una réplica completa de forma independiente
     */
    public double[] simularReplicaCompleta(int tamanoRecomendado) { // Método para simular una réplica completa
        double[] costos = new double[tamanoRecomendado]; // Arreglo para los costos
        int inventarioFinal = 0; // Inventario inicial
        NormalDistribution dist = new NormalDistribution(Constantes.MEDIA_DEMANDA, Constantes.DESVIACION_DEMANDA); // Distribución normal
        Random random = new Random(); // Generador de números aleatorios

        for (int dia = 0; dia < tamanoRecomendado; dia++) { // Recorre los días
            ModelosDeDatos.ResultadoSimulacion resultado = simularDia(inventarioFinal, dist, random); // Simula el día
            costos[dia] = resultado.costoTotal; // Guarda el costo total
            inventarioFinal = resultado.inventarioFinal; // Actualiza el inventario final
        }

        return costos; // Devuelve el arreglo de costos
    }
}