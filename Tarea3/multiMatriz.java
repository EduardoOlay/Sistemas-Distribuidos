import java.io.*;
import java.net.*;

public class multiMatriz {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Debes proporcionar el número de nodo (1, 2 o 3), N y M como argumentos.");
            System.exit(1);
        }

        int nodeNumber = Integer.parseInt(args[0]);
        int N = Integer.parseInt(args[1]);
        int M = Integer.parseInt(args[2]);

        if (nodeNumber == 1) {
            // Nodo 1
            try {
                ServerSocket serverSocket = new ServerSocket(8080);
                System.out.println("Esperando conexión de nodo 2 y 3....");
                // Esperar conexiones entrantes de nodo 2 y nodo 3
                Socket socket2 = serverSocket.accept();
                Socket socket3 = serverSocket.accept();
                System.out.println("\nConexión realizada");

                // Inicialización de matrices A y B
                double[][] A = initializeMatrixA(N, M);
                double[][] B = initializeMatrixB(N, M);

                // Cálculo de la transpuesta de B (BT)
                double[][] BT = new double[N][M];
                for (int i = 0; i < N; i++) {
                    for (int j = 0; j < M; j++) {
                        BT[i][j] = B[j][i];
                    }
                }
//Implemnetación Realizada por Jose Eduardo

                // Dividir A y BT en porciones y enviar a nodos 2 y 3
                int numRowsPerNode = N / 6; // Dividir A en 6 partes
                double[][] A1 = new double[numRowsPerNode][M];
                double[][] A2 = new double[numRowsPerNode][M];
                double[][] A3 = new double[numRowsPerNode][M];
                double[][] A4 = new double[numRowsPerNode][M];
                double[][] A5 = new double[numRowsPerNode][M];
                double[][] A6 = new double[numRowsPerNode][M];

                // Dividir la matriz A en las 6 partes
                for (int i = 0; i < N; i++) {
                    for (int j = 0; j < M; j++) {
                        int part = i / numRowsPerNode;
                        int rowInPart = i % numRowsPerNode;
    
                        switch (part) {
                            case 0:
                                A1[rowInPart][j] = A[i][j];
                                break;
                            case 1:
                                A2[rowInPart][j] = A[i][j];
                                break;
                            case 2:
                                A3[rowInPart][j] = A[i][j];
                                break;
                            case 3:
                                A4[rowInPart][j] = A[i][j];
                                break;
                            case 4:
                                A5[rowInPart][j] = A[i][j];
                                break;
                            case 5:
                                A6[rowInPart][j] = A[i][j];
                                break;
                        }
                    }
                }
                int numRowsPerNodeB = N / 6; // Dividir A en 6 partes
                double[][] BT1 = new double[numRowsPerNode][M];
                double[][] BT2 = new double[numRowsPerNode][M];
                double[][] BT3 = new double[numRowsPerNode][M];
                double[][] BT4 = new double[numRowsPerNode][M];
                double[][] BT5 = new double[numRowsPerNode][M];
                double[][] BT6 = new double[numRowsPerNode][M];

                // Dividir la matriz BT en las 6 partes
                for (int i = 0; i < N; i++) {
                    for (int j = 0; j < M; j++) {
                        int part = i / numRowsPerNodeB;
                        int rowInPart = i % numRowsPerNodeB;
    
                        switch (part) {
                            case 0:
                                BT1[rowInPart][j] = BT[i][j];
                                break;
                            case 1:
                                BT2[rowInPart][j] = BT[i][j];
                                break;
                            case 2:
                                BT3[rowInPart][j] = BT[i][j];
                                break;
                            case 3:
                                BT4[rowInPart][j] = BT[i][j];
                                break;
                            case 4:
                                BT5[rowInPart][j] = BT[i][j];
                                break;
                            case 5:
                                BT6[rowInPart][j] = BT[i][j];
                                break;
                        }
                    }
                }

                ObjectOutputStream out2 = new ObjectOutputStream(socket2.getOutputStream());
                ObjectOutputStream out3 = new ObjectOutputStream(socket3.getOutputStream());

               // Enviar las partes correspondientes de A y BT a nodo 2
               System.out.println("\nEnviando matrices a Nodo 2");
                out2.writeObject(A1);
                out2.writeObject(A2);
                out2.writeObject(A3);
                out2.writeObject(BT1);
                out2.writeObject(BT2);
                out2.writeObject(BT3);
                out2.writeObject(BT4);
                out2.writeObject(BT5);
                out2.writeObject(BT6);

                // Enviar las partes correspondientes de A y BT a nodo 3
                System.out.println("\nEnviando matrices a Nodo 3");
                out3.writeObject(A4);
                out3.writeObject(A5);
                out3.writeObject(A6);
                out3.writeObject(BT1);
                out3.writeObject(BT2);
                out3.writeObject(BT3);
                out3.writeObject(BT4);
                out3.writeObject(BT5);
                out3.writeObject(BT6);

                // Recibir C1 al C18 de nodo 2
                // Crear un ObjectInputStream para recibir los datos
                ObjectInputStream in2 = new ObjectInputStream(socket2.getInputStream());

                // Crear un arreglo de matrices para almacenar C1 al C18
                double[][][] matricesC1toC18 = new double[18][][];

                // Recibir las matrices C1 al C18
                for (int i = 0; i < 18; i++) {
                    matricesC1toC18[i] = (double[][]) in2.readObject();
}
                System.out.println("\nMatrices recibidas del Nodo2");

                // Recibir C19 al C36 de nodo 3
                ObjectInputStream in3 = new ObjectInputStream(socket3.getInputStream());
                // Crear un arreglo para almacenar las matrices C19 a C36
                double[][][] C19toC36 = new double[18][][];

                // Recibir las matrices C19 a C36 del Nodo 3
                for (int i = 0; i < 18; i++) {
                    C19toC36[i] = (double[][]) in3.readObject();
                }
                System.out.println("\nMatrices recibidas del Nodo3");

                // Combinar matrices C1 al C18 y C19 al C36 para obtener la matriz final C
                double[][][][] C = new double[6][6][][]; // Matriz C dividida en 6x6 bloques de N/6 x N/6

                int blockIndex = 0;

                for (int i = 0; i < 6; i++) {
                    for (int j = 0; j < 6; j++) {
                        if (blockIndex < 18) {
                            C[i][j] = matricesC1toC18[blockIndex];
                        } else {
                            C[i][j] = C19toC36[blockIndex - 18];
                        }
                        blockIndex++;
                    }
                }

                // Ahora, la matriz C es un arreglo 6x6 de bloques de N/6 x N/6
                System.out.println("\ncalculando cheksum de C...");
                // Calcular el checksum de C
                double checksum = 0.0;
                for (int i = 0; i < 6; i++) {
                    for (int j = 0; j < 6; j++) {
                        double[][] block = C[i][j];
                        for (int row = 0; row < block.length; row++) {
                            for (int col = 0; col < block[0].length; col++) {
                                checksum += block[row][col];
                            }
                        }
                    }
                }

             if(N==6 || M==5){
                // Mostrar las matrices A, B y C
                System.out.println("Matriz A:");
                printMatrix(A);
                System.out.println("\nMatriz B:");
                printMatrix(B);
                // Imprimir la matriz C
                System.out.println("\nMatriz C:");
                for (int i = 0; i < 6; i++) {
                    for (int j = 0; j < 6; j++) {
                        double[][] block = C[i][j];
                        for (int row = 0; row < block.length; row++) {
                            for (int col = 0; col < block[0].length; col++) {
                                System.out.print(block[row][col] + "   ");
                            }
                            System.out.println();
                        }
                    }
                }
             }             
                System.out.println("Checksum de C: " + checksum);

                // Cerrar sockets y limpiar recursos
                socket2.close();
                socket3.close();
                serverSocket.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else if (nodeNumber == 2) {
            // Nodo 2
            try {
                Socket socket1 = new Socket("localhost", 8080); // Reemplaza "NODO1_IP" con la IP del Nodo 1

                // Recibir las partes correspondientes de A y BT del Nodo 1
                ObjectInputStream in1 = new ObjectInputStream(socket1.getInputStream());

                double[][] A1 = (double[][]) in1.readObject();
                double[][] A2 = (double[][]) in1.readObject();
                double[][] A3 = (double[][]) in1.readObject();
                double[][] BT1 = (double[][]) in1.readObject();
                double[][] BT2 = (double[][]) in1.readObject();
                double[][] BT3 = (double[][]) in1.readObject();
                double[][] BT4 = (double[][]) in1.readObject();
                double[][] BT5 = (double[][]) in1.readObject();
                double[][] BT6 = (double[][]) in1.readObject();

                System.out.println("Matrices recibidas");
                System.out.println("\nRealizando cálculos para obtener submatrices....");
                // Realizar los cálculos para obtener C1 al C18
                double[][] C1 = multiplyMatrices(A1, BT1);
                double[][] C2 = multiplyMatrices(A1, BT2);
                double[][] C3 = multiplyMatrices(A1, BT3);
                double[][] C4 = multiplyMatrices(A1, BT4);
                double[][] C5 = multiplyMatrices(A1, BT5);
                double[][] C6 = multiplyMatrices(A1, BT6);
                double[][] C7 = multiplyMatrices(A2, BT1);
                double[][] C8 = multiplyMatrices(A2, BT2);
                double[][] C9 = multiplyMatrices(A2, BT3);
                double[][] C10 = multiplyMatrices(A2, BT4);
                double[][] C11 = multiplyMatrices(A2, BT5);
                double[][] C12 = multiplyMatrices(A2, BT6);
                double[][] C13 = multiplyMatrices(A3, BT1);
                double[][] C14 = multiplyMatrices(A3, BT2);
                double[][] C15 = multiplyMatrices(A3, BT3);
                double[][] C16 = multiplyMatrices(A3, BT4);
                double[][] C17 = multiplyMatrices(A3, BT5);
                double[][] C18 = multiplyMatrices(A3, BT6);


                // Enviar C1 al C18 a nodo 1
                ObjectOutputStream out = new ObjectOutputStream(socket1.getOutputStream());

                // Crear un arreglo de matrices C1 al C18
                double[][][] matricesC = {C1, C2, C3, C4, C5, C6, C7, C8, C9, C10, C11, C12, C13, C14, C15, C16, C17, C18};
                // Enviar las matrices C1 al C18
                for (int i = 0; i < matricesC.length; i++) {
                    out.writeObject(matricesC[i]);
                }
                System.out.println("Matrices enviadas");

                // Cerrar socket y limpiar recursos
                socket1.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else if (nodeNumber == 3) {
            // Nodo 3
            try {
                Socket socket1 = new Socket("localhost", 8080); // Reemplaza "NODO1_IP" con la IP del Nodo 1

                // Recibir las partes correspondientes de A y BT del Nodo 1
                ObjectInputStream in1 = new ObjectInputStream(socket1.getInputStream());
                // Crear variables para almacenar las matrices recibidas
                double[][] A4 = (double[][]) in1.readObject();
                double[][] A5 = (double[][]) in1.readObject();
                double[][] A6 = (double[][]) in1.readObject();
                double[][] BT1 = (double[][]) in1.readObject();
                double[][] BT2 = (double[][]) in1.readObject();
                double[][] BT3 = (double[][]) in1.readObject();
                double[][] BT4 = (double[][]) in1.readObject();
                double[][] BT5 = (double[][]) in1.readObject();
                double[][] BT6 = (double[][]) in1.readObject();

                 // Realizar los cálculos para obtener C19 al C36
                 System.out.println("Matrices recibidas");
                System.out.println("\nRealizando cálculos para obtener submatrices....");
                 double[][] C19 = multiplyMatrices(A4, BT1);
                 double[][] C20 = multiplyMatrices(A4, BT2);
                 double[][] C21 = multiplyMatrices(A4, BT3);
                 double[][] C22 = multiplyMatrices(A4, BT4);
                 double[][] C23 = multiplyMatrices(A4, BT5);
                 double[][] C24 = multiplyMatrices(A4, BT6);
                 double[][] C25 = multiplyMatrices(A5, BT1);
                 double[][] C26 = multiplyMatrices(A5, BT2);
                 double[][] C27 = multiplyMatrices(A5, BT3);
                 double[][] C28 = multiplyMatrices(A5, BT4);
                 double[][] C29 = multiplyMatrices(A5, BT5);
                 double[][] C30 = multiplyMatrices(A5, BT6);
                 double[][] C31 = multiplyMatrices(A6, BT1);
                 double[][] C32 = multiplyMatrices(A6, BT2);
                 double[][] C33 = multiplyMatrices(A6, BT3);
                 double[][] C34 = multiplyMatrices(A6, BT4);
                 double[][] C35 = multiplyMatrices(A6, BT5);
                 double[][] C36 = multiplyMatrices(A6, BT6);


                // Enviar (C19 al C36) a nodo 1
                ObjectOutputStream out = new ObjectOutputStream(socket1.getOutputStream());

                // Crear un arreglo de matrices C19 al C36
                double[][][] matricesC = {C19, C20, C21, C22, C23, C24, C25, C26, C27, C28, C29, C30, C31, C32, C33, C34, C35, C36};
                // Enviar las matrices C19 al C36
                for (int i = 0; i < matricesC.length; i++) {
                    out.writeObject(matricesC[i]);
                }
                
                System.out.println("Matrices enviadas");
                // Cerrar socket y limpiar recursos
                socket1.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Número de nodo no válido. Debe ser 1, 2 o 3.");
            System.exit(1);
        }
    }

    private static double[][] initializeMatrixA(int N, int M) {
        double[][] A = new double[N][M];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                A[i][j] = 5 * i - 2 * j;
            }
        }
        return A;
    }

    private static double[][] initializeMatrixB(int N, int M) {
        double[][] B = new double[M][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                B[j][i] = 6 * i + 3 * j;
            }
        }
        return B;
    }

     // Función para multiplicar dos matrices
     private static double[][] multiplyMatrices(double[][] A, double[][] B) {

        int numRows=A.length;
        int numCols=A[0].length;

        if(numRows != B.length || numCols != B[0].length){
          System.err.println("No se pueden multiplicar las matrices. Las dimensiones no coinciden.");
          System.exit(1);  
        }

        double[][] C = new double[numRows][numCols];

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                C[i][j] = A[i][j] * B[i][j];
            }
        }

        return C;
    }

        private static void printMatrix(double[][] matrix) {
        int numRows = matrix.length;
        int numCols = matrix[0].length;
    
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                System.out.print(matrix[i][j] + "  ");
            }
            System.out.println("  ");
        }
    }

}
