package actividad_5.colas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Panel con simulación de cola usando los datos específicos del ejemplo del BNB (8 clientes). */
public class PanelColaPredefinida extends JPanel {
    private final DefaultTableModel modeloServicio;
    private final DefaultTableModel modeloLlegadas;
    private final DefaultTableModel modeloSim;
    private final JTextArea resumen;

    // Números aleatorios del ejemplo (8 clientes)
    private static final double[] RAND_LLEGADA = {0.50, 0.28, 0.68, 0.36, 0.90, 0.62, 0.27, 0.50};
    private static final double[] RAND_SERVICIO = {0.52, 0.37, 0.82, 0.69, 0.98, 0.96, 0.33, 0.50};

    public PanelColaPredefinida(){
        setLayout(new BorderLayout(8,8));
        EstilosUI.aplicarEstiloPanel(this);

        JLabel titulo = new JLabel("Simulación Cola Banco BNB - Ejemplo predefinido (8 clientes)");
        EstilosUI.aplicarEstiloTitulo(titulo);
        add(titulo, BorderLayout.NORTH);

        // Panel principal dividido
        JPanel panelPrincipal = new JPanel(new BorderLayout(8,8));
        EstilosUI.aplicarEstiloPanel(panelPrincipal);

        // Panel izquierdo con distribuciones
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
        tServ.getColumnModel().getColumn(0).setPreferredWidth(90);
        tServ.getColumnModel().getColumn(1).setPreferredWidth(130);
        tServ.getColumnModel().getColumn(2).setPreferredWidth(140);
        tServ.getColumnModel().getColumn(3).setPreferredWidth(120);
        JScrollPane spServ = new JScrollPane(tServ);
        spServ.setBorder(BorderFactory.createTitledBorder("Datos del tiempo de servicio"));
        panelIzq.add(spServ);

        // 2) Distribución tiempo entre llegadas
        modeloLlegadas = new DefaultTableModel(new Object[]{"Probabilidad","Distribución acumulada","Rango de Nros aleatorios","Tiempo entre llegadas cliente"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        llenarDistribucionLlegadas();
        JTable tLleg = new JTable(modeloLlegadas);
        EstilosUI.aplicarEstiloTabla(tLleg);
        tLleg.getTableHeader().setBackground(new Color(200, 255, 200));
        tLleg.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tLleg.getColumnModel().getColumn(0).setPreferredWidth(90);
        tLleg.getColumnModel().getColumn(1).setPreferredWidth(130);
        tLleg.getColumnModel().getColumn(2).setPreferredWidth(140);
        tLleg.getColumnModel().getColumn(3).setPreferredWidth(180);
        JScrollPane spLleg = new JScrollPane(tLleg);
        spLleg.setBorder(BorderFactory.createTitledBorder("Datos de la llegada de los clientes"));
        panelIzq.add(spLleg);

        panelPrincipal.add(panelIzq, BorderLayout.WEST);

        // Tabla de simulación (lado derecho)
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
                if(row < getRowCount()-1){ // fila de datos (no totales)
                    // Alternar colores para mejor legibilidad
                    if(row % 2 == 0){
                        c.setBackground(Color.WHITE);
                    } else {
                        c.setBackground(new Color(245, 245, 245));
                    }
                    // Resaltar tiempos de espera > 0
                    Object espera = getValueAt(row, 8);
                    if(espera instanceof Integer && (Integer)espera > 0){
                        c.setBackground(new Color(255, 255, 200)); // amarillo claro
                    }
                } else { // fila de totales
                    c.setBackground(new Color(220, 220, 220));
                }
                return c;
            }
        };

        EstilosUI.aplicarEstiloTabla(tablaSim);
        tablaSim.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablaSim.getColumnModel().getColumn(0).setPreferredWidth(70);  // # cliente
        tablaSim.getColumnModel().getColumn(1).setPreferredWidth(70);  // # aleatorio llegada
        tablaSim.getColumnModel().getColumn(2).setPreferredWidth(90);  // intervalo llegadas
        tablaSim.getColumnModel().getColumn(3).setPreferredWidth(90);  // hora llegadas
        tablaSim.getColumnModel().getColumn(4).setPreferredWidth(70);  // # aleatorio servicio
        tablaSim.getColumnModel().getColumn(5).setPreferredWidth(70);  // t servicio
        tablaSim.getColumnModel().getColumn(6).setPreferredWidth(100); // inicio servicio
        tablaSim.getColumnModel().getColumn(7).setPreferredWidth(90);  // final servicio
        tablaSim.getColumnModel().getColumn(8).setPreferredWidth(70);  // t espera
        tablaSim.getColumnModel().getColumn(9).setPreferredWidth(70);  // t ocioso

        simular();
        JScrollPane spSim = new JScrollPane(tablaSim);
        spSim.setBorder(BorderFactory.createTitledBorder("Simulación de la cola del banco"));
        spSim.setPreferredSize(new Dimension(700, 400));
        panelPrincipal.add(spSim, BorderLayout.CENTER);

        add(panelPrincipal, BorderLayout.CENTER);

        // Área de resumen
        resumen = new JTextArea();
        resumen.setEditable(false);
        resumen.setWrapStyleWord(true);
        resumen.setLineWrap(true);
        resumen.setBackground(getBackground());
        resumen.setBorder(BorderFactory.createTitledBorder("Análisis de resultados"));
        resumen.setFont(new Font("Arial", Font.PLAIN, 12));
        resumen.setPreferredSize(new Dimension(0, 140));

        mostrarResumen();
        add(resumen, BorderLayout.SOUTH);
    }

