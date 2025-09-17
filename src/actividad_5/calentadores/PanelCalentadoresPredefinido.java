package actividad_5.calentadores; // Define el paquete donde se encuentra esta clase

import javax.swing.*; // Importa todas las clases de Swing para componentes de interfaz gráfica
import javax.swing.table.DefaultTableModel; // Importa el modelo de datos para tablas
import java.awt.*; // Importa clases de AWT para layouts y colores

/** Panel con el ejemplo de 20 semanas usando números aleatorios proporcionados. */
public class PanelCalentadoresPredefinido extends JPanel { // Clase que extiende JPanel para crear un panel de simulación con datos predefinidos
    private final DefaultTableModel modeloFrecuencia; // tabla original (ventas, frecuencia) - Modelo para la tabla que muestra las frecuencias observadas de ventas
    private final DefaultTableModel modeloProb;       // tabla con probabilidades - Modelo para la tabla que muestra las probabilidades empíricas calculadas
    private final DefaultTableModel modeloRangos;     // tabla con acumulada y rangos - Modelo para la tabla que muestra distribución acumulada y rangos de probabilidad
    private final DefaultTableModel modeloSim;        // simulación 20 semanas - Modelo para la tabla que muestra los resultados de la simulación

    private static final int[] FRECUENCIAS = {6,5,9,12,8,7,3}; // Array con las frecuencias observadas para cada valor de venta (4,5,6,7,8,9,10 calentadores respectivamente)
    private static final double[] RAND = {0.10,0.24,0.03,0.32,0.23,0.59,0.95,0.34,0.34,0.51,0.08,0.48,0.66,0.97,0.03,0.96,0.46,0.74,0.77,0.44}; // Array con 20 números aleatorios predefinidos para la simulación

