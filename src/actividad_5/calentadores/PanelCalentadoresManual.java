package actividad_5.calentadores; // Define el paquete donde se encuentra esta clase

import javax.swing.*; // Importa todas las clases de Swing para componentes de interfaz gráfica
import javax.swing.table.DefaultTableModel; // Importa el modelo de datos para tablas
import java.awt.*; // Importa clases de AWT para layouts y colores
import java.awt.event.ActionEvent; // Importa la clase para manejar eventos de acción

/** Panel para ingresar manualmente números aleatorios U(0,1) y simular ventas semanales. */
public class PanelCalentadoresManual extends JPanel { // Clase que extiende JPanel para crear un panel de simulación manual
    private final JSpinner spSemanas; // Control spinner para seleccionar el número de semanas a simular
    private final JSpinner spInventario; // Control spinner para establecer el inventario fijo por semana
    private final JButton btnCrear; // Botón para crear la tabla de entrada de números aleatorios
    private final JButton btnCalcular; // Botón para calcular los resultados de la simulación
    private final DefaultTableModel modeloRangos; // Modelo para la tabla que muestra los rangos de probabilidad (referencia)
    private final DefaultTableModel modeloInput; // tabla de entrada de números aleatorios - Modelo para la tabla donde el usuario ingresa números aleatorios
    private final DefaultTableModel modeloSim; // Modelo para la tabla que muestra los resultados de la simulación