    private void llenarDistribucionServicio(){
        modeloServicio.setRowCount(0);
        double[][] rangos = ColaBancoModelo.getRangosServicio();

        for(int i = 0; i < ColaBancoModelo.SERVICIO_VALORES.length; i++){
            String rango = UtilFormatoColas.f2(rangos[i][0]) + " - " + UtilFormatoColas.f2(rangos[i][1]);
            modeloServicio.addRow(new Object[]{
                UtilFormatoColas.f2(ColaBancoModelo.SERVICIO_PROBS[i]),
                UtilFormatoColas.f2(ColaBancoModelo.SERVICIO_PROBS[i] == 0 ? 0 : (i == 0 ? ColaBancoModelo.SERVICIO_PROBS[i] : getSumaAcumulada(ColaBancoModelo.SERVICIO_PROBS, i))),
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

    private void simular(){
        modeloSim.setRowCount(0);
        int horaLlegadaAcum = 0; // minutos desde 09:00
        int finServicioAnterior = 0;
        int totalEspera = 0;
        int totalOcioso = 0;

        for(int i = 0; i < RAND_LLEGADA.length; i++){
            double rLleg = RAND_LLEGADA[i];
            int intervaloLlegada = ColaBancoModelo.tiempoInterLlegada(rLleg);
            horaLlegadaAcum += intervaloLlegada;
            String horaLlegada = UtilFormatoColas.horaDesdeBase(horaLlegadaAcum);

            double rServ = RAND_SERVICIO[i];
            int tiempoServicio = ColaBancoModelo.tiempoServicio(rServ);

            int inicioServicio = Math.max(horaLlegadaAcum, finServicioAnterior);
            int tiempoEspera = inicioServicio - horaLlegadaAcum;
            int tiempoOcioso = (inicioServicio > finServicioAnterior) ? inicioServicio - finServicioAnterior : 0;
            int finServicio = inicioServicio + tiempoServicio;

            totalEspera += tiempoEspera;
            totalOcioso += tiempoOcioso;

            modeloSim.addRow(new Object[]{
                i + 1,
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
    }

    private void mostrarResumen(){
        // Calcular estadísticas
        int totalEspera = 0;
        int totalOcioso = 0;
        int clientesConEspera = 0;

        for(int i = 0; i < RAND_LLEGADA.length; i++){
            // Simular nuevamente para obtener los datos
            double rLleg = RAND_LLEGADA[i];
            int intervaloLlegada = ColaBancoModelo.tiempoInterLlegada(rLleg);

            double rServ = RAND_SERVICIO[i];
            int tiempoServicio = ColaBancoModelo.tiempoServicio(rServ);

            // Calcular tiempos (simplificado para el resumen)
            if(i > 0){ // Los clientes después del primero pueden tener espera
                // Esta es una simplificación; los valores reales están en la tabla
            }
        }

        // Obtener datos de la tabla simulada
        int filas = modeloSim.getRowCount() - 1; // Excluir fila de totales
        totalEspera = 0;
        totalOcioso = 0;

        for(int i = 0; i < filas; i++){
            Object espera = modeloSim.getValueAt(i, 8);
            Object ocioso = modeloSim.getValueAt(i, 9);

            if(espera instanceof Integer){
                int esp = (Integer)espera;
                totalEspera += esp;
                if(esp > 0) clientesConEspera++;
            }

            if(ocioso instanceof Integer){
                totalOcioso += (Integer)ocioso;
            }
        }

        double promedioEspera = totalEspera / (double)RAND_LLEGADA.length;
        double porcentajeEspera = (clientesConEspera / (double)RAND_LLEGADA.length) * 100;

        StringBuilder sb = new StringBuilder();
        sb.append("ANÁLISIS DE LA SIMULACIÓN DEL BANCO BNB (Ventanilla Auto):\n\n");
        sb.append("RESULTADOS OBTENIDOS:\n");
        sb.append("• Total de clientes simulados: ").append(RAND_LLEGADA.length).append(" clientes\n");
        sb.append("• Tiempo total de espera: ").append(totalEspera).append(" minutos\n");
        sb.append("• Tiempo promedio de espera: ").append(UtilFormatoColas.f2(promedioEspera)).append(" minutos\n");
        sb.append("• Clientes que esperaron: ").append(clientesConEspera).append(" de ").append(RAND_LLEGADA.length);
        sb.append(" (").append(UtilFormatoColas.f2(porcentajeEspera)).append("%)\n");
        sb.append("• Tiempo total de cajero ocioso: ").append(totalOcioso).append(" minutos\n\n");

        sb.append("EVALUACIÓN DEL OBJETIVO:\n");
        sb.append("• Política del banco: Cliente promedio no debe esperar más de 2 minutos\n");
        sb.append("• Resultado: ").append(promedioEspera <= 2.0 ? "✓ CUMPLE" : "✗ NO CUMPLE").append(" el objetivo\n");

        if(promedioEspera > 2.0){
            sb.append("• Recomendación: Considerar agregar más cajeros o reducir tiempo de servicio\n");
        } else {
            sb.append("• La ventanilla de servicio en el auto cumple satisfactoriamente el criterio establecido\n");
        }

        sb.append("\nINTERPRETACIÓN:\n");
        sb.append("Las celdas resaltadas en amarillo indican clientes que tuvieron que esperar en la fila.\n");
        sb.append("El tiempo ocioso representa períodos donde el cajero no estaba atendiendo clientes.");

        resumen.setText(sb.toString());
    }
}