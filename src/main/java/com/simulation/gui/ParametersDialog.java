package com.simulation.gui; // Declaración del paquete de la interfaz gráfica de usuario

import com.simulation.config.SimulationParameters; // Importa la clase de parámetros de configuración de la simulación
import javafx.event.ActionEvent; // Importa ActionEvent para manejar eventos de acciones de botones
import javafx.geometry.Insets; // Importa Insets para definir márgenes y espaciados internos
import javafx.scene.Node; // Importa Node que es la clase base de todos los elementos de la escena JavaFX
import javafx.scene.control.ButtonBar; // Importa ButtonBar para definir tipos de datos de botones
import javafx.scene.control.ButtonType; // Importa ButtonType para crear tipos personalizados de botones
import javafx.scene.control.Dialog; // Importa Dialog para crear ventanas de diálogo modales
import javafx.scene.control.Label; // Importa Label para crear etiquetas de texto
import javafx.scene.control.TextField; // Importa TextField para crear campos de texto editables
import javafx.scene.layout.GridPane; // Importa GridPane para organizar elementos en una cuadrícula
import javafx.scene.layout.Priority; // Importa Priority para definir prioridades de crecimiento de elementos

/**
 * Diálogo sencillo para editar los parámetros básicos de la simulación DIGEMIC.
 */
public class ParametersDialog extends Dialog<Boolean> { // Declaración de la clase que extiende Dialog con tipo de retorno Boolean
    private final TextField durationField; // Campo de texto final para la duración de la simulación
    private final TextField seedField; // Campo de texto final para la semilla aleatoria
    private final TextField arrivalField; // Campo de texto final para el tiempo medio entre arribos
    private final TextField zonaMinField; // Campo de texto final para el tiempo mínimo en zona de formas
    private final TextField zonaMaxField; // Campo de texto final para el tiempo máximo en zona de formas
    private final TextField servicioField; // Campo de texto final para el tiempo medio de servicio
    private final TextField pausaField; // Campo de texto final para el tiempo medio de pausa del servidor
    private final TextField pasaportesField; // Campo de texto final para la cantidad de pasaportes antes de pausa
    private final TextField entradaCapField; // Campo de texto final para la capacidad de entrada
    private final TextField zonaCapField; // Campo de texto final para la capacidad de zona de formas
    private final TextField sillasCapField; // Campo de texto final para la capacidad de sala de sillas
    private final TextField pieCapField; // Campo de texto final para la capacidad de sala de pie
    private final TextField servidor1CapField; // Campo de texto final para la capacidad del servidor 1
    private final TextField servidor2CapField; // Campo de texto final para la capacidad del servidor 2
    private final TextField probSalaField; // Campo de texto final para la probabilidad de ir directo a sala
    private final TextField probFormasField; // Campo de texto final para la probabilidad de ir a llenar formas
    private final Label errorLabel; // Etiqueta final para mostrar mensajes de error de validación

    private boolean accepted = false; // Variable booleana que indica si el usuario aceptó los cambios (inicializada en false)

