import java.rmi.RemoteException;

public class ClaseRMI  implements InterfazRMI {

  public double[][] multiplicaMatrices(double[][] A, double[][] B, int N,int M) throws RemoteException{
    double[][]C=new double[N/4][N/4];
    for(int i=0;i<N/4;i++)
      for(int j=0; j<N/4;j++)
        for(int k=0;k<M;k++)
            C[i][j] += A[i][k] * B[j][k];
        
    return C;
  }
    
}
