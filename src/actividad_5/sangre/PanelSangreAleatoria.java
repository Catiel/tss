package actividad_5.sangre; // Define el paquete donde se encuentra esta clase para el módulo de simulación de sangre

import javax.swing.*; // Importa todas las clases de Swing para crear la interfaz gráfica
import javax.swing.table.DefaultTableModel; // Importa el modelo de tabla por defecto para manejar datos de tablas
import java.awt.*; // Importa clases para manejo de componentes gráficos y layouts
import java.awt.event.ActionEvent; // Importa la clase para manejar eventos de acción

/** Panel que genera números aleatorios para simular varias semanas. */ // Comentario de documentación que explica el propósito del panel
public class PanelSangreAleatoria extends JPanel { // Declara la clase que extiende JPanel para crear un panel personalizado de simulación aleatoria
    private final JSpinner spSemanas; // Declara spinner para seleccionar el número de semanas a simular
    private final JSpinner spInvInicial; // Declara spinner para establecer el inventario inicial de sangre
    private final JButton btnSimular; // Declara el botón para ejecutar la simulación

    private final DefaultTableModel modeloSupply; // Declara el modelo de datos para la tabla de suministro de sangre
    private final DefaultTableModel modeloPacientes; // Declara el modelo de datos para la tabla de número de pacientes
    private final DefaultTableModel modeloDemanda; // Declara el modelo de datos para la tabla de demanda por paciente
    private final DefaultTableModel modeloSim; // Declara el modelo de datos para la tabla principal de simulación

