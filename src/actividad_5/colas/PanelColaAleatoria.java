package actividad_5.colas; // Define el paquete donde se encuentra esta clase

import javax.swing.*; // Importa todas las clases de Swing para componentes de interfaz gráfica
import javax.swing.table.DefaultTableModel; // Importa el modelo de datos para tablas
import java.awt.*; // Importa clases de AWT para layouts y colores
import java.awt.event.ActionEvent; // Importa la clase para manejar eventos de acción

/** Panel que simula la cola con números aleatorios generados automáticamente. */
public class PanelColaAleatoria extends JPanel { // Clase que extiende JPanel para crear un panel de simulación aleatoria de colas bancarias
    private final JSpinner spClientes; // Control spinner para seleccionar el número de clientes a simular en la cola
    private final JButton btnSimular; // Botón que inicia el proceso de simulación aleatoria de la cola
    private final DefaultTableModel modeloServicio; // Modelo para la tabla que muestra los rangos de probabilidad de tiempos de servicio
    private final DefaultTableModel modeloLlegadas; // Modelo para la tabla que muestra los rangos de probabilidad de tiempos entre llegadas
    private final DefaultTableModel modeloSim; // Modelo para la tabla que muestra los resultados detallados de la simulación de cola
    private final JTextArea resumen; // Área de texto para mostrar el análisis de resultados y evaluación de objetivos del banco

