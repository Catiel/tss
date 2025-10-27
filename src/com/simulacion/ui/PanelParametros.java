package com.simulacion.ui;

import com.simulacion.modelo.SimulacionParametros;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Panel mejorado para configurar parametros con soporte de escenarios
 * Similar a la funcionalidad de ProModel
 */
public class PanelParametros extends JPanel {

    private SimulacionParametros parametros;

    // Campos de texto para parametros
    private JTextField txtDiasSimulacion;
    private JTextField txtHorasPorDia;
    private JTextField txtReplicas;
    private JTextField txtMediaArribos;
    private JTextField txtCapacidadLavadora;
    private JTextField txtMediaLavado;
    private JTextField txtDesviacionLavado;
    private JTextField txtCapacidadAlmacenPintura;
    private JTextField txtCapacidadPintura;
    private JTextField txtMinPintura;
    private JTextField txtModaPintura;
    private JTextField txtMaxPintura;
    private JTextField txtCapacidadAlmacenHorno;
    private JTextField txtCapacidadHorno;
    private JTextField txtCentroHorno;
    private JTextField txtAmplitudHorno;
    private JTextField txtNumeroInspectores;

    // Nuevos campos
    private JCheckBox chkModoRapido;
    private JSlider sliderVelocidad;
    private JComboBox<String> cmbEscenarios;
    private JButton btnGuardarEscenario;
    private JButton btnCargarEscenario;

    // Almacenamiento de escenarios
    private Map<String, SimulacionParametros> escenarios;

