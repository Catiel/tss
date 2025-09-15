package actividad_5.calentadores;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

/** Panel que simula semanas con números aleatorios generados internamente. */
public class PanelCalentadoresAleatorio extends JPanel {
    private final JSpinner spSemanas; // número de semanas
    private final JSpinner spInventario; // inventario fijo por semana
    private final JButton btnSimular; // ejecuta simulación

    private final DefaultTableModel modeloRangos; // tabla de distribución con rangos
    private final DefaultTableModel modeloSim;    // tabla de simulación
    private final JTextArea resumen;              // resumen de resultados

    public PanelCalentadoresAleatorio(){
        setLayout(new BorderLayout(8,8));
        EstilosUI.aplicarEstiloPanel(this);
        JLabel titulo = new JLabel("Simulación aleatoria de ventas de calentadores");
        EstilosUI.aplicarEstiloTitulo(titulo);
        add(titulo, BorderLayout.NORTH);

        // Controles superiores
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        EstilosUI.aplicarEstiloPanel(controles);
        controles.add(new JLabel("Número de semanas:"));
        spSemanas = new JSpinner(new SpinnerNumberModel(20,1,200,1));
        controles.add(spSemanas);
        controles.add(new JLabel("Inventario fijo por semana:"));
        spInventario = new JSpinner(new SpinnerNumberModel(CalentadoresModelo.INVENTARIO_FIJO,1,50,1));
        controles.add(spInventario);
        btnSimular = new JButton("Ejecutar Simulación");
        EstilosUI.aplicarEstiloBoton(btnSimular);
        controles.add(btnSimular);
        add(controles, BorderLayout.BEFORE_FIRST_LINE);

        // Panel principal dividido
        JPanel panelPrincipal = new JPanel(new BorderLayout(8,8));
        EstilosUI.aplicarEstiloPanel(panelPrincipal);

        // Panel izquierdo: tabla de rangos (referencia)
        modeloRangos = new DefaultTableModel(new Object[]{"Prob","Dist.Acum","Inicio","Fin","Ventas"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        llenarRangos();
        JTable tRangos = new JTable(modeloRangos);
        EstilosUI.aplicarEstiloTabla(tRangos);
        tRangos.getTableHeader().setBackground(new Color(200, 220, 255));
        tRangos.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for(int i = 0; i < 5; i++) {
            tRangos.getColumnModel().getColumn(i).setPreferredWidth(65);
        }
        JScrollPane spRangos = new JScrollPane(tRangos);
        spRangos.setBorder(BorderFactory.createTitledBorder("Distribución de probabilidad (referencia)"));
        spRangos.setPreferredSize(new Dimension(350, 300));
        panelPrincipal.add(spRangos, BorderLayout.WEST);

        // Panel derecho: tabla de simulación
        modeloSim = new DefaultTableModel(new Object[]{
            "# de semana","numeros aleatorios","Ventas de calentador","Faltantes"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
            @Override public Class<?> getColumnClass(int c){
                if(c == 0 || c == 2 || c == 3) return Integer.class;
                return String.class;
            }
        };
        JTable tablaSim = new JTable(modeloSim){
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer,int row,int column){
                Component c = super.prepareRenderer(renderer,row,column);
                if(row < getRowCount()-2){ // fila de datos (no totales ni promedio)
                    Object falt = getValueAt(row,3);
                    if(Integer.valueOf(1).equals(falt)) {
                        c.setBackground(new Color(255, 200, 200)); // rojo claro para faltantes
                    } else {
                        c.setBackground(Color.white);
                    }
                } else { // fila de totales o promedio
                    c.setBackground(new Color(220, 220, 220));
                }
                return c;
            }
        };
        EstilosUI.aplicarEstiloTabla(tablaSim);
        tablaSim.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablaSim.getColumnModel().getColumn(0).setPreferredWidth(80);
        tablaSim.getColumnModel().getColumn(1).setPreferredWidth(110);
        tablaSim.getColumnModel().getColumn(2).setPreferredWidth(120);
        tablaSim.getColumnModel().getColumn(3).setPreferredWidth(80);

        JScrollPane spSim = new JScrollPane(tablaSim);
        spSim.setBorder(BorderFactory.createTitledBorder("Resultados de la simulación"));
        panelPrincipal.add(spSim, BorderLayout.CENTER);

        add(panelPrincipal, BorderLayout.CENTER);

        // Área de resumen
        resumen = new JTextArea();
        resumen.setEditable(false);
        resumen.setBackground(getBackground());
        resumen.setBorder(BorderFactory.createTitledBorder("Análisis de resultados"));
        resumen.setLineWrap(true);
        resumen.setWrapStyleWord(true);
        resumen.setFont(new Font("Arial", Font.PLAIN, 12));
        resumen.setPreferredSize(new Dimension(0, 120));
        add(resumen, BorderLayout.SOUTH);

        btnSimular.addActionListener(this::simular);

        // Ejecutar simulación inicial
        simular(null);
    }

    private void llenarRangos(){
        modeloRangos.setRowCount(0);
        double inicio=0, acum=0;
        for(int i=0;i<CalentadoresModelo.VENTAS.length;i++){
            double p=CalentadoresModelo.PROBS[i];
            acum+=p;
            if(i==CalentadoresModelo.VENTAS.length-1)acum=1.0;
            double fin=acum;
            modeloRangos.addRow(new Object[]{
                UtilFormatoCalent.f2(p),
                UtilFormatoCalent.f2(acum),
                UtilFormatoCalent.f2(inicio),
                UtilFormatoCalent.f2(fin),
                CalentadoresModelo.VENTAS[i]
            });
            inicio=fin;
        }
    }

    private void simular(ActionEvent e){
        int semanas = (int) spSemanas.getValue();
        int inventario = (int) spInventario.getValue();
        modeloSim.setRowCount(0);

        int totalVentas=0;
        int faltantes=0;
        StringBuilder faltSemanas = new StringBuilder();
        boolean first=true;

        for(int s=1;s<=semanas;s++){
            double r = Math.random();
            int ventasDemanda = CalentadoresModelo.ventasPara(r);
            int falt = ventasDemanda > inventario ? 1 : 0;

            if(falt==1){
                faltantes++;
                if(!first) faltSemanas.append(", ");
                faltSemanas.append(s);
                first=false;
            }
            totalVentas += ventasDemanda;

            modeloSim.addRow(new Object[]{
                s,
                UtilFormatoCalent.f2(r),
                ventasDemanda,
                falt
            });
        }

        // Fila de totales
        modeloSim.addRow(new Object[]{
            "Total",
            "",
            totalVentas,
            faltantes
        });

        // Fila de promedio
        double promedio = totalVentas / (double)semanas;
        modeloSim.addRow(new Object[]{
            "B) ventas promedio con simulación",
            "",
            UtilFormatoCalent.f2(promedio),
            ""
        });

        double esperado = CalentadoresModelo.esperado();
        double porcentajeFaltantes = (faltantes/(double)semanas) * 100;

        StringBuilder sb = new StringBuilder();
        sb.append("RESULTADOS DE LA SIMULACIÓN (").append(semanas).append(" semanas, inventario: ").append(inventario).append(" calentadores/semana):\n\n");
        sb.append("• Semanas con faltantes: ").append(faltantes).append(" de ").append(semanas);
        sb.append(" (").append(UtilFormatoCalent.f2(porcentajeFaltantes)).append("%)\n");
        if(faltantes > 0) {
            sb.append("• Semanas específicas con faltantes: ").append(faltSemanas).append("\n");
        }
        sb.append("• Total de calentadores demandados: ").append(totalVentas).append(" unidades\n");
        sb.append("• Promedio de ventas simulado: ").append(UtilFormatoCalent.f2(promedio)).append(" calentadores/semana\n");
        sb.append("• Valor esperado teórico: ").append(UtilFormatoCalent.f2(esperado)).append(" calentadores/semana\n");
        sb.append("• Diferencia simulado vs teórico: ").append(UtilFormatoCalent.f2(Math.abs(promedio - esperado))).append("\n\n");
        sb.append("INTERPRETACIÓN:\n");
        sb.append("Las filas marcadas en rojo indican semanas donde la demanda superó el inventario disponible.\n");
        sb.append("Con más semanas de simulación, el promedio converge hacia el valor esperado teórico.");

        resumen.setText(sb.toString());
    }
}