    public PanelColaAleatoria(){ // Constructor que inicializa todo el panel de simulación aleatoria de colas
        setLayout(new BorderLayout(8,8)); // Establece el layout BorderLayout con espaciado de 8 píxeles horizontal y vertical
        EstilosUI.aplicarEstiloPanel(this); // Aplica el estilo visual predefinido al panel

        JLabel titulo = new JLabel("Simulación aleatoria Cola Banco BNB"); // Crea la etiqueta del título específica para el Banco BNB
        EstilosUI.aplicarEstiloTitulo(titulo); // Aplica el estilo específico para títulos
        add(titulo, BorderLayout.NORTH); // Agrega el título en la parte superior del panel

        // Controles superiores
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Crea panel de controles con layout de flujo alineado a la izquierda
        EstilosUI.aplicarEstiloPanel(controles); // Aplica estilo al panel de controles
        controles.add(new JLabel("Número de clientes:")); // Agrega etiqueta para el control del número de clientes
        spClientes = new JSpinner(new SpinnerNumberModel(20,1,200,1)); // Inicializa spinner con valor inicial 20, mínimo 1, máximo 200, incremento 1
        controles.add(spClientes); // Agrega el spinner de clientes al panel de controles
        btnSimular = new JButton("Ejecutar Simulación"); // Crea el botón para ejecutar la simulación aleatoria
        EstilosUI.aplicarEstiloBoton(btnSimular); // Aplica estilo predefinido al botón
        controles.add(btnSimular); // Agrega el botón al panel de controles
        add(controles, BorderLayout.BEFORE_FIRST_LINE); // Agrega el panel de controles en la parte superior

        // Panel principal dividido
        JPanel panelPrincipal = new JPanel(new BorderLayout(8,8)); // Crea el panel principal con BorderLayout y espaciado
        EstilosUI.aplicarEstiloPanel(panelPrincipal); // Aplica estilo al panel principal

        // Panel izquierdo: tablas de distribución (referencia)
        JPanel panelIzq = new JPanel(new GridLayout(2,1,5,5)); // Crea panel izquierdo con layout de rejilla 2x1 (2 filas, 1 columna) con espaciado de 5 píxeles
        EstilosUI.aplicarEstiloPanel(panelIzq); // Aplica estilo al panel izquierdo

        // 1) Distribución tiempo de servicio
        modeloServicio = new DefaultTableModel(new Object[]{"Probabilidad","Distribución acumulada","Rango de Nros aleatorios","Tiempo de servicio"},0){ // Crea modelo de tabla no editable para mostrar distribución de tiempos de servicio
            @Override public boolean isCellEditable(int r,int c){return false;} // Override para hacer toda la tabla de solo lectura
        };
        llenarDistribucionServicio(); // Llama al método para poblar la tabla de distribución de tiempos de servicio
        JTable tServ = new JTable(modeloServicio); // Crea la tabla de tiempos de servicio usando el modelo
        EstilosUI.aplicarEstiloTabla(tServ); // Aplica estilo predefinido a la tabla de servicio
        tServ.getTableHeader().setBackground(new Color(255, 240, 200)); // Establece color naranja claro para el encabezado de la tabla de servicio
        tServ.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Desactiva el redimensionado automático de columnas
        tServ.getColumnModel().getColumn(0).setPreferredWidth(80); // Establece ancho de 80 píxeles para columna "Probabilidad"
        tServ.getColumnModel().getColumn(1).setPreferredWidth(100); // Establece ancho de 100 píxeles para columna "Distribución acumulada"
        tServ.getColumnModel().getColumn(2).setPreferredWidth(120); // Establece ancho de 120 píxeles para columna "Rango de Nros aleatorios"
        tServ.getColumnModel().getColumn(3).setPreferredWidth(90); // Establece ancho de 90 píxeles para columna "Tiempo de servicio"
        JScrollPane spServ = new JScrollPane(tServ); // Envuelve la tabla de servicio en un scroll pane
        spServ.setBorder(BorderFactory.createTitledBorder("Tiempo de servicio (referencia)")); // Agrega borde con título explicativo
        spServ.setPreferredSize(new Dimension(400, 180)); // Establece tamaño preferido del scroll pane de servicio
        panelIzq.add(spServ); // Agrega el scroll pane de servicio a la parte superior del panel izquierdo

        // 2) Distribución tiempo entre llegadas
        modeloLlegadas = new DefaultTableModel(new Object[]{"Probabilidad","Distribución acumulada","Rango de Nros aleatorios","Tiempo entre llegadas"},0){ // Crea modelo de tabla no editable para mostrar distribución de tiempos entre llegadas
            @Override public boolean isCellEditable(int r,int c){return false;} // Override para hacer toda la tabla de solo lectura
        };
        llenarDistribucionLlegadas(); // Llama al método para poblar la tabla de distribución de tiempos entre llegadas
        JTable tLleg = new JTable(modeloLlegadas); // Crea la tabla de tiempos entre llegadas usando el modelo
        EstilosUI.aplicarEstiloTabla(tLleg); // Aplica estilo predefinido a la tabla de llegadas
        tLleg.getTableHeader().setBackground(new Color(200, 255, 200)); // Establece color verde claro para el encabezado de la tabla de llegadas
        tLleg.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Desactiva redimensionado automático de columnas
        tLleg.getColumnModel().getColumn(0).setPreferredWidth(80); // Establece ancho de 80 píxeles para columna "Probabilidad"
        tLleg.getColumnModel().getColumn(1).setPreferredWidth(100); // Establece ancho de 100 píxeles para columna "Distribución acumulada"
        tLleg.getColumnModel().getColumn(2).setPreferredWidth(120); // Establece ancho de 120 píxeles para columna "Rango de Nros aleatorios"
        tLleg.getColumnModel().getColumn(3).setPreferredWidth(110); // Establece ancho de 110 píxeles para columna "Tiempo entre llegadas"
        JScrollPane spLleg = new JScrollPane(tLleg); // Envuelve la tabla de llegadas en un scroll pane
        spLleg.setBorder(BorderFactory.createTitledBorder("Tiempo entre llegadas (referencia)")); // Agrega borde con título
        spLleg.setPreferredSize(new Dimension(400, 200)); // Establece tamaño preferido del scroll pane de llegadas
        panelIzq.add(spLleg); // Agrega el scroll pane de llegadas a la parte inferior del panel izquierdo

        panelPrincipal.add(panelIzq, BorderLayout.WEST); // Agrega el panel izquierdo (con las 2 tablas de distribución) al lado oeste del panel principal

        // Panel derecho: tabla de simulación
        modeloSim = new DefaultTableModel(new Object[]{ // Crea modelo para la tabla de simulación con 10 columnas detalladas para análisis de cola
            "# de cliente","# aleatorio","Intervalo entre llegadas","hora de llegadas","# aleatorio","t servicio","inicio del servicio","final del servicio","t espera","t ocioso"},0){
            @Override public boolean isCellEditable(int r,int c){return false;} // Override para hacer toda la tabla de solo lectura
            @Override public Class<?> getColumnClass(int c){ // Override para especificar el tipo de datos de cada columna
                if(c == 0 || c == 2 || c == 5 || c == 8 || c == 9) return Integer.class; // Columnas 0,2,5,8,9 son de tipo Integer (números enteros)
                return String.class; // Las demás columnas son de tipo String (números aleatorios y horas formateadas)
            }
        };

        JTable tablaSim = new JTable(modeloSim){ // Crea la tabla de simulación con comportamiento personalizado de renderizado
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer,int row,int column){ // Override para personalizar el renderizado de celdas
                Component c = super.prepareRenderer(renderer,row,column); // Obtiene el componente renderizado por defecto
                if(row < getRowCount()-2){ // fila de datos (no totales ni promedio) - Verifica si es una fila de datos normales
                    // Resaltar tiempos de espera > 0
                    Object espera = getValueAt(row, 8); // Obtiene el valor de la columna "t espera"
                    if(espera instanceof Integer && (Integer)espera > 0){ // Si hay tiempo de espera mayor a 0
                        c.setBackground(new Color(255, 255, 200)); // amarillo claro para espera - Establece fondo amarillo para resaltar clientes que esperaron
                    } else if(row % 2 == 0){ // Si es fila par sin espera
                        c.setBackground(Color.WHITE); // Establece fondo blanco para filas pares
                    } else { // Si es fila impar sin espera
                        c.setBackground(new Color(245, 245, 245)); // Establece fondo gris claro para filas impares (patrón zebra)
                    }
                } else { // fila de totales o promedio
                    c.setBackground(new Color(220, 220, 220)); // Establece fondo gris para filas de totales y promedios
                }
                return c; // Retorna el componente con el color de fondo apropiado
            }
        };

