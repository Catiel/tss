package actividad_4.ejercicio_1;

/**
 * Controlador de parámetros específico para el EJERCICIO 1 (versión francesa).
 * Patrón Singleton: centraliza los parámetros usados por la UI y el modelo de cálculo del VAN.
 *
 * Parámetros por defecto adaptados al enunciado:
 * - Precio unitario: $50
 * - Coste variable unitario: $10
 * - Tamaño de mercado inicial: 300000 unidades
 * - Crecimiento: 10% los primeros 5 años, 5% los siguientes 5 años
 * - Cuota de mercado actual (versión en inglés): 30%
 * - Cuota de mercado con la nueva versión (francesa): 40%
 * - Coste fijo de crear la versión francesa: $6.000.000
 * - Horizonte: 10 años
 */
public class ControladorParametros {
    private static ControladorParametros instancia;

    // Parámetros del ejercicio
    private int tamanoMercadoInicial = 300_000;
    private double crecimientoPrimeros5 = 0.10; // 10%
    private double crecimientoProximos5 = 0.05; // 5%
    private double precioVentaUnitario = 50.0;
    private double costoVariableUnitario = 10.0;
    private double costeFijoNuevaVersion = 6_000_000.0;
    private double cuotaMercadoVersionIngles = 0.30; // 30%
    private double cuotaMercadoConNuevaVersion = 0.40; // 40%
    private int horizonteAnios = 10;
    private double tasaDescuento = 0.10; // Por defecto 10%
    // Campos de compatibilidad con la versión previa del paquete
    // (algunos panels y modelos esperan estos nombres)
    private int demandaInicial = tamanoMercadoInicial;
    private double crecimientoAnual = crecimientoPrimeros5;
    private ControladorParametros() {
    }

    public static ControladorParametros getInstancia() {
        if (instancia == null) instancia = new ControladorParametros();
        return instancia;
    }

    // Getters / Setters
    public int getTamanoMercadoInicial() {
        return tamanoMercadoInicial;
    }

    public void setTamanoMercadoInicial(int tamanoMercadoInicial) {
        this.tamanoMercadoInicial = tamanoMercadoInicial;
    }

    public double getCrecimientoPrimeros5() {
        return crecimientoPrimeros5;
    }

    public void setCrecimientoPrimeros5(double crecimientoPrimeros5) {
        this.crecimientoPrimeros5 = crecimientoPrimeros5;
    }

    public double getCrecimientoProximos5() {
        return crecimientoProximos5;
    }

    public void setCrecimientoProximos5(double crecimientoProximos5) {
        this.crecimientoProximos5 = crecimientoProximos5;
    }

    public double getPrecioVentaUnitario() {
        return precioVentaUnitario;
    }

    public void setPrecioVentaUnitario(double precioVentaUnitario) {
        this.precioVentaUnitario = precioVentaUnitario;
    }

    public double getCostoVariableUnitario() {
        return costoVariableUnitario;
    }

    public void setCostoVariableUnitario(double costoVariableUnitario) {
        this.costoVariableUnitario = costoVariableUnitario;
    }

    public double getCosteFijoNuevaVersion() {
        return costeFijoNuevaVersion;
    }

    public void setCosteFijoNuevaVersion(double costeFijoNuevaVersion) {
        this.costeFijoNuevaVersion = costeFijoNuevaVersion;
    }

    public double getCuotaMercadoVersionIngles() {
        return cuotaMercadoVersionIngles;
    }

    public void setCuotaMercadoVersionIngles(double cuotaMercadoVersionIngles) {
        this.cuotaMercadoVersionIngles = cuotaMercadoVersionIngles;
    }

    public double getCuotaMercadoConNuevaVersion() {
        return cuotaMercadoConNuevaVersion;
    }

    public void setCuotaMercadoConNuevaVersion(double cuotaMercadoConNuevaVersion) {
        this.cuotaMercadoConNuevaVersion = cuotaMercadoConNuevaVersion;
    }

    public int getHorizonteAnios() {
        return horizonteAnios;
    }

    public void setHorizonteAnios(int horizonteAnios) {
        this.horizonteAnios = horizonteAnios;
    }

    public double getTasaDescuento() {
        return tasaDescuento;
    }

    public void setTasaDescuento(double tasaDescuento) {
        this.tasaDescuento = tasaDescuento;
    }

    // Métodos de compatibilidad con el API anterior
    public int getDemandaInicial() {
        return demandaInicial;
    }

    public void setDemandaInicial(int demandaInicial) {
        this.demandaInicial = demandaInicial;
        this.tamanoMercadoInicial = demandaInicial;
    }

    /**
     * Devuelve el crecimiento anual por compatibilidad. Actualmente
     * se usan dos tasas (primeros 5 años y próximos 5 años). Aquí
     * devolvemos la tasa de los primeros 5 años para mantener
     * compatibilidad con código existente.
     */
    public double getCrecimientoAnual() {
        return crecimientoAnual;
    }

    public void setCrecimientoAnual(double crecimientoAnual) {
        this.crecimientoAnual = crecimientoAnual;
        this.crecimientoPrimeros5 = crecimientoAnual;
    }

}
