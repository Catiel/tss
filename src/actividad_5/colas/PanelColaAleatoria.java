package actividad_5.colas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

/** Panel que simula la cola con números aleatorios generados automáticamente. */
public class PanelColaAleatoria extends JPanel {
    private final JSpinner spClientes;
    private final JButton btnSimular;
    private final DefaultTableModel modeloServicio;
    private final DefaultTableModel modeloLlegadas;
    private final DefaultTableModel modeloSim;
    private final JTextArea resumen;

    public PanelColaAleatoria(){
        setLayout(new BorderLayout(8,8));
        EstilosUI.aplicarEstiloPanel(this);

        JLabel titulo = new JLabel("Simulación aleatoria Cola Banco BNB");
        EstilosUI.aplicarEstiloTitulo(titulo);
        add(titulo, BorderLayout.NORTH);

        // Controles superiores
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        EstilosUI.aplicarEstiloPanel(controles);
        controles.add(new JLabel("Número de clientes:"));
        spClientes = new JSpinner(new SpinnerNumberModel(20,1,200,1));
        controles.add(spClientes);
        btnSimular = new JButton("Ejecutar Simulación");
        EstilosUI.aplicarEstiloBoton(btnSimular);
        controles.add(btnSimular);
        add(controles, BorderLayout.BEFORE_FIRST_LINE);

        // Panel principal dividido
        JPanel panelPrincipal = new JPanel(new BorderLayout(8,8));
        EstilosUI.aplicarEstiloPanel(panelPrincipal);

        // Panel izquierdo: tablas de distribución (referencia)
        JPanel panelIzq = new JPanel(new GridLayout(2,1,5,5));
        EstilosUI.aplicarEstiloPanel(panelIzq);

        // 1) Distribución tiempo de servicio
        modeloServicio = new DefaultTableModel(new Object[]{"Probabilidad","Distribución acumulada","Rango de Nros aleatorios","Tiempo de servicio"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        llenarDistribucionServicio();
        JTable tServ = new JTable(modeloServicio);
        EstilosUI.aplicarEstiloTabla(tServ);
        tServ.getTableHeader().setBackground(new Color(255, 240, 200));
        tServ.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tServ.getColumnModel().getColumn(0).setPreferredWidth(80);
        tServ.getColumnModel().getColumn(1).setPreferredWidth(100);
        tServ.getColumnModel().getColumn(2).setPreferredWidth(120);
        tServ.getColumnModel().getColumn(3).setPreferredWidth(90);
        JScrollPane spServ = new JScrollPane(tServ);
        spServ.setBorder(BorderFactory.createTitledBorder("Tiempo de servicio (referencia)"));
        spServ.setPreferredSize(new Dimension(400, 180));
        panelIzq.add(spServ);

        // 2) Distribución tiempo entre llegadas
        modeloLlegadas = new DefaultTableModel(new Object[]{"Probabilidad","Distribución acumulada","Rango de Nros aleatorios","Tiempo entre llegadas"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        llenarDistribucionLlegadas();
        JTable tLleg = new JTable(modeloLlegadas);
        EstilosUI.aplicarEstiloTabla(tLleg);
        tLleg.getTableHeader().setBackground(new Color(200, 255, 200));
        tLleg.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tLleg.getColumnModel().getColumn(0).setPreferredWidth(80);
        tLleg.getColumnModel().getColumn(1).setPreferredWidth(100);
        tLleg.getColumnModel().getColumn(2).setPreferredWidth(120);
        tLleg.getColumnModel().getColumn(3).setPreferredWidth(110);
        JScrollPane spLleg = new JScrollPane(tLleg);
        spLleg.setBorder(BorderFactory.createTitledBorder("Tiempo entre llegadas (referencia)"));
        spLleg.setPreferredSize(new Dimension(400, 200));
        panelIzq.add(spLleg);

        panelPrincipal.add(panelIzq, BorderLayout.WEST);

        // Panel derecho: tabla de simulación
        modeloSim = new DefaultTableModel(new Object[]{
            "# de cliente","# aleatorio","Intervalo entre llegadas","hora de llegadas","# aleatorio","t servicio","inicio del servicio","final del servicio","t espera","t ocioso"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
            @Override public Class<?> getColumnClass(int c){
                if(c == 0 || c == 2 || c == 5 || c == 8 || c == 9) return Integer.class;
                return String.class;
            }
        };

        JTable tablaSim = new JTable(modeloSim){
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer,int row,int column){
                Component c = super.prepareRenderer(renderer,row,column);
                if(row < getRowCount()-2){ // fila de datos (no totales ni promedio)
                    // Resaltar tiempos de espera > 0
                    Object espera = getValueAt(row, 8);
                    if(espera instanceof Integer && (Integer)espera > 0){
                        c.setBackground(new Color(255, 255, 200)); // amarillo claro para espera
                    } else if(row % 2 == 0){
                        c.setBackground(Color.WHITE);
                    } else {
                        c.setBackground(new Color(245, 245, 245));
                    }
                } else { // fila de totales o promedio
                    c.setBackground(new Color(220, 220, 220));
                }
                return c;
            }
        };

        EstilosUI.aplicarEstiloTabla(tablaSim);
        tablaSim.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablaSim.getColumnModel().getColumn(0).setPreferredWidth(60);  // # cliente
        tablaSim.getColumnModel().getColumn(1).setPreferredWidth(70);  // # aleatorio llegada
        tablaSim.getColumnModel().getColumn(2).setPreferredWidth(90);  // intervalo llegadas
        tablaSim.getColumnModel().getColumn(3).setPreferredWidth(85);  // hora llegadas
        tablaSim.getColumnModel().getColumn(4).setPreferredWidth(70);  // # aleatorio servicio
        tablaSim.getColumnModel().getColumn(5).setPreferredWidth(65);  // t servicio
        tablaSim.getColumnModel().getColumn(6).setPreferredWidth(90);  // inicio servicio
        tablaSim.getColumnModel().getColumn(7).setPreferredWidth(85);  // final servicio
        tablaSim.getColumnModel().getColumn(8).setPreferredWidth(60);  // t espera
        tablaSim.getColumnModel().getColumn(9).setPreferredWidth(60);  // t ocioso

        JScrollPane spSim = new JScrollPane(tablaSim);
        spSim.setBorder(BorderFactory.createTitledBorder("Resultados de la simulación"));
        panelPrincipal.add(spSim, BorderLayout.CENTER);

        add(panelPrincipal, BorderLayout.CENTER);

        // Área de resumen
        resumen = new JTextArea();
        resumen.setEditable(false);
        resumen.setBackground(getBackground());
        resumen.setLineWrap(true);
        resumen.setWrapStyleWord(true);
        resumen.setBorder(BorderFactory.createTitledBorder("Análisis de resultados"));
        resumen.setFont(new Font("Arial", Font.PLAIN, 12));
        resumen.setPreferredSize(new Dimension(0, 140));
        add(resumen, BorderLayout.SOUTH);

        btnSimular.addActionListener(this::simular);

        // Ejecutar simulación inicial
        simular(null);
    }

    private void llenarDistribucionServicio(){
        modeloServicio.setRowCount(0);
        double[][] rangos = ColaBancoModelo.getRangosServicio();

        for(int i = 0; i < ColaBancoModelo.SERVICIO_VALORES.length; i++){
            String rango = UtilFormatoColas.f2(rangos[i][0]) + " - " + UtilFormatoColas.f2(rangos[i][1]);
            modeloServicio.addRow(new Object[]{
                UtilFormatoColas.f2(ColaBancoModelo.SERVICIO_PROBS[i]),
                UtilFormatoColas.f2(getSumaAcumulada(ColaBancoModelo.SERVICIO_PROBS, i)),
                rango,
                ColaBancoModelo.SERVICIO_VALORES[i]
            });
        }
    }

    private void llenarDistribucionLlegadas(){
        modeloLlegadas.setRowCount(0);
        double[][] rangos = ColaBancoModelo.getRangosLlegada();

        for(int i = 0; i < ColaBancoModelo.LLEGADA_VALORES.length; i++){
            String rango = UtilFormatoColas.f2(rangos[i][0]) + " - " + UtilFormatoColas.f2(rangos[i][1]);
            modeloLlegadas.addRow(new Object[]{
                UtilFormatoColas.f2(ColaBancoModelo.LLEGADA_PROBS[i]),
                UtilFormatoColas.f2(getSumaAcumulada(ColaBancoModelo.LLEGADA_PROBS, i)),
                rango,
                ColaBancoModelo.LLEGADA_VALORES[i]
            });
        }
    }

    private double getSumaAcumulada(double[] probs, int hasta){
        double suma = 0;
        for(int i = 0; i <= hasta; i++){
            suma += probs[i];
        }
        return suma;
    }

    private void simular(ActionEvent e){
        int clientes = (int) spClientes.getValue();
        modeloSim.setRowCount(0);

        int horaLlegadaAcum = 0; // minutos desde 09:00
        int finServicioAnterior = 0;
        int totalEspera = 0;
        int totalOcioso = 0;
        int clientesConEspera = 0;
        StringBuilder clientesEspera = new StringBuilder();
        boolean first = true;

        for(int i = 1; i <= clientes; i++){
            double rLleg = Math.random();
            int intervaloLlegada = ColaBancoModelo.tiempoInterLlegada(rLleg);
            horaLlegadaAcum += intervaloLlegada;
            String horaLlegada = UtilFormatoColas.horaDesdeBase(horaLlegadaAcum);

            double rServ = Math.random();
            int tiempoServicio = ColaBancoModelo.tiempoServicio(rServ);

            int inicioServicio = Math.max(horaLlegadaAcum, finServicioAnterior);
            int tiempoEspera = inicioServicio - horaLlegadaAcum;
            int tiempoOcioso = (inicioServicio > finServicioAnterior) ? inicioServicio - finServicioAnterior : 0;
            int finServicio = inicioServicio + tiempoServicio;

            if(tiempoEspera > 0){
                clientesConEspera++;
                if(!first) clientesEspera.append(", ");
                clientesEspera.append(i);
                first = false;
            }

            totalEspera += tiempoEspera;
            totalOcioso += tiempoOcioso;

            modeloSim.addRow(new Object[]{
                i,
                UtilFormatoColas.f2(rLleg),
                intervaloLlegada,
                horaLlegada,
                UtilFormatoColas.f2(rServ),
                tiempoServicio,
                UtilFormatoColas.horaDesdeBase(inicioServicio),
                UtilFormatoColas.horaDesdeBase(finServicio),
                tiempoEspera,
                tiempoOcioso
            });

            finServicioAnterior = finServicio;
        }

        // Fila de totales
        modeloSim.addRow(new Object[]{
            "TOTALES",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            totalEspera,
            totalOcioso
        });

        // Fila de promedio
        double promedioEspera = totalEspera / (double)clientes;
        modeloSim.addRow(new Object[]{
            "PROMEDIO espera por cliente",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            UtilFormatoColas.f2(promedioEspera),
            ""
        });

        // Actualizar resumen
        double porcentajeEspera = (clientesConEspera / (double)clientes) * 100;

        StringBuilder sb = new StringBuilder();
        sb.append("RESULTADOS DE LA SIMULACIÓN ALEATORIA (").append(clientes).append(" clientes):\n\n");
        sb.append("TIEMPOS DE ESPERA:\n");
        sb.append("• Total tiempo de espera: ").append(totalEspera).append(" minutos\n");
        sb.append("• Tiempo promedio de espera: ").append(UtilFormatoColas.f2(promedioEspera)).append(" minutos\n");
        sb.append("• Clientes que esperaron: ").append(clientesConEspera).append(" de ").append(clientes);
        sb.append(" (").append(UtilFormatoColas.f2(porcentajeEspera)).append("%)\n");
        if(clientesConEspera > 0) {
            sb.append("• Clientes específicos que esperaron: ").append(clientesEspera).append("\n");
        }
        sb.append("• Tiempo total de cajero ocioso: ").append(totalOcioso).append(" minutos\n\n");

        sb.append("EVALUACIÓN DEL OBJETIVO DEL BANCO:\n");
        sb.append("• Política: Cliente promedio no debe esperar más de 2 minutos\n");
        sb.append("• Resultado: ").append(promedioEspera <= 2.0 ? "✓ CUMPLE" : "✗ NO CUMPLE").append(" el objetivo\n\n");

        if(promedioEspera > 2.0){
            double exceso = promedioEspera - 2.0;
            sb.append("ANÁLISIS DEL PROBLEMA:\n");
            sb.append("• Exceso sobre el objetivo: ").append(UtilFormatoColas.f2(exceso)).append(" minutos\n");
            sb.append("• Porcentaje de clientes afectados: ").append(UtilFormatoColas.f2(porcentajeEspera)).append("%\n\n");
            sb.append("RECOMENDACIONES:\n");
            sb.append("• Considerar agregar más cajeros en la ventanilla auto\n");
            sb.append("• Optimizar el tiempo de servicio por cliente\n");
            sb.append("• Implementar sistema de citas para distribuir llegadas\n\n");
        } else {
            sb.append("CONCLUSIÓN POSITIVA:\n");
            sb.append("• La ventanilla de servicio en el auto cumple satisfactoriamente el criterio establecido\n");
            sb.append("• El sistema actual es eficiente para el nivel de demanda simulado\n\n");
        }

        sb.append("INTERPRETACIÓN:\n");
        sb.append("Las celdas resaltadas en amarillo indican clientes que tuvieron que esperar en la fila.\n");
        sb.append("Con más clientes simulados, los resultados se estabilizan y son más representativos.\n");
        sb.append("Cada ejecución genera resultados diferentes debido a la naturaleza aleatoria de la simulación.");

        resumen.setText(sb.toString());
    }
}