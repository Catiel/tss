package actividad_3.ejercicio_1;

public class ControladorParametros {
    private static ControladorParametros instancia;
    private double costeUnitario = 50;
    private double tipoCambio = 1.22;
    private double constanteDemanda = 27556759;
    private double elasticidad = -2.4;

    private ControladorParametros() {
    }

    public static ControladorParametros getInstancia() {
        if (instancia == null) instancia = new ControladorParametros();
        return instancia;
    }

    public double getCosteUnitario() {
        return costeUnitario;
    }

    public void setCosteUnitario(double v) {
        costeUnitario = v;
    }

    public double getTipoCambio() {
        return tipoCambio;
    }

    public void setTipoCambio(double v) {
        tipoCambio = v;
    }

    public double getConstanteDemanda() {
        return constanteDemanda;
    }

    public void setConstanteDemanda(double v) {
        constanteDemanda = v;
    }

    public double getElasticidad() {
        return elasticidad;
    }

    public void setElasticidad(double v) {
        elasticidad = v;
    }

}

