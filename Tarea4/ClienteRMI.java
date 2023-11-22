import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class ClienteRMI {

    private static final String IP = "40.124.184.155"; // Puedes cambiar a localhost
    private static final String IP2 = "20.51.250.201"; // Puedes cambiar a localhost
	private static final int PUERTO = 8080; //Si cambias aquí el puerto, recuerda cambiarlo en el servidor
    public static void main(String[] args)
            throws MalformedURLException, RemoteException, NotBoundException, InterruptedException {
        if (args.length != 2) {
            System.out.println("Uso: java ClienteRMI <N> <M>");
            System.exit(1);
        }

        int N = Integer.parseInt(args[0]);
        int M = Integer.parseInt(args[1]);

        try {

            Registry registry = LocateRegistry.getRegistry(IP, PUERTO);
            Registry registry2 = LocateRegistry.getRegistry(IP2, PUERTO);
            InterfazRMI Nodo1 = (InterfazRMI) registry.lookup("Nodo1"); //Buscar en el registro...
            InterfazRMI Nodo2 = (InterfazRMI) registry2.lookup("Nodo2"); //Buscar en el registro...
            // Listo para ocupar los metodos

            // Inicializar las matrices A y B
            double[][] A = new double[N][M];
            double[][] B = new double[M][N];
            
            System.out.println("Inicializando Matrices..");
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < M; j++) {
                    A[i][j] = 6 * i - 2 * j;
                }
            }

            for (int i = 0; i < M; i++) {
                for (int j = 0; j < N; j++) {
                    B[i][j] = 8 * i + 3 * j;
                }
            }

            // Obtener la matriz traspuesta BT de la matriz B
            double[][] BT = new double[N][M];
            for (int i = 0; i < M; i++) {
                for (int j = 0; j < N; j++) {
                    BT[j][i] = B[i][j];
                }
            }

            // Dividimos las matrices A y BT en N/4 renglones por M columnas
            System.out.println("Dividiendo Matrices....");

            double[][] A1 = separa_matriz(A, 0);
            double[][] A2 = separa_matriz(A, (N / 4));
            double[][] A3 = separa_matriz(A, (N / 4) * 2);
            double[][] A4 = separa_matriz(A, (N / 4) * 3);

            double[][] BT1 = separa_matriz(BT, 0);
            double[][] BT2 = separa_matriz(BT, (N / 4));
            double[][] BT3 = separa_matriz(BT, (N / 4) * 2);
            double[][] BT4 = separa_matriz(BT, (N / 4) * 3);

            // Utilizamos hilos para comenzar a calcular las multiplicaciones mediante
            double[][] C = new double[N][N];
            
            System.out.println("\nCalculando la multiplicacion de Matrices....");
            // threads y el metodo remoto
            Thread thread1 = new Thread(() -> {
                try {
                    double[][] C1 = Nodo1.multiplicaMatrices(A1, BT1,N,M);
                    double[][] C2 = Nodo1.multiplicaMatrices(A1, BT2,N,M);
                    double[][] C3 = Nodo1.multiplicaMatrices(A1, BT3,N,M);
                    double[][] C4 = Nodo1.multiplicaMatrices(A1, BT4,N,M);
                    double[][] C5 = Nodo1.multiplicaMatrices(A2, BT1,N,M);
                    double[][] C6 = Nodo1.multiplicaMatrices(A2, BT2,N,M);
                    double[][] C7 = Nodo1.multiplicaMatrices(A2, BT3,N,M);
                    double[][] C8 = Nodo1.multiplicaMatrices(A2, BT4,N,M);

                    // Obtenemos la matriz C de las submatrices obteidas por los métodos remotos
                    acomoda_matriz(C, C1, 0, 0,N);
                    acomoda_matriz(C, C2, 0, (N/4),N);
                    acomoda_matriz(C, C3,0,(N/4)*2,N);
                    acomoda_matriz(C, C4,0, (N/4)*3,N);
                    acomoda_matriz(C, C5, (N/4),0,N );
                    acomoda_matriz(C, C6, (N/4), (N/4),N );
                    acomoda_matriz(C, C7, (N/4), (N/4)*2,N);
                    acomoda_matriz(C, C8, (N/4), (N/4)*3,N);
                } catch (RemoteException e) {
                    // Manejo de excepciones específico para el thread 1.
                    e.printStackTrace();
                }
            });

            Thread thread2 = new Thread(() -> {
                try {
                    double[][] C9 = Nodo2.multiplicaMatrices(A3, BT1,N,M);
                    double[][] C10 = Nodo2.multiplicaMatrices(A3, BT2,N,M);
                    double[][] C11 = Nodo2.multiplicaMatrices(A3, BT3,N,M);
                    double[][] C12 = Nodo2.multiplicaMatrices(A3, BT4,N,M);
                    double[][] C13 = Nodo2.multiplicaMatrices(A4, BT1,N,M);
                    double[][] C14 = Nodo2.multiplicaMatrices(A4, BT2,N,M);
                    double[][] C15 = Nodo2.multiplicaMatrices(A4, BT3,N,M);
                    double[][] C16 = Nodo2.multiplicaMatrices(A4, BT4,N,M);

                   // Obtenemos la matriz C de las submatrices obteidas por los métodos remotos
                    acomoda_matriz(C, C9, (N/4)*2, 0,N);
                    acomoda_matriz(C, C10, (N/4)*2, (N/4),N);
                    acomoda_matriz(C, C11, (N/4)*2, (N/4)*2,N);
                    acomoda_matriz(C, C12, (N/4)*2, (N/4)*3,N);
                    acomoda_matriz(C, C13, (N/4)*3 ,0,N );
                    acomoda_matriz(C, C14, (N/4)*3, (N/4),N);
                    acomoda_matriz(C, C15, (N/4)*3, (N/4)*2,N);
                    acomoda_matriz(C, C16, (N/4)*3, (N/4)*3,N);;
                } catch (RemoteException e) {
                    // Manejo de excepciones específico para el thread 2.
                    e.printStackTrace();
                }
            });

            thread1.start();
            thread2.start();

            
            thread1.join();
            thread2.join();

            System.out.println("\nObteniendo Cheksum....");
            //Se calcula el cheksum de la matriz C
            double cheksum =0;
            for(int i=0;i<N;i++){
                for(int j=0;j<N;j++){
                    cheksum += C[i][j];
                }
            }
            // Mostrar el resultado
            if(N==8 && M==4){
                System.out.println("Matriz A:");
                imprimirMatriz(A);
                System.out.println("\nMatriz B:");
                imprimirMatriz(B);
                System.out.println("\nMatriz c:");
                imprimirMatriz(C);
                System.out.println("\nEl Cheksum del producto de matrices es: "+cheksum);  
            }else{
                System.out.println("\nEl Cheksum del producto de matrices es: "+cheksum);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void imprimirMatriz(double[][] matriz) {
        for (int i = 0; i < matriz.length; i++) {
            for (int j = 0; j < matriz[0].length; j++) {
                System.out.print(matriz[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static double[][] separa_matriz(double[][] A, int inicio) {
        int N = A.length;
        double[][] M = new double[N / 4][A[0].length];
        for (int i = 0; i < N / 4; i++) {
            for (int j = 0; j < A[0].length; j++) {
                M[i][j] = A[i + inicio][j];
            }
        }
        return M;
    }

    public static void acomoda_matriz(double[][] C, double[][] A, int renglon, int columna, int N) {
        for (int i = 0; i < N / 4; i++) {
            for (int j = 0; j < N / 4; j++) {
                C[i + renglon][j + columna] = A[i][j];
            }
        }
    }

}
