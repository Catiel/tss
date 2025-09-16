package actividad_5.colas; // Define el paquete donde se encuentra esta clase

import javax.swing.*; // Importa todas las clases de Swing para componentes de interfaz gráfica
import javax.swing.table.DefaultTableModel; // Importa el modelo de datos para tablas
import java.awt.*; // Importa clases de AWT para layouts y colores

/** Panel con simulación de cola usando los datos específicos del ejemplo del BNB (8 clientes). */
public class PanelColaPredefinida extends JPanel { // Clase que extiende JPanel para crear un panel de simulación predefinida de colas bancarias con datos específicos del Banco BNB
    private final DefaultTableModel modeloServicio; // Modelo para la tabla que muestra los rangos de probabilidad de tiempos de servicio
    private final DefaultTableModel modeloLlegadas; // Modelo para la tabla que muestra los rangos de probabilidad de tiempos entre llegadas
    private final DefaultTableModel modeloSim; // Modelo para la tabla que muestra los resultados detallados de la simulación de cola
    private final JTextArea resumen; // Área de texto para mostrar el análisis de resultados y evaluación de objetivos del banco

    // Números aleatorios del ejemplo (8 clientes)
    private static final double[] RAND_LLEGADA = {0.50, 0.28, 0.68, 0.36, 0.90, 0.62, 0.27, 0.50}; // Array con 8 números aleatorios predefinidos para determinar tiempos entre llegadas de clientes
    private static final double[] RAND_SERVICIO = {0.52, 0.37, 0.82, 0.69, 0.98, 0.96, 0.33, 0.50}; // Array con 8 números aleatorios predefinidos para determinar tiempos de servicio correspondientes

