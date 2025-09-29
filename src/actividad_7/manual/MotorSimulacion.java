package actividad_7.manual;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;

import javax.swing.table.DefaultTableModel;
import java.util.Arrays;
import java.util.Random;

/**
 * Clase responsable de ejecutar las simulaciones y cálculos estadísticos
 */
public class MotorSimulacion {

    // Variables para resultados de análisis
    private double promedio; // Promedio de los costos
    private double desviacion; // Desviación estándar de los costos
    private int tamanoRecomendado; // Tamaño recomendado de la muestra
    private boolean esNormal; // Indica si la distribución es normal
    private double pValue; // Valor p de la prueba de normalidad
    private double valorAd; // Valor de Anderson-Darling (simulado)

    // ========================== GETTERS ==========================
    public double getPromedio() { return promedio; } // Devuelve el promedio
    public double getDesviacion() { return desviacion; } // Devuelve la desviación
    public int getTamanoRecomendado() { return tamanoRecomendado; } // Devuelve el tamaño recomendado
    public boolean isEsNormal() { return esNormal; } // Devuelve si es normal
    public double getPValue() { return pValue; } // Devuelve el valor p
    public double getValorAd() { return valorAd; } // Devuelve el valor Ad

    /**
     * Ejecuta la simulación completa y llena la tabla con los resultados
     * MODIFICADO: Ahora recibe un array de valores Rn manuales
     */
    public double[] generarSimulacionYllenarTabla(int dias, DefaultTableModel modeloTabla, double[] valoresRn) {
        modeloTabla.setRowCount(0); // Limpiar tabla existente
        int inventarioFinal = 0; // Inventario inicial es cero al comenzar
        NormalDistribution dist = new NormalDistribution(Constantes.MEDIA_DEMANDA, Constantes.DESVIACION_DEMANDA); // Distribución normal para la demanda
        double[] costosTotales = new double[dias]; // Arreglo para los costos totales

        // Simular cada día individualmente usando los valores Rn proporcionados
        for (int dia = 1; dia <= dias; dia++) {
            double rnManual = valoresRn[dia - 1]; // Usar valor Rn manual
            // Simular un día y obtener todos los resultados
            ModelosDeDatos.ResultadoSimulacion resultado = simularDiaConRnManual(inventarioFinal, dist, rnManual); // Simula el día
            costosTotales[dia - 1] = resultado.costoTotal; // Guardar costo total (índice base 0)
            inventarioFinal = resultado.inventarioFinal; // El inventario final se convierte en inicial del siguiente día

            // Crear fila para mostrar en la tabla
            Object[] fila = {
                dia, // Día actual
                resultado.inventarioInicial, // Inventario inicial
                Constantes.POLITICA_PRODUCCION, // Política de producción
                resultado.totalDisponible, // Total disponible
                String.format("%.4f", resultado.rn), // Valor Rn
                resultado.demanda, // Demanda
                resultado.ventas, // Ventas
                resultado.ventasPerdidas, // Ventas perdidas
                resultado.inventarioFinal, // Inventario final
                resultado.costoFaltante, // Costo por faltante
                resultado.costoInventario, // Costo de inventario
                resultado.costoTotal // Costo total
            };
            modeloTabla.addRow(fila); // Agrega la fila a la tabla
        }
        return costosTotales; // Devuelve los costos totales
    }

    /**
     * Versión original del método para mantener compatibilidad con réplicas (usa valores aleatorios)
     */
    public double[] generarSimulacionYllenarTabla(int dias, DefaultTableModel modeloTabla) {
        // Generar valores Rn aleatorios para mantener compatibilidad
        Random random = new Random(); // Generador de números aleatorios
        double[] valoresRnAleatorios = new double[dias]; // Arreglo para los valores aleatorios
        for (int i = 0; i < dias; i++) {
            valoresRnAleatorios[i] = random.nextDouble(); // Genera un valor aleatorio entre 0 y 1
        }
        return generarSimulacionYllenarTabla(dias, modeloTabla, valoresRnAleatorios); // Llama al método principal
    }