    public PanelCalentadoresManual(){ // Constructor que inicializa todo el panel de simulación manual
        setLayout(new BorderLayout(8,8)); // Establece el layout BorderLayout con espaciado de 8 píxeles horizontal y vertical
        EstilosUI.aplicarEstiloPanel(this); // Aplica el estilo visual predefinido al panel
        JLabel titulo = new JLabel("Simulación manual de ventas de calentadores (ingrese números aleatorios)"); // Crea la etiqueta del título
        EstilosUI.aplicarEstiloTitulo(titulo); // Aplica el estilo específico para títulos
        add(titulo, BorderLayout.NORTH); // Agrega el título en la parte superior del panel

        // Controles superiores
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Crea panel de controles con layout de flujo alineado a la izquierda
        EstilosUI.aplicarEstiloPanel(controles); // Aplica estilo al panel de controles
        controles.add(new JLabel("Número de semanas:")); // Agrega etiqueta para el control de semanas
        spSemanas = new JSpinner(new SpinnerNumberModel(20,1,200,1)); // Inicializa spinner de semanas con valor inicial 20, mínimo 1, máximo 200, incremento 1
        controles.add(spSemanas); // Agrega el spinner de semanas al panel de controles
        controles.add(new JLabel("Inventario fijo por semana:")); // Agrega etiqueta para el control de inventario
        spInventario = new JSpinner(new SpinnerNumberModel(CalentadoresModelo.INVENTARIO_FIJO,1,50,1)); // Inicializa spinner de inventario con valor del modelo, mínimo 1, máximo 50, incremento 1
        controles.add(spInventario); // Agrega el spinner de inventario al panel de controles
        btnCrear = new JButton("Crear tabla de entrada"); // Crea el botón para generar la tabla de entrada
        EstilosUI.aplicarEstiloBoton(btnCrear); // Aplica estilo predefinido al botón de crear
        controles.add(btnCrear); // Agrega el botón de crear al panel de controles
        btnCalcular = new JButton("Calcular resultados"); // Crea el botón para ejecutar los cálculos
        EstilosUI.aplicarEstiloBoton(btnCalcular); // Aplica estilo predefinido al botón de calcular
        controles.add(btnCalcular); // Agrega el botón de calcular al panel de controles
        add(controles, BorderLayout.BEFORE_FIRST_LINE); // Agrega el panel de controles en la parte superior

        // Panel principal dividido
        JPanel panelPrincipal = new JPanel(new BorderLayout(8,8)); // Crea el panel principal con BorderLayout y espaciado
        EstilosUI.aplicarEstiloPanel(panelPrincipal); // Aplica estilo al panel principal

        // Panel izquierdo: tabla de rangos (referencia)
        modeloRangos = new DefaultTableModel(new Object[]{"Prob","Dist.Acum","Inicio","Fin","Ventas"},0){@Override public boolean isCellEditable(int r,int c){return false;}}; // Crea modelo de tabla no editable con columnas para probabilidad, distribución acumulada, inicio, fin y ventas
        llenarRangos(); // Llama al método para poblar la tabla de rangos con datos del modelo
        JTable tRangos = new JTable(modeloRangos); // Crea la tabla usando el modelo de rangos
        EstilosUI.aplicarEstiloTabla(tRangos); // Aplica estilo predefinido a la tabla
        tRangos.getTableHeader().setBackground(new Color(200, 220, 255)); // Establece color azul claro para el encabezado de la tabla de rangos
        tRangos.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Desactiva el redimensionado automático de columnas
        for(int i = 0; i < 5; i++) { // Bucle para configurar el ancho de cada una de las 5 columnas
            tRangos.getColumnModel().getColumn(i).setPreferredWidth(65); // Establece ancho preferido de 65 píxeles para cada columna
        }
        JScrollPane spRangos = new JScrollPane(tRangos); // Envuelve la tabla en un scroll pane para permitir desplazamiento
        spRangos.setBorder(BorderFactory.createTitledBorder("Distribución de probabilidad (referencia)")); // Agrega borde con título explicativo
        spRangos.setPreferredSize(new Dimension(350, 300)); // Establece tamaño preferido del scroll pane
        panelPrincipal.add(spRangos, BorderLayout.WEST); // Agrega el scroll pane con la tabla de rangos al lado izquierdo

        // Panel central dividido verticalmente
        JPanel panelCentral = new JPanel(new GridLayout(2,1,5,5)); // Crea panel central con layout de rejilla 2x1 con espaciado de 5 píxeles
        EstilosUI.aplicarEstiloPanel(panelCentral); // Aplica estilo al panel central

        // Tabla de entrada de números aleatorios (editable)
        modeloInput = new DefaultTableModel(new Object[]{"Semana","#Aleatorio [0,1)"},0){ // Crea modelo para tabla de entrada con 2 columnas
            @Override public boolean isCellEditable(int r,int c){ // Override para controlar qué celdas son editables
                return c == 1; // Solo la columna de números aleatorios es editable - Solo permite editar la segunda columna (números aleatorios)
            }
            @Override public Class<?> getColumnClass(int c){ // Override para especificar el tipo de datos de cada columna
                return c == 0 ? Integer.class : String.class; // Primera columna es Integer (número de semana), segunda es String (número aleatorio)
            }
        };
        JTable tablaInput = new JTable(modeloInput); // Crea la tabla de entrada usando el modelo
        EstilosUI.aplicarEstiloTabla(tablaInput); // Aplica estilo predefinido a la tabla de entrada
        tablaInput.getTableHeader().setBackground(new Color(255, 255, 200)); // Establece color amarillo claro para el encabezado de la tabla de entrada
        tablaInput.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Desactiva redimensionado automático de columnas
        tablaInput.getColumnModel().getColumn(0).setPreferredWidth(60); // Establece ancho de 60 píxeles para columna "Semana"
        tablaInput.getColumnModel().getColumn(1).setPreferredWidth(120); // Establece ancho de 120 píxeles para columna "#Aleatorio [0,1)"
        JScrollPane spInput = new JScrollPane(tablaInput); // Envuelve la tabla de entrada en un scroll pane
        spInput.setBorder(BorderFactory.createTitledBorder("1. Ingrese números aleatorios [0,1)")); // Agrega borde con título instructivo
        panelCentral.add(spInput); // Agrega el scroll pane de entrada a la parte superior del panel central

        // Tabla de simulación (solo lectura)
        modeloSim = new DefaultTableModel(new Object[]{ // Crea modelo para la tabla de resultados de simulación con 4 columnas
            "# de semana","numeros aleatorios","Ventas de calentador","Faltantes"},0){
            @Override public boolean isCellEditable(int r,int c){return false;} // Override para hacer toda la tabla de solo lectura
            @Override public Class<?> getColumnClass(int c){ // Override para especificar el tipo de datos de cada columna
                if(c == 0 || c == 2 || c == 3) return Integer.class; // Columnas 0, 2, 3 son de tipo Integer
                return String.class; // La columna 1 es de tipo String (números aleatorios formateados)
            }
        };
        JTable tablaSim = new JTable(modeloSim){ // Crea la tabla de simulación con comportamiento personalizado
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer,int row,int column){ // Override para personalizar el renderizado de celdas
                Component c = super.prepareRenderer(renderer,row,column); // Obtiene el componente renderizado por defecto
                if(row < getRowCount()-2){ // fila de datos (no totales ni promedio) - Verifica si es una fila de datos normales
                    Object falt = getValueAt(row,3); // Obtiene el valor de la columna "Faltantes"
                    if(Integer.valueOf(1).equals(falt)) { // Si hay faltantes (valor = 1)
                        c.setBackground(new Color(255, 200, 200)); // rojo claro para faltantes - Establece fondo rojo claro
                    } else {
                        c.setBackground(Color.white); // Establece fondo blanco para filas sin faltantes
                    }
                } else { // fila de totales o promedio
                    c.setBackground(new Color(220, 220, 220)); // Establece fondo gris para filas de resumen
                }
                return c; // Retorna el componente con el color de fondo apropiado
            }
        };
        EstilosUI.aplicarEstiloTabla(tablaSim); // Aplica estilo predefinido a la tabla de simulación
        tablaSim.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Desactiva redimensionado automático de columnas
        tablaSim.getColumnModel().getColumn(0).setPreferredWidth(80); // Establece ancho de 80 píxeles para columna "# de semana"
        tablaSim.getColumnModel().getColumn(1).setPreferredWidth(110); // Establece ancho de 110 píxeles para columna "numeros aleatorios"
        tablaSim.getColumnModel().getColumn(2).setPreferredWidth(120); // Establece ancho de 120 píxeles para columna "Ventas de calentador"
        tablaSim.getColumnModel().getColumn(3).setPreferredWidth(80); // Establece ancho de 80 píxeles para columna "Faltantes"

