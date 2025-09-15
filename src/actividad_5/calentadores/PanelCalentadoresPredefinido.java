package actividad_5.calentadores;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Panel con el ejemplo de 20 semanas usando números aleatorios proporcionados. */
public class PanelCalentadoresPredefinido extends JPanel {
    private final DefaultTableModel modeloFrecuencia; // tabla original (ventas, frecuencia)
    private final DefaultTableModel modeloProb;       // tabla con probabilidades
    private final DefaultTableModel modeloRangos;     // tabla con acumulada y rangos
    private final DefaultTableModel modeloSim;        // simulación 20 semanas

    private static final int[] FRECUENCIAS = {6,5,9,12,8,7,3};
    private static final double[] RAND = {0.10,0.24,0.03,0.32,0.23,0.59,0.95,0.34,0.34,0.51,0.08,0.48,0.66,0.97,0.03,0.96,0.46,0.74,0.77,0.44};

    public PanelCalentadoresPredefinido(){
        setLayout(new BorderLayout(8,8));
        EstilosUI.aplicarEstiloPanel(this);
        JLabel titulo = new JLabel("Simulación de ventas de calentadores (ejemplo 20 semanas - inventario fijo 8)");
        EstilosUI.aplicarEstiloTitulo(titulo);
        add(titulo, BorderLayout.NORTH);

        // Panel principal dividido
        JPanel panelPrincipal = new JPanel(new BorderLayout(8,8));
        EstilosUI.aplicarEstiloPanel(panelPrincipal);

        // Panel izquierdo con distribuciones
        JPanel panelIzq = new JPanel(new GridLayout(3,1,5,5));
        EstilosUI.aplicarEstiloPanel(panelIzq);

        // 1) Frecuencias
        modeloFrecuencia = new DefaultTableModel(new Object[]{"Ventas/semana","# semanas que se vendió esta cantidad"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        llenarFrecuencias();
        JTable tFreq = new JTable(modeloFrecuencia);
        EstilosUI.aplicarEstiloTabla(tFreq);
        tFreq.getTableHeader().setBackground(new Color(255, 240, 200));
        tFreq.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tFreq.getColumnModel().getColumn(0).setPreferredWidth(90);
        tFreq.getColumnModel().getColumn(1).setPreferredWidth(180);
        JScrollPane spFreq = new JScrollPane(tFreq);
        spFreq.setBorder(BorderFactory.createTitledBorder("1. Frecuencias observadas (50 semanas)"));
        panelIzq.add(spFreq);

        // 2) Probabilidades
        modeloProb = new DefaultTableModel(new Object[]{"Ventas","# semanas","Probabilidad"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        llenarProbabilidades();
        JTable tProb = new JTable(modeloProb);
        EstilosUI.aplicarEstiloTabla(tProb);
        tProb.getTableHeader().setBackground(new Color(200, 255, 200));
        tProb.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tProb.getColumnModel().getColumn(0).setPreferredWidth(60);
        tProb.getColumnModel().getColumn(1).setPreferredWidth(80);
        tProb.getColumnModel().getColumn(2).setPreferredWidth(90);
        JScrollPane spProb = new JScrollPane(tProb);
        spProb.setBorder(BorderFactory.createTitledBorder("2. Probabilidades empíricas"));
        panelIzq.add(spProb);

        // 3) Rangos
        modeloRangos = new DefaultTableModel(new Object[]{"Prob","Dist.Acum","Inicio rango","Fin rango","Ventas"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        llenarRangos();
        JTable tRangos = new JTable(modeloRangos);
        EstilosUI.aplicarEstiloTabla(tRangos);
        tRangos.getTableHeader().setBackground(new Color(200, 220, 255));
        tRangos.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for(int i = 0; i < 5; i++) {
            tRangos.getColumnModel().getColumn(i).setPreferredWidth(65);
        }
        JScrollPane spRangos = new JScrollPane(tRangos);
        spRangos.setBorder(BorderFactory.createTitledBorder("3. Distribución acumulada y rangos"));
        panelIzq.add(spRangos);

        panelPrincipal.add(panelIzq, BorderLayout.WEST);

        // Tabla de simulación (lado derecho)
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
                if(row < getRowCount()-1){ // fila de datos (no totales)
                    Object falt = getValueAt(row,3);
                    if(Integer.valueOf(1).equals(falt)) {
                        c.setBackground(new Color(255, 200, 200)); // rojo claro para faltantes
                    } else {
                        c.setBackground(Color.white);
                    }
                } else { // fila de totales
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

        simular();
        JScrollPane spSim = new JScrollPane(tablaSim);
        spSim.setBorder(BorderFactory.createTitledBorder("4. Simulación (inventario constante 8 calentadores/semana)"));
        spSim.setPreferredSize(new Dimension(600, 400));
        panelPrincipal.add(spSim, BorderLayout.CENTER);

        add(panelPrincipal, BorderLayout.CENTER);

        // Resumen de resultados
        JTextArea resumen = new JTextArea();
        resumen.setEditable(false);
        resumen.setWrapStyleWord(true);
        resumen.setLineWrap(true);
        resumen.setBackground(getBackground());
        resumen.setBorder(BorderFactory.createTitledBorder("Resultados del análisis"));
        resumen.setFont(new Font("Arial", Font.PLAIN, 12));

        int totalVentas = totalVentas();
        int faltantes = contarFaltantes();
        String semanasFaltantes = listarSemanasFaltantes();
        double promedioSim = promedioSimulado();
        double esperado = CalentadoresModelo.esperado();

        StringBuilder sb = new StringBuilder();
        sb.append("ANÁLISIS DE LA SIMULACIÓN (20 semanas con inventario fijo de 8 calentadores):\n\n");
        sb.append("a) Faltantes: ").append(faltantes).append(" semanas con demanda > inventario\n");
        sb.append("   Semanas con faltantes: ").append(semanasFaltantes).append("\n\n");
        sb.append("b) Total de calentadores vendidos: ").append(totalVentas).append(" unidades\n");
        sb.append("   Promedio de ventas simulado: ").append(UtilFormatoCalent.f2(promedioSim)).append(" calentadores/semana\n\n");
        sb.append("c) Valor esperado analítico E(ventas): ").append(UtilFormatoCalent.f2(esperado)).append(" calentadores/semana\n");
        sb.append("   Diferencia entre simulado y teórico: ").append(UtilFormatoCalent.f2(Math.abs(promedioSim - esperado))).append("\n\n");
        sb.append("INTERPRETACIÓN:\n");
        sb.append("- Con un inventario fijo de 8 calentadores, hubo faltantes en ").append(faltantes).append(" de 20 semanas (").append(UtilFormatoCalent.f2((faltantes/20.0)*100)).append("%)\n");
        sb.append("- La simulación con más semanas convergerá al valor esperado teórico\n");
        sb.append("- Las semanas marcadas en rojo indican cuando la demanda superó el inventario disponible");

        resumen.setText(sb.toString());
        add(resumen, BorderLayout.SOUTH);
    }

    private void llenarFrecuencias(){
        modeloFrecuencia.setRowCount(0);
        for(int i=0;i<CalentadoresModelo.VENTAS.length;i++){
            modeloFrecuencia.addRow(new Object[]{CalentadoresModelo.VENTAS[i], FRECUENCIAS[i]});
        }
        modeloFrecuencia.addRow(new Object[]{"TOTAL",50});
    }

    private void llenarProbabilidades(){
        modeloProb.setRowCount(0);
        for(int i=0;i<CalentadoresModelo.VENTAS.length;i++){
            modeloProb.addRow(new Object[]{CalentadoresModelo.VENTAS[i], FRECUENCIAS[i], UtilFormatoCalent.f2(CalentadoresModelo.PROBS[i])});
        }
        modeloProb.addRow(new Object[]{"TOTAL",50,UtilFormatoCalent.f2(1.0)});
    }

    private void llenarRangos(){
        modeloRangos.setRowCount(0);
        double inicio=0, acum=0;
        for(int i=0;i<CalentadoresModelo.VENTAS.length;i++){
            double p = CalentadoresModelo.PROBS[i];
            acum += p;
            if(i==CalentadoresModelo.VENTAS.length-1) acum = 1.0;
            double fin = acum;
            modeloRangos.addRow(new Object[]{
                UtilFormatoCalent.f2(p),
                UtilFormatoCalent.f2(acum),
                UtilFormatoCalent.f2(inicio),
                UtilFormatoCalent.f2(fin),
                CalentadoresModelo.VENTAS[i]
            });
            inicio = fin;
        }
    }

    private void simular(){
        modeloSim.setRowCount(0);
        int totalFaltantes = 0;
        int totalVentasAcum = 0;

        for(int s=0;s<RAND.length;s++){
            double r = RAND[s];
            int ventasDemanda = CalentadoresModelo.ventasPara(r);
            int falta = (ventasDemanda > CalentadoresModelo.INVENTARIO_FIJO) ? 1 : 0;

            if(falta == 1) totalFaltantes++;
            totalVentasAcum += ventasDemanda;

            modeloSim.addRow(new Object[]{
                s+1,
                UtilFormatoCalent.f2(r),
                ventasDemanda,
                falta
            });
        }

        // Fila de totales
        modeloSim.addRow(new Object[]{
            "Total",
            "",
            totalVentasAcum,
            totalFaltantes
        });

        // Fila de promedio
        double promedio = totalVentasAcum / (double)RAND.length;
        modeloSim.addRow(new Object[]{
            "B) ventas promedio con simulación",
            "",
            UtilFormatoCalent.f2(promedio),
            ""
        });
    }

    private int totalVentas(){
        int t=0;
        for(int i=0;i<RAND.length;i++){
            t+= CalentadoresModelo.ventasPara(RAND[i]);
        }
        return t;
    }

    private double promedioSimulado(){
        return totalVentas() / (double) RAND.length;
    }

    private int contarFaltantes(){
        int c=0;
        for(double r: RAND) {
            if(CalentadoresModelo.ventasPara(r) > CalentadoresModelo.INVENTARIO_FIJO) c++;
        }
        return c;
    }

    private String listarSemanasFaltantes(){
        StringBuilder sb=new StringBuilder();
        boolean first=true;
        for(int i=0;i<RAND.length;i++){
            if(CalentadoresModelo.ventasPara(RAND[i]) > CalentadoresModelo.INVENTARIO_FIJO){
                if(!first) sb.append(", ");
                sb.append(i+1);
                first=false;
            }
        }
        return sb.toString();
    }
}