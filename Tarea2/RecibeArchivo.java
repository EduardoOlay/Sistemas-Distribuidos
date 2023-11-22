import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class RecibeArchivo {

    public static void main(String[] args) {
        try {
            int puerto = 12345;
            DatagramSocket socket = new DatagramSocket(puerto);

            System.out.println("El servidor está en línea. Esperando conexiones de clientes...");

            while (true) {
                DatagramPacket solicitud = new DatagramPacket(new byte[1024], 1024);
                socket.receive(solicitud);

                // Crea un nuevo hilo para manejar la solicitud del cliente
                Thread clienteHandlerThread = new Thread(new ClienteHandler(socket, solicitud));
                clienteHandlerThread.start();

                // Espera a que el hilo actual del cliente se complete antes de continuar
                clienteHandlerThread.join();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class ClienteHandler implements Runnable {
        private DatagramSocket socket;
        private DatagramPacket solicitud;

        public ClienteHandler(DatagramSocket socket, DatagramPacket solicitud) {
            this.socket = socket;
            this.solicitud = solicitud;
        }

        @Override
        public void run() {
            try {
                System.out.println("Nueva conexión establecida con el cliente " + solicitud.getAddress());

                // Recibe el nombre del archivo, la longitud y la clave del cliente
                byte[] datos = solicitud.getData();
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(datos);
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

                String nombreArchivo = (String) objectInputStream.readObject();
                long longitudArchivo = objectInputStream.readLong();
                String claveEnBase64 = (String) objectInputStream.readObject();

                System.out.println("Cliente " + solicitud.getAddress() + ": Archivo recibido: " + nombreArchivo + ", Tamaño: "
                        + longitudArchivo);

                // Decodifica la clave Base64
                byte[] claveEnBytes = Base64.getDecoder().decode(claveEnBase64);
                SecretKey claveAES = new SecretKeySpec(claveEnBytes, "AES");

                // Inicializa el descifrador AES en modo ECB sin relleno
                Cipher descifrador = Cipher.getInstance("AES/ECB/NoPadding");
                descifrador.init(Cipher.DECRYPT_MODE, claveAES);

                FileOutputStream fileOutputStream = new FileOutputStream(nombreArchivo);

                int expectedPacketId = 1;
                Set<Integer> receivedPacketIds = new HashSet<>();

                while (fileOutputStream.getChannel().size() < longitudArchivo) {
                    socket.receive(solicitud);

                    byte[] decryptedBytes = descifrador.doFinal(solicitud.getData(), 0, solicitud.getLength());

                    ByteBuffer packetBuffer = ByteBuffer.wrap(decryptedBytes);
                    int packetId = packetBuffer.getInt();
                    int bytesRead = packetBuffer.getInt();
                    byte[] content = new byte[bytesRead];
                    packetBuffer.get(content);
                    byte[] receivedCrcBytes = new byte[8];
                    packetBuffer.get(receivedCrcBytes);
                    long receivedCrcValue = ByteBuffer.wrap(receivedCrcBytes).getLong();

                    Checksum crc32 = new CRC32();
                    crc32.update(content);
                    long calculatedCrcValue = crc32.getValue();

                    if (calculatedCrcValue != receivedCrcValue) {
                        System.err.println("Error en la verificación CRC32 para el paquete " + packetId + ". Se solicita reenvío...");

                        requestPacketResend(socket, solicitud.getAddress(), solicitud.getPort(), packetId);
                        continue;
                    }

                    if (receivedPacketIds.contains(packetId)) {
                        continue;
                    }

                    fileOutputStream.write(content);
                    receivedPacketIds.add(packetId);
                    sendConfirmation(socket, solicitud.getAddress(), solicitud.getPort(), packetId);

                    System.out.println("Cliente " + solicitud.getAddress() + ": El Paquete " + packetId +
                            "  se ha recibido y procesado con éxito.");

                    expectedPacketId++;
                }

                fileOutputStream.close();
                objectInputStream.close();
                System.out.println("Cliente " + solicitud.getAddress() + ": El archivo  ha sido recibido, a continuación se guardará en el disco.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void sendConfirmation(DatagramSocket socket, InetAddress address, int port, int packetId)
                throws IOException {
            byte[] confirmationData = ByteBuffer.allocate(4).putInt(packetId).array();
            DatagramPacket confirmationPacket = new DatagramPacket(confirmationData, confirmationData.length, address,
                    port);

            socket.send(confirmationPacket);
        }

        private void requestPacketResend(DatagramSocket socket, InetAddress address, int port, int packetId)
                throws IOException {
            byte[] requestResendData = ByteBuffer.allocate(4).putInt(packetId).array();
            DatagramPacket requestResendPacket = new DatagramPacket(requestResendData, requestResendData.length,
                    address, port);

            socket.send(requestResendPacket);
        }
    }
}
