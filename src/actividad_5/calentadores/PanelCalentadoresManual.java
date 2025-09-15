package actividad_5.calentadores;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

/** Panel para ingresar manualmente números aleatorios U(0,1) y simular ventas semanales. */
public class PanelCalentadoresManual extends JPanel {
    private final JSpinner spSemanas;
    private final JSpinner spInventario;
    private final JButton btnCrear;
    private final JButton btnCalcular;
    private final DefaultTableModel modeloRangos;
    private final DefaultTableModel modeloInput; // tabla de entrada de números aleatorios
    private final DefaultTableModel modeloSim;
    private final JTextArea resumen;

    public PanelCalentadoresManual(){
        setLayout(new BorderLayout(8,8));
        EstilosUI.aplicarEstiloPanel(this);
        JLabel titulo = new JLabel("Simulación manual de ventas de calentadores (ingrese números aleatorios)");
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
        btnCrear = new JButton("Crear tabla de entrada");
        EstilosUI.aplicarEstiloBoton(btnCrear);
        controles.add(btnCrear);
        btnCalcular = new JButton("Calcular resultados");
        EstilosUI.aplicarEstiloBoton(btnCalcular);
        controles.add(btnCalcular);
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

        // Panel central dividido verticalmente
        JPanel panelCentral = new JPanel(new GridLayout(2,1,5,5));
        EstilosUI.aplicarEstiloPanel(panelCentral);

        // Tabla de entrada de números aleatorios (editable)
        modeloInput = new DefaultTableModel(new Object[]{"Semana","#Aleatorio [0,1)"},0){
            @Override public boolean isCellEditable(int r,int c){
                return c == 1; // Solo la columna de números aleatorios es editable
            }
            @Override public Class<?> getColumnClass(int c){
                return c == 0 ? Integer.class : String.class;
            }
        };
        JTable tablaInput = new JTable(modeloInput);
        EstilosUI.aplicarEstiloTabla(tablaInput);
        tablaInput.getTableHeader().setBackground(new Color(255, 255, 200));
        tablaInput.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablaInput.getColumnModel().getColumn(0).setPreferredWidth(60);
        tablaInput.getColumnModel().getColumn(1).setPreferredWidth(120);
        JScrollPane spInput = new JScrollPane(tablaInput);
        spInput.setBorder(BorderFactory.createTitledBorder("1. Ingrese números aleatorios [0,1)"));
        panelCentral.add(spInput);

        // Tabla de simulación (solo lectura)
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
        spSim.setBorder(BorderFactory.createTitledBorder("2. Resultados de la simulación"));
        panelCentral.add(spSim);

        panelPrincipal.add(panelCentral, BorderLayout.CENTER);
        add(panelPrincipal, BorderLayout.CENTER);

        // Área de resumen
        resumen = new JTextArea();
        resumen.setEditable(false);
        resumen.setBackground(getBackground());
        resumen.setLineWrap(true);
        resumen.setWrapStyleWord(true);
        resumen.setBorder(BorderFactory.createTitledBorder("Análisis de resultados"));
        resumen.setFont(new Font("Arial", Font.PLAIN, 12));
        resumen.setPreferredSize(new Dimension(0, 120));
        add(resumen, BorderLayout.SOUTH);

        btnCrear.addActionListener(this::crearFilas);
        btnCalcular.addActionListener(this::calcular);
        btnCalcular.setEnabled(false);

        // Agregar instrucciones al final del constructor
        JTextArea instrucciones = new JTextArea("INSTRUCCIONES:\n" +
            "1. Configure el número de semanas e inventario por semana\n" +
            "2. Presione 'Crear tabla de entrada' para generar las filas\n" +
            "3. Ingrese números aleatorios entre 0 y 1 (no incluye 1) en la columna #Aleatorio\n" +
            "4. Presione 'Calcular resultados' para obtener la simulación\n" +
            "5. Las filas rojas en el resultado indican semanas con faltantes (demanda > inventario)");
        instrucciones.setWrapStyleWord(true);
        instrucciones.setLineWrap(true);
        instrucciones.setEditable(false);
        instrucciones.setBackground(new Color(240, 248, 255));
        instrucciones.setFont(new Font("Arial", Font.PLAIN, 11));
        instrucciones.setBorder(BorderFactory.createTitledBorder("Instrucciones de uso"));
        // No agregamos las instrucciones al panel aquí ya que sería demasiado contenido
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

    private void crearFilas(ActionEvent e){
        int semanas = (int) spSemanas.getValue();
        modeloInput.setRowCount(0);
        modeloSim.setRowCount(0);
        resumen.setText("");

        for(int s=1;s<=semanas;s++) {
            modeloInput.addRow(new Object[]{s, ""});
        }
        btnCalcular.setEnabled(true);
    }

    private Double parse(Object v){
        if(v==null) return null;
        String t=v.toString().trim().replace(',', '.');
        if(t.isEmpty()) return null;
        try{
            double d=Double.parseDouble(t);
            if(d<0||d>=1) return null;
            return d;
        }catch(Exception ex){
            return null;
        }
    }

    private void calcular(ActionEvent e){
        if(modeloInput.getRowCount()==0) {
            JOptionPane.showMessageDialog(this, "Primero debe crear la tabla de entrada", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int inventario=(int) spInventario.getValue();
        modeloSim.setRowCount(0);

        int totalVentas=0;
        int faltantes=0;
        StringBuilder faltSem=new StringBuilder();
        boolean first=true;

        for(int i=0;i<modeloInput.getRowCount();i++){
            Double r = parse(modeloInput.getValueAt(i,1));
            if(r==null){
                JOptionPane.showMessageDialog(this,
                    "Semana "+(i+1)+": Número aleatorio inválido\n" +
                    "Debe estar en el rango [0,1) y no estar vacío",
                    "Dato inválido",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            int ventasDemanda = CalentadoresModelo.ventasPara(r);
            int falt = ventasDemanda > inventario ? 1 : 0;

            if(falt==1){
                faltantes++;
                if(!first) faltSem.append(", ");
                faltSem.append(i+1);
                first=false;
            }
            totalVentas += ventasDemanda;

            modeloSim.addRow(new Object[]{
                i+1,
                UtilFormatoCalent.f2(r),
                ventasDemanda,
                falt
            });
        }

        // Fila de totales
        int semanas = modeloInput.getRowCount();
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
            sb.append("• Semanas específicas con faltantes: ").append(faltSem).append("\n");
        }
        sb.append("• Total de calentadores demandados: ").append(totalVentas).append(" unidades\n");
        sb.append("• Promedio de ventas simulado: ").append(UtilFormatoCalent.f2(promedio)).append(" calentadores/semana\n");
        sb.append("• Valor esperado teórico: ").append(UtilFormatoCalent.f2(esperado)).append(" calentadores/semana\n");
        sb.append("• Diferencia simulado vs teórico: ").append(UtilFormatoCalent.f2(Math.abs(promedio - esperado))).append("\n\n");
        sb.append("INTERPRETACIÓN:\n");
        sb.append("Las filas marcadas en rojo indican semanas donde la demanda superó el inventario disponible.\n");
        sb.append("Los resultados dependen de los números aleatorios ingresados manualmente.");

        resumen.setText(sb.toString());

        JOptionPane.showMessageDialog(this,
            "Simulación completada exitosamente!\n" +
            "Revise los resultados en las tablas y el análisis detallado.",
            "Cálculo terminado",
            JOptionPane.INFORMATION_MESSAGE);
    }
}