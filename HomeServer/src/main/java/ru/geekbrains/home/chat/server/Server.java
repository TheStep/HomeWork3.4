package ru.geekbrains.home.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {
    private final Vector<ClientHandler> clients;

    public Server() {
        DbConnection DbConn = new DbConnection();
        DbConn.connect();
        clients = new Vector<>();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        try {
            ServerSocket serverSocket = new ServerSocket(8189);
            System.out.println("Сервер запущен ожидаем подключеник:  .. ");
            while (true) {
                Socket socket = serverSocket.accept(); //ждем подключения
                executorService.execute(new ClientHandler(this,socket));
                System.out.println("Новый клиент подключился ");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        DbConnection.disconnect();
    }

    public synchronized void subscribe(ClientHandler c) {
        clients.add(c); //добавление юзера
        broadCastMessage("Подключился новый юзер " + c.getUserName()); //оповещение входа юзера
        broadcastClientList();
    }

    public synchronized void unSubscribe(ClientHandler c) {
        clients.remove(c); //удаление юзера
        broadCastMessage(c.getUserName() + " отключился"); //оповещение выхода юзера
        broadcastClientList();
    }

    public synchronized void broadCastMessage(String message) { //отправка сообщений всем юзерам
        for (ClientHandler c : clients) {
            c.sendMessage(message);
        }
    }

    public synchronized void broadcastClientList() { //список юзеров
        StringBuilder builder = new StringBuilder();
        builder.append("/clients_list ");
        for (ClientHandler c : clients) {
            builder.append(c.getUserName()).append(" ");
        }
        String clientsListStr = builder.toString();
        broadCastMessage(clientsListStr);
    }

    public synchronized boolean isUsernameUsed(String username) {
        for (ClientHandler c : clients) {
            if (c.getUserName().equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void sendPersonalMessage(ClientHandler sender, String receiverUsername, String message) { // отправка личных сообщений
        if (sender.getUserName().equalsIgnoreCase(receiverUsername)) {
            sender.sendMessage("Нельзя отправлять личные сообщения самому себе ");
            return;
        }
        for (ClientHandler c : clients) {
            if (c.getUserName().equalsIgnoreCase(receiverUsername)) {
                c.sendMessage("от " + sender.getUserName() + ": " + message);
                sender.sendMessage("пользователю " + receiverUsername + ": " + message);
                return;
            }
        }
        sender.sendMessage("Пользователь " + receiverUsername + " не в сети.");
    }
}
