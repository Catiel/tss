package actividad_5.urnas; // Paquete del proyecto para esta clase

import javax.swing.*; // Importa componentes Swing
import javax.swing.table.DefaultTableModel; // Importa el modelo de tabla por defecto
import java.awt.*; // Importa AWT para layouts y estilos
import java.awt.event.ActionEvent; // Importa clase de eventos de acción

/** Panel para que el usuario ingrese manualmente los números pseudoaleatorios (en [0,1)). */ // Descripción de la finalidad
public class PanelUrnaPredefinidaManual extends JPanel { // Declaración de la clase que extiende JPanel
    private final DefaultTableModel modelo; // Modelo de datos de la tabla
    private final JTextField txtFilas; // Campo para ingresar cuántas filas crear
    private final JButton btnCrear; // Botón para crear filas
    private final JButton btnCalcular; // Botón para calcular colores
    private final JButton btnLimpiar; // Botón para limpiar la tabla
    private final JLabel lblResumen; // Etiqueta para mostrar totales

    public PanelUrnaPredefinidaManual(){ // Constructor del panel
        EstilosUI.aplicarEstiloPanel(this); // Aplica estilo base (fondo blanco)
        setLayout(new BorderLayout(10,10)); // Establece BorderLayout con márgenes

        JLabel titulo = new JLabel("Simulación con números ingresados manualmente"); // Crea etiqueta de título
        EstilosUI.aplicarEstiloTitulo(titulo); // Aplica estilo de título
        titulo.setBorder(BorderFactory.createEmptyBorder(10,10,0,10)); // Añade margen interno
        add(titulo, BorderLayout.NORTH); // Coloca el título en la parte superior

        JPanel panelTop = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Panel superior con FlowLayout
        EstilosUI.aplicarEstiloPanel(panelTop); // Aplica estilo al panel superior
        panelTop.add(new JLabel("Cantidad de filas:")); // Añade etiqueta descriptiva
        txtFilas = new JTextField("10",5); // Campo de texto con valor inicial 10
        panelTop.add(txtFilas); // Añade el campo al panel
        btnCrear = new JButton("Crear filas"); // Crea botón Crear
        EstilosUI.aplicarEstiloBoton(btnCrear); // Estiliza botón Crear
        panelTop.add(btnCrear); // Añade botón Crear
        btnCalcular = new JButton("Calcular colores"); // Crea botón Calcular
        EstilosUI.aplicarEstiloBoton(btnCalcular); // Estiliza botón Calcular
        panelTop.add(btnCalcular); // Añade botón Calcular
        btnLimpiar = new JButton("Limpiar"); // Crea botón Limpiar
        EstilosUI.aplicarEstiloBoton(btnLimpiar); // Estiliza botón Limpiar
        panelTop.add(btnLimpiar); // Añade botón Limpiar
        JLabel lblHint = new JLabel("Ingrese valores entre 0 y 1 (ej: 0.24, 0,72)"); // Etiqueta de ayuda de formato
        EstilosUI.aplicarEstiloLabel(lblHint); // Aplica estilo a ayuda
        panelTop.add(lblHint); // Añade ayuda al panel superior
        add(panelTop, BorderLayout.BEFORE_FIRST_LINE); // Coloca panel superior en layout

        modelo = new DefaultTableModel(new Object[]{"# de pelota","Número aleatorio","Color"},0){ // Crea modelo de tabla con columnas
            @Override public boolean isCellEditable(int r,int c){ return c==1; } // Solo la segunda columna es editable
            @Override public Class<?> getColumnClass(int c){ return c==0?Integer.class:String.class; } // Define tipos de columna
        }; // Fin del modelo anónimo
        JTable tabla = new JTable(modelo); // Crea la tabla con el modelo
        EstilosUI.aplicarEstiloTabla(tabla); // Aplica estilos a la tabla
        tabla.getColumnModel().getColumn(0).setPreferredWidth(90); // Ajusta ancho columna índice
        tabla.getColumnModel().getColumn(1).setPreferredWidth(140); // Ajusta ancho columna número
        tabla.getColumnModel().getColumn(2).setPreferredWidth(120); // Ajusta ancho columna color
        add(new JScrollPane(tabla), BorderLayout.CENTER); // Añade tabla con scroll al centro

        lblResumen = new JLabel(" "); // Inicializa etiqueta de resumen vacía
        EstilosUI.aplicarEstiloLabel(lblResumen); // Aplica estilo a resumen
        lblResumen.setBorder(BorderFactory.createEmptyBorder(5,10,10,10)); // Margen interno del resumen
        add(lblResumen, BorderLayout.SOUTH); // Añade resumen en parte inferior

        btnCalcular.setEnabled(false); // Desactiva Calcular hasta que haya filas

        btnCrear.addActionListener(this::crearFilas); // Asocia acción crear
        btnCalcular.addActionListener(this::calcular); // Asocia acción calcular
        btnLimpiar.addActionListener(e -> limpiar()); // Asocia acción limpiar con lambda
    } // Fin constructor

