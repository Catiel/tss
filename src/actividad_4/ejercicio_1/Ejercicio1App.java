package actividad_4.ejercicio_1; // Paquete donde reside la aplicación del Ejercicio 1

import com.formdev.flatlaf.FlatLightLaf; // Importa Look & Feel FlatLaf

import javax.swing.*; // Importa clases Swing básicas

/**
 * Ventana principal de la aplicación del Ejercicio 1.
 * Permite analizar el modelo de software con diferentes paneles interactivos:
 * - Comparación de Beneficios
 * - Tabla Automática (con valores predefinidos)
 * - Tabla Manual (con valores ingresados por el usuario)
 * - Tabla Random (con valores generados aleatoriamente)
 * - Configuración de parámetros
 */
public class Ejercicio1App extends JFrame { // Clase principal que extiende JFrame
    // Mantener referencias a los paneles para evitar que se pierdan los observadores
    private final PanelModeloPrecios panelModeloPrecios; // Panel comparación de beneficios
    private final PanelTablaAuto panelTablaAuto;         // Panel tabla automática
    private final PanelTablaManual panelTablaManual;     // Panel tabla manual
    private final PanelTablaRandom panelTablaRandom;     // Panel tabla aleatoria
    private final PanelConfiguracion panelConfiguracion; // Panel configuración parámetros

    /**
     * Constructor. Inicializa la ventana principal, los paneles y la interfaz gráfica.
     * Aplica el Look & Feel FlatLaf y configura las pestañas de la aplicación.
     */
    public Ejercicio1App() { // Constructor de la ventana
        try { // Intenta aplicar el Look & Feel FlatLaf
            UIManager.setLookAndFeel(new FlatLightLaf()); // Establece FlatLaf
        } catch (Exception e) { // Captura cualquier error
            System.out.println("No se pudo aplicar FlatLaf, usando el Look & Feel por defecto."); // Mensaje de advertencia
        }
        setTitle("Ejercicio 1 - Proyecto de software"); // Define título de la ventana
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   // Termina la app al cerrar
        setSize(1000, 700);                               // Tamaño inicial de la ventana
        setLocationRelativeTo(null);                     // Centra la ventana en pantalla

        // Creamos las instancias de los paneles una sola vez
        panelModeloPrecios = new PanelModeloPrecios();    // Instancia panel de modelo de precios
        panelTablaAuto = new PanelTablaAuto();            // Instancia panel de tabla automática
        panelTablaManual = new PanelTablaManual();        // Instancia panel de tabla manual
        panelTablaRandom = new PanelTablaRandom();        // Instancia panel de tabla aleatoria
        panelConfiguracion = new PanelConfiguracion();    // Instancia panel de configuración

        JTabbedPane pestanas = new JTabbedPane();         // Crea contenedor de pestañas
        pestanas.addTab("Comparación de Beneficios", panelModeloPrecios); // Añade pestaña beneficios
        pestanas.addTab("Tabla Automática", panelTablaAuto);             // Añade pestaña automática
        pestanas.addTab("Tabla Manual", panelTablaManual);               // Añade pestaña manual
        pestanas.addTab("Tabla Random", panelTablaRandom);               // Añade pestaña aleatoria
        pestanas.addTab("Configuración", panelConfiguracion);            // Añade pestaña configuración
        add(pestanas); // Agrega el contenedor de pestañas al frame
    }

    /**
     * Método principal. Lanza la aplicación Swing en el hilo de eventos.
     */
    public static void main(String[] args) { // Punto de entrada
        SwingUtilities.invokeLater(() -> {    // Asegura ejecución en EDT
            Ejercicio1App app = new Ejercicio1App(); // Crea instancia de la app
            app.setVisible(true);                   // Muestra la ventana
        });
    }
}
