package actividad_5.sangre; // Define el paquete donde se encuentra esta clase para el módulo de simulación de sangre

import javax.swing.*; // Importa todas las clases de Swing para crear la interfaz gráfica
import javax.swing.table.DefaultTableModel; // Importa el modelo de tabla por defecto para manejar datos de tablas
import java.awt.*; // Importa clases para manejo de componentes gráficos y layouts

/** Panel con números aleatorios proporcionados por el ejemplo (6 semanas). */ // Comentario de documentación que explica el propósito del panel con datos predefinidos
public class PanelSangrePredefinida extends JPanel { // Declara la clase que extiende JPanel para crear un panel personalizado con simulación predefinida
    private final DefaultTableModel modeloDistribuciones; // Declara el modelo de datos para la tabla combinada inicial que muestra las tres distribuciones juntas
    private final DefaultTableModel modeloSupply; // Declara el modelo de datos para la tabla derivada de suministro de sangre
    private final DefaultTableModel modeloPacientes; // Declara el modelo de datos para la tabla derivada de número de pacientes
    private final DefaultTableModel modeloDemanda; // Declara el modelo de datos para la tabla derivada de demanda por paciente
    private final DefaultTableModel modeloSim; // Declara el modelo de datos para la tabla de simulación semanal

    // Números aleatorios del ejemplo por semana
    private final double[] RAND_SUPPLY = {0.59,0.22,0.39,0.06,0.85,0.08}; // Array de números aleatorios predefinidos para el suministro de cada una de las 6 semanas
    private final double[] RAND_PACIENTES = {0.27,0.51,0.67,0.91,0.56,0.27}; // Array de números aleatorios predefinidos para el número de pacientes de cada una de las 6 semanas
    // Matriz de números de demanda por paciente por semana (max 4)
    private final double[][] RAND_DEM = { // Matriz bidimensional de números aleatorios predefinidos para la demanda individual de cada paciente por semana
            {0.79, Double.NaN, Double.NaN, Double.NaN}, // Semana 1: solo 1 paciente, por eso los otros 3 valores son NaN (No es un Número)
            {0.42,0.30,Double.NaN,Double.NaN}, // Semana 2: 2 pacientes, por eso solo los primeros 2 valores son válidos
            {0.71,0.36,Double.NaN,Double.NaN}, // Semana 3: 2 pacientes, por eso solo los primeros 2 valores son válidos
            {0.72,0.86,0.33,Double.NaN}, // Semana 4: 3 pacientes, por eso los primeros 3 valores son válidos
            {0.63,0.93,Double.NaN,Double.NaN}, // Semana 5: 2 pacientes, por eso solo los primeros 2 valores son válidos
            {0.60,Double.NaN,Double.NaN,Double.NaN} // Semana 6: solo 1 paciente, por eso los otros 3 valores son NaN
    };

