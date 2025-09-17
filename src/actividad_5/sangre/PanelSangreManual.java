package actividad_5.sangre; // Define el paquete donde se encuentra esta clase para el módulo de simulación de sangre

import javax.swing.*; // Importa todas las clases de Swing para crear la interfaz gráfica
import javax.swing.table.DefaultTableModel; // Importa el modelo de tabla por defecto para manejar datos de tablas
import java.awt.*; // Importa clases para manejo de componentes gráficos y layouts
import java.awt.event.ActionEvent; // Importa la clase para manejar eventos de acción
import java.util.ArrayList; // Importa la clase ArrayList para listas dinámicas (aunque no se usa en este código)
import java.util.List; // Importa la interfaz List para manejo de listas (aunque no se usa en este código)

/** Panel para ingresar manualmente números aleatorios y ejecutar la simulación. */ // Comentario de documentación que explica el propósito del panel
public class PanelSangreManual extends JPanel { // Declara la clase que extiende JPanel para crear un panel personalizado de simulación manual
    private final JSpinner spSemanas; // Declara spinner para seleccionar el número de semanas a simular
    private final JSpinner spInvInicial; // Declara spinner para establecer el inventario inicial de sangre
    private final JButton btnCrear; // Declara el botón para crear la tabla de entrada de números aleatorios
    private final JButton btnCalcular; // Declara el botón para calcular los resultados de la simulación
    private final DefaultTableModel modeloSupply; // Declara el modelo de datos para la tabla de suministro de sangre
    private final DefaultTableModel modeloPacientes; // Declara el modelo de datos para la tabla de número de pacientes
    private final DefaultTableModel modeloDemanda; // Declara el modelo de datos para la tabla de demanda por paciente
    private final DefaultTableModel modeloInput; // Declara el modelo de datos para la tabla de entrada de números aleatorios manuales
    private final DefaultTableModel modeloSim; // Declara el modelo de datos para la tabla principal de simulación final