        JScrollPane spSim = new JScrollPane(tablaSim); // Envuelve la tabla de simulación en un scroll pane
        spSim.setBorder(BorderFactory.createTitledBorder("2. Resultados de la simulación")); // Agrega borde con título
        panelCentral.add(spSim); // Agrega el scroll pane de simulación a la parte inferior del panel central

        panelPrincipal.add(panelCentral, BorderLayout.CENTER); // Agrega el panel central al centro del panel principal
        add(panelPrincipal, BorderLayout.CENTER); // Agrega el panel principal al centro del panel general

        btnCrear.addActionListener(this::crearFilas); // Asocia el método crearFilas como listener del evento de clic del botón crear
        btnCalcular.addActionListener(this::calcular); // Asocia el método calcular como listener del evento de clic del botón calcular
        btnCalcular.setEnabled(false); // Desactiva el botón calcular inicialmente hasta que se cree la tabla

        // Agregar instrucciones al final del constructor
        JTextArea instrucciones = new JTextArea("INSTRUCCIONES:\n" + // Crea área de texto con instrucciones de uso
            "1. Configure el número de semanas e inventario por semana\n" +
            "2. Presione 'Crear tabla de entrada' para generar las filas\n" +
            "3. Ingrese números aleatorios entre 0 y 1 (no incluye 1) en la columna #Aleatorio\n" +
            "4. Presione 'Calcular resultados' para obtener la simulación\n" +
            "5. Las filas rojas en el resultado indican semanas con faltantes (demanda > inventario)");
        instrucciones.setWrapStyleWord(true); // Activa ajuste de líneas por palabras en las instrucciones
        instrucciones.setLineWrap(true); // Activa ajuste automático de líneas en las instrucciones
        instrucciones.setEditable(false); // Hace las instrucciones de solo lectura
        instrucciones.setBackground(new Color(240, 248, 255)); // Establece fondo azul muy claro para las instrucciones
        instrucciones.setFont(new Font("Arial", Font.PLAIN, 11)); // Establece fuente Arial, normal, tamaño 11 para las instrucciones
        instrucciones.setBorder(BorderFactory.createTitledBorder("Instrucciones de uso")); // Agrega borde con título a las instrucciones
        // No agregamos las instrucciones al panel aquí ya que sería demasiado contenido - Comentario explicando por qué no se agrega el componente
    }

    private void llenarRangos(){ // Método que llena la tabla de rangos con los datos del modelo de probabilidad
        modeloRangos.setRowCount(0); // Limpia todas las filas existentes en el modelo de rangos
        double inicio=0, acum=0; // Inicializa variables para calcular rangos: inicio del rango y acumulado de probabilidades
        for(int i=0;i<CalentadoresModelo.VENTAS.length;i++){ // Itera sobre todos los valores de venta del modelo
            double p=CalentadoresModelo.PROBS[i]; // Obtiene la probabilidad del valor de venta actual
            acum+=p; // Suma la probabilidad al acumulado
            if(i==CalentadoresModelo.VENTAS.length-1)acum=1.0; // Asegura que el último valor acumulado sea exactamente 1.0
            double fin=acum; // El final del rango actual es el acumulado actual
            modeloRangos.addRow(new Object[]{ // Agrega una fila a la tabla de rangos con los datos calculados
                UtilFormatoCalent.f2(p), // Probabilidad formateada a 2 decimales
                UtilFormatoCalent.f2(acum), // Probabilidad acumulada formateada a 2 decimales
                UtilFormatoCalent.f2(inicio), // Inicio del rango formateado a 2 decimales
                UtilFormatoCalent.f2(fin), // Fin del rango formateado a 2 decimales
                CalentadoresModelo.VENTAS[i] // Valor de ventas correspondiente
            });
            inicio=fin; // El inicio del siguiente rango es el fin del rango actual
        }
    }

    private void crearFilas(ActionEvent e){ // Método que crea las filas en la tabla de entrada para los números aleatorios
        int semanas = (int) spSemanas.getValue(); // Obtiene el número de semanas seleccionado por el usuario
        modeloInput.setRowCount(0); // Limpia todas las filas existentes en la tabla de entrada
        modeloSim.setRowCount(0); // Limpia todas las filas existentes en la tabla de simulación

        for(int s=1;s<=semanas;s++) { // Bucle que crea una fila para cada semana
            modeloInput.addRow(new Object[]{s, ""}); // Agrega fila con número de semana y campo vacío para número aleatorio
        }
        btnCalcular.setEnabled(true); // Activa el botón calcular una vez que se han creado las filas
    }

    private Double parse(Object v){ // Método utilitario que parsea y valida un número aleatorio ingresado por el usuario
        if(v==null) return null; // Retorna null si el valor es null
        String t=v.toString().trim().replace(',', '.'); // Convierte a string, elimina espacios y reemplaza comas por puntos
        if(t.isEmpty()) return null; // Retorna null si el string está vacío
        try{
            double d=Double.parseDouble(t); // Intenta parsear el string como double
            if(d<0||d>=1) return null; // Retorna null si el número no está en el rango [0,1)
            return d; // Retorna el número si es válido
        }catch(Exception ex){
            return null; // Retorna null si hay error en el parsing
        }
    }

    private void calcular(ActionEvent e){ // Método que ejecuta los cálculos de la simulación con los números aleatorios ingresados
        if(modeloInput.getRowCount()==0) { // Verifica si existe la tabla de entrada
            JOptionPane.showMessageDialog(this, "Primero debe crear la tabla de entrada", "Error", JOptionPane.WARNING_MESSAGE); // Muestra mensaje de error si no hay tabla
            return; // Sale del método si no hay tabla
        }

        int inventario=(int) spInventario.getValue(); // Obtiene el inventario por semana seleccionado por el usuario
        modeloSim.setRowCount(0); // Limpia todas las filas existentes en la tabla de simulación

        int totalVentas=0; // Acumulador para el total de ventas simuladas
        int faltantes=0; // Contador de semanas con faltantes (demanda > inventario)
        StringBuilder faltSem=new StringBuilder(); // Builder para construir la lista de semanas con faltantes
        boolean first=true; // Flag para controlar la puntuación en la lista de semanas con faltantes

        for(int i=0;i<modeloInput.getRowCount();i++){ // Bucle que procesa cada fila de la tabla de entrada
            Double r = parse(modeloInput.getValueAt(i,1)); // Parsea y valida el número aleatorio de la fila actual
            if(r==null){ // Si el número aleatorio es inválido
                JOptionPane.showMessageDialog(this, // Muestra diálogo de error con información específica
                    "Semana "+(i+1)+": Número aleatorio inválido\n" +
                    "Debe estar en el rango [0,1) y no estar vacío",
                    "Dato inválido",
                    JOptionPane.ERROR_MESSAGE);
                return; // Sale del método si hay un número inválido
            }

            int ventasDemanda = CalentadoresModelo.ventasPara(r); // Convierte el número aleatorio en demanda de ventas usando el modelo
            int falt = ventasDemanda > inventario ? 1 : 0; // Determina si hay faltantes: 1 si demanda > inventario, 0 si no

            if(falt==1){ // Si hay faltantes en esta semana
                faltantes++; // Incrementa el contador de semanas con faltantes
                if(!first) faltSem.append(", "); // Agrega coma si no es la primera semana con faltantes
                faltSem.append(i+1); // Agrega el número de semana a la lista (i+1 porque i empieza en 0)
                first=false; // Marca que ya no es la primera semana
            }
            totalVentas += ventasDemanda; // Suma la demanda de esta semana al total

            modeloSim.addRow(new Object[]{ // Agrega una fila a la tabla de simulación con los resultados de la semana
                i+1, // Número de semana (i+1 porque i empieza en 0)
                UtilFormatoCalent.f2(r), // Número aleatorio ingresado, formateado a 2 decimales
                ventasDemanda, // Demanda de ventas calculada
                falt // Indicador de faltantes (1 o 0)
            });
        }

        // Fila de totales
        int semanas = modeloInput.getRowCount(); // Obtiene el número total de semanas procesadas
        modeloSim.addRow(new Object[]{ // Agrega fila con totales al final de la tabla
            "Total", // Etiqueta en la primera columna
            "", // Columna de números aleatorios vacía
            totalVentas, // Total de ventas acumuladas
            faltantes // Total de semanas con faltantes
        });

        // Fila de promedio
        double promedio = totalVentas / (double)semanas; // Calcula el promedio de ventas por semana
        modeloSim.addRow(new Object[]{ // Agrega fila con el promedio
            "B) ventas promedio con simulación", // Etiqueta descriptiva
            "", // Columna de números aleatorios vacía
            UtilFormatoCalent.f2(promedio), // Promedio formateado a 2 decimales
            "" // Columna de faltantes vacía
        });

        double esperado = CalentadoresModelo.esperado(); // Obtiene el valor esperado teórico del modelo
        double porcentajeFaltantes = (faltantes/(double)semanas) * 100; // Calcula el porcentaje de semanas con faltantes

        StringBuilder sb = new StringBuilder(); // Builder para construir el texto del resumen
        sb.append("RESULTADOS DE LA SIMULACIÓN (").append(semanas).append(" semanas, inventario: ").append(inventario).append(" calentadores/semana):\n\n"); // Encabezado con parámetros de la simulación
        sb.append("• Semanas con faltantes: ").append(faltantes).append(" de ").append(semanas); // Número de semanas con faltantes
        sb.append(" (").append(UtilFormatoCalent.f2(porcentajeFaltantes)).append("%)\n"); // Porcentaje de semanas con faltantes
        if(faltantes > 0) { // Si hubo semanas con faltantes
            sb.append("• Semanas específicas con faltantes: ").append(faltSem).append("\n"); // Lista las semanas específicas
        }
        sb.append("• Total de calentadores demandados: ").append(totalVentas).append(" unidades\n"); // Total de demanda
        sb.append("• Promedio de ventas simulado: ").append(UtilFormatoCalent.f2(promedio)).append(" calentadores/semana\n"); // Promedio simulado
        sb.append("• Valor esperado teórico: ").append(UtilFormatoCalent.f2(esperado)).append(" calentadores/semana\n"); // Valor teórico esperado
        sb.append("• Diferencia simulado vs teórico: ").append(UtilFormatoCalent.f2(Math.abs(promedio - esperado))).append("\n\n"); // Diferencia entre simulado y teórico
        sb.append("INTERPRETACIÓN:\n"); // Sección de interpretación
        sb.append("Las filas marcadas en rojo indican semanas donde la demanda superó el inventario disponible.\n"); // Explicación del código de colores
        sb.append("Los resultados dependen de los números aleatorios ingresados manualmente."); // Nota sobre la dependencia de los números ingresados

        JOptionPane.showMessageDialog(this, // Muestra diálogo de confirmación de finalización
            "Simulación completada exitosamente!\n" +
            "",
            "Cálculo terminado",
            JOptionPane.INFORMATION_MESSAGE);
    }
}