    public ParametersDialog(SimulationParameters parameters) { // Constructor que recibe los parámetros actuales de la simulación
        setTitle("Parámetros de la simulación"); // Establece el título de la ventana del diálogo
        setHeaderText("Ajusta los valores y confirma para aplicar los cambios"); // Establece el texto del encabezado del diálogo

        ButtonType applyButtonType = new ButtonType("Aplicar", ButtonBar.ButtonData.OK_DONE); // Crea un tipo de botón personalizado "Aplicar" con comportamiento OK_DONE
        getDialogPane().getButtonTypes().addAll(applyButtonType, ButtonType.CANCEL); // Agrega los botones "Aplicar" y "Cancelar" al panel del diálogo

        GridPane grid = new GridPane(); // Crea un nuevo GridPane para organizar los elementos en cuadrícula
        grid.setHgap(12); // Establece el espaciado horizontal entre columnas en 12 píxeles
        grid.setVgap(8); // Establece el espaciado vertical entre filas en 8 píxeles
        grid.setPadding(new Insets(16, 20, 10, 20)); // Establece padding interno: 16 arriba, 20 derecha, 10 abajo, 20 izquierda

        int row = 0; // Variable contador para el número de fila actual (inicializada en 0)
        grid.add(sectionLabel("Generales"), 0, row++, 2, 1); // Agrega etiqueta de sección "Generales" ocupando 2 columnas e incrementa row

        durationField = createField(Double.toString(parameters.getSimulationDurationMinutes())); // Crea campo de texto para duración inicializado con valor actual
        addRow(grid, row++, "Duración (min)", durationField); // Agrega fila con etiqueta y campo de duración, incrementa row

        seedField = createField(Long.toString(parameters.getBaseRandomSeed())); // Crea campo de texto para semilla inicializado con valor actual
        addRow(grid, row++, "Semilla base", seedField); // Agrega fila con etiqueta y campo de semilla, incrementa row

        arrivalField = createField(Double.toString(parameters.getArrivalMeanTime())); // Crea campo de texto para tiempo de arribo inicializado con valor actual
        addRow(grid, row++, "Media arribos (min)", arrivalField); // Agrega fila con etiqueta y campo de arribos, incrementa row

        grid.add(sectionLabel("Zona de formularios"), 0, row++, 2, 1); // Agrega etiqueta de sección "Zona de formularios" ocupando 2 columnas e incrementa row

        zonaMinField = createField(Double.toString(parameters.getZonaFormasMin())); // Crea campo de texto para tiempo mínimo inicializado con valor actual
        addRow(grid, row++, "Tiempo mínimo", zonaMinField); // Agrega fila con etiqueta y campo de tiempo mínimo, incrementa row

        zonaMaxField = createField(Double.toString(parameters.getZonaFormasMax())); // Crea campo de texto para tiempo máximo inicializado con valor actual
        addRow(grid, row++, "Tiempo máximo", zonaMaxField); // Agrega fila con etiqueta y campo de tiempo máximo, incrementa row

        grid.add(sectionLabel("Servidores"), 0, row++, 2, 1); // Agrega etiqueta de sección "Servidores" ocupando 2 columnas e incrementa row

        servicioField = createField(Double.toString(parameters.getServicioMean())); // Crea campo de texto para tiempo de servicio inicializado con valor actual
        addRow(grid, row++, "Tiempo servicio medio", servicioField); // Agrega fila con etiqueta y campo de servicio, incrementa row

        pausaField = createField(Double.toString(parameters.getPausaServidorMean())); // Crea campo de texto para tiempo de pausa inicializado con valor actual
        addRow(grid, row++, "Pausa media (cada tanda)", pausaField); // Agrega fila con etiqueta y campo de pausa, incrementa row

        pasaportesField = createField(Integer.toString(parameters.getPasaportesPorPausa())); // Crea campo de texto para pasaportes por pausa inicializado con valor actual
        addRow(grid, row++, "Pasaportes por pausa", pasaportesField); // Agrega fila con etiqueta y campo de pasaportes, incrementa row

        grid.add(sectionLabel("Capacidades"), 0, row++, 2, 1); // Agrega etiqueta de sección "Capacidades" ocupando 2 columnas e incrementa row

        entradaCapField = createField(formatCapacity(parameters.getEntradaCapacity())); // Crea campo de texto para capacidad de entrada formateada (inf si es MAX_VALUE)
        addRow(grid, row++, "Entrada", entradaCapField); // Agrega fila con etiqueta y campo de entrada, incrementa row

        zonaCapField = createField(formatCapacity(parameters.getZonaFormasCapacity())); // Crea campo de texto para capacidad de zona de formas formateada
        addRow(grid, row++, "Zona de formas", zonaCapField); // Agrega fila con etiqueta y campo de zona de formas, incrementa row

        sillasCapField = createField(formatCapacity(parameters.getSalaSillasCapacity())); // Crea campo de texto para capacidad de sala de sillas formateada
        addRow(grid, row++, "Sala sillas", sillasCapField); // Agrega fila con etiqueta y campo de sillas, incrementa row

        pieCapField = createField(formatCapacity(parameters.getSalaDePieCapacity())); // Crea campo de texto para capacidad de sala de pie formateada
        addRow(grid, row++, "Sala de pie", pieCapField); // Agrega fila con etiqueta y campo de pie, incrementa row

        servidor1CapField = createField(formatCapacity(parameters.getServidor1Capacity())); // Crea campo de texto para capacidad del servidor 1 formateada
        addRow(grid, row++, "Servidor 1", servidor1CapField); // Agrega fila con etiqueta y campo de servidor 1, incrementa row

        servidor2CapField = createField(formatCapacity(parameters.getServidor2Capacity())); // Crea campo de texto para capacidad del servidor 2 formateada
        addRow(grid, row++, "Servidor 2", servidor2CapField); // Agrega fila con etiqueta y campo de servidor 2, incrementa row

        grid.add(sectionLabel("Probabilidades de ruteo"), 0, row++, 2, 1); // Agrega etiqueta de sección "Probabilidades de ruteo" ocupando 2 columnas e incrementa row

        probSalaField = createField(Double.toString(parameters.getDirectoASalaProb())); // Crea campo de texto para probabilidad de ir directo a sala inicializado con valor actual
        addRow(grid, row++, "Directo a sala", probSalaField); // Agrega fila con etiqueta y campo de probabilidad sala, incrementa row

        probFormasField = createField(Double.toString(parameters.getAFormasProb())); // Crea campo de texto para probabilidad de ir a formas inicializado con valor actual
        addRow(grid, row++, "A llenar formas", probFormasField); // Agrega fila con etiqueta y campo de probabilidad formas, incrementa row

        errorLabel = new Label(); // Crea una nueva etiqueta para mostrar mensajes de error
        errorLabel.setStyle("-fx-text-fill: #d32f2f;"); // Establece el estilo de la etiqueta con color de texto rojo (#d32f2f)
        grid.add(errorLabel, 0, row, 2, 1); // Agrega la etiqueta de error ocupando 2 columnas en la fila actual

        getDialogPane().setContent(grid); // Establece el GridPane como contenido del panel del diálogo

        Node applyButton = getDialogPane().lookupButton(applyButtonType); // Obtiene la referencia al botón "Aplicar" del panel del diálogo
        applyButton.addEventFilter(ActionEvent.ACTION, event -> { // Agrega un filtro de eventos que intercepta la acción del botón antes de procesarla
            if (!applyChanges(parameters)) { // Intenta aplicar los cambios; si falla (retorna false)
                event.consume(); // Consume el evento para evitar que el diálogo se cierre
            } else { // Si los cambios se aplicaron exitosamente
                accepted = true; // Marca que el usuario aceptó los cambios
            }
        });

        setResultConverter(buttonType -> buttonType == applyButtonType && accepted); // Establece el convertidor de resultado: retorna true solo si se presionó Aplicar y se aceptó
    }

