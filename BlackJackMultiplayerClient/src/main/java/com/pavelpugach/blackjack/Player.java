package com.pavelpugach.blackjack;

import java.io.*;
import java.util.ArrayList;

/**
 * Player class responsible for all playing process on client side.
 *
 * <p>
 *     This class contains method, which are manage main game process.
 * </p>
 * <p>
 *     <p>Also, it has a lot of private fields:
 *     <p>money - count of user's money</p>
 *     <p>points - count of user's points in current game</p>
 *     <p>myCards - user's cards in current game</p>
 *     <p>in, out - InputStream and OutputStream of socket connection</p>
 *     <p>dis, dout - wrapper for InputStream and OutputStream to exchange String game data</p>
 *     <p>ois - wrapper for InputStream to exchange Object's data</p>
 *     <p>bet - user's bet in current game</p>
 *     <p> reader - always reads data from keyboard</p>
 *     <p> finishgame - flag, that show status of the game</p>
 *     <p>answer - store user decision after making bet</p>
 * </p>
 *@author Pavel Puhach
 *@version 1.0.0
 *@created 3.06.2015
 */
public class Player {
    private int money = 100;
    private int points;
    private ArrayList<Card> myCards;
    private InputStream in;
    private OutputStream out;
    private ObjectInputStream ois;
    private DataOutputStream dout;
    private DataInputStream dis;
    private int bet;
    private BufferedReader reader;
    private boolean finishgame = false;
    private String answer;

    public Player(InputStream in, OutputStream out){
        this.in = in;
        this.out = out;
        myCards = new ArrayList<Card>();
        dis = new DataInputStream(in);
        dout = new DataOutputStream(out);
    }

    /**
     * Take bet from user and send it to the server.
     *
     * <p>
     *     Ask user to enter the bet from keyboard, than parse it to integer and sent to the server. Also, updated money field.
     * </p>
     */
    public void makeBet(){
        System.out.println("Make your bet:");


        try {
            bet = Integer.parseInt(readFromKeyboard());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Your bet must contains only integer numbers");
            makeBet();
        }
        if(bet<1||bet>money){
            informAbountUncorrectBet();
            return;
        }
        try {
            out.write(bet);
            out.flush();
            money = money - bet;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Problems with connection");
        }

    }

    /**
     * Takes card from server and show it to user.
     *
     * <p>
     * Wait for card from the server, when it comes - deserialize it, show it to user and add to myCards list.
     * </p>
     */
    public void takeCardsAndLook(){
        try {
            ois = new ObjectInputStream(in);
            Card card = (Card) ois.readObject();
            points = points + card.getPoints();
            System.out.println("Card name - "+card.getCardName());
            System.out.println("Card points - "+card.getPoints());
            myCards.add(card);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Take users decision, send it to server and decide what to do next.
     *
     * <p>
     *     Take user decision, check the correctness of input and send it to server.
     *     <p>The decision can be:</p>
     *     <p>HIT - take a card and continue game</p>
     *     <p>STAND - finish game and see the result</p>
     *    <p> DOUBLE - doubled bet, take a card and see the result</p>
     *     <p>Depends on decision method behaves differently.</p>
     * </p>
     */
    public void takeDecisionsAndSend(){
        while (!finishgame) {
            System.out.println("type your decision:");
            try {
                answer = readFromKeyboard();
                if (answer == null)
                    return;
                if (!answer.equals("h") && !answer.equals("s") && !answer.equals("d")) {
                    informAboutUncorrectDecision();
                    return;
                }
                dout.writeUTF(answer);
                dout.flush();
                switch (answer){
                    case "s":
                        finishGame();
                        break;
                    case "d":
                        money = money - bet;
                        bet = bet*2;
                        takeCardsAndLook();
                        finishGame();
                        break;
                    case "h":
                        takeCardsAndLook();
                        if (points >21)
                            finishGame();
                        break;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Informed user about incorrect bet and runs makeBet() again.
     *
     */
    public void informAbountUncorrectBet(){
        System.out.println("Your BET must be in range from 1 to "+money);
        makeBet();
    }

    /**
     * Informed user about incorrect decision and runs takeDecisionsAndSend() again.
     *
     */
    public void informAboutUncorrectDecision(){
        System.out.println("You can type only 'h', 's' or 'd'");
        takeDecisionsAndSend();
    }

    /**
     * Return users current balance.
     *
     * @return money
     */
    public int getCurrentBalance(){
        return money;
    }

    /**
     * Finishs current game and shows the result for user.
     *
     * <p>
     *     Finishs current game and reads result from the sever. Then show result to user and changes money of user if he won.
     *     Also, it sets the initial value for some fiels to use them in the next game.
     * </p>
     */
    public void finishGame(){
        finishgame = true;
        try {
            String result = dis.readUTF();
            int countsOfGamersAtTable = in.read();
            System.out.println(result);
            if (result.equals("You win"))
                money = money+bet*countsOfGamersAtTable;
            System.out.println("Your balance is "+getCurrentBalance());
        } catch (Exception e) {
            e.printStackTrace();
        }

        points = 0;
        myCards = new ArrayList<>();
        bet = 0;
        answer = "";
    }

    /**
     * Reads message from the server and returns it.
     *
     * @return message from the server
     */
    public String readMessage(){
        String message = "";
        try {
            message = dis.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return message;
    }

    /**
     * Reads users message from keyboard and returns it.
     *
     * @return user message
     */
    public String readFromKeyboard(){
        reader = new BufferedReader(new InputStreamReader(System.in));
        String message = "";
        try {
            message = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return message;
    }

    /**
     * Makes flag finishgame = false to start new game.
     */
    public void startNewGame(){
        finishgame = false;
    }
}