    public PanelSangrePredefinida(){ // Constructor de la clase
        setLayout(new BorderLayout(8,8)); // Establece el layout del panel con separación de 8 píxeles
        EstilosUI.aplicarEstiloPanel(this); // Aplica el estilo visual al panel

        JLabel titulo = new JLabel("Simulación de plasma (números proporcionados del ejemplo)"); // Crea la etiqueta del título
        EstilosUI.aplicarEstiloTitulo(titulo); // Aplica el estilo del título
        add(titulo, BorderLayout.NORTH); // Agrega el título en la parte superior del panel

        // Panel izquierdo con todas las tablas de distribuciones
        JPanel panelIzq = new JPanel(new GridLayout(4,1,5,5)); // Crea el panel izquierdo con layout de grilla (4 filas, 1 columna, separación 5px)
        EstilosUI.aplicarEstiloPanel(panelIzq); // Aplica el estilo al panel izquierdo

        // 1. Tabla combinada original (solo para referencia)
        modeloDistribuciones = new DefaultTableModel(new Object[]{ // Inicializa el modelo de la tabla combinada con 6 columnas que muestran las 3 distribuciones lado a lado
                "Pintas/Entrega", "Probabilidad", "Pacientes/Semana", "Probabilidad", "Pintas/Paciente", "Probabilidad"},0){ // Define columnas alternando valores y probabilidades para las 3 distribuciones
            @Override public boolean isCellEditable(int r,int c){return false;} // Sobrescribe el método para hacer todas las celdas no editables
        };
        llenarTablaCombinada(); // Llama al método para llenar la tabla combinada con datos del modelo
        JTable tablaComb = new JTable(modeloDistribuciones); // Crea la tabla combinada usando el modelo
        EstilosUI.aplicarEstiloTabla(tablaComb); // Aplica el estilo a la tabla
        panelIzq.add(new JScrollPane(tablaComb)); // Agrega la tabla con scroll al panel izquierdo

        // 2. Tabla de suministro con rangos
        modeloSupply = new DefaultTableModel(new Object[]{"Prob","Acum","Ini","Fin","Pintas"},0){ // Inicializa el modelo de la tabla de suministro con columnas de probabilidad, acumulada, inicio, fin y pintas
            @Override public boolean isCellEditable(int r,int c){return false;} // Sobrescribe el método para hacer todas las celdas no editables
        };
        JTable tSup = new JTable(modeloSupply); // Crea la tabla de suministro usando el modelo
        EstilosUI.aplicarEstiloTabla(tSup); // Aplica el estilo a la tabla
        tSup.getTableHeader().setBackground(new Color(200, 255, 200)); // Establece el color de fondo del encabezado (verde claro)
        panelIzq.add(new JScrollPane(tSup)); // Agrega la tabla con scroll al panel izquierdo

        // 3. Tabla de pacientes con rangos
        modeloPacientes = new DefaultTableModel(new Object[]{"Prob","Acum","Ini","Fin","#Pac"},0){ // Inicializa el modelo de la tabla de pacientes con columnas similares pero con número de pacientes
            @Override public boolean isCellEditable(int r,int c){return false;} // Sobrescribe el método para hacer todas las celdas no editables
        };
        JTable tPac = new JTable(modeloPacientes); // Crea la tabla de pacientes usando el modelo
        EstilosUI.aplicarEstiloTabla(tPac); // Aplica el estilo a la tabla
        tPac.getTableHeader().setBackground(new Color(200, 220, 255)); // Establece el color de fondo del encabezado (azul claro)
        panelIzq.add(new JScrollPane(tPac)); // Agrega la tabla con scroll al panel izquierdo

        // 4. Tabla de demanda por paciente con rangos
        modeloDemanda = new DefaultTableModel(new Object[]{"Prob","Acum","Ini","Fin","Pintas"},0){ // Inicializa el modelo de la tabla de demanda con columnas de probabilidad, acumulada, inicio, fin y pintas
            @Override public boolean isCellEditable(int r,int c){return false;} // Sobrescribe el método para hacer todas las celdas no editables
        };
        JTable tDem = new JTable(modeloDemanda); // Crea la tabla de demanda usando el modelo
        EstilosUI.aplicarEstiloTabla(tDem); // Aplica el estilo a la tabla
        tDem.getTableHeader().setBackground(new Color(255, 200, 255)); // Establece el color de fondo del encabezado (rosa claro)
        panelIzq.add(new JScrollPane(tDem)); // Agrega la tabla con scroll al panel izquierdo

        llenarDerivadas(); // Llama al método para llenar las tablas derivadas con rangos calculados
        add(panelIzq, BorderLayout.WEST); // Agrega el panel izquierdo al lado oeste del panel principal

        // Tabla de simulación principal (lado derecho)
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

        JScrollPane spSim = new JScrollPane(tablaSim); // Crea un panel de desplazamiento para la tabla de simulación
        spSim.setPreferredSize(new Dimension(900, 400)); // Establece el tamaño preferido del panel de desplazamiento
        add(spSim, BorderLayout.CENTER); // Agrega el panel de desplazamiento al centro

        simular(); // Ejecuta inmediatamente la simulación con los datos predefinidos

        JTextArea descripcion = new JTextArea("Simulación con datos del ejemplo: 6 semanas con inventario inicial 0. " + // Crea área de texto con descripción de la simulación
            "Los números aleatorios están predefinidos según el ejercicio. Cada paciente tiene su propia fila."); // Continúa la descripción explicando el formato de salida
        descripcion.setWrapStyleWord(true); // Habilita el ajuste de línea por palabra
        descripcion.setLineWrap(true); // Habilita el ajuste automático de línea
        descripcion.setEditable(false); // Hace el área de texto no editable
        descripcion.setBackground(getBackground()); // Establece el mismo color de fondo que el panel
        add(descripcion, BorderLayout.SOUTH); // Agrega la descripción en la parte inferior
    }

