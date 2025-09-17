package actividad_5.sangre; // Define el paquete donde se encuentra esta clase para el módulo de simulación de sangre

/** Modelo de distribuciones para el problema de plasma/sangre. // Comentario de documentación que explica el propósito de la clase modelo
 *  Cantidades suministradas (pintas/entrega) -> Probabilidades: // Describe la primera distribución: suministro de sangre por entrega
 *  4:0.15, 5:0.20, 6:0.25, 7:0.15, 8:0.15, 9:0.10 // Lista los valores posibles de suministro (4-9 pintas) con sus probabilidades respectivas
 *  Pacientes por semana -> 0:0.25, 1:0.25, 2:0.30, 3:0.15, 4:0.05 // Lista los valores posibles de pacientes por semana (0-4) con sus probabilidades
 *  Demanda por paciente (pintas) -> 1:0.40, 2:0.30, 3:0.20, 4:0.10 // Lista los valores posibles de demanda por paciente (1-4 pintas) con sus probabilidades
 */
public class SangreModelo { // Declara la clase modelo que contiene las distribuciones de probabilidad para la simulación de sangre
    // Valores y probabilidades (ordenados)
    public static final int[] SUPPLY_VALUES = {4,5,6,7,8,9}; // Array estático final que contiene los valores posibles de suministro de sangre en pintas por entrega (de 4 a 9)
    public static final double[] SUPPLY_PROBS = {0.15,0.20,0.25,0.15,0.15,0.10}; // Array estático final que contiene las probabilidades correspondientes para cada valor de suministro

    public static final int[] PACIENTES_VALUES = {0,1,2,3,4}; // Array estático final que contiene los valores posibles de número de pacientes por semana (de 0 a 4)
    public static final double[] PACIENTES_PROBS = {0.25,0.25,0.30,0.15,0.05}; // Array estático final que contiene las probabilidades correspondientes para cada número de pacientes

    public static final int[] DEMANDA_VALUES = {1,2,3,4}; // Array estático final que contiene los valores posibles de demanda por paciente individual en pintas (de 1 a 4)
    public static final double[] DEMANDA_PROBS = {0.40,0.30,0.20,0.10}; // Array estático final que contiene las probabilidades correspondientes para cada valor de demanda por paciente

    private static int valorDesdeDistribucion(double r, int[] valores, double[] probs){ // Método privado genérico que convierte un número aleatorio en un valor según una distribución de probabilidad discreta
        double acum = 0; // Inicializa el acumulador de probabilidades en 0
        for(int i=0;i<probs.length;i++){ // Itera sobre cada probabilidad en el array de probabilidades
            acum += probs[i]; // Acumula la probabilidad actual al total acumulado
            if(r < acum + 1e-12) return valores[i]; // Si el número aleatorio es menor que la probabilidad acumulada (más un pequeño factor de tolerancia), retorna el valor correspondiente
        }
        return valores[valores.length-1]; // Si no se encuentra ninguna coincidencia (caso extremo), retorna el último valor del array
    }

    public static int suministro(double r){ return valorDesdeDistribucion(r, SUPPLY_VALUES, SUPPLY_PROBS); } // Método público que convierte un número aleatorio en un valor de suministro usando la distribución de suministro
    public static int pacientes(double r){ return valorDesdeDistribucion(r, PACIENTES_VALUES, PACIENTES_PROBS); } // Método público que convierte un número aleatorio en un número de pacientes usando la distribución de pacientes
    public static int demandaPaciente(double r){ return valorDesdeDistribucion(r, DEMANDA_VALUES, DEMANDA_PROBS); } // Método público que convierte un número aleatorio en una demanda por paciente usando la distribución de demanda individual
}
