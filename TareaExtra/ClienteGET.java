import java.io.*;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class ClienteGET {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Uso: java SecureClientGET <IP del servidor> <puerto> <nombre del archivo>");
            return;
        }

        String serverIP = args[0];
        int serverPort = Integer.parseInt(args[1]);
        String fileName = args[2];

        try {           
            System.setProperty("javax.net.ssl.trustStore","keystore_cliente.jks");
            System.setProperty("javax.net.ssl.trustStorePassword","123456");
            SSLSocketFactory clientFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket connection = (SSLSocket) clientFactory.createSocket(serverIP, serverPort);

            DataOutputStream output = new DataOutputStream(connection.getOutputStream());
            DataInputStream input = new DataInputStream(connection.getInputStream());

            output.writeUTF("GET " + fileName);

            String response = input.readUTF();

            if ("OK".equals(response)) {
                int fileSize = input.readInt();

                File file = new File(fileName);
                FileOutputStream fos = new FileOutputStream(file);

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

                fos.close();

                if (totalBytesRead == fileSize) {
                    System.out.println("El archivo se recibió con éxito: " + fileName);
                } else {
                    file.delete(); // Eliminar el archivo si la transferencia no se completó
                    System.out.println("Error al recibir el archivo: " + fileName);
                }
            } else {
                System.out.println("Error: El servidor respondió con un mensaje de error.");
            }

            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
