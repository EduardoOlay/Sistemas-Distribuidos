import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class EnviaArchivo {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java EnviaArchivo <file_path>");
            return;
        }

        String filePath = args[0];
        File file = new File(filePath);

        if (!file.exists()) {
            System.err.println("File does not exist.");
            return;
        }

        try {
            EnviaArchivo sender = new EnviaArchivo();
            sender.send(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(File file) throws Exception {
        // Genera de manera aleatoria una clave AES-256
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        SecretKey aesKey = keyGenerator.generateKey();

        // Convierte la clave en bytes
        byte[] aesKeyBytes = aesKey.getEncoded();

        // Codifica la clave en Base64
        String aesKeyBase64 = Base64.getEncoder().encodeToString(aesKeyBytes);

        // Abre el archivo especificado
        FileInputStream fileInputStream = new FileInputStream(file);

        // Inicializa el cifrador AES en modo ECB sin relleno
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);

        // Envía el archivo al servidor
        sendFileToServer(file, aesKeyBase64, fileInputStream, cipher);
    }

    private void sendFileToServer(File file, String aesKeyBase64, FileInputStream fileInputStream, Cipher cipher)
            throws Exception {
        DatagramSocket socket = new DatagramSocket();
        InetAddress servidorDireccion = InetAddress.getByName("localhost");
        int servidorPuerto = 12345;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

        objectOutputStream.writeObject(file.getName());
        objectOutputStream.writeLong(file.length());
        objectOutputStream.writeObject(aesKeyBase64);

        byte[] metaData = byteArrayOutputStream.toByteArray();

        DatagramPacket metaDataPacket = new DatagramPacket(metaData, metaData.length, servidorDireccion, servidorPuerto);
        socket.send(metaDataPacket);

        byte[] buffer = new byte[1008];
        int packetId = 0;

        while (true) {
            int bytesRead = fileInputStream.read(buffer);

            if (bytesRead == -1) {
                break;
            }

            packetId++;

            Checksum crc32 = new CRC32();
            crc32.update(buffer, 0, bytesRead);
            long crcValue = crc32.getValue();
            byte[] crcBytes = new byte[8];
            ByteBuffer.wrap(crcBytes).putLong(crcValue);

            byte[] packetData = new byte[1024];
            ByteBuffer.wrap(packetData).putInt(packetId).putInt(bytesRead).put(buffer, 0, bytesRead).put(crcBytes);

            byte[] encryptedBytes = cipher.doFinal(packetData);

            socket.send(new DatagramPacket(encryptedBytes, encryptedBytes.length, servidorDireccion, servidorPuerto));

            waitForConfirmation(socket, packetId);
        }

        fileInputStream.close();
        objectOutputStream.close();
        socket.close();
    }

    private void waitForConfirmation(DatagramSocket socket, int expectedPacketId) throws IOException {
        byte[] confirmationData = new byte[4];
        DatagramPacket confirmationPacket = new DatagramPacket(confirmationData, confirmationData.length);

        while (true) {
            socket.receive(confirmationPacket);
            int confirmedPacketId = ByteBuffer.wrap(confirmationData).getInt();

            if (confirmedPacketId == expectedPacketId) {
                System.out.println("Cliente: Confirmación para el paquete " + confirmedPacketId + " recibida del servidor.");
                break;
            }
        }
    }
}
