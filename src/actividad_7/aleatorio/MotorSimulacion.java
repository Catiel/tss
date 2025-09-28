package actividad_7.aleatorio;

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
    private double promedio;
    private double desviacion;
    private int tamanoRecomendado;
    private boolean esNormal;
    private double pValue;
    private double valorAd;

    // ========================== GETTERS ==========================
    public double getPromedio() { return promedio; }
    public double getDesviacion() { return desviacion; }
    public int getTamanoRecomendado() { return tamanoRecomendado; }
    public boolean isEsNormal() { return esNormal; }
    public double getPValue() { return pValue; }
    public double getValorAd() { return valorAd; }

    /**
     * Ejecuta la simulación completa y llena la tabla con los resultados
     */
    public double[] generarSimulacionYllenarTabla(int dias, DefaultTableModel modeloTabla) {
        modeloTabla.setRowCount(0); // Limpiar tabla existente
        int inventarioFinal = 0; // Inventario inicial es cero al comenzar
        NormalDistribution dist = new NormalDistribution(Constantes.MEDIA_DEMANDA, Constantes.DESVIACION_DEMANDA);
        Random random = new Random();
        double[] costosTotales = new double[dias];

        // Simular cada día individualmente
        for (int dia = 1; dia <= dias; dia++) {
            // Simular un día y obtener todos los resultados
            ModelosDeDatos.ResultadoSimulacion resultado = simularDia(inventarioFinal, dist, random);
            costosTotales[dia - 1] = resultado.costoTotal; // Guardar costo total (índice base 0)
            inventarioFinal = resultado.inventarioFinal; // El inventario final se convierte en inicial del siguiente día

            // Crear fila para mostrar en la tabla
            Object[] fila = {
                dia,
                resultado.inventarioInicial,
                Constantes.POLITICA_PRODUCCION,
                resultado.totalDisponible,
                String.format("%.4f", resultado.rn),
                resultado.demanda,
                resultado.ventas,
                resultado.ventasPerdidas,
                resultado.inventarioFinal,
                resultado.costoFaltante,
                resultado.costoInventario,
                resultado.costoTotal
            };
            modeloTabla.addRow(fila);
        }
        return costosTotales;
    }

    /**
     * Simula las operaciones de un día específico
     */
    public ModelosDeDatos.ResultadoSimulacion simularDia(int inventarioFinalAnterior, NormalDistribution dist, Random random) {
        ModelosDeDatos.ResultadoSimulacion resultado = new ModelosDeDatos.ResultadoSimulacion();

        // Valores iniciales del día
        resultado.inventarioInicial = inventarioFinalAnterior;
        resultado.totalDisponible = resultado.inventarioInicial + Constantes.POLITICA_PRODUCCION;

        // Generación de demanda aleatoria
        resultado.rn = random.nextDouble();
        resultado.demanda = (int) Math.round(dist.inverseCumulativeProbability(resultado.rn));

        // Cálculos de ventas y faltantes
        resultado.ventas = Math.min(resultado.demanda, resultado.totalDisponible);
        resultado.ventasPerdidas = Math.max(0, resultado.demanda - resultado.ventas);
        resultado.inventarioFinal = resultado.totalDisponible - resultado.ventas;

        // Cálculos de costos
        resultado.costoFaltante = resultado.ventasPerdidas * Constantes.COSTO_FALTANTE_UNITARIO;
        resultado.costoInventario = resultado.inventarioFinal * Constantes.COSTO_INVENTARIO_UNITARIO;
        resultado.costoTotal = resultado.costoFaltante + resultado.costoInventario;

        return resultado;
    }

    /**
     * Calcula estadísticas descriptivas de un array de datos
     */
    public ModelosDeDatos.EstadisticasSimulacion calcularEstadisticas(double[] costosTotales) {
        ModelosDeDatos.EstadisticasSimulacion stats = new ModelosDeDatos.EstadisticasSimulacion();

        // Cálculos usando streams de Java 8 para eficiencia
        stats.suma = Arrays.stream(costosTotales).sum();
        stats.sumaCuadrados = Arrays.stream(costosTotales).map(x -> x * x).sum();
        stats.promedio = stats.suma / costosTotales.length;

        // Varianza usando fórmula: E[X²] - (E[X])²
        stats.varianza = (stats.sumaCuadrados / costosTotales.length) - (stats.promedio * stats.promedio);
        stats.desviacion = Math.sqrt(stats.varianza);

        // Valores extremos
        stats.minimo = Arrays.stream(costosTotales).min().orElse(Double.NaN);
        stats.maximo = Arrays.stream(costosTotales).max().orElse(Double.NaN);

        return stats;
    }

    /**
     * Ejecuta cálculos estadísticos y pruebas de normalidad sobre los datos
     */
    public void calcularEstadisticasYPruebas(double[] costosTotales) {
        // Calcular estadísticas descriptivas básicas
        ModelosDeDatos.EstadisticasSimulacion stats = calcularEstadisticas(costosTotales);
        promedio = stats.promedio;
        desviacion = stats.desviacion;

        // Prueba de normalidad Kolmogorov-Smirnov
        pValue = new KolmogorovSmirnovTest().kolmogorovSmirnovTest(
            new NormalDistribution(promedio, desviacion), costosTotales, false);

        // Determinar si los datos siguen distribución normal (α = 0.05)
        esNormal = pValue > 0.05;

        // Calcular tamaño de muestra recomendado usando fórmula estadística
        tamanoRecomendado = esNormal ? (int) Math.ceil(Math.pow((desviacion / Constantes.ERROR_PERMITIDO) * Constantes.VALOR_T, 2)) : -1;

        valorAd = pValue; // Mantener compatibilidad (simulado)
    }

    /**
     * Simula una réplica completa de forma independiente
     */
    public double[] simularReplicaCompleta(int tamanoRecomendado) {
        double[] costos = new double[tamanoRecomendado];
        int inventarioFinal = 0;
        NormalDistribution dist = new NormalDistribution(Constantes.MEDIA_DEMANDA, Constantes.DESVIACION_DEMANDA);
        Random random = new Random();

        for (int dia = 0; dia < tamanoRecomendado; dia++) {
            ModelosDeDatos.ResultadoSimulacion resultado = simularDia(inventarioFinal, dist, random);
            costos[dia] = resultado.costoTotal;
            inventarioFinal = resultado.inventarioFinal;
        }

        return costos;
    }
}
