package actividad_5.dulce; // Define el paquete donde se encuentra esta clase

import javax.swing.*; // Importa todas las clases de Swing para crear la interfaz gráfica
import javax.swing.table.DefaultTableModel; // Importa el modelo de tabla por defecto para manejar datos de tablas
import java.awt.*; // Importa clases para manejo de componentes gráficos y layouts
import java.awt.event.ActionEvent; // Importa la clase para manejar eventos de acción

/** Panel para ingresar manualmente números aleatorios y simular ganancias */ // Comentario de documentación de la clase
public class PanelDulceManual extends JPanel { // Declara la clase que extiende JPanel para crear un panel personalizado
    private final JSpinner spReplicas; // Declara un spinner para seleccionar el número de réplicas
    private final JButton btnCrear; // Declara el botón para crear la tabla de entrada
    private final JButton btnCalcular; // Declara el botón para calcular los resultados
    private final DefaultTableModel modeloDist; // Declara el modelo de datos para la tabla de distribución
    private final DefaultTableModel modeloInput; // Declara el modelo de datos para la tabla de entrada de números aleatorios
    private final DefaultTableModel modeloSim; // Declara el modelo de datos para la tabla de simulación
    private final DefaultTableModel modeloComp; // Declara el modelo de datos para la tabla de comparación
    private final JTextField txtDecisiones; // Declara el campo de texto para ingresar las decisiones Q (lista separada por comas)

