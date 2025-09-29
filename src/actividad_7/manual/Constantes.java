package actividad_7.manual; // Declaración del paquete donde se encuentra la clase

import java.awt.*; // Importa la clase Color y Font para definir colores y fuentes

/**
 * Clase que contiene todas las constantes utilizadas en la simulación
 */
public class Constantes { // Declaración de la clase Constantes

    // ========================== PARÁMETROS DEL SISTEMA ==========================
    public static final double ERROR_PERMITIDO = 500; // Error permitido en la simulación
    public static final double VALOR_T = 1.9665; // Valor t para cálculos estadísticos
    public static int POLITICA_PRODUCCION = 60; // Política de producción (no final para permitir modificación)
    public static final double MEDIA_DEMANDA = 80; // Media de la demanda diaria
    public static final double DESVIACION_DEMANDA = 10; // Desviación estándar de la demanda
    public static final double COSTO_FALTANTE_UNITARIO = 800; // Costo unitario por faltante
    public static final double COSTO_INVENTARIO_UNITARIO = 500; // Costo unitario de inventario

    // ========================== OPCIONES DE POLÍTICA ==========================
    public static final int[] OPCIONES_POLITICA = {60, 70, 80}; // Opciones posibles para la política de producción

    // ========================== CONFIGURACIÓN DE TABLA ==========================
    public static final String[] COLUMNAS = { // Nombres de las columnas de la tabla
        "Día", // Columna 0: Día
        "Inventario inicial (Uds)", // Columna 1: Inventario inicial
        "Política de producción (Uds)", // Columna 2: Política de producción
        "Total disponible (Uds)", // Columna 3: Total disponible
        "Rn", // Columna 4: Número aleatorio
        "Demanda (uds)", // Columna 5: Demanda
        "Ventas (Uds)", // Columna 6: Ventas
        "Ventas perdidas (uds)", // Columna 7: Ventas perdidas
        "Inventario final (Uds)", // Columna 8: Inventario final
        "Costo faltante ($)", // Columna 9: Costo por faltante
        "Costo de inventarios ($)", // Columna 10: Costo de inventario
        "Costo total ($)" // Columna 11: Costo total
    };

    public static final int[] ANCHOS_COLUMNAS = { // Anchos preferidos de cada columna
        50, // Ancho columna Día
        140, // Ancho columna Inventario inicial
        150, // Ancho columna Política de producción
        140, // Ancho columna Total disponible
        70, // Ancho columna Rn
        100, // Ancho columna Demanda
        100, // Ancho columna Ventas
        120, // Ancho columna Ventas perdidas
        130, // Ancho columna Inventario final
        150, // Ancho columna Costo faltante
        170, // Ancho columna Costo de inventarios
        150 // Ancho columna Costo total
    };

    // ========================== COLORES DEL TEMA ==========================
    public static final Color COLOR_PRIMARIO = new Color(30, 144, 255); // Color principal del tema
    public static final Color COLOR_FILA_PAR = Color.WHITE; // Color para filas pares
    public static final Color COLOR_FILA_IMPAR = new Color(235, 245, 255); // Color para filas impares
    public static final Color COLOR_FONDO_REPLICA = new Color(255, 255, 200); // Color de fondo para réplicas
    public static final Color COLOR_PANEL_REPLICA = new Color(248, 250, 255); // Color de panel para réplicas

    // ========================== FUENTES ==========================
    public static final Font FUENTE_GENERAL = new Font("Segoe UI", Font.PLAIN, 14); // Fuente general
    public static final Font FUENTE_HEADER = new Font("Segoe UI", Font.BOLD, 15); // Fuente para encabezados
    public static final Font FUENTE_TITULO = new Font("Segoe UI Semibold", Font.BOLD, 18); // Fuente para títulos
    public static final Font FUENTE_VALOR = new Font("Segoe UI", Font.BOLD, 14); // Fuente para valores
    public static final Font FUENTE_REPLICA = new Font("Segoe UI Semibold", Font.BOLD, 16); // Fuente para réplicas

    // ========================== COLORES PARA GRÁFICAS ==========================
    public static final Color[] COLORES_REPLICAS = { // Colores para distinguir réplicas en gráficas
        new Color(255, 99, 132),   // Rojo/Rosa para la primera réplica
        new Color(54, 162, 235),   // Azul para la segunda réplica
        new Color(255, 205, 86),   // Amarillo para la tercera réplica
        new Color(75, 192, 192),   // Verde/Turquesa para la cuarta réplica
        new Color(153, 102, 255)   // Púrpura para la quinta réplica
    };

    /**
     * Método para cambiar la política de producción
     */
    public static void setPoliticaProduccion(int nuevaPolitica) { // Método para modificar la política de producción
        POLITICA_PRODUCCION = nuevaPolitica; // Asigna el nuevo valor a la política de producción
    }
}