    private void crearFilas(ActionEvent e){ // Método para crear filas vacías
        int n; // Número de filas solicitadas
        try { // Intento de parseo
            n = Integer.parseInt(txtFilas.getText().trim()); // Convierte texto a entero
            if(n<=0 || n>200){ // Valida rango permitido
                JOptionPane.showMessageDialog(this,"Ingrese un número entre 1 y 200","Cantidad inválida",JOptionPane.WARNING_MESSAGE); // Mensaje de advertencia
                return; // Sale si fuera de rango
            } // Fin validación rango
        } catch (NumberFormatException ex){ // Captura error de formato
            JOptionPane.showMessageDialog(this,"Valor no numérico","Error",JOptionPane.ERROR_MESSAGE); return; } // Mensaje de error y salida
        modelo.setRowCount(0); // Limpia filas anteriores
        for(int i=0;i<n;i++) modelo.addRow(new Object[]{i+1, "", ""}); // Agrega n filas vacías numeradas
        btnCalcular.setEnabled(true); // Activa el botón Calcular
        lblResumen.setText(" "); // Limpia el resumen
    } // Fin crearFilas

    private void calcular(ActionEvent e){ // Método para procesar números y asignar colores
        int verdes=0, rojas=0, amarillas=0; // Contadores por color
        for(int i=0;i<modelo.getRowCount();i++){ // Itera sobre cada fila existente
            String txt = (modelo.getValueAt(i,1)==null)?"":modelo.getValueAt(i,1).toString(); // Obtiene texto del número (o vacío)
            if(txt.trim().isEmpty()){ // Verifica vacío
                JOptionPane.showMessageDialog(this,"Fila "+(i+1)+": falta el número aleatorio","Dato faltante",JOptionPane.WARNING_MESSAGE); return; } // Mensaje y retorno
            double r; // Variable para número convertido
            try { r = UtilFormatoUrnas.parse(txt); } catch (NumberFormatException ex){ // Intento de parseo flexible
                JOptionPane.showMessageDialog(this,"Fila "+(i+1)+": formato inválido ('"+txt+"')","Error",JOptionPane.ERROR_MESSAGE); return; } // Error de formato
            if(r<0 || r>=1){ // Valida rango [0,1)
                JOptionPane.showMessageDialog(this,"Fila "+(i+1)+": el número debe estar en [0,1)","Fuera de rango",JOptionPane.WARNING_MESSAGE); return; } // Error de rango
            String color = UrnaModelo.colorPara(r); // Determina color por probabilidad
            modelo.setValueAt(color, i, 2); // Coloca color en la fila
            if("verdes".equals(color)){ // Caso verde
                verdes++; // Incrementa contador verdes
            } else if("rojas".equals(color)){ // Caso roja
                rojas++; // Incrementa contador rojas
            } else if("amarillas".equals(color)){ // Caso amarilla
                amarillas++; // Incrementa contador amarillas
            } // Fin if-else colores
        } // Fin for filas
        lblResumen.setText("Totales -> verdes: "+verdes+", rojas: "+rojas+", amarillas: "+amarillas); // Muestra totales en etiqueta
    } // Fin calcular

    private void limpiar(){ // Método para limpiar tabla y estado
        modelo.setRowCount(0); // Elimina todas las filas
        btnCalcular.setEnabled(false); // Desactiva botón Calcular
        lblResumen.setText(" "); // Borra texto de resumen
    } // Fin limpiar
} // Fin clase PanelUrnaPredefinidaManual
