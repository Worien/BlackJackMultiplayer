package com.pavelpugach.blackjack;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Class responsible for connecting to the server and geting input and output streams for exchanging data between client and server.
 *
 * <p>
 *     Class responsible for connecting to the server and geting input and output streams for exchanging data between client and server.
 *     For connecting to the server class has void method - connect().
 *     For geting input and outpput streams class has methods - getInputStrean() and getOutputStream().
 * </p>
 */
public class Connection {
    private Socket socket;
    private InputStream in;
    private OutputStream out;

    /**
     *Establishes a connection with the server.
     * <p>
     *   Establishes a connection with the server, that running in localhost on port 7776. If you want to change server coordinates
     *   you should change params in Socket constructor.
     * </p>
     */
    public void connect(){
        try {
            socket = new Socket("localhost", 7776);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Takes InputStream from the socket connection and returned it.
     *
     * Takes InputStream from the socket connection and returned it.
     * @return InputStream
    */
    public InputStream getInputStream(){
        try {
            in = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return in;
    }
    /**
     * Takes OutputStream from the socket connection and returned it.
     *
     * Takes OutputStream from the socket connection and returned it.
     * @return OutputStream
     */
    public OutputStream getOutputStream(){
        try {
            out = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out;
    }
}
