package actividad_7.predeterminado;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;

import javax.swing.table.DefaultTableModel;
import java.util.Arrays;

/**
 * Clase responsable de ejecutar las simulaciones y cálculos estadísticos
 * VERSIÓN MODIFICADA: Usa valores predeterminados para Rn en lugar de números aleatorios
 */
public class MotorSimulacion { // Declaración de la clase MotorSimulacion

    // Valores predeterminados de Rn para usar en lugar de números aleatorios
    private static final double[] VALORES_RN_PREDETERMINADOS = {
        0.1628, 0.9778, 0.9808, 0.3117, 0.1491, 0.7647, 0.3877, 0.1762, 0.9117, 0.5975,
        0.0169, 0.7455, 0.5636, 0.7469, 0.9945, 0.0037, 0.5312, 0.6681, 0.1098, 0.1189,
        0.2845, 0.0149, 0.5418, 0.6632, 0.0363, 0.6550, 0.6569, 0.6313, 0.1508, 0.4003,
        0.6104, 0.6598, 0.7033, 0.4693, 0.8578, 0.0930, 0.1657, 0.5059, 0.5617, 0.4291,
        0.4889, 0.3920, 0.8458, 0.4780, 0.2202, 0.1921, 0.2753, 0.3650, 0.6969, 0.0907,
        0.7260, 0.7319, 0.7055, 0.1942, 0.7140, 0.7803, 0.0311, 0.2674, 0.5437, 0.6672,
        0.9180, 0.9786, 0.6886, 0.7016, 0.8006, 0.3842, 0.0871, 0.8077, 0.0670, 0.4536,
        0.0264, 0.8178, 0.6329, 0.0782, 0.3292, 0.2091, 0.9383, 0.4623, 0.9931, 0.2036,
        0.1027, 0.7855, 0.8902, 0.9108, 0.6375, 0.6660, 0.6567, 0.3203, 0.7503, 0.6693,
        0.1329, 0.0623, 0.7044, 0.7040, 0.7619, 0.2452, 0.1996, 0.1967, 0.3574, 0.9069,
        0.6108, 0.3784, 0.9607, 0.2996, 0.4512, 0.9315, 0.9288, 0.0944, 0.5862, 0.0214,
        0.3139, 0.6093, 0.2343, 0.5644, 0.9701, 0.8387, 0.7711, 0.2368, 0.5784, 0.3144,
        0.9149, 0.0314, 0.0841, 0.8449, 0.5297, 0.4349, 0.8134, 0.7551, 0.8627, 0.8865,
        0.6910, 0.0985, 0.4680, 0.8738, 0.0972, 0.2819, 0.0006, 0.4159, 0.7757, 0.4284,
        0.7882, 0.2020, 0.8854, 0.9264, 0.1881, 0.4926, 0.2612, 0.3265, 0.0385, 0.9961,
        0.6566, 0.8860, 0.0740, 0.8569, 0.5562, 0.8467, 0.1840, 0.6218, 0.3777, 0.5964,
        0.9382, 0.9378, 0.4427, 0.0542, 0.5702, 0.1417, 0.3969, 0.3612, 0.5143, 0.2003,
        0.9665, 0.3510, 0.8885, 0.7235, 0.3352, 0.3350, 0.4036, 0.2568, 0.1238, 0.6145,
        0.5330, 0.5890, 0.5687, 0.0937, 0.2636, 0.3428, 0.0946, 0.1678, 0.7715, 0.5071,
        0.9651, 0.5960, 0.1738, 0.5484, 0.0583, 0.8040, 0.0803, 0.2020, 0.0901, 0.1747,
        0.2846, 0.1999, 0.0747, 0.0541, 0.5142, 0.6155, 0.4585, 0.7558, 0.2666, 0.4703,
        0.5880, 0.7880, 0.2540, 0.9500, 0.0225, 0.3449, 0.2648, 0.4139, 0.0049, 0.8287,
        0.1098, 0.2915, 0.7517, 0.9130, 0.8235, 0.4071, 0.4879, 0.7387, 0.8592, 0.1306,
        0.3138, 0.5710, 0.9282, 0.9171, 0.8254, 0.3492, 0.6249, 0.3296, 0.4313, 0.1613,
        0.8798, 0.7297, 0.0126, 0.3520, 0.5360, 0.1620, 0.8373, 0.1201, 0.6078, 0.1984,
        0.7816, 0.9560, 0.2216, 0.5660, 0.9985, 0.2656, 0.3817, 0.9587, 0.1282, 0.6779,
        0.1939, 0.6495, 0.3822, 0.6285, 0.1281, 0.3822, 0.0811, 0.0313, 0.6832, 0.4958,
        0.3480, 0.7750, 0.1225, 0.9852, 0.9603, 0.6660, 0.7092, 0.5756, 0.6661, 0.6226,
        0.6417, 0.9131, 0.2706, 0.7778, 0.2403, 0.6886, 0.2475, 0.4468, 0.1467, 0.0409,
        0.9365, 0.0517, 0.8980, 0.7705, 0.8339, 0.7891, 0.7454, 0.1011, 0.5350, 0.0257,
        0.9371, 0.6629, 0.4066, 0.5769, 0.0115, 0.8794, 0.7308, 0.6329, 0.8310, 0.8516,
        0.8049, 0.1990, 0.3883, 0.1172, 0.3434, 0.2076, 0.5586, 0.1180, 0.9518, 0.4895,
        0.4454, 0.5012, 0.2099, 0.3971, 0.3511, 0.1370, 0.7855, 0.3306, 0.6207, 0.4304,
        0.3260, 0.7306, 0.0090, 0.8987, 0.6650, 0.6672, 0.8990, 0.2745, 0.1442, 0.3120,
        0.6528, 0.8088, 0.0143, 0.0822, 0.5085, 0.8759, 0.1854, 0.7976, 0.0921, 0.6305,
        0.8927, 0.9338, 0.7851, 0.0845, 0.1794, 0.9979, 0.0300, 0.4437, 0.2609, 0.9055,
        0.5131, 0.4990, 0.3980, 0.2247, 0.7077
    };

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
     * MODIFICADO: Usa valores predeterminados para la primera pantalla (365 días)
     */
    public double[] generarSimulacionYllenarTabla(int dias, DefaultTableModel modeloTabla) {
        modeloTabla.setRowCount(0); // Limpiar tabla existente
        int inventarioFinal = 0; // Inventario inicial es cero al comenzar
        NormalDistribution dist = new NormalDistribution(Constantes.MEDIA_DEMANDA, Constantes.DESVIACION_DEMANDA); // Distribución normal para la demanda
        double[] costosTotales = new double[dias]; // Arreglo para los costos totales

        // Determinar si usar valores predeterminados (primera pantalla = 365 días)
        boolean usarValoresPredeterminados = (dias == 365); // Si los días son 365, usar valores predeterminados

        // Simular cada día individualmente
        for (int dia = 1; dia <= dias; dia++) { // Para cada día
            double rn; // Valor aleatorio o predeterminado

            if (usarValoresPredeterminados && (dia - 1) < VALORES_RN_PREDETERMINADOS.length) { // Si se usan valores predeterminados
                rn = VALORES_RN_PREDETERMINADOS[dia - 1]; // Usar valor predeterminado
            } else {
                rn = Math.random(); // Usar valor aleatorio
            }

            // Simular un día y obtener todos los resultados
            ModelosDeDatos.ResultadoSimulacion resultado = simularDia(inventarioFinal, dist, rn); // Simula el día
            costosTotales[dia - 1] = resultado.costoTotal; // Guardar costo total (índice base 0)
            inventarioFinal = resultado.inventarioFinal; // El inventario final se convierte en inicial del siguiente día

            // Crear fila para mostrar en la tabla
            Object[] fila = {
                dia, // Día
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
        return costosTotales; // Devuelve el arreglo de costos
    }

    /**
     * Simula las operaciones de un día específico
     * MODIFICADO: Recibe el valor Rn como parámetro en lugar de generarlo aleatoriamente
     */
    public ModelosDeDatos.ResultadoSimulacion simularDia(int inventarioFinalAnterior, NormalDistribution dist, double rn) {
        ModelosDeDatos.ResultadoSimulacion resultado = new ModelosDeDatos.ResultadoSimulacion(); // Crea el objeto resultado

        // Valores iniciales del día
        resultado.inventarioInicial = inventarioFinalAnterior; // Inventario inicial
        resultado.totalDisponible = resultado.inventarioInicial + Constantes.POLITICA_PRODUCCION; // Total disponible

        // Usar el valor Rn proporcionado
        resultado.rn = rn; // Valor Rn
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

        return stats; // Devuelve las estadísticas
    }

    /**
     * Ejecuta cálculos estadísticos y pruebas de normalidad sobre los datos
     */
    public void calcularEstadisticasYPruebas(double[] costosTotales) {
        // Calcular estadísticas descriptivas básicas
        ModelosDeDatos.EstadisticasSimulacion stats = calcularEstadisticas(costosTotales); // Calcula estadísticas
        promedio = stats.promedio; // Asigna el promedio
        desviacion = stats.desviacion; // Asigna la desviación

        // Prueba de normalidad Kolmogorov-Smirnov
        pValue = new KolmogorovSmirnovTest().kolmogorovSmirnovTest(
            new NormalDistribution(promedio, desviacion), costosTotales, false); // Calcula el valor p

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
            .sum(); // Suma de cuadrados
        double desviacionReplicas = Math.sqrt(sumaCuadrados / (r - 1)); // Desviación estándar

        // Fórmula NO NORMAL: IC = x̄ ± s/√(rα/2)
        double denominador = Math.sqrt(r * alfa / 2); // Denominador
        double margenError = desviacionReplicas / denominador; // Margen de error

        double intervaloInferior = promedioReplicas - margenError; // Límite inferior
        double intervaloSuperior = promedioReplicas + margenError; // Límite superior

        return new double[]{intervaloInferior, intervaloSuperior}; // Devuelve el intervalo
    }

    /**
     * Simula una réplica completa de forma independiente
     * NOTA: Las réplicas usan números aleatorios para mantener independencia
     */
    public double[] simularReplicaCompleta(int tamanoRecomendado) {
        double[] costos = new double[tamanoRecomendado]; // Arreglo de costos
        int inventarioFinal = 0; // Inventario inicial
        NormalDistribution dist = new NormalDistribution(Constantes.MEDIA_DEMANDA, Constantes.DESVIACION_DEMANDA); // Distribución normal

        for (int dia = 0; dia < tamanoRecomendado; dia++) { // Para cada día
            // Para réplicas, usar números aleatorios para mantener independencia
            double rn = Math.random(); // Valor aleatorio
            ModelosDeDatos.ResultadoSimulacion resultado = simularDia(inventarioFinal, dist, rn); // Simula el día
            costos[dia] = resultado.costoTotal; // Guarda el costo
            inventarioFinal = resultado.inventarioFinal; // Actualiza el inventario
        }

        return costos; // Devuelve el arreglo de costos
    }
}