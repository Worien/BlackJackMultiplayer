package com.pavelpugach.blackjack;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Sets up server and wait for connections.
 *
 * Class with main method, which sets up server, create Dealer instance, run dealer object in another thread and waiting
 * for a new clients.
 * If new client has been connected and the game isn't started yet - main() creates new Player object and adds it to the game table.
 *
 *@author Pavel Puhach
 *@version 1.0.0
 *@created 3.06.2015
 */
public class MainServerClass {
    public static boolean gamestarted;

    public static void main(String[] args) {

        ServerSocket ss = null;
        try {
            ss = new ServerSocket(7776);
        } catch (IOException e) {
            e.printStackTrace();
        }
        gamestarted = false;
        Dealer dealer = new Dealer();
        new Thread(dealer).start();
       while (!gamestarted){

           Socket s = null;
           try {

               s = ss.accept();
           } catch (IOException e) {
               System.out.println("Player has been disconected");
               e.printStackTrace();
           }

            Player player = new Player(s, dealer);
            dealer.addPlayerToTheGame(player);
        }
    }
}