    public PanelSangreManual(){ // Constructor de la clase
        setLayout(new BorderLayout(8,8)); // Establece el layout del panel con separación de 8 píxeles
        EstilosUI.aplicarEstiloPanel(this); // Aplica el estilo visual al panel
        JLabel titulo = new JLabel("Simulación manual (ingrese números aleatorios en [0,1))"); // Crea la etiqueta del título
        EstilosUI.aplicarEstiloTitulo(titulo); // Aplica el estilo del título
        add(titulo, BorderLayout.NORTH); // Agrega el título en la parte superior del panel

        // Controles superiores
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Crea el panel de controles con layout de flujo alineado a la izquierda
        EstilosUI.aplicarEstiloPanel(controles); // Aplica el estilo al panel de controles
        controles.add(new JLabel("Semanas:")); // Agrega la etiqueta para el número de semanas
        spSemanas = new JSpinner(new SpinnerNumberModel(6,1,20,1)); // Inicializa el spinner con valor inicial 6, mínimo 1, máximo 20, incremento 1
        controles.add(spSemanas); // Agrega el spinner de semanas al panel de controles
        controles.add(new JLabel("Inventario inicial:")); // Agrega la etiqueta para el inventario inicial
        spInvInicial = new JSpinner(new SpinnerNumberModel(0,0,1000,1)); // Inicializa el spinner con valor inicial 0, mínimo 0, máximo 1000, incremento 1
        controles.add(spInvInicial); // Agrega el spinner de inventario inicial al panel de controles
        btnCrear = new JButton("Crear tabla"); // Crea el botón para crear la tabla de entrada
        EstilosUI.aplicarEstiloBoton(btnCrear); // Aplica el estilo al botón crear
        controles.add(btnCrear); // Agrega el botón crear al panel de controles
        btnCalcular = new JButton("Calcular"); // Crea el botón para calcular los resultados
        EstilosUI.aplicarEstiloBoton(btnCalcular); // Aplica el estilo al botón calcular
        controles.add(btnCalcular); // Agrega el botón calcular al panel de controles
        add(controles, BorderLayout.BEFORE_FIRST_LINE); // Agrega el panel de controles en la parte superior antes del contenido principal

        // Panel principal dividido
        JPanel panelPrincipal = new JPanel(new BorderLayout(5,5)); // Crea el panel principal con layout BorderLayout y separación de 5 píxeles
        EstilosUI.aplicarEstiloPanel(panelPrincipal); // Aplica el estilo al panel principal

        // Panel izquierdo con distribuciones
        JPanel panelIzq = new JPanel(new GridLayout(3,1,5,5)); // Crea el panel izquierdo con layout de grilla (3 filas, 1 columna, separación 5px)
        EstilosUI.aplicarEstiloPanel(panelIzq); // Aplica el estilo al panel izquierdo

        modeloSupply = new DefaultTableModel(new Object[]{"Prob","Acum","Ini","Fin","Pintas"},0){ // Inicializa el modelo de la tabla de suministro con columnas de probabilidad, acumulada, inicio, fin y pintas
            @Override public boolean isCellEditable(int r,int c){return false;} // Sobrescribe el método para hacer todas las celdas no editables
        };
        modeloPacientes = new DefaultTableModel(new Object[]{"Prob","Acum","Ini","Fin","#Pac"},0){ // Inicializa el modelo de la tabla de pacientes con columnas similares pero con número de pacientes
            @Override public boolean isCellEditable(int r,int c){return false;} // Sobrescribe el método para hacer todas las celdas no editables
        };
        modeloDemanda = new DefaultTableModel(new Object[]{"Prob","Acum","Ini","Fin","Pintas"},0){ // Inicializa el modelo de la tabla de demanda con columnas de probabilidad, acumulada, inicio, fin y pintas
            @Override public boolean isCellEditable(int r,int c){return false;} // Sobrescribe el método para hacer todas las celdas no editables
        };

        llenarDerivadas(); // Llama al método para llenar las tablas de distribuciones con datos calculados

        JTable tSup = new JTable(modeloSupply); // Crea la tabla de suministro usando el modelo
        EstilosUI.aplicarEstiloTabla(tSup); // Aplica el estilo a la tabla
        tSup.getTableHeader().setBackground(new Color(200, 255, 200)); // Establece el color de fondo del encabezado (verde claro)
        panelIzq.add(new JScrollPane(tSup)); // Agrega la tabla con scroll al panel izquierdo

        JTable tPac = new JTable(modeloPacientes); // Crea la tabla de pacientes usando el modelo
        EstilosUI.aplicarEstiloTabla(tPac); // Aplica el estilo a la tabla
        tPac.getTableHeader().setBackground(new Color(200, 220, 255)); // Establece el color de fondo del encabezado (azul claro)
        panelIzq.add(new JScrollPane(tPac)); // Agrega la tabla con scroll al panel izquierdo

        JTable tDem = new JTable(modeloDemanda); // Crea la tabla de demanda usando el modelo
        EstilosUI.aplicarEstiloTabla(tDem); // Aplica el estilo a la tabla
        tDem.getTableHeader().setBackground(new Color(255, 200, 255)); // Establece el color de fondo del encabezado (rosa claro)
        panelIzq.add(new JScrollPane(tDem)); // Agrega la tabla con scroll al panel izquierdo

        panelPrincipal.add(panelIzq, BorderLayout.WEST); // Agrega el panel izquierdo al lado oeste del panel principal

        // Panel central con tabla de entrada y simulación
        JPanel panelCentro = new JPanel(new GridLayout(2,1,5,5)); // Crea el panel central con layout de grilla (2 filas, 1 columna, separación 5px)
        EstilosUI.aplicarEstiloPanel(panelCentro); // Aplica el estilo al panel central

        // Tabla de entrada de números aleatorios (editable)
        modeloInput = new DefaultTableModel(new Object[]{ // Inicializa el modelo de la tabla de entrada con 3 columnas
                "Semana","#Alea.Suministro","#Alea.Pacientes"},0){ // Define las columnas: semana, número aleatorio para suministro, número aleatorio para pacientes
            @Override public boolean isCellEditable(int r,int c){ // Sobrescribe el método para determinar qué celdas son editables
                // Solo columnas de suministro y pacientes son editables (1-2)
                return c == 1 || c == 2; // Solo las columnas 1 y 2 (números aleatorios) son editables
            }
            @Override public Class<?> getColumnClass(int c){ // Sobrescribe el método para definir el tipo de datos de cada columna
                return c == 0 ? Integer.class : String.class; // Primera columna es Integer (semana), las demás son String (números aleatorios)
            }
        };
        JTable tablaInput = new JTable(modeloInput); // Crea la tabla de entrada usando el modelo
        EstilosUI.aplicarEstiloTabla(tablaInput); // Aplica el estilo a la tabla
        tablaInput.getTableHeader().setBackground(new Color(255, 255, 200)); // Establece el color de fondo del encabezado (amarillo claro)
        JScrollPane scrollInput = new JScrollPane(tablaInput); // Crea un panel de desplazamiento para la tabla de entrada
        scrollInput.setPreferredSize(new Dimension(600, 200)); // Establece el tamaño preferido del panel de desplazamiento
        scrollInput.setBorder(BorderFactory.createTitledBorder("1. Ingrese números aleatorios [0,1)")); // Establece un borde con título instructivo
        panelCentro.add(scrollInput); // Agrega el panel de desplazamiento al panel central

        // Tabla de simulación final (solo lectura)
        modeloSim = new DefaultTableModel(new Object[]{ // Inicializa el modelo de la tabla de simulación principal con múltiples columnas
                "Semana","Inventario Inicial","#Aleatorio","Pintas","Sangre Disponible Total", // Columnas para datos semanales y de suministro
                "#Aleatorio","#Pacientes","Nro de Paciente","#Aleatorio","Pintas","#Pintas Restantes"},0){ // Columnas para datos de pacientes y demanda
            @Override public boolean isCellEditable(int r,int c){return false;} // Sobrescribe el método para hacer todas las celdas no editables
            @Override public Class<?> getColumnClass(int c){ // Sobrescribe el método para definir el tipo de datos de cada columna
                if(c == 0 || c == 1 || c == 3 || c == 4 || c == 6 || c == 7 || c == 9 || c == 10) return Integer.class; // Columnas numéricas enteras
                return String.class; // Las demás columnas son String (números aleatorios formateados)
            }
        };
        JTable tablaSim = new JTable(modeloSim); // Crea la tabla de simulación usando el modelo
        EstilosUI.aplicarEstiloTabla(tablaSim); // Aplica el estilo a la tabla
        JScrollPane scrollSim = new JScrollPane(tablaSim); // Crea un panel de desplazamiento para la tabla de simulación
        scrollSim.setPreferredSize(new Dimension(900, 300)); // Establece el tamaño preferido del panel de desplazamiento
        scrollSim.setBorder(BorderFactory.createTitledBorder("2. Resultados de la simulación")); // Establece un borde con título
        panelCentro.add(scrollSim); // Agrega el panel de desplazamiento al panel central

        panelPrincipal.add(panelCentro, BorderLayout.CENTER); // Agrega el panel central al centro del panel principal
        add(panelPrincipal, BorderLayout.CENTER); // Agrega el panel principal al centro del panel actual

        btnCrear.addActionListener(this::crearTabla); // Agrega un listener al botón crear que ejecuta el método crearTabla
        btnCalcular.addActionListener(this::calcular); // Agrega un listener al botón calcular que ejecuta el método calcular
        btnCalcular.setEnabled(false); // Desactiva inicialmente el botón calcular hasta que se cree la tabla

        JTextArea ayuda = new JTextArea("PASOS:\n" + // Crea área de texto con instrucciones detalladas
            "1. Configure semanas e inventario inicial\n" + // Paso 1 de las instrucciones
            "2. Presione 'Crear tabla' para generar filas\n" + // Paso 2 de las instrucciones
            "3. Ingrese números aleatorios (0 ≤ r < 1) para SUMINISTRO y PACIENTES únicamente\n" + // Paso 3 de las instrucciones
            "4. Presione 'Calcular' para obtener los resultados\n" + // Paso 4 de las instrucciones
            "NOTA: Los números aleatorios para la demanda de cada paciente se generan automáticamente."); // Nota importante sobre la generación automática
        ayuda.setWrapStyleWord(true); // Habilita el ajuste de línea por palabra
        ayuda.setLineWrap(true); // Habilita el ajuste automático de línea
        ayuda.setEditable(false); // Hace el área de texto no editable
        ayuda.setBackground(getBackground()); // Establece el mismo color de fondo que el panel
        ayuda.setFont(new Font("Arial", Font.PLAIN, 11)); // Establece la fuente en Arial, normal, tamaño 11
        add(ayuda, BorderLayout.SOUTH); // Agrega el área de ayuda en la parte inferior
    }

