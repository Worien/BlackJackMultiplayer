package com.pavelpugach.blackjack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

/**
 * Dealer class is responsible for management of game process.
 *
 * <p>
 *     Dealer class defines when the game start and ends, priority of players steps,
 *     operations with cards and manage game process at all.
 * </p>
 * <p>
 *     It has next fields:
 *     <p>card - saves different cards objects in the game</p>
 *     <p>deckOfCards - arraylist that contains all 52 cards in random order</p>
 *     <p>players - arraylist of players, that take a part in the game</p>
 *     <p>pointsOfPlayers - arraylist of all players points</p>
 *     <p>reader - reader, created for reading String from keyboard in server side</p>
 *     <p>start - if start = "start" - the game will start</p>
 *     <p>result - variable for saving result of players</p>
 *     <p>iterator - mostly it is iterator for players list</p>
 * </p>
 */
public class Dealer implements Runnable {
    Card card;
    private ArrayList<Card> deckOfCards;
    volatile ArrayList<Player> players;
    ArrayList<Integer> pointsOfPlayers;
    BufferedReader reader;
    String start;
    String result;
    volatile Iterator<Player> iterator;

    /**
     * Method thad manage all game process, order of steps for all players and decides who loose or win.
     */
    @Override
    public void run(){
        pointsOfPlayers = new ArrayList<>();
        players = new ArrayList<>();
        reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.println("Type 'start' and push Enter to start the game:");
            try {
                start = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!start.equals("start"))
                return;
            for (Player pl : players) {
                pl.sendMessage(start);
            }
            MainServerClass.gamestarted = true;
            iterator = players.iterator();
            while (iterator.hasNext()) {
                Player pl = iterator.next();
                pl.sendMessage("makebet");
                pl.takeBet(iterator);
            }
            fillDeck();
            iterator = players.iterator();
            while (iterator.hasNext()) {
                Player pl = iterator.next();
                pl.sendRandomCard();
                pl.sendRandomCard();
            }
            iterator = players.iterator();
            while (iterator.hasNext()) {
                Player pl = iterator.next();
                pl.sendMessage("makedecision");
                pl.takeUserDesicionsAndAct(iterator);
            }

            for (Player pl : players) {
                if (pl.getPlayerPoints() > 21) {
                    result = "You loose";
                    System.out.println(result);
                    pl.sendResult(result);
                    pl.sendCountOfPlayers(players.size());
                } else {
                    pointsOfPlayers.add(pl.getPlayerPoints());
                }
            }
            Collections.sort(pointsOfPlayers);
            for (Player pl : players) {
                if (pl.getPlayerPoints() == pointsOfPlayers.get(pointsOfPlayers.size() - 1)) {
                    result = "You win";
                    System.out.println(result);
                    pl.sendResult(result);
                    pl.sendCountOfPlayers(players.size());
                } else {
                    result = "You loose";
                    System.out.println(result);
                    pl.sendResult(result);
                    pl.sendCountOfPlayers(players.size());
                }
            }
            for (Player pl:players){
                pl.refreshPlayerData();
            }
            MainServerClass.gamestarted = false;
            pointsOfPlayers = new ArrayList<>();
            result = "";
        }
    }

    /**
     * Checks the players size and if it's < 5 adds new player to the game.
     *
     * @param player
     */
    public void addPlayerToTheGame(Player player){
        if (players.size()<5){
            players.add(player);
            System.out.println("Player successful join to the game");
        } else{
            player.sendMessage("Sorry, but game table is full, try again later");
        }

    }

    /**
     * Fills the deckOfCards by each of types*4.
     */
    private void fillDeck(){
        deckOfCards = new ArrayList<Card>();
        for(Cards l:Cards.values()){
            card = new Card(l);
            deckOfCards.add(card);
            card = new Card(l);
            deckOfCards.add(card);
            card = new Card(l);
            deckOfCards.add(0, card);
            card = new Card(l);
            deckOfCards.add(2, card);
        }
        System.out.println("Count of card in dec = "+deckOfCards.size());

    }

    /**
     * Takes random card from deckOfCards and returns it.
     *
     * @return random card from deckOfCards
     */
    public Card takeRandomCard(){
        Random rnd = new Random(System.currentTimeMillis());
        int i = rnd.nextInt(deckOfCards.size());
        card = deckOfCards.get(i);
        deckOfCards.remove(i);
        return card;
    }

    /**
     * Removes player from game table (players)
     *
     * @param iterator
     */
    public void removePlayerFromGame(Iterator iterator){
        System.out.println("Player has been disconected");
        iterator.remove();
    }
}