    public PanelColaPredefinida(){ // Constructor que inicializa todo el panel de simulación predefinida de colas del Banco BNB
        setLayout(new BorderLayout(8,8)); // Establece el layout BorderLayout con espaciado de 8 píxeles horizontal y vertical
        EstilosUI.aplicarEstiloPanel(this); // Aplica el estilo visual predefinido al panel

        JLabel titulo = new JLabel("Simulación Cola Banco BNB - Ejemplo predefinido (8 clientes)"); // Crea la etiqueta del título específica para el ejemplo del Banco BNB con 8 clientes
        EstilosUI.aplicarEstiloTitulo(titulo); // Aplica el estilo específico para títulos
        add(titulo, BorderLayout.NORTH); // Agrega el título en la parte superior del panel

        // Panel principal dividido
        JPanel panelPrincipal = new JPanel(new BorderLayout(8,8)); // Crea el panel principal con BorderLayout y espaciado
        EstilosUI.aplicarEstiloPanel(panelPrincipal); // Aplica estilo al panel principal

        // Panel izquierdo con distribuciones
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
        tServ.getColumnModel().getColumn(0).setPreferredWidth(90); // Establece ancho de 90 píxeles para columna "Probabilidad"
        tServ.getColumnModel().getColumn(1).setPreferredWidth(130); // Establece ancho de 130 píxeles para columna "Distribución acumulada"
        tServ.getColumnModel().getColumn(2).setPreferredWidth(140); // Establece ancho de 140 píxeles para columna "Rango de Nros aleatorios"
        tServ.getColumnModel().getColumn(3).setPreferredWidth(120); // Establece ancho de 120 píxeles para columna "Tiempo de servicio"
        JScrollPane spServ = new JScrollPane(tServ); // Envuelve la tabla de servicio en un scroll pane
        spServ.setBorder(BorderFactory.createTitledBorder("Datos del tiempo de servicio")); // Agrega borde con título específico para servicio
        panelIzq.add(spServ); // Agrega el scroll pane de servicio a la parte superior del panel izquierdo

        // 2) Distribución tiempo entre llegadas
        modeloLlegadas = new DefaultTableModel(new Object[]{"Probabilidad","Distribución acumulada","Rango de Nros aleatorios","Tiempo entre llegadas cliente"},0){ // Crea modelo de tabla no editable para mostrar distribución de tiempos entre llegadas
            @Override public boolean isCellEditable(int r,int c){return false;} // Override para hacer toda la tabla de solo lectura
        };
        llenarDistribucionLlegadas(); // Llama al método para poblar la tabla de distribución de tiempos entre llegadas
        JTable tLleg = new JTable(modeloLlegadas); // Crea la tabla de tiempos entre llegadas usando el modelo
        EstilosUI.aplicarEstiloTabla(tLleg); // Aplica estilo predefinido a la tabla de llegadas
        tLleg.getTableHeader().setBackground(new Color(200, 255, 200)); // Establece color verde claro para el encabezado de la tabla de llegadas
        tLleg.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Desactiva redimensionado automático de columnas
        tLleg.getColumnModel().getColumn(0).setPreferredWidth(90); // Establece ancho de 90 píxeles para columna "Probabilidad"
        tLleg.getColumnModel().getColumn(1).setPreferredWidth(130); // Establece ancho de 130 píxeles para columna "Distribución acumulada"
        tLleg.getColumnModel().getColumn(2).setPreferredWidth(140); // Establece ancho de 140 píxeles para columna "Rango de Nros aleatorios"
        tLleg.getColumnModel().getColumn(3).setPreferredWidth(180); // Establece ancho de 180 píxeles para columna "Tiempo entre llegadas cliente"
        JScrollPane spLleg = new JScrollPane(tLleg); // Envuelve la tabla de llegadas en un scroll pane
        spLleg.setBorder(BorderFactory.createTitledBorder("Datos de la llegada de los clientes")); // Agrega borde con título específico para llegadas
        panelIzq.add(spLleg); // Agrega el scroll pane de llegadas a la parte inferior del panel izquierdo

        panelPrincipal.add(panelIzq, BorderLayout.WEST); // Agrega el panel izquierdo (con las 2 tablas de distribución) al lado oeste del panel principal

        // Tabla de simulación (lado derecho)
        modeloSim = new DefaultTableModel(new Object[]{ // Crea modelo para la tabla de simulación con 10 columnas detalladas para análisis completo de cola
            "# de cliente","# aleatorio","Intervalo entre llegadas","hora de llegadas","# aleatorio","t servicio","inicio del servicio","final del servicio","t espera","t ocioso"},0){
            @Override public boolean isCellEditable(int r,int c){return false;} // Override para hacer toda la tabla de solo lectura
            @Override public Class<?> getColumnClass(int c){ // Override para especificar el tipo de datos de cada columna
                if(c == 0 || c == 2 || c == 5 || c == 8 || c == 9) return Integer.class; // Columnas 0,2,5,8,9 son de tipo Integer (números enteros)
                return String.class; // Las demás columnas son de tipo String (números aleatorios formateados y horas)
            }
        };

        JTable tablaSim = new JTable(modeloSim){ // Crea la tabla de simulación con comportamiento personalizado de renderizado
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer,int row,int column){ // Override para personalizar el renderizado de celdas
                Component c = super.prepareRenderer(renderer,row,column); // Obtiene el componente renderizado por defecto
                if(row < getRowCount()-1){ // fila de datos (no totales) - Verifica si es una fila de datos normales (no la fila de totales)
                    // Alternar colores para mejor legibilidad
                    if(row % 2 == 0){ // Si es fila par
                        c.setBackground(Color.WHITE); // Establece fondo blanco para filas pares
                    } else { // Si es fila impar
                        c.setBackground(new Color(245, 245, 245)); // Establece fondo gris claro para filas impares (patrón zebra)
                    }
                    // Resaltar tiempos de espera > 0
                    Object espera = getValueAt(row, 8); // Obtiene el valor de la columna "t espera"
                    if(espera instanceof Integer && (Integer)espera > 0){ // Si hay tiempo de espera mayor a 0
                        c.setBackground(new Color(255, 255, 200)); // amarillo claro - Establece fondo amarillo para resaltar clientes que esperaron (prevalece sobre patrón zebra)
                    }
                } else { // fila de totales
                    c.setBackground(new Color(220, 220, 220)); // Establece fondo gris para fila de totales
                }
                return c; // Retorna el componente con el color de fondo apropiado
            }
        };

