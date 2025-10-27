package com.simulacion.ui;

import com.simulacion.estadisticas.EstadisticasSimulacion;
import com.simulacion.estadisticas.ResultadoReplica;
import com.simulacion.modelo.SimulacionParametros;
import com.simulacion.motor.MotorSimulacion;
import com.simulacion.motor.SimulacionListener;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Ventana principal de la aplicacion de simulacion
 * Integra todos los paneles y maneja multiples replicas
 */
public class SimulacionFrame extends JFrame implements SimulacionListener {

    private SimulacionParametros parametros;
    private MotorSimulacion motor;
    private Thread hiloSimulacion;

    private PanelParametros panelParametros;
    private PanelAnimacion panelAnimacion;
    private PanelGraficas panelGraficas;
    private PanelEstadisticas panelEstadisticas;

    private JButton btnIniciar;
    private JButton btnPausar;
    private JButton btnDetener;
    private JButton btnReiniciar;

    private JLabel lblTiempoSimulacion;
    private JProgressBar progressBar;

    private List<ResultadoReplica> resultadosReplicas;
    private int replicaActual;

    public SimulacionFrame() {
        parametros = new SimulacionParametros();
        parametros.setReplicas(3); // 3 replicas por defecto
        motor = new MotorSimulacion(parametros);
        motor.agregarListener(this);

        resultadosReplicas = new ArrayList<>();
        replicaActual = 0;

        configurarVentana();
        crearComponentes();
        organizarLayout();
    }

    private void configurarVentana() {
        setTitle("Simulación de Línea de Empaque - ProModel en Java");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
    }

    private void crearComponentes() {
        panelParametros = new PanelParametros(parametros);
        panelAnimacion = new PanelAnimacion();
        panelGraficas = new PanelGraficas();
        panelEstadisticas = new PanelEstadisticas();

        btnIniciar = new JButton("Iniciar Simulación");
        btnPausar = new JButton("Pausar");
        btnDetener = new JButton("Detener");
        btnReiniciar = new JButton("Reiniciar");

        btnIniciar.setEnabled(true);
        btnPausar.setEnabled(false);
        btnDetener.setEnabled(false);
        btnReiniciar.setEnabled(false);

        lblTiempoSimulacion = new JLabel("Tiempo: 0.00 min");
        lblTiempoSimulacion.setFont(new Font("Arial", Font.BOLD, 14));

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);