    private void llenarDerivadas(){ // Método para llenar las tablas de distribuciones con rangos calculados
        // Tabla de suministro con rangos acumulados
        modeloSupply.setRowCount(0); // Limpia todas las filas de la tabla de suministro
        double acum = 0, ini = 0; // Inicializa variables para cálculos acumulados e inicio de rango
        for(int i = 0; i < SangreModelo.SUPPLY_VALUES.length; i++){ // Itera sobre cada valor de suministro en el modelo
            double p = SangreModelo.SUPPLY_PROBS[i]; // Obtiene la probabilidad del valor de suministro actual
            acum += p; // Acumula la probabilidad
            if(i == SangreModelo.SUPPLY_VALUES.length-1) acum = 1.0; // Asegura que el último valor acumulado sea exactamente 1.0
            double fin = acum; // Establece el fin del rango como el valor acumulado actual
            modeloSupply.addRow(new Object[]{ // Agrega una nueva fila a la tabla de suministro
                UtilFormatoSangre.fmt2(p), // Probabilidad formateada with 2 decimales
                UtilFormatoSangre.fmt2(acum), // Probabilidad acumulada formateada
                UtilFormatoSangre.fmt2(ini), // Inicio del rango formateado
                UtilFormatoSangre.fmt2(fin), // Fin del rango formateado
                SangreModelo.SUPPLY_VALUES[i] // Valor de pintas de suministro
            });
            ini = fin; // El fin del rango actual se convierte en el inicio del siguiente
        }

        // Tabla de pacientes con rangos acumulados
        modeloPacientes.setRowCount(0); // Limpia todas las filas de la tabla de pacientes
        acum = 0; // Reinicia el acumulador
        ini = 0; // Reinicia el inicio del rango
        for(int i = 0; i < SangreModelo.PACIENTES_VALUES.length; i++){ // Itera sobre cada valor de número de pacientes
            double p = SangreModelo.PACIENTES_PROBS[i]; // Obtiene la probabilidad del número de pacientes actual
            acum += p; // Acumula la probabilidad
            if(i == SangreModelo.PACIENTES_VALUES.length-1) acum = 1.0; // Asegura que el último valor acumulado sea exactamente 1.0
            double fin = acum; // Establece el fin del rango
            modeloPacientes.addRow(new Object[]{ // Agrega una nueva fila a la tabla de pacientes
                UtilFormatoSangre.fmt2(p), // Probabilidad formateada
                UtilFormatoSangre.fmt2(acum), // Probabilidad acumulada formateada
                UtilFormatoSangre.fmt2(ini), // Inicio del rango formateado
                UtilFormatoSangre.fmt2(fin), // Fin del rango formateado
                SangreModelo.PACIENTES_VALUES[i] // Número de pacientes
            });
            ini = fin; // Actualiza el inicio para el siguiente rango
        }

        // Tabla de demanda por paciente con rangos acumulados
        modeloDemanda.setRowCount(0); // Limpia todas las filas de la tabla de demanda
        acum = 0; // Reinicia el acumulador
        ini = 0; // Reinicia el inicio del rango
        for(int i = 0; i < SangreModelo.DEMANDA_VALUES.length; i++){ // Itera sobre cada valor de demanda por paciente
            double p = SangreModelo.DEMANDA_PROBS[i]; // Obtiene la probabilidad de la demanda actual
            acum += p; // Acumula la probabilidad
            if(i == SangreModelo.DEMANDA_VALUES.length-1) acum = 1.0; // Asegura que el último valor acumulado sea exactamente 1.0
            double fin = acum; // Establece el fin del rango
            modeloDemanda.addRow(new Object[]{ // Agrega una nueva fila a la tabla de demanda
                UtilFormatoSangre.fmt2(p), // Probabilidad formateada
                UtilFormatoSangre.fmt2(acum), // Probabilidad acumulada formateada
                UtilFormatoSangre.fmt2(ini), // Inicio del rango formateado
                UtilFormatoSangre.fmt2(fin), // Fin del rango formateado
                SangreModelo.DEMANDA_VALUES[i] // Pintas de demanda por paciente
            });
            ini = fin; // Actualiza el inicio para el siguiente rango
        }
    }