    /**
     * Constructor del panel de parametros mejorado
     * @param parametros objeto de parametros a modificar
     */
    public PanelParametros(SimulacionParametros parametros) {
        this.parametros = parametros;
        this.escenarios = new HashMap<>();

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));

        // Panel de escenarios
        panelPrincipal.add(crearPanelEscenarios());

        // Panel de parametros con scroll
        JPanel panelParametros = new JPanel(new GridBagLayout());
        crearCampos(panelParametros);

        JScrollPane scrollPane = new JScrollPane(panelParametros);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(panelPrincipal, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        cargarValoresIniciales();
        cargarEscenariosPreestablecidos();
    }

    /**
     * Crea el panel de gestion de escenarios
     */
    private JPanel crearPanelEscenarios() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Gestión de Escenarios"));

        // Combo de escenarios
        cmbEscenarios = new JComboBox<>();
        cmbEscenarios.addActionListener(e -> cargarEscenarioSeleccionado());
        panel.add(cmbEscenarios);

        // Botones
        JPanel panelBotones = new JPanel(new GridLayout(1, 2, 5, 5));
        btnGuardarEscenario = new JButton("Guardar");
        btnCargarEscenario = new JButton("Cargar");

        btnGuardarEscenario.addActionListener(e -> guardarEscenario());
        btnCargarEscenario.addActionListener(e -> cargarEscenarioSeleccionado());

        panelBotones.add(btnGuardarEscenario);
        panelBotones.add(btnCargarEscenario);
        panel.add(panelBotones);

        // Modo rapido
        chkModoRapido = new JCheckBox("Modo Rápido (Sin Animación)", true);
        chkModoRapido.setToolTipText("Ejecuta la simulación a máxima velocidad");
        panel.add(chkModoRapido);

        return panel;
    }

    /**
     * Crea todos los campos de entrada
     */
    private void crearCampos(JPanel panel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.weightx = 1.0;

        int fila = 0;

        // SIMULACION
        agregarSeparador(panel, "═══ SIMULACIÓN ═══", fila++, gbc);
        txtDiasSimulacion = agregarCampo(panel, "Días:", fila++, gbc, "90 días de simulación");
        txtHorasPorDia = agregarCampo(panel, "Horas/día:", fila++, gbc, "24 horas por día");
        txtReplicas = agregarCampo(panel, "Réplicas:", fila++, gbc, "Número de réplicas a ejecutar");

        // ARRIBOS
        agregarSeparador(panel, "═══ ARRIBOS ═══", fila++, gbc);
        txtMediaArribos = agregarCampo(panel, "E(λ) min:", fila++, gbc, "Tiempo entre arribos E(2)");

        // LAVADO
        agregarSeparador(panel, "═══ LAVADO ═══", fila++, gbc);
        txtCapacidadLavadora = agregarCampo(panel, "Capacidad:", fila++, gbc, "5 piezas simultáneas");
        txtMediaLavado = agregarCampo(panel, "N(μ) min:", fila++, gbc, "Media tiempo lavado");
        txtDesviacionLavado = agregarCampo(panel, "N(σ) min:", fila++, gbc, "Desviación estándar");

        // PINTURA
        agregarSeparador(panel, "═══ PINTURA ═══", fila++, gbc);
        txtCapacidadAlmacenPintura = agregarCampo(panel, "Cap. Almacén:", fila++, gbc, "10 piezas máximo");
        txtCapacidadPintura = agregarCampo(panel, "Cap. Pintura:", fila++, gbc, "3 piezas simultáneas");
        txtMinPintura = agregarCampo(panel, "T(a) min:", fila++, gbc, "Mínimo triangular");
        txtModaPintura = agregarCampo(panel, "T(b) min:", fila++, gbc, "Moda triangular");
        txtMaxPintura = agregarCampo(panel, "T(c) min:", fila++, gbc, "Máximo triangular");

        // HORNO
        agregarSeparador(panel, "═══ HORNO ═══", fila++, gbc);
        txtCapacidadAlmacenHorno = agregarCampo(panel, "Cap. Almacén:", fila++, gbc, "10 piezas máximo");
        txtCapacidadHorno = agregarCampo(panel, "Cap. Horno:", fila++, gbc, "1 pieza a la vez");
        txtCentroHorno = agregarCampo(panel, "U(c) min:", fila++, gbc, "Centro uniforme");
        txtAmplitudHorno = agregarCampo(panel, "U(a) min:", fila++, gbc, "Amplitud uniforme");

        // INSPECCION
        agregarSeparador(panel, "═══ INSPECCIÓN ═══", fila++, gbc);
        txtNumeroInspectores = agregarCampo(panel, "Inspectores:", fila++, gbc, "2 inspectores (CORREGIDO)");

        // Espaciador
        gbc.gridy = fila;
        gbc.weighty = 1.0;
        panel.add(new JLabel(), gbc);
    }

    /**
     * Agrega un separador visual con titulo
     */
    private void agregarSeparador(JPanel panel, String titulo, int fila, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.gridwidth = 2;

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 11));
        lblTitulo.setForeground(new Color(0, 102, 204));
        panel.add(lblTitulo, gbc);

        gbc.gridwidth = 1;
    }

    /**
     * Agrega un campo de entrada con etiqueta y tooltip
     */
    private JTextField agregarCampo(JPanel panel, String etiqueta, int fila,
                                     GridBagConstraints gbc, String tooltip) {
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.weightx = 0.4;

        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(new Font("Arial", Font.PLAIN, 10));
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.6;
        JTextField campo = new JTextField(8);
        campo.setFont(new Font("Arial", Font.PLAIN, 10));
        campo.setToolTipText(tooltip);
        panel.add(campo, gbc);

        return campo;
    }

    /**
     * Carga los valores iniciales desde el objeto parametros
     */
    private void cargarValoresIniciales() {
        txtDiasSimulacion.setText(String.valueOf(parametros.getDiasSimulacion()));
        txtHorasPorDia.setText(String.valueOf(parametros.getHorasPorDia()));
        txtReplicas.setText(String.valueOf(parametros.getReplicas()));
        txtMediaArribos.setText(String.valueOf(parametros.getMediaArribos()));
        txtCapacidadLavadora.setText(String.valueOf(parametros.getCapacidadLavadora()));
        txtMediaLavado.setText(String.valueOf(parametros.getMediaTiempoLavado()));
        txtDesviacionLavado.setText(String.valueOf(parametros.getDesviacionTiempoLavado()));
        txtCapacidadAlmacenPintura.setText(String.valueOf(parametros.getCapacidadAlmacenPintura()));
        txtCapacidadPintura.setText(String.valueOf(parametros.getCapacidadPintura()));
        txtMinPintura.setText(String.valueOf(parametros.getMinPintura()));
        txtModaPintura.setText(String.valueOf(parametros.getModaPintura()));
        txtMaxPintura.setText(String.valueOf(parametros.getMaxPintura()));
        txtCapacidadAlmacenHorno.setText(String.valueOf(parametros.getCapacidadAlmacenHorno()));
        txtCapacidadHorno.setText(String.valueOf(parametros.getCapacidadHorno()));
        txtCentroHorno.setText(String.valueOf(parametros.getCentroHorno()));
        txtAmplitudHorno.setText(String.valueOf(parametros.getAmplitudHorno()));
        txtNumeroInspectores.setText(String.valueOf(parametros.getNumeroInspectores()));
        chkModoRapido.setSelected(parametros.isModoRapido());
    }

    /**
     * Aplica los valores ingresados al objeto parametros
     */
    public void aplicarParametros() {
        try {
            parametros.setDiasSimulacion(Integer.parseInt(txtDiasSimulacion.getText()));
            parametros.setHorasPorDia(Integer.parseInt(txtHorasPorDia.getText()));
            parametros.setReplicas(Integer.parseInt(txtReplicas.getText()));
            parametros.setMediaArribos(Double.parseDouble(txtMediaArribos.getText()));
            parametros.setCapacidadLavadora(Integer.parseInt(txtCapacidadLavadora.getText()));
            parametros.setMediaTiempoLavado(Double.parseDouble(txtMediaLavado.getText()));
            parametros.setDesviacionTiempoLavado(Double.parseDouble(txtDesviacionLavado.getText()));
            parametros.setCapacidadAlmacenPintura(Integer.parseInt(txtCapacidadAlmacenPintura.getText()));
            parametros.setCapacidadPintura(Integer.parseInt(txtCapacidadPintura.getText()));
            parametros.setMinPintura(Double.parseDouble(txtMinPintura.getText()));
            parametros.setModaPintura(Double.parseDouble(txtModaPintura.getText()));
            parametros.setMaxPintura(Double.parseDouble(txtMaxPintura.getText()));
            parametros.setCapacidadAlmacenHorno(Integer.parseInt(txtCapacidadAlmacenHorno.getText()));
            parametros.setCapacidadHorno(Integer.parseInt(txtCapacidadHorno.getText()));
            parametros.setCentroHorno(Double.parseDouble(txtCentroHorno.getText()));
            parametros.setAmplitudHorno(Double.parseDouble(txtAmplitudHorno.getText()));
            parametros.setNumeroInspectores(Integer.parseInt(txtNumeroInspectores.getText()));
            parametros.setModoRapido(chkModoRapido.isSelected());
            parametros.setVelocidadSimulacion(parametros.isModoRapido() ? 0 : 10);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Error en los parámetros ingresados. Verifique los valores.",
                "Error de Parámetros",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Carga escenarios preestablecidos
     */
    private void cargarEscenariosPreestablecidos() {
        // Escenario Base (actual)
        SimulacionParametros base = parametros.copiar();
        base.setNombreEscenario("Escenario Base");
        escenarios.put("Escenario Base", base);

        // Escenario con 2 hornos
        SimulacionParametros dosHornos = parametros.copiar();
        dosHornos.setNombreEscenario("2 Hornos");
        dosHornos.setCapacidadHorno(2);
        escenarios.put("2 Hornos", dosHornos);

        // Escenario con 3 inspectores
        SimulacionParametros tresInspectores = parametros.copiar();
        tresInspectores.setNombreEscenario("3 Inspectores");
        tresInspectores.setNumeroInspectores(3);
        escenarios.put("3 Inspectores", tresInspectores);

        // Escenario optimizado
        SimulacionParametros optimizado = parametros.copiar();
        optimizado.setNombreEscenario("Optimizado");
        optimizado.setCapacidadHorno(2);
        optimizado.setNumeroInspectores(3);
        escenarios.put("Optimizado", optimizado);

        // Agregar al combo
        cmbEscenarios.removeAllItems();
        for (String nombre : escenarios.keySet()) {
            cmbEscenarios.addItem(nombre);
        }
    }

    /**
     * Guarda el escenario actual
     */
    private void guardarEscenario() {
        String nombre = JOptionPane.showInputDialog(this,
            "Nombre del escenario:",
            "Guardar Escenario",
            JOptionPane.QUESTION_MESSAGE);

        if (nombre != null && !nombre.trim().isEmpty()) {
            aplicarParametros();
            SimulacionParametros nuevo = parametros.copiar();
            nuevo.setNombreEscenario(nombre);
            escenarios.put(nombre, nuevo);

            if (cmbEscenarios.getSelectedItem() == null ||
                !cmbEscenarios.getSelectedItem().equals(nombre)) {
                cmbEscenarios.addItem(nombre);
            }
            cmbEscenarios.setSelectedItem(nombre);

            JOptionPane.showMessageDialog(this,
                "Escenario guardado exitosamente",
                "Guardar",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Carga el escenario seleccionado
     */
    private void cargarEscenarioSeleccionado() {
        String seleccionado = (String) cmbEscenarios.getSelectedItem();
        if (seleccionado != null && escenarios.containsKey(seleccionado)) {
            SimulacionParametros escenario = escenarios.get(seleccionado);

            // Copiar valores del escenario a parametros
            parametros.setDiasSimulacion(escenario.getDiasSimulacion());
            parametros.setHorasPorDia(escenario.getHorasPorDia());
            parametros.setReplicas(escenario.getReplicas());
            parametros.setMediaArribos(escenario.getMediaArribos());
            parametros.setCapacidadLavadora(escenario.getCapacidadLavadora());
            parametros.setMediaTiempoLavado(escenario.getMediaTiempoLavado());
            parametros.setDesviacionTiempoLavado(escenario.getDesviacionTiempoLavado());
            parametros.setCapacidadAlmacenPintura(escenario.getCapacidadAlmacenPintura());
            parametros.setCapacidadPintura(escenario.getCapacidadPintura());
            parametros.setMinPintura(escenario.getMinPintura());
            parametros.setModaPintura(escenario.getModaPintura());
            parametros.setMaxPintura(escenario.getMaxPintura());
            parametros.setCapacidadAlmacenHorno(escenario.getCapacidadAlmacenHorno());
            parametros.setCapacidadHorno(escenario.getCapacidadHorno());
            parametros.setCentroHorno(escenario.getCentroHorno());
            parametros.setAmplitudHorno(escenario.getAmplitudHorno());
            parametros.setNumeroInspectores(escenario.getNumeroInspectores());
            parametros.setModoRapido(escenario.isModoRapido());

            // Actualizar UI
            cargarValoresIniciales();
        }
    }
}
