package com.simulacion.modelo;

/**
 * Clase que almacena todos los parametros configurables de la simulacion
 * Ahora incluye opciones para multiples escenarios y configuracion avanzada
 */
public class SimulacionParametros {

    // Parametros de tiempo de simulacion
    private int diasSimulacion = 90;
    private int horasPorDia = 24;
    private int replicas = 3;

    // Velocidad de simulacion (0 = maxima, >0 = delay en ms)
    private int velocidadSimulacion = 0; // 0 = sin animacion (mas rapido)

    // Parametros de arribos (en minutos)
    private double mediaArribos = 2.0; // Exponencial E(2)

    // Parametros de Lavado
    private int capacidadLavadora = 5;
    private double mediaTiempoLavado = 10.0;
    private double desviacionTiempoLavado = 2.0; // Normal N(10, 2)

    // Parametros de Almacen de Pintura
    private int capacidadAlmacenPintura = 10;

    // Parametros de Pintura
    private int capacidadPintura = 3;
    private double minPintura = 4.0;
    private double modaPintura = 8.0;
    private double maxPintura = 10.0; // Triangular T(4, 8, 10)

    // Parametros de Almacen de Horno
    private int capacidadAlmacenHorno = 10;

    // Parametros de Horno
    private int capacidadHorno = 1;
    private double centroHorno = 3.0;
    private double amplitudHorno = 1.0; // Uniforme U(3, 1) = U(2, 4)

    // Parametros de Inspeccion (CORREGIDO: 2 inspectores, 3 elementos por pieza)
    private int numeroInspectores = 2;
    private int elementosPorPieza = 3;
    private double mediaTiempoInspeccionElemento = 2.0; // Exponencial E(2) por elemento

    // Parametros de transporte (CORREGIDOS según ProModel)
    private double mediaTrasladoRecepcionLavado = 3.0; // E(3)
    private double mediaTrasladoLavadoPintura = 2.0; // E(2)
    // PINTURA -> ALMACEN_HORNO: U(3.5, 1.5) = U(2, 5)
    private double centroTrasladoPinturaHorno = 3.5;
    private double amplitudTrasladoPinturaHorno = 1.5;
    // HORNO -> INSPECCION: U(2, 1) = U(1, 3)
    private double centroTrasladoHornoInspeccion = 2.0;
    private double amplitudTrasladoHornoInspeccion = 1.0;

    // Parametros economicos
    private double utilidadPorPieza = 5.0;
    private double costoHornoAdicional = 100000.0;
    private double costoInspectorAdicional = 50000.0;

    // NUEVO: Configuracion de escenarios
    private String nombreEscenario = "Escenario Base";
    private boolean modoRapido = true; // Sin animacion por defecto
    private boolean guardarDetalle = false;

    // Getters y Setters

    public int getDiasSimulacion() {
        return diasSimulacion;
    }

    public void setDiasSimulacion(int diasSimulacion) {
        this.diasSimulacion = diasSimulacion;
    }

    public int getHorasPorDia() {
        return horasPorDia;
    }

    public void setHorasPorDia(int horasPorDia) {
        this.horasPorDia = horasPorDia;
    }

