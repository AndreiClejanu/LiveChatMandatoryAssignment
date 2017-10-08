package chat;

/**
 * Created by andy on 02/10/17.
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client_Side {
    static Scanner sInput; //read from the socket
    static PrintWriter sOutput; //write on the socket;
    static Socket socket;
    static Scanner networkInput;
    static PrintWriter networkOutput;

    //the server, the port and the username
    static String username;
    static  int port = 2222;
    static InetAddress ip;

    public static Scanner getNetworkInput()
        {
            return networkInput;
        }

    public static void main(String[] args)
        {
            try
                {
                    Scanner scan = new Scanner(System.in);
                    System.out.print("IP Address: ");

                    ip = InetAddress.getByName(scan.next());

                    System.out.print("\nPort Number: ");
                    port = Integer.parseInt(scan.next());
                    // ip = InetAddress.getLocalHost();
                }
            catch(UnknownHostException e)
                {
                    System.out.println("\nHost IP not found");
                    System.exit(1);
                }

            join();
            Thread alive = new Thread()
                {
                    public void run()
                        {
                            try
                                {
                                    networkOutput = new PrintWriter(socket.getOutputStream(), true);
                                    while(true)
                                        {
                                            networkOutput.println("ALIVE");
                                            Thread.sleep(60000);
                                        }
                                }
                            catch (Exception e)
                                {
                                    e.printStackTrace();
                                }

                        }
                };
            alive.start();
            ChatReader messageListener = new ChatReader();
            messageListener.start();
            sendMessages();

        }

    public static void join()
        {
            socket = null;
            try
                {
                    socket = new Socket(ip, port);
                    networkInput = new Scanner(socket.getInputStream());
                    PrintWriter networkOutput = new PrintWriter(socket.getOutputStream(),true);
                    Scanner userInput = new Scanner(System.in);
                    //receive message from server
                    String receive = "";
                    int ok = 1;
                    while(ok==1)
                        {
                            ok = 0;
                            System.out.print("\nUsername: ");
                            username = userInput.next();
                            if(username.length()>11)
                                ok = 1;
                            for(Character c:username.toCharArray())
                                {
                                    if(c=='_'||c=='-'||(c>64&&c<91)||(c>96&&c<123)||(c>=48&&c<=57)) {
                    }
                                    else
                                        ok = 1;
                                }
                            if(ok == 0)
                                {
                                    //sent username to server
                                    networkOutput.println("JOIN "+ username +", "+ socket.getInetAddress()+":"+socket.getPort());
                                    //receive message from server
                                    receive = networkInput.nextLine();
                                    if(receive.equalsIgnoreCase("J_ERR"))
                                        {
                                            ok = 1;
                                        }
                                    System.out.println("\nServer Message-> "+receive);
                                }
                            else
                                {
                                    System.out.println("Incorrect Username");
                                }


                        }

                }
            catch(IOException e)
                {
                    e.printStackTrace();
                }

        }
    public static void sendMessages()
        {
            try
                {
                    //  Scanner networkInput = new Scanner(socket.getInputStream());
                    networkOutput = new PrintWriter(socket.getOutputStream(), true);

                    //set up stream for keyboard entry
                    Scanner userInput = new Scanner(System.in);
                    String message, response;
                    do
                        {
                            message = userInput.nextLine();
                            if(!message.equalsIgnoreCase("quit"))
                                {
                                    //send message to socket
                                    networkOutput.println("DATA "+username+": "+message);
                                }


                        }
                    while(!message.equalsIgnoreCase("quit"));

                }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        finally
            {
                try
                    {
                        System.out.println("\nClosing connection...");
                        networkOutput.println(username + "has disconnected from chat");
                        socket.close();
                    }
                catch (IOException e)
                    {
                        System.out.println("\nUnable to disconnect");
                        System.exit(1);
                    }
            }
        }
}
class ChatReader extends Thread{

    public void run()
        {
            while(true)
                {
                    if(Client_Side.getNetworkInput().hasNext())
                        {
                            System.out.println(Client_Side.getNetworkInput().nextLine());

                        }
                }
         }
}