    private void crearTabla(ActionEvent e){ // Método que se ejecuta cuando se presiona el botón crear tabla
        int semanas = (int) spSemanas.getValue(); // Obtiene el número de semanas seleccionado del spinner
        modeloInput.setRowCount(0); // Limpia todas las filas de la tabla de entrada
        modeloSim.setRowCount(0); // Limpia todas las filas de la tabla de simulación

        for(int s = 1; s <= semanas; s++){ // Itera sobre cada semana para crear las filas necesarias
            modeloInput.addRow(new Object[]{ // Agrega una nueva fila a la tabla de entrada
                s,          // Número de semana
                "",         // Campo vacío para #Aleatorio.Suministro (para entrada manual)
                ""          // Campo vacío para #Aleatorio.Pacientes (para entrada manual)
            });
        }
        btnCalcular.setEnabled(true); // Activa el botón calcular después de crear la tabla
    }

    private Double parseRand(Object obj){ // Método para validar y convertir un valor a número aleatorio válido
        if(obj == null) return null; // Si el valor es nulo, retorna null
        String t = obj.toString().trim(); // Convierte a string y quita espacios en blanco
        if(t.isEmpty()) return null; // Si está vacío, retorna null
        try{ // Intenta convertir el string a número
            double v = Double.parseDouble(t.replace(',','.')); // Convierte el string a double, reemplazando coma por punto
            if(v < 0 || v >= 1){ // Valida que esté en el rango [0,1)
                return null; // Si no está en el rango válido, retorna null
            }
            return v; // Retorna el número válido
        }catch(Exception ex){ // Si hay error en la conversión
            return null; // Retorna null
        }
    }