    public int getReplicas() {
        return replicas;
    }

    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }

    public int getVelocidadSimulacion() {
        return velocidadSimulacion;
    }

    public void setVelocidadSimulacion(int velocidadSimulacion) {
        this.velocidadSimulacion = velocidadSimulacion;
    }

    public double getMediaArribos() {
        return mediaArribos;
    }

    public void setMediaArribos(double mediaArribos) {
        this.mediaArribos = mediaArribos;
    }

    public int getCapacidadLavadora() {
        return capacidadLavadora;
    }

    public void setCapacidadLavadora(int capacidadLavadora) {
        this.capacidadLavadora = capacidadLavadora;
    }

    public double getMediaTiempoLavado() {
        return mediaTiempoLavado;
    }

    public void setMediaTiempoLavado(double mediaTiempoLavado) {
        this.mediaTiempoLavado = mediaTiempoLavado;
    }

    public double getDesviacionTiempoLavado() {
        return desviacionTiempoLavado;
    }

    public void setDesviacionTiempoLavado(double desviacionTiempoLavado) {
        this.desviacionTiempoLavado = desviacionTiempoLavado;
    }

    public int getCapacidadAlmacenPintura() {
        return capacidadAlmacenPintura;
    }

    public void setCapacidadAlmacenPintura(int capacidadAlmacenPintura) {
        this.capacidadAlmacenPintura = capacidadAlmacenPintura;
    }

    public int getCapacidadPintura() {
        return capacidadPintura;
    }

    public void setCapacidadPintura(int capacidadPintura) {
        this.capacidadPintura = capacidadPintura;
    }

    public double getMinPintura() {
        return minPintura;
    }

    public void setMinPintura(double minPintura) {
        this.minPintura = minPintura;
    }

    public double getModaPintura() {
        return modaPintura;
    }

    public void setModaPintura(double modaPintura) {
        this.modaPintura = modaPintura;
    }

    public double getMaxPintura() {
        return maxPintura;
    }

    public void setMaxPintura(double maxPintura) {
        this.maxPintura = maxPintura;
    }

    public int getCapacidadAlmacenHorno() {
        return capacidadAlmacenHorno;
    }

    public void setCapacidadAlmacenHorno(int capacidadAlmacenHorno) {
        this.capacidadAlmacenHorno = capacidadAlmacenHorno;
    }

    public int getCapacidadHorno() {
        return capacidadHorno;
    }

    public void setCapacidadHorno(int capacidadHorno) {
        this.capacidadHorno = capacidadHorno;
    }

    public double getCentroHorno() {
        return centroHorno;
    }

    public void setCentroHorno(double centroHorno) {
        this.centroHorno = centroHorno;
    }

    public double getAmplitudHorno() {
        return amplitudHorno;
    }

    public void setAmplitudHorno(double amplitudHorno) {
        this.amplitudHorno = amplitudHorno;
    }

    public int getNumeroInspectores() {
        return numeroInspectores;
    }

    public void setNumeroInspectores(int numeroInspectores) {
        this.numeroInspectores = numeroInspectores;
    }

    public int getElementosPorPieza() {
        return elementosPorPieza;
    }

    public void setElementosPorPieza(int elementosPorPieza) {
        this.elementosPorPieza = elementosPorPieza;
    }

    public double getMediaTiempoInspeccionElemento() {
        return mediaTiempoInspeccionElemento;
    }

    public void setMediaTiempoInspeccionElemento(double mediaTiempoInspeccionElemento) {
        this.mediaTiempoInspeccionElemento = mediaTiempoInspeccionElemento;
    }

    public double getMediaTrasladoRecepcionLavado() {
        return mediaTrasladoRecepcionLavado;
    }

    public void setMediaTrasladoRecepcionLavado(double mediaTrasladoRecepcionLavado) {
        this.mediaTrasladoRecepcionLavado = mediaTrasladoRecepcionLavado;
    }

    public double getMediaTrasladoLavadoPintura() {
        return mediaTrasladoLavadoPintura;
    }

    public void setMediaTrasladoLavadoPintura(double mediaTrasladoLavadoPintura) {
        this.mediaTrasladoLavadoPintura = mediaTrasladoLavadoPintura;
    }

    public double getCentroTrasladoPinturaHorno() {
        return centroTrasladoPinturaHorno;
    }

    public void setCentroTrasladoPinturaHorno(double centroTrasladoPinturaHorno) {
        this.centroTrasladoPinturaHorno = centroTrasladoPinturaHorno;
    }

    public double getAmplitudTrasladoPinturaHorno() {
        return amplitudTrasladoPinturaHorno;
    }

    public void setAmplitudTrasladoPinturaHorno(double amplitudTrasladoPinturaHorno) {
        this.amplitudTrasladoPinturaHorno = amplitudTrasladoPinturaHorno;
    }

    public double getCentroTrasladoHornoInspeccion() {
        return centroTrasladoHornoInspeccion;
    }

    public void setCentroTrasladoHornoInspeccion(double centroTrasladoHornoInspeccion) {
        this.centroTrasladoHornoInspeccion = centroTrasladoHornoInspeccion;
    }

    public double getAmplitudTrasladoHornoInspeccion() {
        return amplitudTrasladoHornoInspeccion;
    }

    public void setAmplitudTrasladoHornoInspeccion(double amplitudTrasladoHornoInspeccion) {
        this.amplitudTrasladoHornoInspeccion = amplitudTrasladoHornoInspeccion;
    }

    public double getUtilidadPorPieza() {
        return utilidadPorPieza;
    }

    public void setUtilidadPorPieza(double utilidadPorPieza) {
        this.utilidadPorPieza = utilidadPorPieza;
    }

    public double getCostoHornoAdicional() {
        return costoHornoAdicional;
    }

    public void setCostoHornoAdicional(double costoHornoAdicional) {
        this.costoHornoAdicional = costoHornoAdicional;
    }

    public double getCostoInspectorAdicional() {
        return costoInspectorAdicional;
    }

    public void setCostoInspectorAdicional(double costoInspectorAdicional) {
        this.costoInspectorAdicional = costoInspectorAdicional;
    }

    public String getNombreEscenario() {
        return nombreEscenario;
    }

    public void setNombreEscenario(String nombreEscenario) {
        this.nombreEscenario = nombreEscenario;
    }

    public boolean isModoRapido() {
        return modoRapido;
    }

    public void setModoRapido(boolean modoRapido) {
        this.modoRapido = modoRapido;
    }

    public boolean isGuardarDetalle() {
        return guardarDetalle;
    }

    public void setGuardarDetalle(boolean guardarDetalle) {
        this.guardarDetalle = guardarDetalle;
    }

    /**
     * Obtiene el tiempo total de simulacion en minutos
     * @return tiempo total en minutos
     */
    public double getTiempoTotalSimulacion() {
        return diasSimulacion * horasPorDia * 60.0;
    }

    /**
     * Crea una copia de los parametros actuales
     * @return nueva instancia con los mismos valores
     */
    public SimulacionParametros copiar() {
        SimulacionParametros copia = new SimulacionParametros();
        copia.setDiasSimulacion(this.diasSimulacion);
        copia.setHorasPorDia(this.horasPorDia);
        copia.setReplicas(this.replicas);
        copia.setVelocidadSimulacion(this.velocidadSimulacion);
        copia.setMediaArribos(this.mediaArribos);
        copia.setCapacidadLavadora(this.capacidadLavadora);
        copia.setMediaTiempoLavado(this.mediaTiempoLavado);
        copia.setDesviacionTiempoLavado(this.desviacionTiempoLavado);
        copia.setCapacidadAlmacenPintura(this.capacidadAlmacenPintura);
        copia.setCapacidadPintura(this.capacidadPintura);
        copia.setMinPintura(this.minPintura);
        copia.setModaPintura(this.modaPintura);
        copia.setMaxPintura(this.maxPintura);
        copia.setCapacidadAlmacenHorno(this.capacidadAlmacenHorno);
        copia.setCapacidadHorno(this.capacidadHorno);
        copia.setCentroHorno(this.centroHorno);
        copia.setAmplitudHorno(this.amplitudHorno);
        copia.setNumeroInspectores(this.numeroInspectores);
        copia.setElementosPorPieza(this.elementosPorPieza);
        copia.setMediaTiempoInspeccionElemento(this.mediaTiempoInspeccionElemento);
        copia.setNombreEscenario(this.nombreEscenario);
        copia.setModoRapido(this.modoRapido);
        copia.setGuardarDetalle(this.guardarDetalle);
        return copia;
    }
}