        configurarAcciones();
    }

    private void configurarAcciones() {
        btnIniciar.addActionListener(e -> iniciarSimulacion());
        btnPausar.addActionListener(e -> pausarReanudarSimulacion());
        btnDetener.addActionListener(e -> detenerSimulacion());
        btnReiniciar.addActionListener(e -> reiniciarSimulacion());
    }

    private void organizarLayout() {
        setLayout(new BorderLayout(10, 10));

        // Panel izquierdo (parametros)
        JPanel panelIzquierdo = new JPanel(new BorderLayout());
        panelIzquierdo.add(new JLabel("Parámetros de Simulación", JLabel.CENTER), BorderLayout.NORTH);
        panelIzquierdo.add(new JScrollPane(panelParametros), BorderLayout.CENTER);
        panelIzquierdo.setPreferredSize(new Dimension(300, 0));

        // Panel central (animacion y graficas)
        JPanel panelCentral = new JPanel(new BorderLayout(5, 5));

        JPanel panelAnimacionConTitulo = new JPanel(new BorderLayout());
        panelAnimacionConTitulo.add(new JLabel("Visualización del Proceso", JLabel.CENTER), BorderLayout.NORTH);
        panelAnimacionConTitulo.add(panelAnimacion, BorderLayout.CENTER);
        panelAnimacionConTitulo.setPreferredSize(new Dimension(0, 400));

        JPanel panelGraficasConTitulo = new JPanel(new BorderLayout());
        panelGraficasConTitulo.add(new JLabel("Gráficas en Tiempo Real", JLabel.CENTER), BorderLayout.NORTH);
        panelGraficasConTitulo.add(panelGraficas, BorderLayout.CENTER);

        panelCentral.add(panelAnimacionConTitulo, BorderLayout.NORTH);
        panelCentral.add(panelGraficasConTitulo, BorderLayout.CENTER);

        // Panel derecho (estadisticas)
        JPanel panelDerecho = new JPanel(new BorderLayout());
        panelDerecho.add(new JLabel("Estadísticas", JLabel.CENTER), BorderLayout.NORTH);
        panelDerecho.add(panelEstadisticas, BorderLayout.CENTER);
        panelDerecho.setPreferredSize(new Dimension(300, 0));

        // Panel inferior (controles)
        JPanel panelControl = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panelControl.add(btnIniciar);
        panelControl.add(btnPausar);
        panelControl.add(btnDetener);
        panelControl.add(btnReiniciar);
        panelControl.add(new JLabel("    "));
        panelControl.add(lblTiempoSimulacion);

        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.add(panelControl, BorderLayout.NORTH);
        panelInferior.add(progressBar, BorderLayout.CENTER);

        // Agregar todos los paneles
        add(panelIzquierdo, BorderLayout.WEST);
        add(panelCentral, BorderLayout.CENTER);
        add(panelDerecho, BorderLayout.EAST);
        add(panelInferior, BorderLayout.SOUTH);
    }

    private void iniciarSimulacion() {
        panelParametros.aplicarParametros();

        int replicas = parametros.getReplicas();
        String mensaje = String.format(
            "Se ejecutarán %d réplicas de %d días cada una.\n" +
            "Modo: %s\n\n" +
            "¿Desea continuar?",
            replicas,
            parametros.getDiasSimulacion(),
            parametros.isModoRapido() ? "RÁPIDO (sin animación)" : "VISUAL (con animación)"
        );

        int opcion = JOptionPane.showConfirmDialog(this,
            mensaje,
            "Confirmar Simulación",
            JOptionPane.YES_NO_OPTION);

        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }

        btnIniciar.setEnabled(false);
        btnPausar.setEnabled(!parametros.isModoRapido());
        btnDetener.setEnabled(true);
        btnReiniciar.setEnabled(false);

        panelGraficas.limpiar();
        panelAnimacion.limpiar();
        panelEstadisticas.limpiar();
        panelEstadisticas.setParametros(parametros);

        resultadosReplicas.clear();
        replicaActual = 0;

        hiloSimulacion = new Thread(() -> ejecutarReplicas());
        hiloSimulacion.start();
    }

    private void ejecutarReplicas() {
        long inicioTotal = System.currentTimeMillis();
        int totalReplicas = parametros.getReplicas();

        for (int i = 1; i <= totalReplicas; i++) {
            replicaActual = i;

            SwingUtilities.invokeLater(() -> {
                lblTiempoSimulacion.setText(String.format("Réplica %d de %d",
                    replicaActual, totalReplicas));
            });

            motor = new MotorSimulacion(parametros);
            motor.agregarListener(this);
            motor.agregarListener(panelGraficas);

            if (!parametros.isModoRapido()) {
                motor.agregarListener(panelAnimacion);
            }

            long inicioReplica = System.currentTimeMillis();
            motor.ejecutarSimulacion();
            long finReplica = System.currentTimeMillis();

            guardarResultadoReplica(motor.getEstadisticas(), i);

            double segundos = (finReplica - inicioReplica) / 1000.0;
            System.out.println(String.format("Réplica %d completada en %.2f segundos", i, segundos));

            if (i < totalReplicas) {
                panelGraficas.limpiar();
                panelAnimacion.limpiar();
            }
        }

        long finTotal = System.currentTimeMillis();
        double segundosTotal = (finTotal - inicioTotal) / 1000.0;
        System.out.println(String.format("Todas las réplicas completadas en %.2f segundos", segundosTotal));

        SwingUtilities.invokeLater(() -> {
            mostrarResumenConsolidado();
            btnIniciar.setEnabled(false);
            btnPausar.setEnabled(false);
            btnDetener.setEnabled(false);
            btnReiniciar.setEnabled(true);
            progressBar.setValue(100);
            progressBar.setString("Simulación Completada");

            JOptionPane.showMessageDialog(this,
                String.format("Simulación completada\n\n%d réplicas ejecutadas\nPromedio: %.0f piezas completadas",
                    totalReplicas,
                    resultadosReplicas.stream().mapToInt(ResultadoReplica::getPiezasCompletadas).average().orElse(0)),
                "Simulación Finalizada",
                JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void guardarResultadoReplica(EstadisticasSimulacion stats, int numeroReplica) {
        ResultadoReplica resultado = new ResultadoReplica(numeroReplica);
        double tiempoTotal = parametros.getTiempoTotalSimulacion();

        resultado.setPiezasCompletadas(stats.getPiezasCompletadas());
        resultado.setPiezasEnSistema(stats.getPiezasEnSistema());
        resultado.setTiempoPromedioEnSistema(stats.getTiempoPromedioEnSistema());
        resultado.setTiempoPromedioEspera(stats.getTiempoPromedioEspera());

        resultado.setUtilizacionLavado(stats.getUtilizacionLavado(tiempoTotal, parametros.getCapacidadLavadora()));
        resultado.setUtilizacionPintura(stats.getUtilizacionPintura(tiempoTotal, parametros.getCapacidadPintura()));
        resultado.setUtilizacionHorno(stats.getUtilizacionHorno(tiempoTotal, parametros.getCapacidadHorno()));
        resultado.setUtilizacionInspeccion(stats.getUtilizacionInspeccion(tiempoTotal, parametros.getNumeroInspectores()));

        resultado.setMaxColaRecepcion(stats.getMaxColaLavado());
        resultado.setMaxColaAlmacenPintura(stats.getMaxColaAlmacenPintura());
        resultado.setMaxColaAlmacenHorno(stats.getMaxColaAlmacenHorno());
        resultado.setMaxColaInspeccion(stats.getMaxColaInspeccion());

        resultado.setColaPromedioRecepcion(stats.getColaPromedioLavado(tiempoTotal));
        resultado.setColaPromedioAlmacenPintura(stats.getColaPromedioAlmacenPintura(tiempoTotal));
        resultado.setColaPromedioAlmacenHorno(stats.getColaPromedioAlmacenHorno(tiempoTotal));
        resultado.setColaPromedioInspeccion(stats.getColaPromedioInspeccion(tiempoTotal));

        resultadosReplicas.add(resultado);
    }

    private void mostrarResumenConsolidado() {
        panelEstadisticas.mostrarResumenReplicas(resultadosReplicas, parametros);
    }

    private void pausarReanudarSimulacion() {
        if (motor.isPausada()) {
            motor.reanudar();
            btnPausar.setText("Pausar");
        } else {
            motor.pausar();
            btnPausar.setText("Reanudar");
        }
    }

    private void detenerSimulacion() {
        motor.detener();
        if (hiloSimulacion != null) {
            hiloSimulacion.interrupt();
        }
        btnIniciar.setEnabled(false);
        btnPausar.setEnabled(false);
        btnDetener.setEnabled(false);
        btnReiniciar.setEnabled(true);
    }

    private void reiniciarSimulacion() {
        motor.inicializar();
        panelGraficas.limpiar();
        panelAnimacion.limpiar();
        panelEstadisticas.limpiar();

        lblTiempoSimulacion.setText("Tiempo: 0.00 min");
        progressBar.setValue(0);

        btnIniciar.setEnabled(true);
        btnPausar.setEnabled(false);
        btnDetener.setEnabled(false);
        btnReiniciar.setEnabled(false);
        btnPausar.setText("Pausar");

        resultadosReplicas.clear();
        replicaActual = 0;
    }

    @Override
    public void onActualizacion(double tiempoActual, EstadisticasSimulacion estadisticas) {
        SwingUtilities.invokeLater(() -> {
            lblTiempoSimulacion.setText(String.format("Réplica %d - Tiempo: %.2f min (%.2f días)",
                replicaActual, tiempoActual, tiempoActual / (60 * 24)));

            double progresoReplica = (tiempoActual / parametros.getTiempoTotalSimulacion()) * 100;
            double progresoTotal = ((replicaActual - 1) * 100.0 + progresoReplica) / parametros.getReplicas();

            progressBar.setValue((int) progresoTotal);
            progressBar.setString(String.format("Réplica %d/%d - %.1f%%",
                replicaActual, parametros.getReplicas(), progresoTotal));

            panelEstadisticas.actualizar(tiempoActual, estadisticas, parametros);
        });
    }

    @Override
    public void onFinalizacion(EstadisticasSimulacion estadisticas) {
        // No hacer nada aqui, el resumen consolidado se muestra
        // despues de todas las replicas en mostrarResumenConsolidado()
    }
}