    public PanelDulceManual() { // Constructor de la clase
        setLayout(new BorderLayout(8, 8)); // Establece el layout del panel con separación de 8 píxeles
        EstilosUI.aplicarEstiloPanel(this); // Aplica el estilo visual al panel

        JLabel titulo = new JLabel("Dulce Ada - Simulación manual"); // Crea la etiqueta del título
        EstilosUI.aplicarEstiloTitulo(titulo); // Aplica el estilo del título
        add(titulo, BorderLayout.NORTH); // Agrega el título en la parte superior del panel

        // Controles superiores
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Crea el panel de controles con layout de flujo alineado a la izquierda
        EstilosUI.aplicarEstiloPanel(controles); // Aplica el estilo al panel de controles
        controles.add(new JLabel("Número de réplicas:")); // Agrega la etiqueta para el número de réplicas
        spReplicas = new JSpinner(new SpinnerNumberModel(100, 10, 500, 10)); // Inicializa el spinner con valor inicial 100, mínimo 10, máximo 500, incremento 10
        controles.add(spReplicas); // Agrega el spinner al panel de controles
        controles.add(new JLabel("Decisiones Q (coma):")); // Agrega la etiqueta para las decisiones Q
        txtDecisiones = new JTextField("40,50,60,70,80,90",14); // Crea el campo de texto con valores por defecto y ancho de 14 columnas
        controles.add(txtDecisiones); // Agrega el campo de texto al panel de controles
        btnCrear = new JButton("Crear tabla de entrada"); // Crea el botón para crear la tabla de entrada
        EstilosUI.aplicarEstiloBoton(btnCrear); // Aplica el estilo al botón crear
        controles.add(btnCrear); // Agrega el botón crear al panel de controles
        btnCalcular = new JButton("Calcular resultados"); // Crea el botón para calcular resultados
        EstilosUI.aplicarEstiloBoton(btnCalcular); // Aplica el estilo al botón calcular
        controles.add(btnCalcular); // Agrega el botón calcular al panel de controles
        add(controles, BorderLayout.BEFORE_FIRST_LINE); // Agrega el panel de controles en la parte superior antes del contenido principal

        // Panel principal dividido
        JPanel panelPrincipal = new JPanel(new BorderLayout(8, 8)); // Crea el panel principal con layout BorderLayout y separación de 8 píxeles
        EstilosUI.aplicarEstiloPanel(panelPrincipal); // Aplica el estilo al panel principal

        // Panel izquierdo: distribución y entrada de datos
        JPanel panelIzq = new JPanel(); // Crea el panel izquierdo
        panelIzq.setLayout(new BoxLayout(panelIzq, BoxLayout.Y_AXIS)); // Establece layout vertical (BoxLayout en eje Y)
        EstilosUI.aplicarEstiloPanel(panelIzq); // Aplica el estilo al panel izquierdo

        // Tabla de distribución (referencia)
        modeloDist = new DefaultTableModel(new Object[]{ // Inicializa el modelo de la tabla de distribución con las columnas
            "Probabilidad", "Distribución acumulada", "Rangos de #s aleatorios", "Demanda" // Define los nombres de las columnas
        }, 0) { // Establece 0 filas iniciales
            @Override
            public boolean isCellEditable(int r, int c) { // Sobrescribe el método para determinar si una celda es editable
                return false; // Hace que todas las celdas sean no editables
            }
        };
        llenarDistribucion(); // Llama al método para llenar la tabla de distribución con datos

        JTable tDist = new JTable(modeloDist); // Crea la tabla de distribución usando el modelo
        EstilosUI.aplicarEstiloTabla(tDist); // Aplica el estilo a la tabla
        tDist.getTableHeader().setBackground(new Color(200, 240, 255)); // Establece el color de fondo del encabezado (azul claro)
        tDist.getTableHeader().setForeground(Color.BLACK); // Establece el color del texto del encabezado en negro
        tDist.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Desactiva el redimensionamiento automático de columnas
        tDist.getColumnModel().getColumn(0).setPreferredWidth(90); // Establece el ancho preferido de la primera columna (Probabilidad)
        tDist.getColumnModel().getColumn(1).setPreferredWidth(120); // Establece el ancho preferido de la segunda columna (Distribución acumulada)
        tDist.getColumnModel().getColumn(2).setPreferredWidth(140); // Establece el ancho preferido de la tercera columna (Rangos)
        tDist.getColumnModel().getColumn(3).setPreferredWidth(70); // Establece el ancho preferido de la cuarta columna (Demanda)

        JScrollPane spDist = new JScrollPane(tDist); // Crea un panel de desplazamiento para la tabla de distribución
        spDist.setBorder(BorderFactory.createTitledBorder("Distribución de probabilidades (referencia)")); // Establece un borde con título
        spDist.setPreferredSize(new Dimension(430, 180)); // Establece el tamaño preferido del panel de desplazamiento
        panelIzq.add(spDist); // Agrega el panel de desplazamiento al panel izquierdo

        panelIzq.add(Box.createVerticalStrut(5)); // Agrega un espaciador vertical de 5 píxeles

        // Tabla de entrada de números aleatorios (editable)
        modeloInput = new DefaultTableModel(new Object[]{ // Inicializa el modelo de la tabla de entrada
            "Replica", "# Aleatorio [0,1)" // Define los nombres de las columnas
        }, 0) { // Establece 0 filas iniciales
            @Override
            public boolean isCellEditable(int r, int c) { // Sobrescribe el método para determinar si una celda es editable
                return c == 1; // Solo la columna de números aleatorios (índice 1) es editable
            }
            @Override
            public Class<?> getColumnClass(int c) { // Sobrescribe el método para definir el tipo de datos de cada columna
                return c == 0 ? Integer.class : String.class; // Primera columna es Integer, segunda es String
            }
        };
        JTable tablaInput = new JTable(modeloInput); // Crea la tabla de entrada usando el modelo
        EstilosUI.aplicarEstiloTabla(tablaInput); // Aplica el estilo a la tabla
        tablaInput.getTableHeader().setBackground(new Color(255, 255, 200)); // Establece el color de fondo del encabezado (amarillo claro)
        tablaInput.getTableHeader().setForeground(Color.BLACK); // Establece el color del texto del encabezado en negro
        tablaInput.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Desactiva el redimensionamiento automático de columnas
        tablaInput.getColumnModel().getColumn(0).setPreferredWidth(60); // Establece el ancho preferido de la primera columna (Replica)
        tablaInput.getColumnModel().getColumn(1).setPreferredWidth(120); // Establece el ancho preferido de la segunda columna (# Aleatorio)
        JScrollPane spInput = new JScrollPane(tablaInput); // Crea un panel de desplazamiento para la tabla de entrada
        spInput.setBorder(BorderFactory.createTitledBorder("1. Ingrese números aleatorios [0,1)")); // Establece un borde con título
        spInput.setPreferredSize(new Dimension(190, 180)); // Establece el tamaño preferido del panel de desplazamiento
        panelIzq.add(spInput); // Agrega el panel de desplazamiento al panel izquierdo

        panelPrincipal.add(panelIzq, BorderLayout.WEST); // Agrega el panel izquierdo al lado oeste del panel principal

        // Panel central dividido verticalmente
        JPanel panelCentral = new JPanel(new GridLayout(2, 1, 5, 5)); // Crea el panel central con layout de grilla (2 filas, 1 columna, separación 5px)
        EstilosUI.aplicarEstiloPanel(panelCentral); // Aplica el estilo al panel central

        // Tabla de comparación de decisiones
        modeloComp = new DefaultTableModel(new Object[]{ // Inicializa el modelo de la tabla de comparación
            "Compra", "Ganancia promedio" // Define los nombres de las columnas
        }, 0) { // Establece 0 filas iniciales
            @Override
            public boolean isCellEditable(int r, int c) { // Sobrescribe el método para determinar si una celda es editable
                return false; // Hace que todas las celdas sean no editables
            }
        };
        JTable tablaComp = new JTable(modeloComp); // Crea la tabla de comparación usando el modelo
        EstilosUI.aplicarEstiloTabla(tablaComp); // Aplica el estilo a la tabla
        tablaComp.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Desactiva el redimensionamiento automático de columnas
        tablaComp.getColumnModel().getColumn(0).setPreferredWidth(70); // Establece el ancho preferido de la primera columna (Compra)
        tablaComp.getColumnModel().getColumn(1).setPreferredWidth(130); // Establece el ancho preferido de la segunda columna (Ganancia promedio)
        JScrollPane spComp = new JScrollPane(tablaComp); // Crea un panel de desplazamiento para la tabla de comparación
        spComp.setBorder(BorderFactory.createTitledBorder("Comparación de decisiones")); // Establece un borde con título
        panelCentral.add(spComp); // Agrega el panel de desplazamiento al panel central

        // Tabla de simulación (solo lectura)
        modeloSim = new DefaultTableModel(new Object[]{ // Inicializa el modelo de la tabla de simulación
            "Replica", "# Aleatorio", "Demanda", "Ganancia" // Define los nombres de las columnas
        }, 0) { // Establece 0 filas iniciales
            @Override
            public boolean isCellEditable(int r, int c) { // Sobrescribe el método para determinar si una celda es editable
                return false; // Hace que todas las celdas sean no editables
            }
            @Override
            public Class<?> getColumnClass(int c) { // Sobrescribe el método para definir el tipo de datos de cada columna
                if (c == 0 || c == 2) return Integer.class; // Primera y tercera columna son Integer
                return String.class; // Las demás columnas son String
            }
        };

        JTable tablaSim = new JTable(modeloSim) { // Crea la tabla de simulación con renderizado personalizado
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) { // Sobrescribe el método para personalizar la apariencia de las celdas
                Component c = super.prepareRenderer(renderer, row, column); // Obtiene el componente renderizado por defecto
                if (row == getRowCount() - 1) { // Si es la última fila (fila de promedio)
                    c.setBackground(new Color(220, 220, 220)); // Establece color de fondo gris para la fila de promedio
                } else if (row % 2 == 0) { // Si es una fila par
                    c.setBackground(Color.WHITE); // Establece color de fondo blanco
                } else { // Si es una fila impar
                    c.setBackground(new Color(245, 245, 245)); // Establece color de fondo gris claro
                }
                return c; // Retorna el componente con el color aplicado
            }
        };