    public PanelSangreAleatoria(){ // Constructor de la clase
        setLayout(new BorderLayout(8,8)); // Establece el layout del panel con separación de 8 píxeles
        EstilosUI.aplicarEstiloPanel(this); // Aplica el estilo visual al panel
        JLabel titulo = new JLabel("Simulación aleatoria de plasma (números generados automáticamente)"); // Crea la etiqueta del título
        EstilosUI.aplicarEstiloTitulo(titulo); // Aplica el estilo del título
        add(titulo, BorderLayout.NORTH); // Agrega el título en la parte superior del panel

        // Controles superiores
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Crea el panel de controles con layout de flujo alineado a la izquierda
        EstilosUI.aplicarEstiloPanel(controles); // Aplica el estilo al panel de controles
        controles.add(new JLabel("Semanas:")); // Agrega la etiqueta para el número de semanas
        spSemanas = new JSpinner(new SpinnerNumberModel(6,1,52,1)); // Inicializa el spinner con valor inicial 6, mínimo 1, máximo 52, incremento 1
        controles.add(spSemanas); // Agrega el spinner de semanas al panel de controles
        controles.add(new JLabel("Inventario inicial:")); // Agrega la etiqueta para el inventario inicial
        spInvInicial = new JSpinner(new SpinnerNumberModel(0,0,1000,1)); // Inicializa el spinner con valor inicial 0, mínimo 0, máximo 1000, incremento 1
        controles.add(spInvInicial); // Agrega el spinner de inventario inicial al panel de controles
        btnSimular = new JButton("Simular"); // Crea el botón de simulación
        EstilosUI.aplicarEstiloBoton(btnSimular); // Aplica el estilo al botón
        controles.add(btnSimular); // Agrega el botón al panel de controles
        add(controles, BorderLayout.BEFORE_FIRST_LINE); // Agrega el panel de controles en la parte superior antes del contenido principal

        // Panel izquierdo con tablas de distribuciones
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

        add(panelIzq, BorderLayout.WEST); // Agrega el panel izquierdo al lado oeste del panel principal

        // Tabla de simulación principal
        modeloSim = new DefaultTableModel(new Object[]{ // Inicializa el modelo de la tabla de simulación principal con múltiples columnas
                "Semana","Inventario Inicial","#Aleatorio","Pintas","Sangre Disponible Total", // Columnas para datos semanales y de suministro
                "#Aleatorio","#Pacientes","Nro de Paciente","#Aleatorio","Pintas","#Pintas Restantes"},0){ // Columnas para datos de pacientes y demanda
            @Override public boolean isCellEditable(int r,int c){return false;} // Sobrescribe el método para hacer todas las celdas no editables
            @Override public Class<?> getColumnClass(int c){ // Sobrescribe el método para definir el tipo de datos de cada columna
                if(c == 0 || c == 1 || c == 3 || c == 4 || c == 6 || c == 7 || c == 9 || c == 10) return Integer.class; // Columnas numéricas enteras
                return String.class; // Las demás columnas son String (números aleatorios formateados)
            }
        };
        JTable tabla = new JTable(modeloSim); // Crea la tabla de simulación usando el modelo
        EstilosUI.aplicarEstiloTabla(tabla); // Aplica el estilo a la tabla
        JScrollPane scrollSim = new JScrollPane(tabla); // Crea un panel de desplazamiento para la tabla
        scrollSim.setPreferredSize(new Dimension(900, 400)); // Establece el tamaño preferido del panel de desplazamiento
        add(scrollSim, BorderLayout.CENTER); // Agrega el panel de desplazamiento al centro

        btnSimular.addActionListener(this::simular); // Agrega un listener al botón que ejecuta el método simular

        JTextArea descripcion = new JTextArea("Configure el número de semanas e inventario inicial, luego presione 'Simular'. " + // Crea área de texto con descripción
            "Los números aleatorios se generan automáticamente usando Math.random(). Cada paciente tiene su propia fila."); // Continúa la descripción
        descripcion.setWrapStyleWord(true); // Habilita el ajuste de línea por palabra
        descripcion.setLineWrap(true); // Habilita el ajuste automático de línea
        descripcion.setEditable(false); // Hace el área de texto no editable
        descripcion.setBackground(getBackground()); // Establece el mismo color de fondo que el panel
        add(descripcion, BorderLayout.SOUTH); // Agrega la descripción en la parte inferior
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

    private double rand(){ // Método auxiliar para generar números aleatorios
        return Math.random(); // Retorna un número aleatorio entre 0.0 (inclusive) y 1.0 (exclusivo)
    }

    private void simular(ActionEvent e){ // Método que se ejecuta cuando se presiona el botón simular
        int semanas = (int) spSemanas.getValue(); // Obtiene el número de semanas seleccionado del spinner
        int inventario = (int) spInvInicial.getValue(); // Obtiene el inventario inicial seleccionado del spinner
        modeloSim.setRowCount(0); // Limpia todas las filas de la tabla de simulación

        for(int sem = 1; sem <= semanas; sem++){ // Itera sobre cada semana a simular
            // Calcular suministro de la semana (ALEATORIO)
            double rSup = rand(); // Genera número aleatorio para determinar el suministro
            int suministro = SangreModelo.suministro(rSup); // Calcula las pintas de suministro basado en el número aleatorio
            int sangreTotal = inventario + suministro; // Calcula el total de sangre disponible (inventario + suministro)

            // Calcular número de pacientes de la semana (ALEATORIO)
            double rPac = rand(); // Genera número aleatorio para determinar el número de pacientes
            int numPacientes = SangreModelo.pacientes(rPac); // Calcula el número de pacientes basado en el número aleatorio

            int sangreRestante = sangreTotal; // Inicializa la sangre restante con el total disponible

            // Si no hay pacientes, agregar una sola fila
            if(numPacientes == 0) { // Si no hay pacientes en esta semana
                modeloSim.addRow(new Object[]{ // Agrega una sola fila a la tabla de simulación
                    sem,                               // Número de semana
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
                // Agregar una fila por cada paciente
                for(int p = 0; p < numPacientes && p < 4; p++){ // Itera sobre cada paciente (máximo 4 para evitar tablas excesivamente largas)
                    // Generar número aleatorio para la demanda de este paciente
                    double rDem = rand(); // Genera número aleatorio para la demanda de este paciente específico
                    int demanda = SangreModelo.demandaPaciente(rDem); // Calcula la demanda de pintas para este paciente

                    sangreRestante -= demanda; // Resta la demanda del paciente de la sangre disponible
                    if(sangreRestante < 0) sangreRestante = 0; // Asegura que la sangre restante no sea negativa

                    // Solo mostrar datos de semana, suministro y pacientes en la primera fila
                    Object colSemana = (p == 0) ? sem : ""; // Muestra el número de semana solo en la primera fila del grupo
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
                        UtilFormatoSangre.fmt2(rDem),     // #Aleatorio demanda de este paciente formateado
                        demanda,                           // Pintas demandadas por este paciente
                        sangreRestante                     // Pintas restantes después de atender a este paciente
                    });
                }
            }

            inventario = sangreRestante; // El inventario para la siguiente semana es la sangre que sobró de esta semana
        }
    }
}