    private void llenarTablaCombinada(){ // Método para llenar la tabla combinada que muestra las tres distribuciones lado a lado
        modeloDistribuciones.setRowCount(0); // Limpia todas las filas de la tabla combinada
        int filas = Math.max(SangreModelo.SUPPLY_VALUES.length, // Calcula el número máximo de filas necesario comparando las longitudes de los tres arrays
                    Math.max(SangreModelo.PACIENTES_VALUES.length, SangreModelo.DEMANDA_VALUES.length)); // Toma el máximo entre suministro, pacientes y demanda

        for(int i = 0; i < filas; i++){ // Itera sobre el número máximo de filas calculado
            String sVal = i < SangreModelo.SUPPLY_VALUES.length ? String.valueOf(SangreModelo.SUPPLY_VALUES[i]) : ""; // Obtiene el valor de suministro si existe, sino cadena vacía
            String sProb = i < SangreModelo.SUPPLY_PROBS.length ? UtilFormatoSangre.fmt2(SangreModelo.SUPPLY_PROBS[i]) : ""; // Obtiene la probabilidad de suministro formateada si existe
            String pVal = i < SangreModelo.PACIENTES_VALUES.length ? String.valueOf(SangreModelo.PACIENTES_VALUES[i]) : ""; // Obtiene el valor de pacientes si existe, sino cadena vacía
            String pProb = i < SangreModelo.PACIENTES_PROBS.length ? UtilFormatoSangre.fmt2(SangreModelo.PACIENTES_PROBS[i]) : ""; // Obtiene la probabilidad de pacientes formateada si existe
            String dVal = i < SangreModelo.DEMANDA_VALUES.length ? String.valueOf(SangreModelo.DEMANDA_VALUES[i]) : ""; // Obtiene el valor de demanda si existe, sino cadena vacía
            String dProb = i < SangreModelo.DEMANDA_PROBS.length ? UtilFormatoSangre.fmt2(SangreModelo.DEMANDA_PROBS[i]) : ""; // Obtiene la probabilidad de demanda formateada si existe
            modeloDistribuciones.addRow(new Object[]{sVal,sProb,pVal,pProb,dVal,dProb}); // Agrega una fila con los 6 valores (3 distribuciones x 2 columnas cada una)
        }
    }