        EstilosUI.aplicarEstiloTabla(tablaSim); // Aplica el estilo a la tabla de simulación
        tablaSim.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Desactiva el redimensionamiento automático de columnas
        tablaSim.getColumnModel().getColumn(0).setPreferredWidth(60); // Establece el ancho preferido de la primera columna (Replica)
        tablaSim.getColumnModel().getColumn(1).setPreferredWidth(80); // Establece el ancho preferido de la segunda columna (# Aleatorio)
        tablaSim.getColumnModel().getColumn(2).setPreferredWidth(70); // Establece el ancho preferido de la tercera columna (Demanda)
        tablaSim.getColumnModel().getColumn(3).setPreferredWidth(90); // Establece el ancho preferido de la cuarta columna (Ganancia)

        JScrollPane spSim = new JScrollPane(tablaSim); // Crea un panel de desplazamiento para la tabla de simulación
        spSim.setBorder(BorderFactory.createTitledBorder("2. Resultados de la simulación")); // Establece un borde con título
        panelCentral.add(spSim); // Agrega el panel de desplazamiento al panel central

        panelPrincipal.add(panelCentral, BorderLayout.CENTER); // Agrega el panel central al centro del panel principal
        add(panelPrincipal, BorderLayout.CENTER); // Agrega el panel principal al centro del panel actual

