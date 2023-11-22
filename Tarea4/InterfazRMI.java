import java.rmi.Remote;
import java.rmi.RemoteException;

/*
	Declarar firma de métodos que serán sobrescritos
*/
public interface InterfazRMI extends Remote {
    public double[][] multiplicaMatrices(double[][] A, double[][] B,int N, int M) throws RemoteException;
}