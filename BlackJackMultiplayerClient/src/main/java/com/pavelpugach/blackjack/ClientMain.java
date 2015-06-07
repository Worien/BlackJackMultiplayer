package com.pavelpugach.blackjack;

import java.io.*;
import java.net.Socket;

/**
 * Main client class for a Black Jack Multiplayer game.
 *
 *<p>
 *     This class is the main class of client side program. Firstly, it creats connection object,
 *     then create player object and then this class are responsible for prioritising of actions
 *     and communications between user and client program in the gameplay.
 *</p>
 *
 *@author Pavel Puhach
 *@version 1.0.0
 *@created 3.06.2015 *
 */
public class ClientMain {
    static boolean onemoregame;

    /**
     * Establishes connection and communication streams and  run play() method.
     *
     * @throws Exception
     */
    public static void main(String[] args) throws Exception{
        onemoregame = true;
        Connection con = new Connection();
        System.out.println("Connecting...");
        con.connect();
        InputStream input = con.getInputStream();
        OutputStream output = con.getOutputStream();
        Player player = new Player(input, output);
        System.out.println("Connection was successful");
        play(player);
    }

    /**
     * Describe user interaction with client program.
     *
     * @param player
     */
    private static void play(Player player) {
        while (onemoregame) {
            System.out.println("Waiting for the next game... it may take a couple of seconds");
            if (player.readMessage().equals("start")) {
                System.out.println("Game starts");
                System.out.println("Waiting for another players make their bets");
                if (!player.readMessage().equals("makebet"))
                    return;
                player.makeBet();
                System.out.println("Your balance after bet is: " + player.getCurrentBalance() + "$");
                System.out.println("");
                System.out.println("Dealer gives you cards:");
                player.takeCardsAndLook();
                player.takeCardsAndLook();
                if (!player.readMessage().equals("makedecision"))
                    return;
                System.out.println("Now you can make a decision what to do");
                System.out.println("Type 's' and press 'Enter' if you want to STAND");
                System.out.println("Type 'h' and press 'Enter' if you want to HIT");
                System.out.println("Type 'd' and press 'Enter' if you want to DOUBLE");
                player.takeDecisionsAndSend();
            } else {
                System.out.println("Sorry, but game table is full. Try to reboot the program and start later");
            }
            System.out.println("Do you want to play one more time? y/n");
            String onemoretime = player.readFromKeyboard();
            if (!onemoretime.equals("y"))
                onemoregame = false;
            player.startNewGame();

        }
    }
}
