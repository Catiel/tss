package actividad_5.calentadores; // Define el paquete donde se encuentra esta clase

import javax.swing.*; // Importa todas las clases de Swing para componentes de interfaz gráfica
import javax.swing.table.DefaultTableModel; // Importa el modelo de datos para tablas
import java.awt.*; // Importa clases de AWT para layouts y colores
import java.awt.event.ActionEvent; // Importa la clase para manejar eventos de acción

/** Panel que simula semanas con números aleatorios generados internamente. */
public class PanelCalentadoresAleatorio extends JPanel { // Clase que extiende JPanel para crear un panel de simulación aleatoria
    private final JSpinner spSemanas; // número de semanas - Control spinner para seleccionar el número de semanas a simular
    private final JSpinner spInventario; // inventario fijo por semana - Control spinner para establecer el inventario por semana
    private final JButton btnSimular; // ejecuta simulación - Botón que inicia el proceso de simulación

    private final DefaultTableModel modeloRangos; // tabla de distribución con rangos - Modelo para la tabla que muestra los rangos de probabilidad
    private final DefaultTableModel modeloSim;    // tabla de simulación - Modelo para la tabla que muestra los resultados de simulación

    public PanelCalentadoresAleatorio(){ // Constructor que inicializa todo el panel de simulación aleatoria
        setLayout(new BorderLayout(8,8)); // Establece el layout BorderLayout con espaciado de 8 píxeles horizontal y vertical
        EstilosUI.aplicarEstiloPanel(this); // Aplica el estilo visual predefinido al panel
        JLabel titulo = new JLabel("Simulación aleatoria de ventas de calentadores"); // Crea la etiqueta del título
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
        btnSimular = new JButton("Ejecutar Simulación"); // Crea el botón para ejecutar la simulación
        EstilosUI.aplicarEstiloBoton(btnSimular); // Aplica estilo predefinido al botón
        controles.add(btnSimular); // Agrega el botón al panel de controles
        add(controles, BorderLayout.BEFORE_FIRST_LINE); // Agrega el panel de controles en la parte superior (antes de la primera línea)

        // Panel principal dividido
        JPanel panelPrincipal = new JPanel(new BorderLayout(8,8)); // Crea el panel principal con BorderLayout y espaciado
        EstilosUI.aplicarEstiloPanel(panelPrincipal); // Aplica estilo al panel principal

        // Panel izquierdo: tabla de rangos (referencia)
        modeloRangos = new DefaultTableModel(new Object[]{"Prob","Dist.Acum","Inicio","Fin","Ventas"},0){@Override public boolean isCellEditable(int r,int c){return false;}}; // Crea modelo de tabla no editable con columnas para probabilidad, distribución acumulada, inicio, fin y ventas
        llenarRangos(); // Llama al método para poblar la tabla de rangos con datos del modelo
        JTable tRangos = new JTable(modeloRangos); // Crea la tabla usando el modelo de rangos
        EstilosUI.aplicarEstiloTabla(tRangos); // Aplica estilo predefinido a la tabla
        tRangos.getTableHeader().setBackground(new Color(200, 220, 255)); // Establece color azul claro para el encabezado de la tabla
        tRangos.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Desactiva el redimensionado automático de columnas
        for(int i = 0; i < 5; i++) { // Bucle para configurar el ancho de cada una de las 5 columnas
            tRangos.getColumnModel().getColumn(i).setPreferredWidth(65); // Establece ancho preferido de 65 píxeles para cada columna
        }
        JScrollPane spRangos = new JScrollPane(tRangos); // Envuelve la tabla en un scroll pane para permitir desplazamiento
        spRangos.setBorder(BorderFactory.createTitledBorder("Distribución de probabilidad (referencia)")); // Agrega borde con título explicativo
        spRangos.setPreferredSize(new Dimension(350, 300)); // Establece tamaño preferido del scroll pane
        panelPrincipal.add(spRangos, BorderLayout.WEST); // Agrega el scroll pane con la tabla de rangos al lado izquierdo

        // Panel derecho: tabla de simulación
        modeloSim = new DefaultTableModel(new Object[]{ // Crea modelo para la tabla de simulación con 4 columnas
            "# de semana","numeros aleatorios","Ventas de calentador","Faltantes"},0){
            @Override public boolean isCellEditable(int r,int c){return false;} // Override para hacer la tabla no editable
            @Override public Class<?> getColumnClass(int c){ // Override para especificar el tipo de datos de cada columna
                if(c == 0 || c == 2 || c == 3) return Integer.class; // Columnas 0, 2, 3 son de tipo Integer (números enteros)
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
        tablaSim.getColumnModel().getColumn(1).setPreferredWidth(110); // Establece ancho de 110 píxeles para columna "números aleatorios"
        tablaSim.getColumnModel().getColumn(2).setPreferredWidth(120); // Establece ancho de 120 píxeles para columna "Ventas de calentador"
        tablaSim.getColumnModel().getColumn(3).setPreferredWidth(80); // Establece ancho de 80 píxeles para columna "Faltantes"

        JScrollPane spSim = new JScrollPane(tablaSim); // Envuelve la tabla de simulación en un scroll pane
        spSim.setBorder(BorderFactory.createTitledBorder("Resultados de la simulación")); // Agrega borde con título
        panelPrincipal.add(spSim, BorderLayout.CENTER); // Agrega el scroll pane al centro del panel principal

        add(panelPrincipal, BorderLayout.CENTER); // Agrega el panel principal al centro del panel general

        // Área de resumen
        btnSimular.addActionListener(this::simular); // Asocia el método simular como listener del evento de clic del botón

        // Ejecutar simulación inicial
        simular(null); // Ejecuta una simulación inicial al crear el panel
    }

    private void llenarRangos(){ // Método que llena la tabla de rangos con los datos del modelo de probabilidad
        modeloRangos.setRowCount(0); // Limpia todas las filas existentes en el modelo
        double inicio=0, acum=0; // Inicializa variables para calcular rangos: inicio del rango y acumulado de probabilidades
        for(int i=0;i<CalentadoresModelo.VENTAS.length;i++){ // Itera sobre todos los valores de venta del modelo
            double p=CalentadoresModelo.PROBS[i]; // Obtiene la probabilidad del valor de venta actual
            acum+=p; // Suma la probabilidad al acumulado
            if(i==CalentadoresModelo.VENTAS.length-1)acum=1.0; // Asegura que el último valor acumulado sea exactamente 1.0
            double fin=acum; // El final del rango actual es el acumulado actual
            modeloRangos.addRow(new Object[]{ // Agrega una fila a la tabla con los datos calculados
                UtilFormatoCalent.f2(p), // Probabilidad formateada a 2 decimales
                UtilFormatoCalent.f2(acum), // Probabilidad acumulada formateada a 2 decimales
                UtilFormatoCalent.f2(inicio), // Inicio del rango formateado a 2 decimales
                UtilFormatoCalent.f2(fin), // Fin del rango formateado a 2 decimales
                CalentadoresModelo.VENTAS[i] // Valor de ventas correspondiente
            });
            inicio=fin; // El inicio del siguiente rango es el fin del rango actual
        }
    }

    private void simular(ActionEvent e){ // Método que ejecuta la simulación de ventas aleatorias
        int semanas = (int) spSemanas.getValue(); // Obtiene el número de semanas seleccionado por el usuario
        int inventario = (int) spInventario.getValue(); // Obtiene el inventario por semana seleccionado por el usuario
        modeloSim.setRowCount(0); // Limpia todas las filas existentes en la tabla de simulación

        int totalVentas=0; // Acumulador para el total de ventas simuladas
        int faltantes=0; // Contador de semanas con faltantes (demanda > inventario)
        StringBuilder faltSemanas = new StringBuilder(); // Builder para construir la lista de semanas con faltantes
        boolean first=true; // Flag para controlar la puntuación en la lista de semanas con faltantes

        for(int s=1;s<=semanas;s++){ // Bucle que simula cada semana desde 1 hasta el número especificado
            double r = Math.random(); // Genera un número aleatorio entre 0 y 1
            int ventasDemanda = CalentadoresModelo.ventasPara(r); // Convierte el número aleatorio en demanda de ventas usando el modelo
            int falt = ventasDemanda > inventario ? 1 : 0; // Determina si hay faltantes: 1 si demanda > inventario, 0 si no

            if(falt==1){ // Si hay faltantes en esta semana
                faltantes++; // Incrementa el contador de semanas con faltantes
                if(!first) faltSemanas.append(", "); // Agrega coma si no es la primera semana con faltantes
                faltSemanas.append(s); // Agrega el número de semana a la lista
                first=false; // Marca que ya no es la primera semana
            }
            totalVentas += ventasDemanda; // Suma la demanda de esta semana al total

            modeloSim.addRow(new Object[]{ // Agrega una fila a la tabla de simulación con los resultados de la semana
                s, // Número de semana
                UtilFormatoCalent.f2(r), // Número aleatorio generado, formateado a 2 decimales
                ventasDemanda, // Demanda de ventas calculada
                falt // Indicador de faltantes (1 o 0)
            });
        }

        // Fila de totales
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
            sb.append("• Semanas específicas con faltantes: ").append(faltSemanas).append("\n"); // Lista las semanas específicas
        }
        sb.append("• Total de calentadores demandados: ").append(totalVentas).append(" unidades\n"); // Total de demanda
        sb.append("• Promedio de ventas simulado: ").append(UtilFormatoCalent.f2(promedio)).append(" calentadores/semana\n"); // Promedio simulado
        sb.append("• Valor esperado teórico: ").append(UtilFormatoCalent.f2(esperado)).append(" calentadores/semana\n"); // Valor teórico esperado
        sb.append("• Diferencia simulado vs teórico: ").append(UtilFormatoCalent.f2(Math.abs(promedio - esperado))).append("\n\n"); // Diferencia entre simulado y teórico
        sb.append("INTERPRETACIÓN:\n"); // Sección de interpretación
        sb.append("Las filas marcadas en rojo indican semanas donde la demanda superó el inventario disponible.\n"); // Explicación del código de colores
        sb.append("Con más semanas de simulación, el promedio converge hacia el valor esperado teórico."); // Explicación sobre convergencia

    }
}