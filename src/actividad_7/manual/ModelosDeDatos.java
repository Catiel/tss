package actividad_7.manual;
/**
 * Clases que encapsulan los datos utilizados en la simulación
 */
public class ModelosDeDatos {

    /**
     * Clase que encapsula todos los resultados de simular un día
     */
    public static class ResultadoSimulacion {
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
    public static class EstadisticasSimulacion {
        public double suma; // Suma de todos los valores
        public double sumaCuadrados; // Suma de cuadrados (para varianza)
        public double promedio; // Media aritmética
        public double varianza; // Varianza de la muestra
        public double desviacion; // Desviación estándar
        public double minimo; // Valor mínimo observado
        public double maximo; // Valor máximo observado
    }
}
