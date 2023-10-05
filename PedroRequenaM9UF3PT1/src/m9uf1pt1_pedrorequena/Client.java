/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package m9uf1pt1_pedrorequena;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author Pedro
 */
public class Client {

    /**
     * @param args the command line arguments
     */
    static Scanner teclat = new Scanner(System.in);

    public static void main(String[] args) throws IOException {

        InetAddress serverAddress = InetAddress.getByName("localhost");
        int serverPort = 7879;
        int portJoc = 0;

        DatagramSocket socket = new DatagramSocket();
        String msg = "";
        System.out.println("1. Crear una partida nova");
        System.out.println("2. Connectar-se a una partida");
        System.out.println("3. Sortir");
        System.out.print("Introdueix opció: ");

        int opcio = teclat.nextInt();
        while (opcio != 3) {
            if (opcio == 1 || opcio == 2 || opcio == 3) {
                
            
            boolean creador = true;
            if (opcio == 1) {
                boolean continuar = true;
                while (continuar) {
                    System.out.print("Introdueix el port del joc: ");
                    if (teclat.hasNextInt()) {
                        portJoc = teclat.nextInt();
                        continuar = false;
                    } else {
                        System.out.println("Port incorrecte");
                        teclat.next();
                    }
                }
                msg = "CREAR," + portJoc;

            }
            if (opcio == 2) {
                msg = "UNIR-ME,";
                creador = false;
            }
            byte[] bytesOUT = msg.getBytes();
            DatagramPacket outPacket = new DatagramPacket(bytesOUT, bytesOUT.length, serverAddress, serverPort);
            socket.send(outPacket);

            if (creador) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String serverResponse = new String(packet.getData()).trim();
                if (serverResponse.equals("CORRECTE")) {
                    creadorPartida(portJoc);
                } else {
                    System.out.println("Aquest port ja està en ús");
                }

            } else {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String serverResponse = new String(packet.getData()).trim();
                String[] responseParts = serverResponse.split("::");
                String ipServer = responseParts[0];
                int portServer = Integer.parseInt(responseParts[1]);
                buscadorPartida(ipServer, portServer);

            }
            }else{
                System.out.println("Opció incorrecte.");
            }
            System.out.println("1. Crear una partida nova");
            System.out.println("2. Connectar-se a una partida");
            System.out.println("3. Sortir");
            System.out.print("Introdueix opció: ");

            opcio = teclat.nextInt();
        }
    }

    private static void creadorPartida(int portJoc) {
        try {
            ServerSocket server = new ServerSocket(portJoc);
            System.out.println("Esperant contrincant...");
            Socket connexio = server.accept();
            System.out.println("Connexió amb el contrincant establerta.");
            int fila = 0, columna = 0;

            DataInputStream in = new DataInputStream(connexio.getInputStream());
            DataOutputStream out = new DataOutputStream(connexio.getOutputStream());

            char[][] tauler = new char[3][3];
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    tauler[i][j] = ' ';
                }
            }

            boolean turnoActual = true;

            while (partidaDisponible(tauler)) {
                if (turnoActual) {
                    System.out.println("Torn del jugador 1 (X)");
                    mostrarTauler(tauler);

                    boolean movimientoValido = false;
                    while (!movimientoValido) {
                        System.out.print("Introdueix la fila (0-2): ");
                        fila = teclat.nextInt();
                        System.out.print("Introdueix la columna (0-2): ");
                        columna = teclat.nextInt();

                        if (fila >= 0 && fila < 3 && columna >= 0 && columna < 3 && tauler[fila][columna] == ' ') {
                            tauler[fila][columna] = 'X';
                            movimientoValido = true;
                        } else {
                            System.out.println("Movimient no possible. Intenta-ho de nou.");
                        }
                    }

                    out.writeInt(fila);
                    out.writeInt(columna);

                    if (comprobarVictoria(tauler, 'X')) {
                        System.out.println("¡El jugador 1 (X) ha guanyat!");
                        break;
                    }
                    if (empate(tauler)) {
                        System.out.println("¡El joc ha terminat en empat!");
                        break;
                    }

                    turnoActual = false;
                } else {

                    System.out.println("Torn del jugador 2 (O)");
                    mostrarTauler(tauler);

                    int filaContr = in.readInt();
                    int columnaContr = in.readInt();

                    tauler[filaContr][columnaContr] = 'O';

                    if (comprobarVictoria(tauler, 'O')) {
                        System.out.println("¡El jugador 2 (O) ha guanyat!");
                        break;
                    }
                    if (empate(tauler)) {
                        System.out.println("¡El joc ha terminat en empat!");
                        break;
                    }

                    turnoActual = true;
                }
            }

            mostrarTauler(tauler);

            connexio.close();
            server.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void buscadorPartida(String ipServer, int portServer) {
        try {
            Socket connexio = new Socket(ipServer, portServer);
            System.out.println("Connexió amb el creador de la partida establerta.");

            DataInputStream in = new DataInputStream(connexio.getInputStream());
            DataOutputStream out = new DataOutputStream(connexio.getOutputStream());
            int fila = 0, columna = 0;

            char[][] tauler = new char[3][3];
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    tauler[i][j] = ' ';
                }
            }

            boolean turnoActual = false;

            while (partidaDisponible(tauler)) {
                if (turnoActual) {
                    System.out.println("Torn del jugador 2 (O)");
                    mostrarTauler(tauler);

                    boolean movimientoValido = false;
                    while (!movimientoValido) {
                        System.out.print("Introdueix la fila (0-2): ");
                        fila = teclat.nextInt();
                        System.out.print("Introdueix la columna (0-2): ");
                        columna = teclat.nextInt();

                        if (fila >= 0 && fila < 3 && columna >= 0 && columna < 3 && tauler[fila][columna] == ' ') {
                            tauler[fila][columna] = 'O';
                            movimientoValido = true;
                        } else {
                            System.out.println("Movimient no possible. Intenta-ho de nou.");
                        }
                    }

                    out.writeInt(fila);
                    out.writeInt(columna);

                    if (comprobarVictoria(tauler, 'O')) {
                        System.out.println("¡El jugador 2 (O) ha guanyat!");
                        break;
                    }
                    if (empate(tauler)) {
                        System.out.println("¡El joc ha terminat en empat!");
                        break;
                    }

                    turnoActual = false;
                } else {
                    System.out.println("Torn del jugador 1 (X)");
                    mostrarTauler(tauler);

                    int filaContr = in.readInt();
                    int columnaContr = in.readInt();

                    tauler[filaContr][columnaContr] = 'X';

                    if (comprobarVictoria(tauler, 'X')) {
                        System.out.println("¡El jugador 1 (X) ha guanyat!");
                        break;
                    }
                    if (empate(tauler)) {
                        System.out.println("¡El joc ha terminat en empat!");
                        break;
                    }

                    turnoActual = true;
                }
            }

            mostrarTauler(tauler);

            connexio.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void mostrarTauler(char[][] tauler) {
        System.out.println("Tauler:");
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                System.out.print(tauler[i][j]);
                if (j < 2) {
                    System.out.print(" | ");
                }
            }
            System.out.println();
            if (i < 2) {
                System.out.println("---------");
            }
        }
    }

    private static boolean comprobarVictoria(char[][] tauler, char jugador) {

        for (int i = 0; i < 3; i++) {
            if (tauler[i][0] == jugador && tauler[i][1] == jugador && tauler[i][2] == jugador) {
                return true;
            }
        }

        for (int j = 0; j < 3; j++) {
            if (tauler[0][j] == jugador && tauler[1][j] == jugador && tauler[2][j] == jugador) {
                return true;
            }
        }

        if (tauler[0][0] == jugador && tauler[1][1] == jugador && tauler[2][2] == jugador) {
            return true;
        }
        if (tauler[0][2] == jugador && tauler[1][1] == jugador && tauler[2][0] == jugador) {
            return true;
        }

        return false;
    }

    private static boolean partidaDisponible(char[][] tauler) {

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (tauler[i][j] == ' ') {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean empate(char[][] tauler) {

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (tauler[i][j] == ' ') {
                    return false;
                }
            }
        }
        return true;
    }

}
