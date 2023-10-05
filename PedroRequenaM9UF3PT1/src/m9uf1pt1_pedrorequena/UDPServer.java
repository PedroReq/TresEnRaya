/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package m9uf1pt1_pedrorequena;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author Pedro
 */
public class UDPServer {

    private static Queue<String> cuaPartides = new LinkedList<String>();

    public static void main(String[] args) throws SocketException {
        DatagramSocket serverSocket = new DatagramSocket(7879);

        byte[] receiveData = new byte[1024];
        try {
            while (true) {
                System.out.println("Esperant conexió:");
                DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(packet);

                new Thread(() -> {
                    String msg = new String(packet.getData()).trim();

                    if (msg.split(",")[0].equals("CREAR")) {
                        String text;
                        if (verificarPuertoDisponible(Integer.parseInt(msg.split(",")[1]))) {
                            crearJoc(packet.getAddress().getHostAddress(), msg.split(",")[1]);
                            text = "CORRECTE";
                            System.out.println("-> OK");
                        } else {
                            text = "INCORRECTE";
                        }

                        byte[] bytesOUT = text.getBytes();
                        System.out.println(packet.getSocketAddress().toString());
                        DatagramPacket outPacket = new DatagramPacket(bytesOUT, bytesOUT.length, packet.getSocketAddress());
                        try {
                            serverSocket.send(outPacket);
                        } catch (IOException ex) {
                            System.out.println(ex.getMessage());
                        }

                    } else {

                        msg = unirsePartida();
                        System.out.println(msg);
                        byte[] bytesOUT = msg.getBytes();
                        System.out.println(packet.getSocketAddress().toString());
                        DatagramPacket outPacket = new DatagramPacket(bytesOUT, bytesOUT.length, packet.getSocketAddress());
                        try {
                            serverSocket.send(outPacket);
                        } catch (IOException ex) {
                            System.out.println(ex.getMessage());
                        }
                    }

                }).start();
            }
        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private static void crearJoc(String iPClient, String portJoc) {
        cuaPartides.add(iPClient + "::" + portJoc);
    }

    private static String unirsePartida() {

        while (cuaPartides.isEmpty()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                System.out.println(ex.getMessage());
            }
        }
        return cuaPartides.poll();

    }

    private static boolean verificarPuertoDisponible(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            serverSocket.close();
            return true;
        } catch (SocketException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

}