        EstilosUI.aplicarEstiloTabla(tablaSim); // Aplica estilo predefinido a la tabla de simulación
        tablaSim.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Desactiva redimensionado automático de columnas
        tablaSim.getColumnModel().getColumn(0).setPreferredWidth(70);  // # cliente - Establece ancho de 70 píxeles para columna de número de cliente
        tablaSim.getColumnModel().getColumn(1).setPreferredWidth(70);  // # aleatorio llegada - Establece ancho de 70 píxeles para número aleatorio de llegada
        tablaSim.getColumnModel().getColumn(2).setPreferredWidth(90);  // intervalo llegadas - Establece ancho de 90 píxeles para intervalo entre llegadas
        tablaSim.getColumnModel().getColumn(3).setPreferredWidth(90);  // hora llegadas - Establece ancho de 90 píxeles para hora de llegada
        tablaSim.getColumnModel().getColumn(4).setPreferredWidth(70);  // # aleatorio servicio - Establece ancho de 70 píxeles para número aleatorio de servicio
        tablaSim.getColumnModel().getColumn(5).setPreferredWidth(70);  // t servicio - Establece ancho de 70 píxeles para tiempo de servicio
        tablaSim.getColumnModel().getColumn(6).setPreferredWidth(100); // inicio servicio - Establece ancho de 100 píxeles para inicio del servicio
        tablaSim.getColumnModel().getColumn(7).setPreferredWidth(90);  // final servicio - Establece ancho de 90 píxeles para final del servicio
        tablaSim.getColumnModel().getColumn(8).setPreferredWidth(70);  // t espera - Establece ancho de 70 píxeles para tiempo de espera
        tablaSim.getColumnModel().getColumn(9).setPreferredWidth(70);  // t ocioso - Establece ancho de 70 píxeles para tiempo ocioso del cajero

        simular(); // Llama al método para ejecutar la simulación con los números aleatorios predefinidos
        JScrollPane spSim = new JScrollPane(tablaSim); // Envuelve la tabla de simulación en un scroll pane
        spSim.setBorder(BorderFactory.createTitledBorder("Simulación de la cola del banco")); // Agrega borde con título específico
        spSim.setPreferredSize(new Dimension(700, 400)); // Establece tamaño preferido del scroll pane
        panelPrincipal.add(spSim, BorderLayout.CENTER); // Agrega el scroll pane de simulación al centro del panel principal

        add(panelPrincipal, BorderLayout.CENTER); // Agrega el panel principal al centro del panel general

        // Área de resumen
        resumen = new JTextArea(); // Crea el área de texto para mostrar el resumen de análisis
        resumen.setEditable(false); // Hace que el área de texto sea de solo lectura
        resumen.setWrapStyleWord(true); // Ajusta las líneas por palabras completas, no por caracteres
        resumen.setLineWrap(true); // Activa el ajuste automático de líneas
        resumen.setBackground(getBackground()); // Establece el mismo color de fondo que el panel padre
        resumen.setBorder(BorderFactory.createTitledBorder("Análisis de resultados")); // Agrega borde con título
        resumen.setFont(new Font("Arial", Font.PLAIN, 12)); // Establece fuente Arial, normal, tamaño 12
        resumen.setPreferredSize(new Dimension(0, 140)); // Establece altura preferida de 140 píxeles, ancho flexible

