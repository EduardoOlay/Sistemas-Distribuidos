import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ServidorRMI {
    private static final int PUERTO = 8080; //Si cambias aquí el puerto, recuerda cambiarlo en el cliente
    public static void main(String[] args) throws Exception {
     Remote remote = UnicastRemoteObject.exportObject(new ClaseRMI(), 0);
        Registry registry = LocateRegistry.createRegistry(PUERTO);
       	System.out.println("Servidor escuchando en el puerto " + String.valueOf(PUERTO));
        registry.bind("Nodo1", remote); // Registrar Nodo
         registry.bind("Nodo2", remote); // Registrar Nodo
    }
}