    /**
     * Simula las operaciones de un día específico usando un valor Rn manual
     * NUEVO MÉTODO: Simula un día con valor Rn proporcionado manualmente
     */
    public ModelosDeDatos.ResultadoSimulacion simularDiaConRnManual(int inventarioFinalAnterior,
            NormalDistribution dist, double rnManual) {
        ModelosDeDatos.ResultadoSimulacion resultado = new ModelosDeDatos.ResultadoSimulacion(); // Objeto para resultados

        // Valores iniciales del día
        resultado.inventarioInicial = inventarioFinalAnterior; // Inventario inicial
        resultado.totalDisponible = resultado.inventarioInicial + Constantes.POLITICA_PRODUCCION; // Total disponible

        // Usar el valor Rn manual proporcionado
        resultado.rn = rnManual; // Valor Rn
        resultado.demanda = (int) Math.round(dist.inverseCumulativeProbability(resultado.rn)); // Demanda generada

        // Cálculos de ventas y faltantes
        resultado.ventas = Math.min(resultado.demanda, resultado.totalDisponible); // Ventas realizadas
        resultado.ventasPerdidas = Math.max(0, resultado.demanda - resultado.ventas); // Ventas perdidas
        resultado.inventarioFinal = resultado.totalDisponible - resultado.ventas; // Inventario final

        // Cálculos de costos
        resultado.costoFaltante = resultado.ventasPerdidas * Constantes.COSTO_FALTANTE_UNITARIO; // Costo por faltante
        resultado.costoInventario = resultado.inventarioFinal * Constantes.COSTO_INVENTARIO_UNITARIO; // Costo de inventario
        resultado.costoTotal = resultado.costoFaltante + resultado.costoInventario; // Costo total

        return resultado; // Devuelve el resultado
    }

    /**
     * Simula las operaciones de un día específico (versión original con Random)
     */
    public ModelosDeDatos.ResultadoSimulacion simularDia(int inventarioFinalAnterior, NormalDistribution dist, Random random) {
        ModelosDeDatos.ResultadoSimulacion resultado = new ModelosDeDatos.ResultadoSimulacion(); // Objeto para resultados

        // Valores iniciales del día
        resultado.inventarioInicial = inventarioFinalAnterior; // Inventario inicial
        resultado.totalDisponible = resultado.inventarioInicial + Constantes.POLITICA_PRODUCCION; // Total disponible

        // Generación de demanda aleatoria
        resultado.rn = random.nextDouble(); // Valor aleatorio Rn
        resultado.demanda = (int) Math.round(dist.inverseCumulativeProbability(resultado.rn)); // Demanda generada

        // Cálculos de ventas y faltantes
        resultado.ventas = Math.min(resultado.demanda, resultado.totalDisponible); // Ventas realizadas
        resultado.ventasPerdidas = Math.max(0, resultado.demanda - resultado.ventas); // Ventas perdidas
        resultado.inventarioFinal = resultado.totalDisponible - resultado.ventas; // Inventario final

        // Cálculos de costos
        resultado.costoFaltante = resultado.ventasPerdidas * Constantes.COSTO_FALTANTE_UNITARIO; // Costo por faltante
        resultado.costoInventario = resultado.inventarioFinal * Constantes.COSTO_INVENTARIO_UNITARIO; // Costo de inventario
        resultado.costoTotal = resultado.costoFaltante + resultado.costoInventario; // Costo total

        return resultado; // Devuelve el resultado
    }

    /**
     * Calcula estadísticas descriptivas de un array de datos
     */
    public ModelosDeDatos.EstadisticasSimulacion calcularEstadisticas(double[] costosTotales) {
        ModelosDeDatos.EstadisticasSimulacion stats = new ModelosDeDatos.EstadisticasSimulacion(); // Objeto para estadísticas

        // Cálculos usando streams de Java 8 para eficiencia
        stats.suma = Arrays.stream(costosTotales).sum(); // Suma total
        stats.sumaCuadrados = Arrays.stream(costosTotales).map(x -> x * x).sum(); // Suma de cuadrados
        stats.promedio = stats.suma / costosTotales.length; // Promedio

        // Varianza usando fórmula: E[X²] - (E[X])²
        stats.varianza = (stats.sumaCuadrados / costosTotales.length) - (stats.promedio * stats.promedio); // Varianza
        stats.desviacion = Math.sqrt(stats.varianza); // Desviación estándar

        // Valores extremos
        stats.minimo = Arrays.stream(costosTotales).min().orElse(Double.NaN); // Valor mínimo
        stats.maximo = Arrays.stream(costosTotales).max().orElse(Double.NaN); // Valor máximo

        return stats; // Devuelve las estadísticas
    }

