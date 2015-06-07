package com.pavelpugach.blackjack;

import java.io.*;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.Callable;

/**
 * Class Player responsible for communication with client side.
 *
 * <p>
 *     Class Player contains methods, which are responsible for communication with client side
 *     and managing game for each player from the game table.
 * </p>
 * <p>
 *     Also, it has a lot of private fields:
 *
 *    <p>playerPoints - count of player's points in current game</p>
 *    <p>socket - socket object that connected with the client program
 *    <p> in, out - InputStream and OutputStream of sockets connection</p>
 *    <p> dis, dout - wrapper for InputStream and OutputStream to exchange String game data</p>
 *    <p> oos - wrapper for OutputStream to exchange Object data</p>
 *    <p> bet - player's bet in current game</p>
 *    <p> dealer - Dealer class instance, it manages game play process</p>
 *    <p> decision - String variable that save player decision</p>
 *    <p> finishgame - flag, that shows status of the game</p>
 *    <p> isInterrupted - flag that shows if additional thread was interrupted</p>
 * </p>
 *
 *@author Pavel Puhach
 *@version 1.0.0
 *@created 3.06.2015
 */
public class Player {
    private volatile InputStream in;
    private volatile OutputStream out;
    private DataOutputStream dout;
    private DataInputStream dis;
    private Socket socket;
    private volatile int bet;
    private int playerPoints;
    private ObjectOutputStream oos;
    private Dealer dealer;
    private volatile String decision;
    private volatile boolean finishgame = false;
    volatile boolean isInterrupted = false;
    Thread thr;

    /**
     * Main constructor, it defines socket and dealer instances and takes in and out streams from socket.
     *
     * @param s - socket
     * @param d - dealer that manage the game
     */
    public Player(Socket s, Dealer d){
        socket = s;
        dealer = d;
        try {
            in = s.getInputStream();
            out = s.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send String message to client in UTF format.
     *
     * @param message - send it to client
     */
    public void sendMessage(String message){
        dout = new DataOutputStream(out);
        try {
            dout.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *Send count of players that was in last game.
     *
     * @param count of players in last game.
     */
    public void sendCountOfPlayers(int count){
        try {
            out.write(count);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read a bet from a client side and set it here.
     *
     * <p>Read a bet in a parallel thread.
     * If the bet is null for a 20 seconds - interupts reading and delete player from the table.</p>
     *
     * @param iterator for players list
     */
    public void takeBet(Iterator iterator){
        thr = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        isInterrupted = false;
                        bet = in.read();
                    } catch (IOException e) {
                        System.out.println("Waiting for player more than 20 seconds. This player have been disconected");
                        disconnectPlayer(iterator);
                    }
                }
            });

        thr.start();

        for (int i =0; i<20; i++){
            if (bet == 0 && !isInterrupted)
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }

        if (bet == 0 && !isInterrupted){
                disconnectPlayer(iterator);
        }
        System.out.println("Bet is "+bet);


    }

    /**
     * Return cuurrent player's bet.
     *
     * @return current player's bet
     */
    public int getBet(){
        return bet;
    }

    /**
     * Makes actions to disconnect player from game correctly.
     *
     * @param iterator for players list
     */
    private void disconnectPlayer(Iterator iterator){
        dealer.removePlayerFromGame(iterator);
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(thr != null) {
            thr.interrupt();
            isInterrupted = true;
        }


    }

    /**
     * Send random card to client.
     *
     * <p>Send random card to client side and update playerPoint here.</p>
     */
    public void sendRandomCard(){
        Card cardForSending = dealer.takeRandomCard();

        try {
            oos = new ObjectOutputStream(out);
            oos.writeObject(cardForSending);
            playerPoints = playerPoints+cardForSending.getPoints();
            if (playerPoints > 21)
                finishGame();
        } catch (IOException e) {
            System.out.println("Can't serialize object");
            e.printStackTrace();
        }
    }

    /**
     * Read user's decision from the client side and decide what to do.
     *
     * <p>
     *     Read user's decision from the client side in parallel thread and decide what to do.
     *     The decision can be:
     *     <p>HIT - take a card and continue game</p>
     *     <p>STAND - finish game and see the result</p>
     *     <p>DOUBLE - doubled bet, take a card and see the result</p>
     *     <p>Depends on decision method behaves differently.</p>
     * </p>
     * @param iterator for players list
     */
    public void takeUserDesicionsAndAct(Iterator iterator){
        while (!finishgame){
            dis = new DataInputStream(in);
            decision = "";
            isInterrupted = false;
            thr = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        decision = dis.readUTF();
                    } catch (Exception e) {
                        System.out.println("Waiting for player more than 20 seconds. This player have been disconected");
                        disconnectPlayer(iterator);
                        finishGame();
                    }
                }
            });
            thr.start();
            for(int i = 0; i<20; i++){
                if (decision.equals("") && !isInterrupted)
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            }
            System.out.println(decision);
            if (decision.equals("") && !isInterrupted) {
                disconnectPlayer(iterator);
            }

            switch (decision){
                case "h":
                    sendRandomCard();
                    break;
                case "s":
                    finishGame();
                    break;
                case "d":
                    sendRandomCard();
                    finishGame();
                    break;
            }
        }
    }

    /**
     * Finishes current game.
     */
    public void finishGame(){
        finishgame = true;
    }

    /**
     * Prepare all player data for a new game if he will want to continue.
     *
     * <p>Refresh all players data and prepare it for new game.</p>
     */
    public void refreshPlayerData(){
        bet = 0;
        playerPoints = 0;
        decision = "";
        finishgame = false;
        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Return playerPoints
     * @return playerPoints
     */
    public int getPlayerPoints(){
        return playerPoints;
    }

    /**
     * Send result of current game to the client side.
     *
     * @param result
     */
    public void sendResult(String result){
        sendMessage(result);
    }

}