        mostrarResumen(); // Llama al método para generar y mostrar el resumen de análisis automáticamente
        add(resumen, BorderLayout.SOUTH); // Agrega el área de resumen en la parte inferior
    }

    private void llenarDistribucionServicio(){ // Método que llena la tabla de distribución de tiempos de servicio con datos del modelo
        modeloServicio.setRowCount(0); // Limpia todas las filas existentes en el modelo de servicio
        double[][] rangos = ColaBancoModelo.getRangosServicio(); // Obtiene los rangos de números aleatorios para tiempos de servicio del modelo

        for(int i = 0; i < ColaBancoModelo.SERVICIO_VALORES.length; i++){ // Itera sobre todos los valores de tiempo de servicio del modelo
            String rango = UtilFormatoColas.f2(rangos[i][0]) + " - " + UtilFormatoColas.f2(rangos[i][1]); // Construye string del rango formateado (inicio - fin)
            modeloServicio.addRow(new Object[]{ // Agrega una fila a la tabla de servicio con los datos calculados
                UtilFormatoColas.f2(ColaBancoModelo.SERVICIO_PROBS[i]), // Probabilidad individual formateada a 2 decimales
                UtilFormatoColas.f2(ColaBancoModelo.SERVICIO_PROBS[i] == 0 ? 0 : (i == 0 ? ColaBancoModelo.SERVICIO_PROBS[i] : getSumaAcumulada(ColaBancoModelo.SERVICIO_PROBS, i))), // Lógica especial para probabilidad acumulada: si prob=0 entonces 0, si es primer elemento usa la probabilidad individual, si no calcula suma acumulada
                rango, // Rango de números aleatorios como string formateado
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
                UtilFormatoColas.f2(getSumaAcumulada(ColaBancoModelo.LLEGADA_PROBS, i)), // Probabilidad acumulada hasta este punto, calculada con método utilitario
                rango, // Rango de números aleatorios como string formateado
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

    private void simular(){ // Método que ejecuta la simulación de cola bancaria usando los números aleatorios predefinidos
        modeloSim.setRowCount(0); // Limpia todas las filas existentes en la tabla de simulación
        int horaLlegadaAcum = 0; // minutos desde 09:00 - Acumulador de tiempo desde el inicio (09:00) en minutos
        int finServicioAnterior = 0; // Tiempo de finalización del servicio del cliente anterior (para calcular disponibilidad del cajero)
        int totalEspera = 0; // Acumulador para el total de tiempo de espera de todos los clientes
        int totalOcioso = 0; // Acumulador para el total de tiempo ocioso del cajero

        for(int i = 0; i < RAND_LLEGADA.length; i++){ // Bucle que procesa cada uno de los 8 clientes con sus números aleatorios predefinidos
            double rLleg = RAND_LLEGADA[i]; // Obtiene el número aleatorio predefinido para llegada del cliente actual
            int intervaloLlegada = ColaBancoModelo.tiempoInterLlegada(rLleg); // Convierte el número aleatorio en tiempo entre llegadas usando el modelo
            horaLlegadaAcum += intervaloLlegada; // Suma el intervalo al tiempo acumulado para obtener hora de llegada de este cliente
            String horaLlegada = UtilFormatoColas.horaDesdeBase(horaLlegadaAcum); // Convierte minutos acumulados a formato de hora (HH:MM)

            double rServ = RAND_SERVICIO[i]; // Obtiene el número aleatorio predefinido para servicio del cliente actual
            int tiempoServicio = ColaBancoModelo.tiempoServicio(rServ); // Convierte el número aleatorio en tiempo de servicio usando el modelo

            int inicioServicio = Math.max(horaLlegadaAcum, finServicioAnterior); // El servicio inicia cuando llega el cliente O cuando termina el servicio anterior (lo que sea mayor)
            int tiempoEspera = inicioServicio - horaLlegadaAcum; // Calcula tiempo de espera: diferencia entre inicio de servicio y llegada
            int tiempoOcioso = (inicioServicio > finServicioAnterior) ? inicioServicio - finServicioAnterior : 0; // Calcula tiempo ocioso del cajero: tiempo entre fin del servicio anterior e inicio del actual
            int finServicio = inicioServicio + tiempoServicio; // Calcula tiempo de finalización del servicio

            totalEspera += tiempoEspera; // Suma el tiempo de espera de este cliente al total
            totalOcioso += tiempoOcioso; // Suma el tiempo ocioso generado por este cliente al total

            modeloSim.addRow(new Object[]{ // Agrega una fila a la tabla de simulación con todos los datos del cliente
                i + 1, // Número de cliente (i+1 porque i empieza en 0)
                UtilFormatoColas.f2(rLleg), // Número aleatorio de llegada predefinido, formateado a 2 decimales
                intervaloLlegada, // Tiempo entre llegadas calculado
                horaLlegada, // Hora de llegada formateada
                UtilFormatoColas.f2(rServ), // Número aleatorio de servicio predefinido, formateado a 2 decimales
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
            "", // Columnas vacías para mantener estructura de la tabla
            "",
            "",
            "",
            "",
            "",
            "",
            totalEspera, // Total de tiempo de espera de todos los clientes
            totalOcioso // Total de tiempo ocioso del cajero
        });
    }

    private void mostrarResumen(){ // Método que genera y muestra el resumen de análisis de la simulación
        // Calcular estadísticas
        int totalEspera = 0; // Variable para acumular tiempo total de espera
        int totalOcioso = 0; // Variable para acumular tiempo total ocioso
        int clientesConEspera = 0; // Contador de clientes que tuvieron que esperar

        for(int i = 0; i < RAND_LLEGADA.length; i++){ // Bucle inicial para procesar cada cliente (aunque los cálculos reales se hacen más abajo)
            // Simular nuevamente para obtener los datos
            double rLleg = RAND_LLEGADA[i]; // Obtiene el número aleatorio de llegada del cliente actual
            int intervaloLlegada = ColaBancoModelo.tiempoInterLlegada(rLleg); // Convierte en tiempo entre llegadas (no se usa en esta versión)

            double rServ = RAND_SERVICIO[i]; // Obtiene el número aleatorio de servicio del cliente actual
            int tiempoServicio = ColaBancoModelo.tiempoServicio(rServ); // Convierte en tiempo de servicio (no se usa en esta versión)

            // Calcular tiempos (simplificado para el resumen)
            if(i > 0){ // Los clientes después del primero pueden tener espera - Comentario indicando que esto es una simplificación
                // Esta es una simplificación; los valores reales están en la tabla - Nota explicativa sobre la lógica
            }
        }

        // Obtener datos de la tabla simulada
        int filas = modeloSim.getRowCount() - 1; // Excluir fila de totales - Calcula número de filas de datos excluyendo la fila de totales
        totalEspera = 0; // Reinicializa el acumulador de tiempo de espera
        totalOcioso = 0; // Reinicializa el acumulador de tiempo ocioso

        for(int i = 0; i < filas; i++){ // Itera sobre todas las filas de datos de la tabla de simulación
            Object espera = modeloSim.getValueAt(i, 8); // Obtiene el valor de tiempo de espera de la fila actual (columna 8)
            Object ocioso = modeloSim.getValueAt(i, 9); // Obtiene el valor de tiempo ocioso de la fila actual (columna 9)

            if(espera instanceof Integer){ // Verifica que el valor de espera sea un entero
                int esp = (Integer)espera; // Convierte a entero
                totalEspera += esp; // Suma al total de tiempo de espera
                if(esp > 0) clientesConEspera++; // Si el tiempo de espera es mayor a 0, incrementa el contador de clientes con espera
            }

            if(ocioso instanceof Integer){ // Verifica que el valor de tiempo ocioso sea un entero
                totalOcioso += (Integer)ocioso; // Suma al total de tiempo ocioso del cajero
            }
        }

        double promedioEspera = totalEspera / (double)RAND_LLEGADA.length; // Calcula el promedio de tiempo de espera por cliente
        double porcentajeEspera = (clientesConEspera / (double)RAND_LLEGADA.length) * 100; // Calcula el porcentaje de clientes que esperaron

        StringBuilder sb = new StringBuilder(); // Builder para construir el texto del resumen
        sb.append("ANÁLISIS DE LA SIMULACIÓN DEL BANCO BNB (Ventanilla Auto):\n\n"); // Encabezado específico del análisis del Banco BNB
        sb.append("RESULTADOS OBTENIDOS:\n"); // Sección de resultados obtenidos
        sb.append("• Total de clientes simulados: ").append(RAND_LLEGADA.length).append(" clientes\n"); // Número total de clientes (8 en este caso)
        sb.append("• Tiempo total de espera: ").append(totalEspera).append(" minutos\n"); // Total de tiempo de espera acumulado
        sb.append("• Tiempo promedio de espera: ").append(UtilFormatoColas.f2(promedioEspera)).append(" minutos\n"); // Promedio de espera por cliente
        sb.append("• Clientes que esperaron: ").append(clientesConEspera).append(" de ").append(RAND_LLEGADA.length); // Número absoluto de clientes con espera
        sb.append(" (").append(UtilFormatoColas.f2(porcentajeEspera)).append("%)\n"); // Porcentaje de clientes que esperaron
        sb.append("• Tiempo total de cajero ocioso: ").append(totalOcioso).append(" minutos\n\n"); // Total de tiempo que el cajero no atendió clientes

        sb.append("EVALUACIÓN DEL OBJETIVO:\n"); // Sección de evaluación contra la política del banco
        sb.append("• Política del banco: Cliente promedio no debe esperar más de 2 minutos\n"); // Define claramente la política del banco
        sb.append("• Resultado: ").append(promedioEspera <= 2.0 ? "✓ CUMPLE" : "✗ NO CUMPLE").append(" el objetivo\n"); // Evalúa automáticamente si cumple o no la política

        if(promedioEspera > 2.0){ // Si no cumple la política (promedio > 2 minutos)
            sb.append("• Recomendación: Considerar agregar más cajeros o reducir tiempo de servicio\n"); // Recomendación específica cuando no cumple
        } else { // Si cumple la política
            sb.append("• La ventanilla de servicio en el auto cumple satisfactoriamente el criterio establecido\n"); // Confirmación positiva cuando sí cumple
        }

        sb.append("\nINTERPRETACIÓN:\n"); // Sección de interpretación de resultados
        sb.append("Las celdas resaltadas en amarillo indican clientes que tuvieron que esperar en la fila.\n"); // Explicación del código de colores amarillo
        sb.append("El tiempo ocioso representa períodos donde el cajero no estaba atendiendo clientes."); // Explicación del concepto de tiempo ocioso

        resumen.setText(sb.toString()); // Establece el texto completo del resumen en el área de texto
    }
}