    /**
     * Ejecuta cálculos estadísticos y pruebas de normalidad sobre los datos
     */
    public void calcularEstadisticasYPruebas(double[] costosTotales) {
        // Calcular estadísticas descriptivas básicas
        ModelosDeDatos.EstadisticasSimulacion stats = calcularEstadisticas(costosTotales); // Estadísticas básicas
        promedio = stats.promedio; // Asigna el promedio
        desviacion = stats.desviacion; // Asigna la desviación

        // Prueba de normalidad Kolmogorov-Smirnov
        pValue = new KolmogorovSmirnovTest().kolmogorovSmirnovTest(
            new NormalDistribution(promedio, desviacion), costosTotales, false); // Valor p

        // Determinar si los datos siguen distribución normal (α = 0.05)
        esNormal = pValue > 0.05; // Es normal si p > 0.05

        if (esNormal) {
            // CASO NORMAL: Usar fórmula existente
            tamanoRecomendado = (int) Math.ceil(Math.pow((desviacion / Constantes.ERROR_PERMITIDO) * Constantes.VALOR_T, 2)); // Tamaño recomendado
        } else {
            // CASO NO NORMAL: Usar fórmula n = (1/α × s/ε)²
            double alfa = 0.05; // Nivel de significancia
            tamanoRecomendado = (int) Math.ceil(Math.pow((1.0/alfa) * (desviacion / Constantes.ERROR_PERMITIDO), 2)); // Tamaño recomendado
        }

        valorAd = pValue; // Mantener compatibilidad (simulado)
    }

    /**
     * Calcula intervalos de confianza para réplicas (casos NO normales)
     */
    public double[] calcularIntervalosConfianzaNoNormal(double[] promediosReplicas) {
        int r = promediosReplicas.length; // Número de réplicas = 5
        double alfa = 0.05; // Nivel de significancia

        // Calcular promedio de las réplicas
        double sumaReplicas = Arrays.stream(promediosReplicas).sum(); // Suma de promedios
        double promedioReplicas = sumaReplicas / r; // Promedio de réplicas

        // Calcular desviación estándar de las réplicas
        double sumaCuadrados = Arrays.stream(promediosReplicas)
            .map(x -> Math.pow(x - promedioReplicas, 2))
            .sum(); // Suma de cuadrados de desviación
        double desviacionReplicas = Math.sqrt(sumaCuadrados / (r - 1)); // Desviación estándar

        // Fórmula NO NORMAL: IC = x̄ ± s/√(rα/2)
        double denominador = Math.sqrt(r * alfa / 2); // Denominador de la fórmula
        double margenError = desviacionReplicas / denominador; // Margen de error

        double intervaloInferior = promedioReplicas - margenError; // Límite inferior
        double intervaloSuperior = promedioReplicas + margenError; // Límite superior

        return new double[]{intervaloInferior, intervaloSuperior}; // Devuelve los intervalos
    }

    /**
     * Simula una réplica completa de forma independiente
     */
    public double[] simularReplicaCompleta(int tamanoRecomendado) {
        double[] costos = new double[tamanoRecomendado]; // Arreglo para los costos
        int inventarioFinal = 0; // Inventario inicial
        NormalDistribution dist = new NormalDistribution(Constantes.MEDIA_DEMANDA, Constantes.DESVIACION_DEMANDA); // Distribución normal
        Random random = new Random(); // Generador de números aleatorios

        for (int dia = 0; dia < tamanoRecomendado; dia++) { // Para cada día
            ModelosDeDatos.ResultadoSimulacion resultado = simularDia(inventarioFinal, dist, random); // Simula el día
            costos[dia] = resultado.costoTotal; // Guarda el costo total
            inventarioFinal = resultado.inventarioFinal; // Actualiza el inventario final
        }

        return costos; // Devuelve los costos
    }
}