import java.io.*;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class ClientePUT {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Uso: java SecureClientPUT <IP del servidor> <puerto> <nombre del archivo>");
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

            File file = new File(fileName);

            if (file.exists() && file.isFile()) {
                FileInputStream fis = new FileInputStream(file);
                int fileSize = (int) file.length();

                output.writeUTF("PUT " + file.getName());
                output.writeInt(fileSize);

                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = fis.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }

                fis.close();

                String response = input.readUTF();
                if ("PUT OK".equals(response)) {
                    System.out.println("El archivo fue recibido por el servidor con éxito.");
                } else if ("PUT ERROR".equals(response)) {
                    System.out.println("Error: El servidor no pudo escribir el archivo en el disco local.");
                } else {
                    System.out.println("Respuesta del servidor: " + response);
                }
            } else {
                System.out.println("Error: No se puede leer el archivo del disco local.");
            }

            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