    public boolean isAccepted() { // Método público que verifica si el usuario aceptó los cambios
        return accepted; // Retorna el valor de la variable accepted
    }

    private TextField createField(String initialValue) { // Método privado que crea un campo de texto con un valor inicial
        TextField field = new TextField(initialValue); // Crea un nuevo TextField con el valor inicial proporcionado
        GridPane.setHgrow(field, Priority.ALWAYS); // Establece que el campo crezca horizontalmente con prioridad ALWAYS
        field.setPrefWidth(160); // Establece el ancho preferido del campo en 160 píxeles
        return field; // Retorna el campo de texto creado
    }

    private void addRow(GridPane grid, int row, String labelText, TextField field) { // Método privado que agrega una fila con etiqueta y campo al grid
        Label label = new Label(labelText + ":"); // Crea una nueva etiqueta con el texto proporcionado más dos puntos
        grid.add(label, 0, row); // Agrega la etiqueta en la columna 0 de la fila especificada
        grid.add(field, 1, row); // Agrega el campo de texto en la columna 1 de la fila especificada
    }

    private Label sectionLabel(String text) { // Método privado que crea una etiqueta de sección con estilo especial
        Label label = new Label(text); // Crea una nueva etiqueta con el texto proporcionado
        label.setStyle("-fx-font-weight: bold; -fx-padding: 8 0 2 0;"); // Establece estilo: negrita y padding (8 arriba, 0 lados, 2 abajo)
        return label; // Retorna la etiqueta de sección creada
    }