    private void calcular(ActionEvent e){ // Método que se ejecuta cuando se presiona el botón calcular
        int inventario = (int) spInvInicial.getValue(); // Obtiene el inventario inicial seleccionado del spinner
        modeloSim.setRowCount(0); // Limpia todas las filas de la tabla de simulación

        for(int r = 0; r < modeloInput.getRowCount(); r++){ // Itera sobre cada fila de la tabla de entrada
            int semana = r + 1; // Calcula el número de semana (índice + 1)

            // VALIDAR Y OBTENER SUMINISTRO
            Double rSup = parseRand(modeloInput.getValueAt(r, 1)); // Obtiene y valida el número aleatorio para suministro
            if(rSup == null){ // Si el número aleatorio no es válido
                mensajeError(semana, "Suministro"); // Muestra mensaje de error específico
                return; // Sale del método sin continuar
            }
            int suministro = SangreModelo.suministro(rSup); // Calcula las pintas de suministro basado en el número aleatorio
            int sangreTotal = inventario + suministro; // Calcula el total de sangre disponible (inventario + suministro)

            // VALIDAR Y OBTENER PACIENTES
            Double rPac = parseRand(modeloInput.getValueAt(r, 2)); // Obtiene y valida el número aleatorio para pacientes
            if(rPac == null){ // Si el número aleatorio no es válido
                mensajeError(semana, "Pacientes"); // Muestra mensaje de error específico
                return; // Sale del método sin continuar
            }
            int numPacientes = SangreModelo.pacientes(rPac); // Calcula el número de pacientes basado en el número aleatorio

            int sangreRestante = sangreTotal; // Inicializa la sangre restante con el total disponible

            // Si no hay pacientes, agregar una sola fila
            if(numPacientes == 0) { // Si no hay pacientes en esta semana
                modeloSim.addRow(new Object[]{ // Agrega una sola fila a la tabla de simulación
                    semana,                            // Número de semana
                    inventario,                        // Inventario inicial de la semana
                    UtilFormatoSangre.fmt2(rSup),     // #Aleatorio suministro formateado
                    suministro,                        // Pintas suministradas
                    sangreTotal,                       // Sangre disponible total
                    UtilFormatoSangre.fmt2(rPac),     // #Aleatorio pacientes formateado
                    numPacientes,                      // Número de pacientes (0)
                    "",                                // Nro de paciente (vacío)
                    "",                                // #Aleatorio demanda (vacío)
                    "",                                // Pintas demanda (vacío)
                    sangreRestante                     // Pintas restantes (igual al total disponible)
                });
            } else { // Si hay pacientes en esta semana
                // Procesar cada paciente individual (NÚMEROS ALEATORIOS AUTOMÁTICOS)
                for(int p = 0; p < numPacientes && p < 4; p++){ // Itera sobre cada paciente (máximo 4 para evitar tablas excesivamente largas)
                    // GENERAR AUTOMÁTICAMENTE el número aleatorio para la demanda
                    double rDem = Math.random(); // Genera número aleatorio automáticamente para la demanda de este paciente específico
                    int demanda = SangreModelo.demandaPaciente(rDem); // Calcula la demanda de pintas para este paciente

                    sangreRestante -= demanda; // Resta la demanda del paciente de la sangre disponible
                    if(sangreRestante < 0) sangreRestante = 0; // Asegura que la sangre restante no sea negativa

                    // Solo mostrar datos de semana, suministro y pacientes en la primera fila
                    Object colSemana = (p == 0) ? semana : ""; // Muestra el número de semana solo en la primera fila del grupo
                    Object colInventario = (p == 0) ? inventario : ""; // Muestra el inventario inicial solo en la primera fila
                    Object colRandSup = (p == 0) ? UtilFormatoSangre.fmt2(rSup) : ""; // Muestra el número aleatorio de suministro solo en la primera fila
                    Object colSuministro = (p == 0) ? suministro : ""; // Muestra las pintas de suministro solo en la primera fila
                    Object colSangreTotal = (p == 0) ? sangreTotal : ""; // Muestra el total de sangre solo en la primera fila
                    Object colRandPac = (p == 0) ? UtilFormatoSangre.fmt2(rPac) : ""; // Muestra el número aleatorio de pacientes solo en la primera fila
                    Object colNumPac = (p == 0) ? numPacientes : ""; // Muestra el número total de pacientes solo en la primera fila

                    modeloSim.addRow(new Object[]{ // Agrega una fila para este paciente específico
                        colSemana,                         // Semana (solo mostrada en la primera fila del grupo)
                        colInventario,                     // Inventario inicial (solo en primera fila)
                        colRandSup,                        // #Aleatorio suministro (solo en primera fila)
                        colSuministro,                     // Pintas suministradas (solo en primera fila)
                        colSangreTotal,                    // Sangre disponible total (solo en primera fila)
                        colRandPac,                        // #Aleatorio pacientes (solo en primera fila)
                        colNumPac,                         // Número de pacientes (solo en primera fila)
                        p + 1,                             // Nro de paciente (1, 2, 3, 4)
                        UtilFormatoSangre.fmt2(rDem),     // #Aleatorio demanda de este paciente formateado (GENERADO AUTOMÁTICAMENTE)
                        demanda,                           // Pintas demandadas por este paciente
                        sangreRestante                     // Pintas restantes después de atender a este paciente
                    });
                }
            }

            inventario = sangreRestante; // El inventario para la siguiente semana es la sangre que sobró de esta semana
        }

        JOptionPane.showMessageDialog(this, // Muestra mensaje de confirmación al completar la simulación
            "Simulación completada exitosamente!\n" + // Mensaje de éxito
            "Los números aleatorios para la demanda de pacientes se generaron automáticamente.", // Recordatorio sobre la generación automática
            "Cálculo terminado", // Título del mensaje
            JOptionPane.INFORMATION_MESSAGE); // Tipo de mensaje informativo
    }

    private void mensajeError(int semana, String campo){ // Método para mostrar mensajes de error específicos
        JOptionPane.showMessageDialog(this, // Muestra un diálogo de error
            "Semana " + semana + ": número aleatorio inválido para " + campo + // Especifica la semana y campo con error
            "\n(debe estar en [0,1) y no estar vacío)", // Explica el formato requerido
            "Dato inválido", // Título del mensaje de error
            JOptionPane.ERROR_MESSAGE); // Tipo de mensaje de error
    }
}