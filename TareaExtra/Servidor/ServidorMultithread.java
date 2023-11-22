import java.io.*;
import java.net.*;
import javax.net.ssl.SSLServerSocketFactory;

public class ServidorMultithread {

    public static void main(String[] args) throws Exception {
        System.setProperty("javax.net.ssl.keyStore","keystore_servidor.jks");
        System.setProperty("javax.net.ssl.keyStorePassword","1234567");
        SSLServerSocketFactory socket_factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        ServerSocket socket_servidor = socket_factory.createServerSocket(8080);
        System.out.println("Servidor listo para recibir peticiones...");
        
        while (true) {
            Socket conexion = socket_servidor.accept();
            Thread clientThread = new ClientHandler(conexion);
            clientThread.start();
        }
    }
}

class ClientHandler extends Thread {
    private Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            DataOutputStream salida = new DataOutputStream(clientSocket.getOutputStream());
            DataInputStream entrada = new DataInputStream(clientSocket.getInputStream());

            String request = entrada.readUTF();
            String[] requestParts = request.split(" ");

            if (requestParts.length >= 2) {
                String action = requestParts[0];
                String fileName = requestParts[1];

                if ("GET".equals(action)) {
                    // Manejar la petición GET
                    sendFile(fileName, salida);
                } else if ("PUT".equals(action)) {
                    // Manejar la petición PUT
                    receiveFile(fileName, entrada,salida);
                } else {
                    salida.writeUTF("ERROR: Acción no válida.");
                }
            } else {
                salida.writeUTF("ERROR: Formato de solicitud incorrecto.");
            }

            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendFile(String fileName, DataOutputStream output) throws IOException {
        File file = new File(fileName);
        if (file.exists() && file.isFile()) {
            FileInputStream fis = new FileInputStream(file);
            int fileSize = (int) file.length();
            
            output.writeUTF("OK");
            output.writeInt(fileSize);
            
            byte[] buffer = new byte[1024];
            int bytesRead;
            
            while ((bytesRead = fis.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            
            fis.close();
        } else {
            output.writeUTF("ERROR: El archivo no existe.");
        }
    }

    private void receiveFile(String fileName, DataInputStream input,DataOutputStream output) throws IOException {
        File file = new File(fileName);
        FileOutputStream fos = new FileOutputStream(file);
        
        int fileSize = input.readInt();
        byte[] buffer = new byte[1024];
        
        int bytesRead;
        int totalBytesRead = 0;
        
        while (totalBytesRead < fileSize) {
            bytesRead = input.read(buffer, 0, Math.min(buffer.length, fileSize - totalBytesRead));
            if (bytesRead == -1) {
                break;
            }
            fos.write(buffer, 0, bytesRead);
            totalBytesRead += bytesRead;
        }
                if (totalBytesRead == fileSize) {
                output.writeUTF("OK");
                System.out.println("Archivo recibido y guardado con éxito: " + fileName);
            } else {
                file.delete(); // Eliminar el archivo si la transferencia no se completó
                output.writeUTF("ERROR");
                System.out.println("Error al recibir el archivo: " + fileName);
            }
        fos.close();
        
    }
}

