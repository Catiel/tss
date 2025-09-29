package actividad_7.aleatorio; // Declaración del paquete donde se encuentra la clase
/**
 * Clases que encapsulan los datos utilizados en la simulación
 */
public class ModelosDeDatos { // Declaración de la clase principal para modelos de datos

    /**
     * Clase que encapsula todos los resultados de simular un día
     */
    public static class ResultadoSimulacion { // Clase interna para resultados de simulación diaria
        public int inventarioInicial; // Inventario al comenzar el día
        public int totalDisponible; // Inventario inicial + producción del día
        public int demanda; // Demanda generada aleatoriamente
        public int ventas; // Unidades efectivamente vendidas
        public int ventasPerdidas; // Demanda no satisfecha
        public int inventarioFinal; // Inventario al final del día
        public double rn; // Número aleatorio utilizado
        public double costoFaltante; // Costo por ventas perdidas
        public double costoInventario; // Costo por mantener inventario
        public double costoTotal; // Suma de ambos costos
    }

    /**
     * Clase que encapsula estadísticas descriptivas de una muestra
     */
    public static class EstadisticasSimulacion { // Clase interna para estadísticas descriptivas
        public double suma; // Suma de todos los valores de la muestra
        public double sumaCuadrados; // Suma de los cuadrados de los valores (para varianza)
        public double promedio; // Media aritmética de la muestra
        public double varianza; // Varianza de la muestra
        public double desviacion; // Desviación estándar de la muestra
        public double minimo; // Valor mínimo observado en la muestra
        public double maximo; // Valor máximo observado en la muestra
    }
}