    private boolean applyChanges(SimulationParameters params) { // Método privado que valida y aplica los cambios a los parámetros
        try { // Bloque try para capturar excepciones de validación
            double duration = parseDouble(durationField, "Duración", 1.0, Double.MAX_VALUE, true, false); // Parsea y valida duración (mínimo 1.0 inclusive)
            long seed = parseLong(seedField, "Semilla"); // Parsea y valida la semilla aleatoria como long
            double arrival = parseDouble(arrivalField, "Media de arribos", 0.01, Double.MAX_VALUE, false, false); // Parsea y valida tiempo de arribo (mayor a 0.01)

            double zonaMin = parseDouble(zonaMinField, "Tiempo mínimo de formularios", 0.01, Double.MAX_VALUE, false, false); // Parsea y valida tiempo mínimo (mayor a 0.01)
            double zonaMax = parseDouble(zonaMaxField, "Tiempo máximo de formularios", 0.01, Double.MAX_VALUE, false, false); // Parsea y valida tiempo máximo (mayor a 0.01)
            if (zonaMax < zonaMin) { // Verifica si el tiempo máximo es menor que el mínimo
                throw new IllegalArgumentException("El tiempo máximo debe ser mayor o igual al mínimo en formularios"); // Lanza excepción con mensaje de error
            }

            double servicio = parseDouble(servicioField, "Tiempo medio de servicio", 0.01, Double.MAX_VALUE, false, false); // Parsea y valida tiempo de servicio (mayor a 0.01)
            double pausa = parseDouble(pausaField, "Pausa media", 0.01, Double.MAX_VALUE, false, false); // Parsea y valida tiempo de pausa (mayor a 0.01)
            int pasaportes = parseInt(pasaportesField, "Pasaportes por pausa", 1, Integer.MAX_VALUE); // Parsea y valida pasaportes por pausa (mínimo 1)

            int entradaCap = parseCapacity(entradaCapField, "Capacidad entrada"); // Parsea capacidad de entrada (inf o número entero)
            int zonaCap = parseCapacity(zonaCapField, "Capacidad zona de formas"); // Parsea capacidad de zona de formas
            int sillasCap = parseCapacity(sillasCapField, "Capacidad sala sillas"); // Parsea capacidad de sala de sillas
            int pieCap = parseCapacity(pieCapField, "Capacidad sala de pie"); // Parsea capacidad de sala de pie
            int servidor1Cap = parseCapacity(servidor1CapField, "Capacidad servidor 1"); // Parsea capacidad del servidor 1
            int servidor2Cap = parseCapacity(servidor2CapField, "Capacidad servidor 2"); // Parsea capacidad del servidor 2

            double probSala = parseDouble(probSalaField, "Probabilidad directo a sala", 0.0, 1.0, true, true); // Parsea y valida probabilidad (entre 0.0 y 1.0 inclusive)
            double probFormas = parseDouble(probFormasField, "Probabilidad a formularios", 0.0, 1.0, true, true); // Parsea y valida probabilidad (entre 0.0 y 1.0 inclusive)
            double probSum = probSala + probFormas; // Calcula la suma de ambas probabilidades
            if (Math.abs(probSum - 1.0) > 1e-6) { // Verifica si la suma difiere de 1.0 por más de 0.000001 (tolerancia de precisión)
                throw new IllegalArgumentException("La suma de probabilidades debe ser 1.0"); // Lanza excepción si las probabilidades no suman 1
            }

            params.setSimulationDurationMinutes(duration); // Establece la duración validada en los parámetros
            params.setBaseRandomSeed(seed); // Establece la semilla validada en los parámetros
            params.setArrivalMeanTime(arrival); // Establece el tiempo de arribo validado en los parámetros
            params.setZonaFormasMin(zonaMin); // Establece el tiempo mínimo de formas validado en los parámetros
            params.setZonaFormasMax(zonaMax); // Establece el tiempo máximo de formas validado en los parámetros
            params.setServicioMean(servicio); // Establece el tiempo de servicio validado en los parámetros
            params.setPausaServidorMean(pausa); // Establece el tiempo de pausa validado en los parámetros
            params.setPasaportesPorPausa(pasaportes); // Establece los pasaportes por pausa validados en los parámetros

            params.setEntradaCapacity(entradaCap); // Establece la capacidad de entrada validada en los parámetros
            params.setZonaFormasCapacity(zonaCap); // Establece la capacidad de zona de formas validada en los parámetros
            params.setSalaSillasCapacity(sillasCap); // Establece la capacidad de sala de sillas validada en los parámetros
            params.setSalaDePieCapacity(pieCap); // Establece la capacidad de sala de pie validada en los parámetros
            params.setServidor1Capacity(servidor1Cap); // Establece la capacidad del servidor 1 validada en los parámetros
            params.setServidor2Capacity(servidor2Cap); // Establece la capacidad del servidor 2 validada en los parámetros

            params.setDirectoASalaProb(probSala); // Establece la probabilidad directo a sala validada en los parámetros
            params.setAFormasProb(probFormas); // Establece la probabilidad a formas validada en los parámetros

            errorLabel.setText(""); // Limpia el texto de la etiqueta de error (sin errores)
            return true; // Retorna true indicando que los cambios se aplicaron exitosamente
        } catch (IllegalArgumentException ex) { // Captura excepciones de argumentos inválidos durante la validación
            errorLabel.setText(ex.getMessage()); // Muestra el mensaje de error en la etiqueta de error
            return false; // Retorna false indicando que los cambios no se aplicaron
        }
    }

