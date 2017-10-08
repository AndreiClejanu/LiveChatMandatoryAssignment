package chat;

/**
 * Created by andy on 01/10/17.
 */
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Server_Side {

    static ServerSocket serverSocket;
    static Socket client;
    static final int PORT = 2222;

    //static Scanner input;
    //static PrintWriter output;
    static ArrayList<ChatThread> clients = new ArrayList<>();

    public static void main(String[] args) throws IOException
        {
            //starts the server at the given port
            try
                {
                    //initialize the serversocket
                    serverSocket = new ServerSocket(PORT);
                }
            catch (IOException e)
                {
                    System.out.println("\nUnable to set up port!");
                    System.exit(1);
                }
            do
                {
                    try
                        {
                            //accepting a client connection
                            client = serverSocket.accept();
                            ChatThread newClient = new ChatThread(client, "thread");
                            newClient.start();
                        }
                    catch (Exception e)
                        {
                            System.out.println("Unable to connect to server");
                            System.exit(1);
                        }
                }
            while (true);
        }

    //return the list of active clients
    public static String getActiveClients()
        {
            String list = "\nOnlineUsers :";
            for(ChatThread c: clients)
                {
                    list += c.getUsername();
                    list += " ";
                }
            return list;
        }
    public static void sendMessage(String message)
        {
            for(ChatThread c : clients)
                {
                    c.getPrintWriter().println( message);
                }
        }
}

class ChatThread extends Thread {
    private Socket client;
    private Scanner input;
    private PrintWriter output;
    private String username = "";
    private boolean running;


    //constructor
    public ChatThread(Socket socket, String username)
        {
            client = socket;
            this.username = username;
            running = true;
            try
                {
                    input = new Scanner(socket.getInputStream());
                    output = new PrintWriter(socket.getOutputStream(), true);

                }
            catch(IOException e)
                {
                    e.printStackTrace();
                }
        }

    public void run()
        {
            while(running)
                {
                long startTime = System.currentTimeMillis();
                String received = "QUIT";
            do
                {
                    try
                        {
                            if(input.hasNextLine())
                                {
                                    received = input.nextLine();
                                    Scanner messageScanner = new Scanner(received);
                                    String key = messageScanner.next();
                                    switch (key)
                                        {
                                            case "JOIN":
                                                {
                                                    String name = received.substring(received.indexOf(" ") + 1, received.indexOf(","));
                                                    if(checkUsername(name))
                                                        {
                                                            output.println("J_OK");
                                                            username = name;
                                                            Server_Side.clients.add(this);
                                                            for(ChatThread c : Server_Side.clients)
                                                                {
                                                                    c.getPrintWriter().println(Server_Side.getActiveClients());
                                                                }
                                                            System.out.println(received);
                                                        }
                                                    else
                                                        {
                                                            output.println("J_ERR"); //send the J_ERR to the user, invalid name
                                                            System.out.println(name + " has been rejected.");
                                                        }
                                                }
                                            break;
                                            case "DATA":
                                                {
                                                    System.out.println(received);
                                                    Server_Side.sendMessage(received);
                                                }
                                            break;
                                            case "QUIT":
                                                {
                                                }
                                            break;
                                            case "ALIVE":
                                                startTime = System.currentTimeMillis();
                                            break;
                                            default:
                                                {
                                                    System.out.println(this.getUsername() + "send a weird message");
                                                    output.println("J_ERR");
                                                    break;
                                                }

                                        }

                                }
                        }
                    catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    long currentTime = System.currentTimeMillis();
                    if(currentTime-startTime>60000)
                        received = "QUIT";
                }
            while(!received.startsWith("QUIT"));
            try
                {
                    if(client!=null)
                        {
                            stopRunning();
                            int i=-1;
                            for(ChatThread c: Server_Side.clients)
                                {
                                    if(c.getUsername().equalsIgnoreCase(username))
                                        i = Server_Side.clients.indexOf(c);
                                }
                            Server_Side.clients.remove(i);
                            for(ChatThread c : Server_Side.clients)
                                {
                                    c.getPrintWriter().println(Server_Side.getActiveClients());
                                }
                            System.out.println(username+" left the conversation");
                            client.close();
                        }
                }
            catch(IOException ioEx)
                {
                    System.out.println("Unable to disconnect!");
                }
                }
        }

    private boolean checkUsername(String name)
        {
            //loop through user threads list
            for (ChatThread clientThread : Server_Side.clients)
                {
                    if(clientThread.username.equalsIgnoreCase(name)) //the name exists
                    return false;
                }
            return true; //the username is valid
        }
    public void stopRunning()
        {
            running = false;
        }
    //getter
    public String getUsername()
        {
            return username;
        }
    public Socket getSocket()
        {
            return client;
        }
    public Scanner getScanner()
        {
            return input;
        }
    public PrintWriter getPrintWriter()
        {
            return output;
        }
}