package com.pavelpugach.blackjack;

import java.io.Serializable;

/**
 * Class of card, who played in the game.
 *
 * <p>
 *     Class of card has two private fields - name, which can be one of enum Cards and - points, which depends on name
 *     and determined in constructor. Also, this class has two getters, which return values of name and points.
 * </p>
 *@author Pavel Puhach
 *@version 1.0.0
 *@created 3.06.2015
 */
public class Card implements Serializable{

    private Cards name;
    private int points;

    public Card(Cards name){
        this.name = name;
        switch (name){
            case TWO:
                points = 2;
                break;
            case THREE:
                points = 3;
                break;
            case FOR:
                points = 4;
                break;
            case FIVE:
                points = 5;
                break;
            case SIX:
                points = 6;
                break;
            case SEVEN:
                points = 7;
                break;
            case EIGHT:
                points = 8;
                break;
            case NINE:
                points = 9;
                break;
            case TEN:
                points = 10;
                break;
            case JACK:
                points = 10;
                break;
            case LADY:
                points = 10;
                break;
            case KING:
                points = 10;
                break;

            case ACE:
                points = 11;
                break;
        }
    }

    public int getPoints(){
        return points;
    }
    public Cards getCardName(){
        return name;
    }
}