    private double parseDouble(TextField field, String label, double min, double max, boolean inclusiveMin, boolean inclusiveMax) { // Método que parsea y valida un double con rango especificado
        String raw = field.getText().trim(); // Obtiene el texto del campo y elimina espacios en blanco al inicio y final
        if (raw.isEmpty()) { // Verifica si el texto está vacío
            throw new IllegalArgumentException(label + ": ingresa un valor numérico"); // Lanza excepción indicando que falta el valor
        }
        double value; // Declara variable para almacenar el valor parseado
        try { // Bloque try para capturar errores de parseo
            value = Double.parseDouble(raw); // Intenta convertir el texto a double
        } catch (NumberFormatException ex) { // Captura excepción si el formato no es válido
            throw new IllegalArgumentException(label + ": formato inválido"); // Lanza excepción con mensaje de formato inválido
        }
        if ((inclusiveMin ? value < min : value <= min) || (inclusiveMax ? value > max : value >= max)) { // Verifica si el valor está fuera del rango considerando inclusividad
            String lower = inclusiveMin ? "≥ " : "> "; // Define símbolo de límite inferior según inclusividad
            String upper = inclusiveMax ? "≤ " : "< "; // Define símbolo de límite superior según inclusividad
            throw new IllegalArgumentException(label + ": debe estar en el rango " + lower + min + " y " + upper + max); // Lanza excepción con mensaje de rango
        }
        return value; // Retorna el valor validado
    }

    private int parseInt(TextField field, String label, int min, int max) { // Método que parsea y valida un entero con rango especificado
        String raw = field.getText().trim(); // Obtiene el texto del campo y elimina espacios en blanco
        if (raw.isEmpty()) { // Verifica si el texto está vacío
            throw new IllegalArgumentException(label + ": ingresa un entero válido"); // Lanza excepción indicando que falta el valor
        }
        int value; // Declara variable para almacenar el valor parseado
        try { // Bloque try para capturar errores de parseo
            value = Integer.parseInt(raw); // Intenta convertir el texto a entero
        } catch (NumberFormatException ex) { // Captura excepción si el formato no es válido
            throw new IllegalArgumentException(label + ": formato inválido"); // Lanza excepción con mensaje de formato inválido
        }
        if (value < min || value > max) { // Verifica si el valor está fuera del rango especificado
            throw new IllegalArgumentException(label + ": debe estar entre " + min + " y " + max); // Lanza excepción con mensaje de rango
        }
        return value; // Retorna el valor validado
    }

    private long parseLong(TextField field, String label) { // Método que parsea y valida un long sin restricción de rango
        String raw = field.getText().trim(); // Obtiene el texto del campo y elimina espacios en blanco
        if (raw.isEmpty()) { // Verifica si el texto está vacío
            throw new IllegalArgumentException(label + ": ingresa un entero válido"); // Lanza excepción indicando que falta el valor
        }
        try { // Bloque try para capturar errores de parseo
            return Long.parseLong(raw); // Intenta convertir el texto a long y lo retorna
        } catch (NumberFormatException ex) { // Captura excepción si el formato no es válido
            throw new IllegalArgumentException(label + ": formato inválido"); // Lanza excepción con mensaje de formato inválido
        }
    }

    private int parseCapacity(TextField field, String label) { // Método que parsea capacidad que puede ser "inf" o un número entero
        String raw = field.getText().trim(); // Obtiene el texto del campo y elimina espacios en blanco
        if (raw.isEmpty() || "inf".equalsIgnoreCase(raw)) { // Verifica si está vacío o es "inf" (ignorando mayúsculas/minúsculas)
            return Integer.MAX_VALUE; // Retorna Integer.MAX_VALUE para representar capacidad infinita
        }
        return parseInt(field, label, 1, Integer.MAX_VALUE); // Si no es "inf", parsea como entero con rango mínimo 1
    }

    private String formatCapacity(int capacity) { // Método que formatea la capacidad para mostrar ("inf" si es infinita)
        return capacity == Integer.MAX_VALUE ? "inf" : Integer.toString(capacity); // Retorna "inf" si es MAX_VALUE, sino retorna el número como string
    }
}