        EstilosUI.aplicarEstiloTabla(tablaSim); // Aplica estilo predefinido a la tabla de simulación
        tablaSim.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Desactiva redimensionado automático de columnas
        tablaSim.getColumnModel().getColumn(0).setPreferredWidth(60);  // # cliente - Establece ancho de 60 píxeles para columna de número de cliente
        tablaSim.getColumnModel().getColumn(1).setPreferredWidth(70);  // # aleatorio llegada - Establece ancho de 70 píxeles para número aleatorio de llegada
        tablaSim.getColumnModel().getColumn(2).setPreferredWidth(90);  // intervalo llegadas - Establece ancho de 90 píxeles para intervalo entre llegadas
        tablaSim.getColumnModel().getColumn(3).setPreferredWidth(85);  // hora llegadas - Establece ancho de 85 píxeles para hora de llegada
        tablaSim.getColumnModel().getColumn(4).setPreferredWidth(70);  // # aleatorio servicio - Establece ancho de 70 píxeles para número aleatorio de servicio
        tablaSim.getColumnModel().getColumn(5).setPreferredWidth(65);  // t servicio - Establece ancho de 65 píxeles para tiempo de servicio
        tablaSim.getColumnModel().getColumn(6).setPreferredWidth(90);  // inicio servicio - Establece ancho de 90 píxeles para inicio del servicio
        tablaSim.getColumnModel().getColumn(7).setPreferredWidth(85);  // final servicio - Establece ancho de 85 píxeles para final del servicio
        tablaSim.getColumnModel().getColumn(8).setPreferredWidth(60);  // t espera - Establece ancho de 60 píxeles para tiempo de espera
        tablaSim.getColumnModel().getColumn(9).setPreferredWidth(60);  // t ocioso - Establece ancho de 60 píxeles para tiempo ocioso del cajero

        JScrollPane spSim = new JScrollPane(tablaSim); // Envuelve la tabla de simulación en un scroll pane
        spSim.setBorder(BorderFactory.createTitledBorder("Resultados de la simulación")); // Agrega borde con título
        panelPrincipal.add(spSim, BorderLayout.CENTER); // Agrega el scroll pane de simulación al centro del panel principal

        add(panelPrincipal, BorderLayout.CENTER); // Agrega el panel principal al centro del panel general