        btnCrear.addActionListener(this::crearFilas); // Agrega un listener al botón crear que ejecuta el método crearFilas
        btnCalcular.addActionListener(this::calcular); // Agrega un listener al botón calcular que ejecuta el método calcular
        btnCalcular.setEnabled(false); // Desactiva inicialmente el botón calcular
    }

    private void llenarDistribucion() { // Método para llenar la tabla de distribución con datos del modelo
        modeloDist.setRowCount(0); // Limpia todas las filas de la tabla
        double[][] rangos = DulceModelo.getRangos(); // Obtiene los rangos de números aleatorios del modelo

        for (int i = 0; i < DulceModelo.DEMANDAS.length; i++) { // Itera sobre cada demanda en el modelo
            String rango = UtilFormatoDulce.p4(rangos[i][0]) + " - " + UtilFormatoDulce.p4(rangos[i][1]); // Formatea el rango como string con 4 decimales
            modeloDist.addRow(new Object[]{ // Agrega una nueva fila a la tabla
                UtilFormatoDulce.p4(DulceModelo.PROB[i]), // Probabilidad formateada con 4 decimales
                UtilFormatoDulce.p4(getSumaAcumulada(DulceModelo.PROB, i)), // Distribución acumulada formateada
                rango, // Rango de números aleatorios
                DulceModelo.DEMANDAS[i] // Valor de la demanda
            });
        }
    }

    private double getSumaAcumulada(double[] probs, int hasta) { // Método para calcular la suma acumulada de probabilidades hasta un índice dado
        double suma = 0; // Inicializa la suma en 0
        for (int i = 0; i <= hasta; i++) { // Itera desde 0 hasta el índice especificado (inclusive)
            suma += probs[i]; // Suma la probabilidad del índice actual
        }
        return suma; // Retorna la suma acumulada
    }

    private void crearFilas(ActionEvent e) { // Método que se ejecuta cuando se presiona el botón crear
        int replicas = (int) spReplicas.getValue(); // Obtiene el número de réplicas seleccionado en el spinner
        modeloInput.setRowCount(0); // Limpia todas las filas de la tabla de entrada
        modeloSim.setRowCount(0); // Limpia todas las filas de la tabla de simulación
        modeloComp.setRowCount(0); // Limpia todas las filas de la tabla de comparación

        // Crear filas con los números fijos como ejemplo
        for (int i = 0; i < Math.min(replicas, DulceModelo.RAND_FIJOS.length); i++) { // Itera hasta el mínimo entre réplicas y números fijos disponibles
            modeloInput.addRow(new Object[]{i + 1, UtilFormatoDulce.p4(DulceModelo.RAND_FIJOS[i])}); // Agrega fila con número de réplica y número aleatorio fijo formateado
        }

        // Si se necesitan más filas, agregar vacías
        for (int i = DulceModelo.RAND_FIJOS.length; i < replicas; i++) { // Itera desde donde terminaron los números fijos hasta completar las réplicas
            modeloInput.addRow(new Object[]{i + 1, ""}); // Agrega fila con número de réplica y campo vacío para entrada manual
        }

        btnCalcular.setEnabled(true); // Activa el botón calcular después de crear las filas
    }

    private Double parseRand(Object v) { // Método para validar y convertir un valor a número aleatorio válido
        if (v == null) return null; // Si el valor es nulo, retorna null
        String t = v.toString().trim().replace(',', '.'); // Convierte a string, quita espacios y reemplaza coma por punto
        if (t.isEmpty()) return null; // Si está vacío, retorna null
        try { // Intenta convertir el string a número
            double d = Double.parseDouble(t); // Convierte el string a double
            if (d < 0 || d >= 1) return null; // Si no está en el rango [0,1), retorna null
            return d; // Retorna el número válido
        } catch (Exception ex) { // Si hay error en la conversión
            return null; // Retorna null
        }
    }

    private void calcular(ActionEvent e) { // Método que se ejecuta cuando se presiona el botón calcular
        if (modeloInput.getRowCount() == 0) { // Si no hay filas en la tabla de entrada
            JOptionPane.showMessageDialog(this, "Primero debe crear la tabla de entrada", "Error", JOptionPane.WARNING_MESSAGE); // Muestra mensaje de error
            return; // Sale del método
        }

        // Validar y obtener números aleatorios
        double[] randoms = new double[modeloInput.getRowCount()]; // Crea arreglo para almacenar números aleatorios
        for (int i = 0; i < modeloInput.getRowCount(); i++) { // Itera sobre cada fila de la tabla de entrada
            Double r = parseRand(modeloInput.getValueAt(i, 1)); // Obtiene y valida el número aleatorio de la fila
            if (r == null) { // Si el número no es válido
                JOptionPane.showMessageDialog(this, // Muestra mensaje de error específico
                    "Réplica " + (i + 1) + ": Número aleatorio inválido\n" + // Indica la réplica con error
                    "Debe estar en el rango [0,1) y no estar vacío", // Explica el formato requerido
                    "Dato inválido", // Título del mensaje
                    JOptionPane.ERROR_MESSAGE); // Tipo de mensaje de error
                return; // Sale del método sin continuar
            }
            randoms[i] = r; // Almacena el número aleatorio válido en el arreglo
        }

        // Llenar tabla de simulación con Q=60
        llenarSimulacion(randoms, 60); // Llena la tabla de simulación usando Q=60 como cantidad de compra

        // Llenar tabla comparativa
        llenarComparativa(randoms); // Llena la tabla comparativa con diferentes valores de Q

        JOptionPane.showMessageDialog(this, // Muestra mensaje de éxito
            "Simulación completada exitosamente!", // Mensaje de confirmación
            "Cálculo terminado", // Título del mensaje
            JOptionPane.INFORMATION_MESSAGE); // Tipo de mensaje informativo
    }

    private void llenarSimulacion(double[] randoms, int Q) { // Método para llenar la tabla de simulación con resultados
        modeloSim.setRowCount(0); // Limpia todas las filas de la tabla de simulación
        double suma = 0; // Inicializa la suma de ganancias en 0

        for (int i = 0; i < randoms.length; i++) { // Itera sobre cada número aleatorio
            double r = randoms[i]; // Obtiene el número aleatorio actual
            int demanda = DulceModelo.demandaPara(r); // Calcula la demanda correspondiente al número aleatorio
            double ganancia = DulceModelo.ganancia(Q, demanda); // Calcula la ganancia para la cantidad Q y demanda
            suma += ganancia; // Acumula la ganancia en la suma total

            modeloSim.addRow(new Object[]{ // Agrega una nueva fila a la tabla de simulación
                i + 1, // Número de réplica
                UtilFormatoDulce.p4(r), // Número aleatorio formateado con 4 decimales
                demanda, // Valor de la demanda
                UtilFormatoDulce.m2(ganancia) // Ganancia formateada como moneda con 2 decimales
            });
        }

        // Fila de promedio
        double promedio = suma / randoms.length; // Calcula el promedio de ganancias
        modeloSim.addRow(new Object[]{ // Agrega la fila de promedio
            "Ganancia promedio", // Etiqueta para la fila de promedio
            "", // Columna vacía
            "", // Columna vacía
            UtilFormatoDulce.m2(promedio) // Promedio formateado como moneda
        });
    }

    private void llenarComparativa(double[] randoms) { // Método para llenar la tabla comparativa con diferentes valores de Q
        modeloComp.setRowCount(0); // Limpia todas las filas de la tabla comparativa
        int[] decisiones = parseDecisiones(); // Obtiene las decisiones Q del campo de texto
        if(decisiones.length==0){ // Si no hay decisiones válidas
            modeloComp.addRow(new Object[]{"(sin Q)", ""}); // Agrega fila indicando que no hay valores Q
            return; // Sale del método
        }
        double mejorProm = -Double.MAX_VALUE; int mejorQ = -1; // Inicializa variables para encontrar la mejor decisión
        for (int Q : decisiones) { // Itera sobre cada decisión Q
            double[] ganancias = DulceModelo.simularGanancias(Q, randoms); // Simula las ganancias para el valor Q actual
            double promedio = DulceModelo.promedio(ganancias); // Calcula el promedio de las ganancias
            if(promedio > mejorProm){ mejorProm = promedio; mejorQ = Q; } // Si el promedio es mejor, actualiza las variables del mejor
            modeloComp.addRow(new Object[]{Q, UtilFormatoDulce.m2(promedio)}); // Agrega fila con el valor Q y su promedio de ganancia
        }
        modeloComp.addRow(new Object[]{"Mejor", mejorQ + " (" + UtilFormatoDulce.m2(mejorProm) + ")"}); // Agrega fila con la mejor decisión y su ganancia
    }

    private int[] parseDecisiones(){ // Método para convertir el texto de decisiones en un arreglo de enteros
        String txt = txtDecisiones.getText(); if(txt==null) return new int[0]; // Obtiene el texto del campo, si es null retorna arreglo vacío
        String[] partes = txt.split(","); java.util.List<Integer> lista = new java.util.ArrayList<>(); // Divide el texto por comas y crea lista para almacenar enteros válidos
        for(String p: partes){ p = p.trim(); if(p.isEmpty()) continue; try { int q = Integer.parseInt(p); if(q>0) lista.add(q);} catch(Exception ignored){} } // Para cada parte: quita espacios, si está vacía continúa, intenta convertir a entero, si es positivo lo agrega a la lista
        int[] arr = new int[lista.size()]; for(int i=0;i<lista.size();i++) arr[i]=lista.get(i); return arr; // Convierte la lista a arreglo y lo retorna
    }
}