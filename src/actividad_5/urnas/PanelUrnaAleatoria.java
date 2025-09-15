package actividad_5.urnas; // Paquete del panel de simulación aleatoria

import javax.swing.*; // Importa componentes de UI Swing
import javax.swing.table.DefaultTableModel; // Importa el modelo de tabla por defecto
import java.awt.*; // Clases de layout y gráficos
import java.awt.event.ActionEvent; // Eventos de acción para botones
import java.util.Random; // Generación de números pseudoaleatorios

/** Panel que genera números pseudoaleatorios y simula las extracciones. */ // Descripción del panel
public class PanelUrnaAleatoria extends JPanel { // Clase que extiende JPanel
    private final DefaultTableModel modelo; // Modelo de datos de la tabla de resultados
    private final JTextField txtCantidad; // Campo para ingresar cantidad de extracciones
    private final JButton btnGenerar; // Botón que lanza la simulación
    private final JLabel lblResumen; // Etiqueta para mostrar totales por color
    private final Random random = new Random(); // Generador de números aleatorios

    public PanelUrnaAleatoria(){ // Constructor del panel
        EstilosUI.aplicarEstiloPanel(this); // Aplica estilo de fondo
        setLayout(new BorderLayout(10,10)); // Usa BorderLayout con separación

        JLabel titulo = new JLabel("Simulación con números generados por el programa"); // Crea etiqueta título
        EstilosUI.aplicarEstiloTitulo(titulo); // Aplica estilo de título
        titulo.setBorder(BorderFactory.createEmptyBorder(10,10,0,10)); // Margen interno superior
        add(titulo, BorderLayout.NORTH); // Añade título al norte

        JPanel panelTop = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Panel superior para controles
        EstilosUI.aplicarEstiloPanel(panelTop); // Estilo de panel superior
        panelTop.add(new JLabel("Cantidad de extracciones:")); // Etiqueta de cantidad
        txtCantidad = new JTextField("10",5); // Campo con valor inicial 10
        panelTop.add(txtCantidad); // Añade campo al panel superior
        btnGenerar = new JButton("Generar"); // Crea botón generar
        EstilosUI.aplicarEstiloBoton(btnGenerar); // Aplica estilo al botón
        panelTop.add(btnGenerar); // Añade botón al panel superior
        add(panelTop, BorderLayout.BEFORE_FIRST_LINE); // Inserta panel superior

        modelo = new DefaultTableModel(new Object[]{"# de pelota","Número aleatorio","Color"},0){ // Modelo de tabla con columnas
            @Override public boolean isCellEditable(int r,int c){return false;} // Celdas no editables
            @Override public Class<?> getColumnClass(int c){ return c==0?Integer.class:String.class; } // Tipos de columnas
        }; // Fin modelo
        JTable tabla = new JTable(modelo); // Crea tabla visual
        EstilosUI.aplicarEstiloTabla(tabla); // Aplica estilo tabla
        add(new JScrollPane(tabla), BorderLayout.CENTER); // Añade tabla con scroll al centro

        lblResumen = new JLabel(" "); // Etiqueta inicial vacía
        EstilosUI.aplicarEstiloLabel(lblResumen); // Estilo etiqueta
        lblResumen.setBorder(BorderFactory.createEmptyBorder(5,10,10,10)); // Margen interno etiqueta
        add(lblResumen, BorderLayout.SOUTH); // Añade etiqueta al sur

        btnGenerar.addActionListener(this::generar); // Asocia acción al botón
    } // Fin constructor

    private void generar(ActionEvent e){ // Método que ejecuta la simulación
        int n; // Variable de cantidad
        try{ // Bloque control errores
            n = Integer.parseInt(txtCantidad.getText().trim()); // Parseo del texto
            if(n<=0 || n>1000){ // Validación de rango
                JOptionPane.showMessageDialog(this,"Ingrese un valor entre 1 y 1000","Cantidad inválida",JOptionPane.WARNING_MESSAGE); // Mensaje de advertencia
                return; // Sale si inválido
            } // Fin if
        }catch(NumberFormatException ex){ // Captura de error numérico
            JOptionPane.showMessageDialog(this,"Valor no numérico","Error",JOptionPane.ERROR_MESSAGE);return; // Mensaje de error
        } // Fin try-catch
        modelo.setRowCount(0); // Limpia filas previas
        int verdes=0, rojas=0, amarillas=0; // Contadores de colores
        for(int i=0;i<n;i++){ // Bucle de extracciones
            double r = random.nextDouble(); // Número aleatorio en [0,1)
            String color = UrnaModelo.colorPara(r); // Determina color según modelo
            if("verdes".equals(color)){ // Si color verde
                verdes++; // Incrementa verdes
            } else if("rojas".equals(color)){ // Si color rojo
                rojas++; // Incrementa rojas
            } else if("amarillas".equals(color)){ // Si color amarillo
                amarillas++; // Incrementa amarillas
            } // Fin if colores
            modelo.addRow(new Object[]{i+1, UtilFormatoUrnas.fmt(r), color}); // Agrega fila con datos
        } // Fin for
        lblResumen.setText("Totales -> verdes: " + verdes + ", rojas: " + rojas + ", amarillas: " + amarillas); // Muestra totales
    } // Fin generar
} // Fin clase PanelUrnaAleatoria