    public PanelCalentadoresPredefinido(){ // Constructor que inicializa todo el panel de simulación predefinida
        setLayout(new BorderLayout(8,8)); // Establece el layout BorderLayout con espaciado de 8 píxeles horizontal y vertical
        EstilosUI.aplicarEstiloPanel(this); // Aplica el estilo visual predefinido al panel
        JLabel titulo = new JLabel("Simulación de ventas de calentadores (ejemplo 20 semanas - inventario fijo 8)"); // Crea la etiqueta del título
        EstilosUI.aplicarEstiloTitulo(titulo); // Aplica el estilo específico para títulos
        add(titulo, BorderLayout.NORTH); // Agrega el título en la parte superior del panel

        // Panel principal dividido
        JPanel panelPrincipal = new JPanel(new BorderLayout(8,8)); // Crea el panel principal con BorderLayout y espaciado
        EstilosUI.aplicarEstiloPanel(panelPrincipal); // Aplica estilo al panel principal

        // Panel izquierdo con distribuciones
        JPanel panelIzq = new JPanel(new GridLayout(3,1,5,5)); // Crea panel izquierdo con layout de rejilla 3x1 (3 filas, 1 columna) con espaciado de 5 píxeles
        EstilosUI.aplicarEstiloPanel(panelIzq); // Aplica estilo al panel izquierdo

        // 1) Frecuencias
        modeloFrecuencia = new DefaultTableModel(new Object[]{"Ventas/semana","# semanas que se vendió esta cantidad"},0){@Override public boolean isCellEditable(int r,int c){return false;}}; // Crea modelo de tabla no editable para mostrar frecuencias observadas
        llenarFrecuencias(); // Llama al método para poblar la tabla de frecuencias con datos históricos
        JTable tFreq = new JTable(modeloFrecuencia); // Crea la tabla de frecuencias usando el modelo
        EstilosUI.aplicarEstiloTabla(tFreq); // Aplica estilo predefinido a la tabla de frecuencias
        tFreq.getTableHeader().setBackground(new Color(255, 240, 200)); // Establece color naranja claro para el encabezado de la tabla de frecuencias
        tFreq.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Desactiva el redimensionado automático de columnas
        tFreq.getColumnModel().getColumn(0).setPreferredWidth(90); // Establece ancho de 90 píxeles para columna "Ventas/semana"
        tFreq.getColumnModel().getColumn(1).setPreferredWidth(180); // Establece ancho de 180 píxeles para columna "# semanas que se vendió esta cantidad"
        JScrollPane spFreq = new JScrollPane(tFreq); // Envuelve la tabla de frecuencias en un scroll pane
        spFreq.setBorder(BorderFactory.createTitledBorder("1. Frecuencias observadas (50 semanas)")); // Agrega borde con título explicativo
        panelIzq.add(spFreq); // Agrega el scroll pane de frecuencias al panel izquierdo

        // 2) Probabilidades
        modeloProb = new DefaultTableModel(new Object[]{"Ventas","# semanas","Probabilidad"},0){@Override public boolean isCellEditable(int r,int c){return false;}}; // Crea modelo de tabla no editable para mostrar probabilidades empíricas
        llenarProbabilidades(); // Llama al método para poblar la tabla de probabilidades calculadas a partir de frecuencias
        JTable tProb = new JTable(modeloProb); // Crea la tabla de probabilidades usando el modelo
        EstilosUI.aplicarEstiloTabla(tProb); // Aplica estilo predefinido a la tabla de probabilidades
        tProb.getTableHeader().setBackground(new Color(200, 255, 200)); // Establece color verde claro para el encabezado de la tabla de probabilidades
        tProb.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Desactiva redimensionado automático de columnas
        tProb.getColumnModel().getColumn(0).setPreferredWidth(60); // Establece ancho de 60 píxeles para columna "Ventas"
        tProb.getColumnModel().getColumn(1).setPreferredWidth(80); // Establece ancho de 80 píxeles para columna "# semanas"
        tProb.getColumnModel().getColumn(2).setPreferredWidth(90); // Establece ancho de 90 píxeles para columna "Probabilidad"
        JScrollPane spProb = new JScrollPane(tProb); // Envuelve la tabla de probabilidades en un scroll pane
        spProb.setBorder(BorderFactory.createTitledBorder("2. Probabilidades empíricas")); // Agrega borde con título
        panelIzq.add(spProb); // Agrega el scroll pane de probabilidades al panel izquierdo

        // 3) Rangos
        modeloRangos = new DefaultTableModel(new Object[]{"Prob","Dist.Acum","Inicio rango","Fin rango","Ventas"},0){@Override public boolean isCellEditable(int r,int c){return false;}}; // Crea modelo de tabla no editable para mostrar rangos de probabilidad acumulada
        llenarRangos(); // Llama al método para poblar la tabla de rangos con distribución acumulada
        JTable tRangos = new JTable(modeloRangos); // Crea la tabla de rangos usando el modelo
        EstilosUI.aplicarEstiloTabla(tRangos); // Aplica estilo predefinido a la tabla de rangos
        tRangos.getTableHeader().setBackground(new Color(200, 220, 255)); // Establece color azul claro para el encabezado de la tabla de rangos
        tRangos.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Desactiva redimensionado automático de columnas
        for(int i = 0; i < 5; i++) { // Bucle para configurar el ancho de cada una de las 5 columnas
            tRangos.getColumnModel().getColumn(i).setPreferredWidth(65); // Establece ancho preferido de 65 píxeles para cada columna
        }
        JScrollPane spRangos = new JScrollPane(tRangos); // Envuelve la tabla de rangos en un scroll pane
        spRangos.setBorder(BorderFactory.createTitledBorder("3. Distribución acumulada y rangos")); // Agrega borde con título
        panelIzq.add(spRangos); // Agrega el scroll pane de rangos al panel izquierdo

        panelPrincipal.add(panelIzq, BorderLayout.WEST); // Agrega el panel izquierdo (con las 3 tablas) al lado oeste del panel principal

        // Tabla de simulación (lado derecho)
        modeloSim = new DefaultTableModel(new Object[]{ // Crea modelo para la tabla de simulación con 4 columnas
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
                if(row < getRowCount()-1){ // fila de datos (no totales) - Verifica si es una fila de datos normales (no la fila de totales)
                    Object falt = getValueAt(row,3); // Obtiene el valor de la columna "Faltantes"
                    if(Integer.valueOf(1).equals(falt)) { // Si hay faltantes (valor = 1)
                        c.setBackground(new Color(255, 200, 200)); // rojo claro para faltantes - Establece fondo rojo claro
                    } else {
                        c.setBackground(Color.white); // Establece fondo blanco para filas sin faltantes
                    }
                } else { // fila de totales
                    c.setBackground(new Color(220, 220, 220)); // Establece fondo gris para fila de totales/resumen
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

        simular(); // Llama al método para ejecutar la simulación con los números aleatorios predefinidos
        JScrollPane spSim = new JScrollPane(tablaSim); // Envuelve la tabla de simulación en un scroll pane
        spSim.setBorder(BorderFactory.createTitledBorder("4. Simulación (inventario constante 8 calentadores/semana)")); // Agrega borde con título
        spSim.setPreferredSize(new Dimension(600, 400)); // Establece tamaño preferido del scroll pane
        panelPrincipal.add(spSim, BorderLayout.CENTER); // Agrega el scroll pane de simulación al centro del panel principal

        add(panelPrincipal, BorderLayout.CENTER); // Agrega el panel principal al centro del panel general

        // Resumen de resultados
        JTextArea resumen = new JTextArea(); // Crea el área de texto para mostrar el resumen de análisis
        resumen.setEditable(false); // Hace que el área de texto sea de solo lectura
        resumen.setWrapStyleWord(true); // Ajusta las líneas por palabras completas, no por caracteres
        resumen.setLineWrap(true); // Activa el ajuste automático de líneas
        resumen.setBackground(getBackground()); // Establece el mismo color de fondo que el panel padre
        resumen.setBorder(BorderFactory.createTitledBorder("Resultados del análisis")); // Agrega borde con título
        resumen.setFont(new Font("Arial", Font.PLAIN, 12)); // Establece fuente Arial, normal, tamaño 12

        int totalVentas = totalVentas(); // Calcula el total de ventas de la simulación
        int faltantes = contarFaltantes(); // Cuenta el número de semanas con faltantes
        String semanasFaltantes = listarSemanasFaltantes(); // Obtiene la lista de semanas específicas con faltantes
        double promedioSim = promedioSimulado(); // Calcula el promedio de ventas simulado
        double esperado = CalentadoresModelo.esperado(); // Obtiene el valor esperado teórico del modelo

        StringBuilder sb = new StringBuilder(); // Builder para construir el texto del resumen

        resumen.setText(sb.toString()); // Establece el texto completo del resumen en el área de texto
        add(resumen, BorderLayout.SOUTH); // Agrega el área de resumen en la parte inferior del panel
    }

    private void llenarFrecuencias(){ // Método que llena la tabla de frecuencias observadas con datos históricos
        modeloFrecuencia.setRowCount(0); // Limpia todas las filas existentes en el modelo de frecuencias
        for(int i=0;i<CalentadoresModelo.VENTAS.length;i++){ // Itera sobre todos los valores de venta del modelo
            modeloFrecuencia.addRow(new Object[]{CalentadoresModelo.VENTAS[i], FRECUENCIAS[i]}); // Agrega fila con valor de venta y su frecuencia observada
        }
        modeloFrecuencia.addRow(new Object[]{"TOTAL",50}); // Agrega fila de total (50 semanas observadas)
    }

    private void llenarProbabilidades(){ // Método que llena la tabla de probabilidades empíricas calculadas a partir de frecuencias
        modeloProb.setRowCount(0); // Limpia todas las filas existentes en el modelo de probabilidades
        for(int i=0;i<CalentadoresModelo.VENTAS.length;i++){ // Itera sobre todos los valores de venta del modelo
            modeloProb.addRow(new Object[]{CalentadoresModelo.VENTAS[i], FRECUENCIAS[i], UtilFormatoCalent.f2(CalentadoresModelo.PROBS[i])}); // Agrega fila con valor, frecuencia y probabilidad calculada
        }
        modeloProb.addRow(new Object[]{"TOTAL",50,UtilFormatoCalent.f2(1.0)}); // Agrega fila de total con probabilidad 1.0
    }

    private void llenarRangos(){ // Método que llena la tabla de rangos con distribución de probabilidad acumulada
        modeloRangos.setRowCount(0); // Limpia todas las filas existentes en el modelo de rangos
        double inicio=0, acum=0; // Inicializa variables para calcular rangos: inicio del rango y acumulado de probabilidades
        for(int i=0;i<CalentadoresModelo.VENTAS.length;i++){ // Itera sobre todos los valores de venta del modelo
            double p = CalentadoresModelo.PROBS[i]; // Obtiene la probabilidad del valor de venta actual
            acum += p; // Suma la probabilidad al acumulado
            if(i==CalentadoresModelo.VENTAS.length-1) acum = 1.0; // Asegura que el último valor acumulado sea exactamente 1.0
            double fin = acum; // El final del rango actual es el acumulado actual
            modeloRangos.addRow(new Object[]{ // Agrega una fila a la tabla de rangos con los datos calculados
                UtilFormatoCalent.f2(p), // Probabilidad individual formateada a 2 decimales
                UtilFormatoCalent.f2(acum), // Probabilidad acumulada formateada a 2 decimales
                UtilFormatoCalent.f2(inicio), // Inicio del rango formateado a 2 decimales
                UtilFormatoCalent.f2(fin), // Fin del rango formateado a 2 decimales
                CalentadoresModelo.VENTAS[i] // Valor de ventas correspondiente
            });
            inicio = fin; // El inicio del siguiente rango es el fin del rango actual
        }
    }

    private void simular(){ // Método que ejecuta la simulación usando los números aleatorios predefinidos
        modeloSim.setRowCount(0); // Limpia todas las filas existentes en la tabla de simulación
        int totalFaltantes = 0; // Contador para el total de semanas con faltantes
        int totalVentasAcum = 0; // Acumulador para el total de ventas

        for(int s=0;s<RAND.length;s++){ // Bucle que procesa cada uno de los 20 números aleatorios predefinidos
            double r = RAND[s]; // Obtiene el número aleatorio de la posición actual
            int ventasDemanda = CalentadoresModelo.ventasPara(r); // Convierte el número aleatorio en demanda de ventas usando el modelo
            int falta = (ventasDemanda > CalentadoresModelo.INVENTARIO_FIJO) ? 1 : 0; // Determina si hay faltantes: 1 si demanda > inventario fijo, 0 si no

            if(falta == 1) totalFaltantes++; // Incrementa el contador si hay faltantes en esta semana
            totalVentasAcum += ventasDemanda; // Suma la demanda de esta semana al total acumulado

            modeloSim.addRow(new Object[]{ // Agrega una fila a la tabla de simulación con los resultados de la semana
                s+1, // Número de semana (s+1 porque s empieza en 0)
                UtilFormatoCalent.f2(r), // Número aleatorio usado, formateado a 2 decimales
                ventasDemanda, // Demanda de ventas calculada
                falta // Indicador de faltantes (1 o 0)
            });
        }

        // Fila de totales
        modeloSim.addRow(new Object[]{ // Agrega fila con totales al final de la tabla
            "Total", // Etiqueta en la primera columna
            "", // Columna de números aleatorios vacía
            totalVentasAcum, // Total de ventas acumuladas
            totalFaltantes // Total de semanas con faltantes
        });

        // Fila de promedio
        double promedio = totalVentasAcum / (double)RAND.length; // Calcula el promedio de ventas dividiendo total entre número de semanas
        modeloSim.addRow(new Object[]{ // Agrega fila con el promedio
            "B) ventas promedio con simulación", // Etiqueta descriptiva
            "", // Columna de números aleatorios vacía
            UtilFormatoCalent.f2(promedio), // Promedio formateado a 2 decimales
            "" // Columna de faltantes vacía
        });
    }

    private int totalVentas(){ // Método que calcula el total de ventas de toda la simulación
        int t=0; // Inicializa acumulador de total
        for(int i=0;i<RAND.length;i++){ // Itera sobre todos los números aleatorios predefinidos
            t+= CalentadoresModelo.ventasPara(RAND[i]); // Suma la demanda calculada para cada número aleatorio
        }
        return t; // Retorna el total acumulado
    }

    private double promedioSimulado(){ // Método que calcula el promedio de ventas simulado
        return totalVentas() / (double) RAND.length; // Divide el total de ventas entre el número de semanas simuladas
    }

    private int contarFaltantes(){ // Método que cuenta el número de semanas con faltantes
        int c=0; // Inicializa contador
        for(double r: RAND) { // Itera sobre cada número aleatorio predefinido
            if(CalentadoresModelo.ventasPara(r) > CalentadoresModelo.INVENTARIO_FIJO) c++; // Incrementa contador si la demanda supera el inventario
        }
        return c; // Retorna el total de semanas con faltantes
    }

    private String listarSemanasFaltantes(){ // Método que genera una lista con los números de semanas que tuvieron faltantes
        StringBuilder sb=new StringBuilder(); // Builder para construir la lista
        boolean first=true; // Flag para controlar la puntuación en la lista
        for(int i=0;i<RAND.length;i++){ // Itera sobre todos los números aleatorios con su índice
            if(CalentadoresModelo.ventasPara(RAND[i]) > CalentadoresModelo.INVENTARIO_FIJO){ // Si hay faltantes en esta semana
                if(!first) sb.append(", "); // Agrega coma si no es la primera semana en la lista
                sb.append(i+1); // Agrega el número de semana (i+1 porque i empieza en 0)
                first=false; // Marca que ya no es la primera semana
            }
        }
        return sb.toString(); // Retorna la lista como string
    }
}