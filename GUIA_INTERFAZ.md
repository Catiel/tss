# Sistema Integrado de Simulación de Producción Cervecera 🍺

## 📋 Descripción General

Sistema de simulación de eventos discretos que modela el proceso completo de producción de cerveza, desde la recepción de materias primas hasta la distribución al mercado. Implementa una interfaz gráfica moderna con visualización en tiempo real y estadísticas completas.

## ✨ Características Principales

### 🎬 Interfaz de Usuario Moderna
- **Diseño con pestañas**: Navegación intuitiva entre animación y estadísticas
- **Tema oscuro profesional**: Colores modernos (#1a1a2e, #16213e, #e94560)
- **Sin pantalla dividida**: Uso completo del espacio disponible
- **Animaciones fluidas**: Visualización en tiempo real de entidades y operadores

### 📊 Sistema de Pestañas

#### Pestaña 1: 🎬 Animación en Vivo
- Canvas de 2000x1200 píxeles con renderizado optimizado
- Iconos emoji grandes (42px) para las 19 locaciones
- Entidades animadas (16px) con velocidad 4.0
- Visualización de acumulación (círculos 1-10, números >10)
- Movimiento sincronizado de operadores con flechas verdes

#### Pestaña 2: 📍 Estadísticas de Locaciones
- Tabla completa con 15 columnas de métricas
- Diseño elegante con gradientes y espaciado mejorado
- Scroll vertical para fácil navegación
- Actualización en tiempo real cada 15 frames
- Métricas incluyen: entradas, salidas, tiempos, utilización, recursos

#### Pestaña 3: 📦 Estadísticas de Entidades
- Tabla detallada de los 8 tipos de entidades
- Seguimiento completo del flujo de materiales
- Estadísticas de ciclo, espera y procesamiento
- Diseño consistente con tema oscuro

### 🔧 Lógica de Simulación ProModel

#### Operaciones JOIN Implementadas
- **COCCION**: MOSTO (principal) + LUPULO (secundario)
- **FERMENTACION**: MOSTO (principal) + LEVADURA (secundario)
- **EMPACADO**: CAJA_VACIA (principal) + BOTELLA (secundario)
  - WAIT 10 min después del JOIN

#### Operación ACCUM
- **ALMACENAJE**: ACCUM 6 (envía 1 entidad cuando 6 acumuladas)

#### Frecuencias de Llegada Ajustadas
- GRANOS_DE_CEBADA: 124 llegadas @ 33.87 min
- LUPULO: 400 llegadas @ 10.5 min
- LEVADURA: 190 llegadas @ 22.11 min
- CAJA_VACIA: 114 llegadas @ 36.84 min

### 🎨 Hoja de Estilos CSS Personalizada

Ubicación: `/src/main/resources/styles/brewery-simulation.css`

**Elementos estilizados:**
- Pestañas con efectos hover y selección
- Tablas con filas alternadas y bordes
- Botones con gradientes y sombras
- Barras de progreso animadas
- Sliders y scrollbars personalizados
- Tooltips informativos

## 🚀 Tecnologías Utilizadas

- **Java 17**: Lenguaje principal
- **JavaFX 17.0.2**: Framework de interfaz gráfica
- **Maven**: Gestión de dependencias y construcción
- **SSJ 3.3.1**: Librería de simulación de eventos discretos
- **Apache Commons Math 3.6.1**: Cálculos estadísticos
- **JFreeChart 1.5.4**: Generación de gráficos (disponible)
- **ControlsFX 11.1.2**: Controles avanzados de JavaFX

## 📦 Estructura del Proyecto

```
sptss/
├── src/main/java/com/simulation/
│   ├── Main.java                          # Simulación en consola
│   ├── gui/
│   │   ├── BrewerySimulationGUI.java     # Aplicación JavaFX principal
│   │   ├── LocationStatsTable.java       # Tabla de 15 columnas
│   │   ├── EntityStatsTable.java         # Tabla de 7 columnas
│   │   ├── VisualLocationManager.java    # Renderizado de locaciones
│   │   ├── VisualEntityManager.java      # Animación de entidades
│   │   └── VisualResourceManager.java    # Movimiento de operadores
│   ├── core/
│   │   ├── SimulationEngine.java         # Motor de eventos discretos
│   │   ├── EventScheduler.java           # Planificador de eventos
│   │   └── SimulationClock.java          # Reloj de simulación
│   ├── processing/
│   │   └── OperationHandler.java         # Lógica JOIN, ACCUM, WAIT
│   ├── entities/
│   │   ├── Entity.java                   # Clase de entidad
│   │   └── EntityType.java               # 8 tipos de entidades
│   ├── locations/
│   │   ├── Location.java                 # Clase de locación
│   │   └── LocationType.java             # 19 tipos de locaciones
│   └── resources/
│       ├── Resource.java                 # Clase de recurso
│       └── ResourceType.java             # 5 tipos de recursos
├── src/main/resources/
│   └── styles/
│       └── brewery-simulation.css        # Estilos CSS personalizados
└── pom.xml                                # Configuración Maven
```

## 🏃 Cómo Ejecutar

### Compilar el Proyecto
```bash
cd c:\Users\olive\IdeaProjects\sptss
mvn clean compile
```

### Ejecutar la Interfaz Gráfica
```bash
mvn javafx:run
```

### Ejecutar Simulación en Consola
```bash
mvn exec:java -Dexec.mainClass="com.simulation.Main"
```

## 🎯 Parámetros de Simulación

- **Duración**: 4200 minutos (70 horas)
- **Locaciones**: 19 estaciones de trabajo
- **Entidades**: 8 tipos de materiales/productos
- **Recursos**: 5 operadores
- **Velocidad de animación**: Ajustable con slider (0.1x - 5.0x)

## 📈 Métricas de Rendimiento vs ProModel

### Resultados Actuales
| Métrica | Java | ProModel | Error |
|---------|------|----------|-------|
| LUPULO exits | 392 | 399 | 1.8% |
| LEVADURA exits | 184 | 192 | 4.2% |
| BOTELLA exits | 468 | 498 | 6.0% |
| SILO_GRANDE entries | 124 | 124 | 0.0% ✅ |
| SILO_LUPULO entries | 400 | 400 | 0.0% ✅ |
| SILO_LEVADURA entries | 190 | 190 | 0.0% ✅ |
| FERMENTACION entries | 97 | 96 | 1.0% ✅ |

### Objetivo
✅ Todos los silos: Error 0% (EXACTO)  
✅ LUPULO, LEVADURA: Error < 5%  
🔄 BOTELLA: Error 6.0% (meta: < 5%)  
🔄 CAJA_CON_CERVEZAS: Error 17.9% (en optimización)

## 🎨 Personalización de Colores

### Paleta Principal
- **Fondo oscuro**: #1a1a2e, #16213e
- **Acento principal**: #e94560 (rosa/rojo)
- **Acento secundario**: #0f3460 (azul oscuro)
- **Texto principal**: #f1faee (blanco suave)
- **Texto secundario**: #a8dadc (azul claro)

### Modificar Estilos
Edita `src/main/resources/styles/brewery-simulation.css` para cambiar:
- Colores de pestañas
- Estilos de tabla
- Botones y controles
- Efectos hover y transiciones

## 🔍 Arquitectura de Eventos

### SimulationListener Interface
```java
- onEntityCreated(Entity entity)
- onEntityArrival(Entity entity, Location location)
- onEntityDeparture(Entity entity, Location location)
- onEntityMove(Entity entity, Point2D from, Point2D to)
- onResourceAcquired(Resource resource, Entity entity, Location location)
- onResourceReleased(Resource resource, Location location)
```

### ResourceTransport System
Sincroniza movimiento de operadores con entidades:
1. Operador adquirido en ubicación A
2. Animación conjunta hacia ubicación B
3. Liberación de operador en destino
4. Flecha verde muestra dirección

## 🐛 Correcciones Implementadas

### Versión Actual
✅ Diseño con pestañas (sin pantalla dividida)  
✅ Tablas con scroll y diseño completo  
✅ Títulos y descripciones en cada pestaña  
✅ CSS personalizado con tema oscuro moderno  
✅ Colores consistentes en toda la aplicación  
✅ Mejora de legibilidad (fuentes más grandes)  

### Versiones Anteriores
✅ JOIN operations (COCCION, FERMENTACION, EMPACADO)  
✅ ACCUM 6 logic en ALMACENAJE  
✅ WAIT 10 min después de JOIN en EMPACADO  
✅ Entity exit/entry counting correcto  
✅ Filtrado de entidades secundarias en JOINs  
✅ Frecuencias de llegada ajustadas  

## 📝 Notas de Desarrollo

### Compilación Exitosa
```
[INFO] BUILD SUCCESS
[INFO] Total time: 8.597 s
[INFO] Compiling 39 source files
```

### Advertencias Conocidas
- 6 warnings en efectivo model de javafx-controls (no afectan funcionalidad)
- Unchecked operations en BrewerySimulationGUI (uso seguro de generics)

## 🔮 Mejoras Futuras

- [ ] Agregar pestaña de Dashboard con gráficos (JFreeChart)
- [ ] Implementar exportación de reportes en PDF
- [ ] Añadir modo de comparación con ProModel
- [ ] Crear animaciones más detalladas de procesos
- [ ] Implementar temas de color personalizables
- [ ] Agregar tooltips informativos en locaciones
- [ ] Optimizar para reducir error en CAJA_CON_CERVEZAS

## 👨‍💻 Autor

Proyecto de simulación de línea de producción cervecera desarrollado con Java y JavaFX.

## 📄 Licencia

Proyecto académico/educativo.
