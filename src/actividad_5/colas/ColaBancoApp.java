package actividad_5.colas; // Define el paquete donde se encuentra esta clase

import com.formdev.flatlaf.FlatLightLaf; // Importa el look and feel moderno FlatLaf
import javax.swing.*; // Importa todas las clases de Swing para la interfaz gráfica

/** Aplicación principal para la simulación de la cola del banco (ventanilla auto). */
public class ColaBancoApp extends JFrame { // Clase principal que extiende JFrame para crear una ventana de aplicación de simulación de colas bancarias
    public ColaBancoApp(){ // Constructor de la aplicación de simulación de colas del banco
        try { UIManager.setLookAndFeel(new FlatLightLaf()); } catch (Exception ignored) {} // Establece el tema visual moderno, ignora errores si falla
        setTitle("Simulación Cola Banco - Tiempo de Espera"); // Define el título de la ventana indicando que simula colas bancarias y tiempos de espera
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Configura que la aplicación termine al cerrar la ventana
        setSize(1050,650); // Establece el tamaño de la ventana en píxeles (ancho x alto) - más ancho que calentadores para acomodar más datos
        setLocationRelativeTo(null); // Centra la ventana en la pantalla

        JTabbedPane tabs = new JTabbedPane(); // Crea un componente de pestañas para organizar los diferentes tipos de simulación
        tabs.addTab("Ejemplo", new PanelColaPredefinida()); // Agrega pestaña con ejemplo predefinido de simulación de cola bancaria
        tabs.addTab("Aleatoria", new PanelColaAleatoria()); // Agrega pestaña para simulación aleatoria de cola bancaria
        tabs.addTab("Manual", new PanelColaManual()); // Agrega pestaña para simulación manual de cola bancaria
        add(tabs); // Añade el panel de pestañas a la ventana principal
    }

    public static void main(String[] args){ SwingUtilities.invokeLater(() -> new ColaBancoApp().setVisible(true)); } // Método principal que inicia la aplicación en el hilo de eventos de Swing
}
