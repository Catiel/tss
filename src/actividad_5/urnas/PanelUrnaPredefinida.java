package actividad_5.urnas; // Paquete del panel de números proporcionados

import javax.swing.*; // Importa componentes Swing
import javax.swing.table.DefaultTableModel; // Importa modelo de tabla por defecto
import java.awt.*; // Importa clases AWT (layouts, color, fuente)

/** Panel que muestra la simulación con los 10 números pseudoaleatorios proporcionados por la práctica. */ // Descripción general del panel
public class PanelUrnaPredefinida extends JPanel { // Inicio de la clase que extiende JPanel
    private final DefaultTableModel modeloSim; // Modelo para la tabla de simulación
    private final DefaultTableModel modeloDist; // Modelo para la tabla de distribución

    private static final double[] NUMEROS = {0.26,0.42,0.95,0.95,0.66,0.17,0.03,0.56,0.83,0.55}; ; // Arreglo con los 10 números fijos

    public PanelUrnaPredefinida(){ // Constructor del panel
        EstilosUI.aplicarEstiloPanel(this); // Aplica fondo estándar
        setLayout(new BorderLayout(10,10)); // Define BorderLayout con separación

        JLabel titulo = new JLabel("Simulación (números proporcionados por la práctica)"); // Crea etiqueta de título
        EstilosUI.aplicarEstiloTitulo(titulo); // Aplica estilo de título
        titulo.setBorder(BorderFactory.createEmptyBorder(10,10,10,10)); // Margen interno del título
        add(titulo, BorderLayout.NORTH); // Añade título en la parte superior

        JPanel centro = new JPanel(); // Panel contenedor central
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS)); // Layout vertical para apilar tablas
        EstilosUI.aplicarEstiloPanel(centro); // Aplica estilo al panel contenedor

        modeloDist = new DefaultTableModel(new Object[]{ // Inicializa columnas de la tabla distribución
                "Distribuciones de probabilidad", // Columna de probabilidades individuales
                "Distribución acumulada", // Columna de acumuladas
                "Rango inicio", // Columna inicio del rango
                "Rango fin", // Columna fin del rango
                "Color"},0){ // Columna color textual
            @Override public boolean isCellEditable(int r,int c){ return false; } // Hace la tabla no editable
        }; // Fin definición modeloDist
        JTable tablaDist = new JTable(modeloDist); // Crea tabla visual para la distribución
        EstilosUI.aplicarEstiloTabla(tablaDist); // Aplica estilo visual a la tabla de distribución
        JScrollPane spDist = new JScrollPane(tablaDist); // Scroll para la tabla de distribución
        spDist.setBorder(BorderFactory.createTitledBorder("Distribución de probabilidades")); // Borde con título
        centro.add(spDist); // Añade la tabla de distribución al contenedor
        centro.add(Box.createVerticalStrut(8)); // Espacio vertical entre tablas

        modeloSim = new DefaultTableModel(new Object[]{"# de pelota","Número aleatorio","Color"},0){ // Modelo de la tabla de simulación
            @Override public boolean isCellEditable(int r,int c){return false;} // Celdas no editables
            @Override public Class<?> getColumnClass(int c){ return c==0?Integer.class:String.class; } // Tipos de columnas
        }; // Fin definición modeloSim
        JTable tablaSim = new JTable(modeloSim); // Crea tabla para la simulación
        EstilosUI.aplicarEstiloTabla(tablaSim); // Aplica estilo a la tabla de simulación
        JScrollPane spSim = new JScrollPane(tablaSim); // Scroll para la tabla de simulación
        spSim.setBorder(BorderFactory.createTitledBorder("Simulación de extracciones")); // Borde con título
        centro.add(spSim); // Añade la tabla de simulación al contenedor

        add(centro, BorderLayout.CENTER); // Inserta el panel central en el centro

        JTextArea area = new JTextArea(); // Área de texto explicativa
        area.setEditable(false); // Deshabilita edición
        area.setBackground(getBackground()); // Igual fondo al del panel
        area.setFont(new Font("Segoe UI", Font.ITALIC, 12)); // Fuente itálica
        area.setText("Se extraen 10 pelotas (con reemplazo)." + // Línea 1
                "\nProbabilidades: 10% verdes, 40% rojas, 50% amarillas." + // Línea 2
                "\nRangos: [0,0.10)->verdes | [0.10,0.50)->rojas | [0.50,1.00]->amarillas."); // Línea 3 con rangos
        add(area, BorderLayout.SOUTH); // Añade el área de texto abajo

        generarDistribucion(); // Genera filas de la tabla de distribución
        llenarSimulacion(); // Llena la tabla de simulación con los números fijos
    } // Fin constructor

    private void generarDistribucion(){ // Método para construir la tabla de distribución
        modeloDist.setRowCount(0); // Limpia cualquier fila previa
        double[] probs = {UrnaModelo.P_VERDE, UrnaModelo.P_ROJA, UrnaModelo.P_AMARILLA}; // Arreglo de probabilidades base
        String[] colores = {"verde","rojas","amarillas"}; // Arreglo de colores asociados
        double acumulada = 0.0; // Inicializa acumulada
        double inicio = 0.0; // Inicio de rango actual
        for(int i=0;i<probs.length;i++){ // Itera cada probabilidad
            double p = probs[i]; // Probabilidad individual
            acumulada += p; // Suma a la acumulada
            if(i==probs.length-1) acumulada = 1.0; // Fuerza 1.0 exacto en la última fila
            double fin = acumulada; // Fin del intervalo
            String pTxt = UtilFormatoUrnas.fmt(p); // Formatea prob individual
            String acTxt = UtilFormatoUrnas.fmt(acumulada); // Formatea acumulada
            String iniTxt = UtilFormatoUrnas.fmt(inicio); // Formatea inicio de rango
            String finTxt = UtilFormatoUrnas.fmt(fin); // Formatea fin de rango
            modeloDist.addRow(new Object[]{pTxt, acTxt, iniTxt, finTxt, colores[i]}); // Inserta fila en la tabla
            inicio = fin; // Actualiza inicio para siguiente intervalo
        } // Fin for
    } // Fin generarDistribucion

    private void llenarSimulacion(){ // Método que llena la tabla de simulación
        modeloSim.setRowCount(0); // Limpia filas previas
        for(int i=0;i<NUMEROS.length;i++){ // Itera cada número fijo
            double r = NUMEROS[i]; // Número actual
            String color = UrnaModelo.colorPara(r); // Determina color
            modeloSim.addRow(new Object[]{i+1, UtilFormatoUrnas.fmt(r), color}); // Agrega fila a la tabla
        } // Fin for
    } // Fin llenarSimulacion
} // Fin clase PanelUrnaPredefinida
