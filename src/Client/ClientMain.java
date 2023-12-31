package Client;

import Command.CollectionManager.CollectionManager;

import Command.CommandList.Exit.Exit;

import Command.CommandList.Insert.Insert;

import Command.CommandProcessor.Command;
import Exceptions.IllegalKeyException;
import Exceptions.IllegalValueException;
import Exceptions.NoSuchCommandException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class ClientMain {
    private Map<String, Command> commands;


    private DatagramSocket datagramSocket;
    private InetAddress inetAddress;
    private byte[] buffer;

    public ClientMain(DatagramSocket datagramSocket, InetAddress inetAddress) {
        this.datagramSocket = datagramSocket;
        this.inetAddress = inetAddress;
    }


    private void execute() throws NoSuchCommandException, IllegalValueException, IllegalKeyException, IOException {

        commands = new TreeMap<>();
        Command cmd = new Exit();
        commands.put(cmd.getName(), cmd);
        cmd = new Insert();
        commands.put(cmd.getName(), cmd);



        boolean result = true;

        Scanner scan = new Scanner(System.in);
        while (result) {
            String commandWithOutArgs = "";
            String args = "";
            Command command;
            try {
                String[] commandLine = scan.nextLine().split(" ");
                commandWithOutArgs = commandLine[0];
                args = "";
                if (commandLine.length > 1) {
                    args = commandLine[1];
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            if ((commands.get(commandWithOutArgs.toUpperCase()) == null) | (commandWithOutArgs.equals(""))) {
                throw new NoSuchCommandException();
            }else {
                command =commands.get(commandWithOutArgs.toUpperCase());
            }
            command.setArgument(args);


            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(command);
            buffer = bos.toByteArray();
            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length,inetAddress,1234);
            datagramSocket.send(datagramPacket);
            datagramSocket.receive(datagramPacket);
            result = true;
            String message = new String(datagramPacket.getData(),0,datagramPacket.getLength());
            System.out.println(message);
        }
    }

    public static void main(String[] args) throws IOException, NoSuchCommandException, IllegalValueException, IllegalKeyException {
        DatagramSocket datagramSocket = new DatagramSocket();
        InetAddress inetAddress = InetAddress.getByName("localhost");
        ClientMain client = new ClientMain(datagramSocket,inetAddress);
        System.out.println("Я клиент");
        client.execute();
    }
}