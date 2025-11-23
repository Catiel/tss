# Simulador de Producción de Cerveza - Interfaz Gráfica

## 🎯 Descripción

Sistema de simulación de eventos discretos con interfaz gráfica JavaFX que modela el proceso completo de producción de cerveza, desde la recepción de materias primas hasta el empaque final.

## 🚀 Cómo Ejecutar

### Opción 1: Usando Maven (Recomendado)

```bash
mvn clean javafx:run
```

### Opción 2: Compilar y ejecutar manualmente

```bash
# Compilar
mvn clean package

# Ejecutar
java --module-path "build/javafx/javafx-sdk-17.0.2/lib" --add-modules javafx.controls,javafx.fxml -jar target/production-line-simulator-1.0.0.jar
```

### Opción 3: Desde el IDE (IntelliJ IDEA / Eclipse)

1. Abre el proyecto en tu IDE
2. Configura el JDK 17
3. Ejecuta la clase principal: `com.simulation.gui.DigemicMain`

## 🎮 Características de la Interfaz Gráfica

### Panel de Control
- **Botón Iniciar**: Comienza la simulación
- **Botón Pausar/Reanudar**: Pausa o reanuda la simulación en ejecución
- **Botón Detener**: Detiene completamente la simulación
- **Botón Reiniciar**: Reinicia la simulación desde cero
- **Control de Velocidad**: Slider para ajustar la velocidad de simulación (0.1x a 5x)
- **Indicador de Tiempo**: Muestra el tiempo actual de simulación en minutos y horas
- **Barra de Progreso**: Visualiza el progreso de la simulación (70 horas totales)

### Visualización de Locaciones
Panel principal que muestra todas las estaciones de trabajo:

- **19 Locaciones** representadas visualmente con su nombre, capacidad y estado actual
- **Indicadores visuales**:
  - Ocupación actual vs. capacidad máxima
  - Barra de capacidad con código de colores:
    - 🟢 Verde: < 50% de utilización
    - 🟠 Naranja: 50-80% de utilización
    - 🔴 Rojo: > 80% de utilización
  - Cola de espera (resaltado en naranja cuando hay entidades esperando)
  - Tooltip con información detallada al pasar el mouse

**Locaciones incluidas:**
- Almacenamiento: SILO_GRANDE, SILO_LUPULO, SILO_LEVADURA, ALMACEN_CAJAS
- Procesamiento: MALTEADO, SECADO, MOLIENDA, MACERADO, FILTRADO
- Cocción y Fermentación: COCCION, FERMENTACION, MADURACION
- Embotellado: ENFRIAMIENTO, EMBOTELLADO, ETIQUETADO, INSPECCION
- Empaque: EMPACADO, ALMACENAJE, MERCADO

### Panel de Recursos
Muestra el estado de todos los recursos del sistema:

- **5 Recursos** con indicadores visuales:
  - 👷 OPERADOR_RECEPCION
  - 👷 OPERADOR_LUPULO
  - 👷 OPERADOR_LEVADURA
  - 👷 OPERADOR_EMPACADO
  - 🚛 CAMION

- **Información por recurso**:
  - Estado actual (Disponible/Ocupado/Parcial)
  - Unidades disponibles vs. totales
  - Barra de utilización con porcentaje
  - Código de colores según carga de trabajo

### Estadísticas en Tiempo Real

#### Tab "Estadísticas de Entidades"
Tabla actualizable que muestra para cada tipo de entidad:
- Nombre de la entidad
- Total de salidas del sistema
- Cantidad actualmente en el sistema
- Tiempo promedio en el sistema (min)
- Tiempo promedio de movimiento (min)
- Tiempo promedio de espera (min)
- Tiempo promedio de operación (min)
- Tiempo promedio de bloqueo (min)

**8 Tipos de Entidades:**
- GRANOS_DE_CEBADA
- LUPULO
- LEVADURA
- MOSTO
- CERVEZA
- BOTELLA_CON_CERVEZA
- CAJA_VACIA
- CAJA_CON_CERVEZAS

#### Tab "Estadísticas de Locaciones"
Tabla actualizable que muestra para cada locación:
- Nombre de la locación
- Capacidad máxima
- Ocupación actual
- Cola actual
- Total de entradas procesadas
- Tiempo promedio por entrada (min)
- Contenido promedio
- Contenido máximo alcanzado
- % de utilización

### Exportación de Reportes
Al finalizar la simulación, puedes exportar reportes detallados en:
- **Texto plano** (`reporte_simulacion.txt`)
- **CSV** para análisis en Excel (`entidades_reporte.csv`, `locaciones_reporte.csv`)

## 📊 Parámetros de Simulación

- **Duración**: 70 horas (4,200 minutos)
- **Arribos programados**:
  - Granos de cebada: cada 25 minutos
  - Lúpulo: cada 10 minutos
  - Levadura: cada 20 minutos
  - Cajas vacías: cada 30 minutos

## 🎨 Interfaz Visual

La interfaz está diseñada con:
- Estilo moderno con Material Design
- Código de colores intuitivo para estados
- Animaciones suaves
- Actualización en tiempo real de todas las métricas
- Responsive design

## 🔧 Requisitos Técnicos

- **Java**: JDK 17 o superior
- **JavaFX**: 17.0.2 (incluido en dependencias)
- **Maven**: 3.6 o superior
- **Memoria RAM**: Mínimo 512 MB

## 📝 Estructura del Proyecto

```
src/main/java/com/simulation/
├── gui/                          # Interfaz gráfica
│   ├── DigemicMain.java         # Clase principal
│   ├── SimulationController.java # Controlador principal
│   ├── LocationVisualizer.java  # Visualización de locaciones
│   ├── ResourcePanel.java       # Panel de recursos
│   └── StatisticsPanel.java     # Panel de estadísticas
├── core/                        # Motor de simulación
├── entities/                    # Entidades del sistema
├── locations/                   # Locaciones/Estaciones
├── resources/                   # Recursos
├── statistics/                  # Recolección de estadísticas
└── output/                      # Generación de reportes

src/main/resources/
├── fxml/
│   └── simulation-view.fxml     # Diseño de la interfaz
└── css/
    └── styles.css               # Estilos visuales
```

## 🐛 Solución de Problemas

### Error: "Module javafx.controls not found"
Asegúrate de tener JavaFX en el classpath. Usa Maven para ejecutar:
```bash
mvn javafx:run
```

### La interfaz no se actualiza
Verifica que el slider de velocidad no esté en la posición mínima (0.1x)

### Rendimiento lento
Reduce la velocidad de simulación usando el slider o cierra otras aplicaciones

## 📚 Uso Básico

1. **Iniciar**: Haz clic en "Iniciar" para comenzar la simulación
2. **Observar**: Monitorea las locaciones, recursos y estadísticas en tiempo real
3. **Ajustar**: Usa el slider para cambiar la velocidad de simulación
4. **Pausar**: Si necesitas analizar un momento específico
5. **Exportar**: Al finalizar, exporta los reportes para análisis posterior

## 🎓 Casos de Uso

- Análisis de cuellos de botella en producción
- Optimización de capacidades de estaciones
- Balanceo de recursos (operadores)
- Estudio de tiempos de procesamiento
- Simulación de escenarios "What-if"

## 👥 Autores

Proyecto desarrollado para el curso de Simulación de Sistemas

## 📄 Licencia

Este proyecto es de uso académico.