        // Área de resumen
        resumen = new JTextArea(); // Crea el área de texto para mostrar el resumen de análisis
        resumen.setEditable(false); // Hace que el área de texto sea de solo lectura
        resumen.setBackground(getBackground()); // Establece el mismo color de fondo que el panel padre
        resumen.setLineWrap(true); // Activa el ajuste automático de líneas
        resumen.setWrapStyleWord(true); // Ajusta las líneas por palabras completas, no por caracteres
        resumen.setBorder(BorderFactory.createTitledBorder("Análisis de resultados")); // Agrega borde con título
        resumen.setFont(new Font("Arial", Font.PLAIN, 12)); // Establece fuente Arial, normal, tamaño 12
        resumen.setPreferredSize(new Dimension(0, 140)); // Establece altura preferida de 140 píxeles, ancho flexible
        add(resumen, BorderLayout.SOUTH); // Agrega el área de resumen en la parte inferior

        btnSimular.addActionListener(this::simular); // Asocia el método simular como listener del evento de clic del botón

        // Ejecutar simulación inicial
        simular(null); // Ejecuta una simulación inicial al crear el panel
    }

    private void llenarDistribucionServicio(){ // Método que llena la tabla de distribución de tiempos de servicio con datos del modelo
        modeloServicio.setRowCount(0); // Limpia todas las filas existentes en el modelo de servicio
        double[][] rangos = ColaBancoModelo.getRangosServicio(); // Obtiene los rangos de números aleatorios para tiempos de servicio del modelo

        for(int i = 0; i < ColaBancoModelo.SERVICIO_VALORES.length; i++){ // Itera sobre todos los valores de tiempo de servicio del modelo
            String rango = UtilFormatoColas.f2(rangos[i][0]) + " - " + UtilFormatoColas.f2(rangos[i][1]); // Construye string del rango formateado (inicio - fin)
            modeloServicio.addRow(new Object[]{ // Agrega una fila a la tabla de servicio con los datos calculados
                UtilFormatoColas.f2(ColaBancoModelo.SERVICIO_PROBS[i]), // Probabilidad individual formateada a 2 decimales
                UtilFormatoColas.f2(getSumaAcumulada(ColaBancoModelo.SERVICIO_PROBS, i)), // Probabilidad acumulada hasta este punto
                rango, // Rango de números aleatorios como string
                ColaBancoModelo.SERVICIO_VALORES[i] // Valor de tiempo de servicio correspondiente
            });
        }
    }

    private void llenarDistribucionLlegadas(){ // Método que llena la tabla de distribución de tiempos entre llegadas con datos del modelo
        modeloLlegadas.setRowCount(0); // Limpia todas las filas existentes en el modelo de llegadas
        double[][] rangos = ColaBancoModelo.getRangosLlegada(); // Obtiene los rangos de números aleatorios para tiempos entre llegadas del modelo

        for(int i = 0; i < ColaBancoModelo.LLEGADA_VALORES.length; i++){ // Itera sobre todos los valores de tiempo entre llegadas del modelo
            String rango = UtilFormatoColas.f2(rangos[i][0]) + " - " + UtilFormatoColas.f2(rangos[i][1]); // Construye string del rango formateado (inicio - fin)
            modeloLlegadas.addRow(new Object[]{ // Agrega una fila a la tabla de llegadas con los datos calculados
                UtilFormatoColas.f2(ColaBancoModelo.LLEGADA_PROBS[i]), // Probabilidad individual formateada a 2 decimales
                UtilFormatoColas.f2(getSumaAcumulada(ColaBancoModelo.LLEGADA_PROBS, i)), // Probabilidad acumulada hasta este punto
                rango, // Rango de números aleatorios como string
                ColaBancoModelo.LLEGADA_VALORES[i] // Valor de tiempo entre llegadas correspondiente
            });
        }
    }

    private double getSumaAcumulada(double[] probs, int hasta){ // Método utilitario que calcula la suma acumulada de probabilidades hasta un índice dado
        double suma = 0; // Inicializa acumulador de suma
        for(int i = 0; i <= hasta; i++){ // Itera desde 0 hasta el índice especificado (inclusive)
            suma += probs[i]; // Suma la probabilidad del índice actual al acumulado
        }
        return suma; // Retorna la suma acumulada
    }

    private void simular(ActionEvent e){ // Método que ejecuta la simulación de cola bancaria con números aleatorios generados automáticamente
        int clientes = (int) spClientes.getValue(); // Obtiene el número de clientes seleccionado por el usuario
        modeloSim.setRowCount(0); // Limpia todas las filas existentes en la tabla de simulación

        int horaLlegadaAcum = 0; // minutos desde 09:00 - Acumulador de tiempo desde el inicio (09:00) en minutos
        int finServicioAnterior = 0; // Tiempo de finalización del servicio del cliente anterior (para calcular disponibilidad del cajero)
        int totalEspera = 0; // Acumulador para el total de tiempo de espera de todos los clientes
        int totalOcioso = 0; // Acumulador para el total de tiempo ocioso del cajero
        int clientesConEspera = 0; // Contador de clientes que tuvieron que esperar
        StringBuilder clientesEspera = new StringBuilder(); // Builder para construir la lista de clientes que esperaron
        boolean first = true; // Flag para controlar la puntuación en la lista de clientes

        for(int i = 1; i <= clientes; i++){ // Bucle que simula la llegada y servicio de cada cliente
            double rLleg = Math.random(); // Genera número aleatorio para determinar tiempo entre llegadas
            int intervaloLlegada = ColaBancoModelo.tiempoInterLlegada(rLleg); // Convierte el número aleatorio en tiempo entre llegadas usando el modelo
            horaLlegadaAcum += intervaloLlegada; // Suma el intervalo al tiempo acumulado para obtener hora de llegada de este cliente
            String horaLlegada = UtilFormatoColas.horaDesdeBase(horaLlegadaAcum); // Convierte minutos acumulados a formato de hora (HH:MM)

            double rServ = Math.random(); // Genera número aleatorio para determinar tiempo de servicio
            int tiempoServicio = ColaBancoModelo.tiempoServicio(rServ); // Convierte el número aleatorio en tiempo de servicio usando el modelo

            int inicioServicio = Math.max(horaLlegadaAcum, finServicioAnterior); // El servicio inicia cuando llega el cliente O cuando termina el servicio anterior (lo que sea mayor)
            int tiempoEspera = inicioServicio - horaLlegadaAcum; // Calcula tiempo de espera: diferencia entre inicio de servicio y llegada
            int tiempoOcioso = (inicioServicio > finServicioAnterior) ? inicioServicio - finServicioAnterior : 0; // Calcula tiempo ocioso del cajero: tiempo entre fin del servicio anterior e inicio del actual
            int finServicio = inicioServicio + tiempoServicio; // Calcula tiempo de finalización del servicio

            if(tiempoEspera > 0){ // Si el cliente tuvo que esperar
                clientesConEspera++; // Incrementa el contador de clientes con espera
                if(!first) clientesEspera.append(", "); // Agrega coma si no es el primer cliente en la lista
                clientesEspera.append(i); // Agrega el número de cliente a la lista
                first = false; // Marca que ya no es el primer cliente
            }

            totalEspera += tiempoEspera; // Suma el tiempo de espera de este cliente al total
            totalOcioso += tiempoOcioso; // Suma el tiempo ocioso generado por este cliente al total

            modeloSim.addRow(new Object[]{ // Agrega una fila a la tabla de simulación con todos los datos del cliente
                i, // Número de cliente
                UtilFormatoColas.f2(rLleg), // Número aleatorio de llegada formateado a 2 decimales
                intervaloLlegada, // Tiempo entre llegadas calculado
                horaLlegada, // Hora de llegada formateada
                UtilFormatoColas.f2(rServ), // Número aleatorio de servicio formateado a 2 decimales
                tiempoServicio, // Tiempo de servicio calculado
                UtilFormatoColas.horaDesdeBase(inicioServicio), // Hora de inicio del servicio formateada
                UtilFormatoColas.horaDesdeBase(finServicio), // Hora de finalización del servicio formateada
                tiempoEspera, // Tiempo de espera del cliente
                tiempoOcioso // Tiempo ocioso del cajero generado por este cliente
            });

            finServicioAnterior = finServicio; // Actualiza el tiempo de finalización para el siguiente cliente
        }

        // Fila de totales
        modeloSim.addRow(new Object[]{ // Agrega fila con totales al final de la tabla
            "TOTALES", // Etiqueta en la primera columna
            "", // Columnas vacías para mantener estructura
            "",
            "",
            "",
            "",
            "",
            "",
            totalEspera, // Total de tiempo de espera de todos los clientes
            totalOcioso // Total de tiempo ocioso del cajero
        });

        // Fila de promedio
        double promedioEspera = totalEspera / (double)clientes; // Calcula el promedio de tiempo de espera por cliente
        modeloSim.addRow(new Object[]{ // Agrega fila con el promedio
            "PROMEDIO espera por cliente", // Etiqueta descriptiva
            "", // Columnas vacías para mantener estructura
            "",
            "",
            "",
            "",
            "",
            "",
            UtilFormatoColas.f2(promedioEspera), // Promedio de espera formateado a 2 decimales
            "" // Columna vacía
        });

        // Actualizar resumen
        double porcentajeEspera = (clientesConEspera / (double)clientes) * 100; // Calcula el porcentaje de clientes que esperaron

        StringBuilder sb = new StringBuilder(); // Builder para construir el texto del resumen
        sb.append("RESULTADOS DE LA SIMULACIÓN ALEATORIA (").append(clientes).append(" clientes):\n\n"); // Encabezado con número de clientes simulados
        sb.append("TIEMPOS DE ESPERA:\n"); // Sección de análisis de esperas
        sb.append("• Total tiempo de espera: ").append(totalEspera).append(" minutos\n"); // Total de tiempo de espera
        sb.append("• Tiempo promedio de espera: ").append(UtilFormatoColas.f2(promedioEspera)).append(" minutos\n"); // Promedio de espera
        sb.append("• Clientes que esperaron: ").append(clientesConEspera).append(" de ").append(clientes); // Número de clientes que esperaron
        sb.append(" (").append(UtilFormatoColas.f2(porcentajeEspera)).append("%)\n"); // Porcentaje de clientes que esperaron
        if(clientesConEspera > 0) { // Si hubo clientes con espera
            sb.append("• Clientes específicos que esperaron: ").append(clientesEspera).append("\n"); // Lista específica de clientes que esperaron
        }
        sb.append("• Tiempo total de cajero ocioso: ").append(totalOcioso).append(" minutos\n\n"); // Tiempo total que el cajero estuvo sin atender

        sb.append("EVALUACIÓN DEL OBJETIVO DEL BANCO:\n"); // Sección de evaluación contra la política del banco
        sb.append("• Política: Cliente promedio no debe esperar más de 2 minutos\n"); // Define la política del banco
        sb.append("• Resultado: ").append(promedioEspera <= 2.0 ? "✓ CUMPLE" : "✗ NO CUMPLE").append(" el objetivo\n\n"); // Evalúa si cumple o no la política

        if(promedioEspera > 2.0){ // Si no cumple la política (promedio > 2 minutos)
            double exceso = promedioEspera - 2.0; // Calcula el exceso sobre la política
            sb.append("ANÁLISIS DEL PROBLEMA:\n"); // Sección de análisis del problema
            sb.append("• Exceso sobre el objetivo: ").append(UtilFormatoColas.f2(exceso)).append(" minutos\n"); // Cuánto se excede del objetivo
            sb.append("• Porcentaje de clientes afectados: ").append(UtilFormatoColas.f2(porcentajeEspera)).append("%\n\n"); // Porcentaje de clientes afectados
            sb.append("RECOMENDACIONES:\n"); // Sección de recomendaciones para mejorar
            sb.append("• Considerar agregar más cajeros en la ventanilla auto\n"); // Recomendación de recursos
            sb.append("• Optimizar el tiempo de servicio por cliente\n"); // Recomendación de eficiencia
            sb.append("• Implementar sistema de citas para distribuir llegadas\n\n"); // Recomendación de gestión de demanda
        } else { // Si cumple la política
            sb.append("CONCLUSIÓN POSITIVA:\n"); // Sección de conclusión positiva
            sb.append("• La ventanilla de servicio en el auto cumple satisfactoriamente el criterio establecido\n"); // Confirmación de cumplimiento
            sb.append("• El sistema actual es eficiente para el nivel de demanda simulado\n\n"); // Confirmación de eficiencia
        }

        sb.append("INTERPRETACIÓN:\n"); // Sección de interpretación de resultados
        sb.append("Las celdas resaltadas en amarillo indican clientes que tuvieron que esperar en la fila.\n"); // Explicación del código de colores
        sb.append("Con más clientes simulados, los resultados se estabilizan y son más representativos.\n"); // Nota sobre estabilidad estadística
        sb.append("Cada ejecución genera resultados diferentes debido a la naturaleza aleatoria de la simulación."); // Nota sobre variabilidad de resultados

        resumen.setText(sb.toString()); // Establece el texto completo del resumen en el área de texto
    }
}