    private void llenarDerivadas(){ // Método para llenar las tablas derivadas con rangos calculados para cada distribución
        // Tabla de suministro con rangos acumulados
        double acum = 0; // Inicializa el acumulador de probabilidades
        double ini = 0; // Inicializa el inicio del rango
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
        acum = 0; // Reinicia el acumulador para la tabla de pacientes
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
        acum = 0; // Reinicia el acumulador para la tabla de demanda
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

    private void simular(){ // Método para ejecutar la simulación completa usando los números aleatorios predefinidos
        modeloSim.setRowCount(0); // Limpia todas las filas de la tabla de simulación
        int inventario = 0; // Inicializa el inventario inicial de la primera semana en 0

        for(int semana = 0; semana < RAND_SUPPLY.length; semana++){ // Itera sobre cada semana (0-5) usando la longitud del array de suministro
            // Calcular suministro de la semana
            double rSup = RAND_SUPPLY[semana]; // Obtiene el número aleatorio predefinido para el suministro de esta semana
            int suministro = SangreModelo.suministro(rSup); // Calcula las pintas de suministro basado en el número aleatorio
            int sangreTotal = inventario + suministro; // Calcula el total de sangre disponible (inventario + suministro)

            // Calcular número de pacientes de la semana
            double rPac = RAND_PACIENTES[semana]; // Obtiene el número aleatorio predefinido para los pacientes de esta semana
            int numPacientes = SangreModelo.pacientes(rPac); // Calcula el número de pacientes basado en el número aleatorio

            int sangreRestante = sangreTotal; // Inicializa la sangre restante con el total disponible

            // Si no hay pacientes, agregar una sola fila
            if(numPacientes == 0) { // Si no hay pacientes en esta semana
                modeloSim.addRow(new Object[]{ // Agrega una sola fila a la tabla de simulación
                    semana + 1,                        // Número de semana (convertido de índice base-0 a base-1)
                    inventario,                        // Inventario inicial de la semana
                    UtilFormatoSangre.fmt2(rSup),     // Número aleatorio para suministro formateado
                    suministro,                        // Pintas de suministro recibidas
                    sangreTotal,                       // Total de sangre disponible
                    UtilFormatoSangre.fmt2(rPac),     // Número aleatorio para pacientes formateado
                    numPacientes,                      // Número de pacientes (0)
                    "",                                // Número de paciente individual (vacío porque no hay pacientes)
                    "",                                // Número aleatorio para demanda (vacío porque no hay pacientes)
                    "",                                // Pintas de demanda (vacío porque no hay pacientes)
                    sangreRestante                     // Pintas restantes (igual al total disponible)
                });
            } else { // Si hay pacientes en esta semana
                // Agregar una fila por cada paciente
                for(int p = 0; p < numPacientes && p < 4; p++){ // Itera sobre cada paciente (máximo 4 para evitar tablas excesivamente largas)
                    double rDem = RAND_DEM[semana][p]; // Obtiene el número aleatorio predefinido para la demanda de este paciente específico
                    if(Double.isNaN(rDem)) break; // Si el valor es NaN (No es un Número), termina el bucle porque no hay más pacientes

                    int demanda = SangreModelo.demandaPaciente(rDem); // Calcula la demanda de pintas para este paciente
                    sangreRestante -= demanda; // Resta la demanda del paciente de la sangre disponible
                    if(sangreRestante < 0) sangreRestante = 0; // Asegura que la sangre restante no sea negativa

                    // Solo mostrar datos de semana, suministro y pacientes en la primera fila
                    Object colSemana = (p == 0) ? (semana + 1) : ""; // Muestra el número de semana solo en la primera fila del grupo de pacientes
                    Object colInventario = (p == 0) ? inventario : ""; // Muestra el inventario inicial solo en la primera fila
                    Object colRandSup = (p == 0) ? UtilFormatoSangre.fmt2(rSup) : ""; // Muestra el número aleatorio de suministro solo en la primera fila
                    Object colSuministro = (p == 0) ? suministro : ""; // Muestra las pintas de suministro solo en la primera fila
                    Object colSangreTotal = (p == 0) ? sangreTotal : ""; // Muestra el total de sangre solo en la primera fila
                    Object colRandPac = (p == 0) ? UtilFormatoSangre.fmt2(rPac) : ""; // Muestra el número aleatorio de pacientes solo en la primera fila
                    Object colNumPac = (p == 0) ? numPacientes : ""; // Muestra el número total de pacientes solo en la primera fila

                    modeloSim.addRow(new Object[]{ // Agrega una fila para este paciente específico
                        colSemana,                         // Semana (solo mostrada en la primera fila del grupo)
                        colInventario,                     // Inventario inicial (solo en primera fila)
                        colRandSup,                        // Número aleatorio de suministro (solo en primera fila)
                        colSuministro,                     // Pintas suministradas (solo en primera fila)
                        colSangreTotal,                    // Sangre disponible total (solo en primera fila)
                        colRandPac,                        // Número aleatorio de pacientes (solo en primera fila)
                        colNumPac,                         // Número total de pacientes (solo en primera fila)
                        p + 1,                             // Número del paciente individual (1, 2, 3, 4)
                        UtilFormatoSangre.fmt2(rDem),     // Número aleatorio para demanda de este paciente formateado
                        demanda,                           // Pintas demandadas por este paciente
                        sangreRestante                     // Pintas restantes después de atender a este paciente
                    });
                }
            }

            inventario = sangreRestante; // El inventario para la siguiente semana es la sangre que sobró de esta semana
        }
    }
}