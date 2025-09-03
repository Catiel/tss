package actividad_3.ejercicio_2;

public class ControladorParametros {
    private static ControladorParametros instancia;

    // Parámetros del modelo de Wozac
    private int demandaInicial = 50000;
    private double crecimientoAnual = 0.05;
    private double costoCapacidadUnitaria = 16.0;
    private double precioVentaUnitario = 3.0;
    private double costoVariableUnitario = 0.20;
    private double costoOperativoUnitario = 0.40;

    private ControladorParametros() {
    }

    public static ControladorParametros getInstancia() {
        if (instancia == null) instancia = new ControladorParametros();
        return instancia;
    }

    public int getDemandaInicial() {
        return demandaInicial;
    }

    public void setDemandaInicial(int v) {
        demandaInicial = v;
    }

    public double getCrecimientoAnual() {
        return crecimientoAnual;
    }

    public void setCrecimientoAnual(double v) {
        crecimientoAnual = v;
    }

    public double getCostoCapacidadUnitaria() {
        return costoCapacidadUnitaria;
    }

    public void setCostoCapacidadUnitaria(double v) {
        costoCapacidadUnitaria = v;
    }

    public double getPrecioVentaUnitario() {
        return precioVentaUnitario;
    }

    public void setPrecioVentaUnitario(double v) {
        precioVentaUnitario = v;
    }

    public double getCostoVariableUnitario() {
        return costoVariableUnitario;
    }

    public void setCostoVariableUnitario(double v) {
        costoVariableUnitario = v;
    }

    public double getCostoOperativoUnitario() {
        return costoOperativoUnitario;
    }

    public void setCostoOperativoUnitario(double v) {
        costoOperativoUnitario = v;
    }
}
