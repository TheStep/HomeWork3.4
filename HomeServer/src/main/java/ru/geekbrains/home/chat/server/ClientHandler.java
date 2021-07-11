package ru.geekbrains.home.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


public class ClientHandler implements Runnable {
    private Server server;
    private Socket socket;
    private String userName;
    private DataInputStream in;
    private DataOutputStream out;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream()); //входящее
            this.out = new DataOutputStream(socket.getOutputStream()); //выходящее
            socket.setSoTimeout(10000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        try {
            while (!consumeAuthorizeMessage(in.readUTF())) ;
            socket.setSoTimeout(0);
            while (consumeRegularMessage(in.readUTF())) ;  //отключаем возможность вызова команд
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Юзер " + userName + " отключился.");
            server.unSubscribe(this);
            closeConnection();
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message); //отправка сообщений
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean consumeRegularMessage(String inputMessage) {
        if (inputMessage.startsWith("/")) {
            if (inputMessage.equals("/exit")) {
                sendMessage("/exit");
                return false;
            }
            if (inputMessage.startsWith("/cn")) {
                String[] tokens = inputMessage.split("\\s+", 2);
                DbConnection.changeNickName(tokens[1], tokens[1]);
                sendMessage("/changeNickName");
                sendMessage("Ваш ник изменен на " + tokens[1]);
                this.userName = tokens[1];
                server.broadcastClientList();

//                server.unSubscribe(ClientHandler.this);
//                userName = tokens[1];
//                server.broadCastMessage("/TextNickName");
//                server.subscribe(ClientHandler.this);
            }
            if (inputMessage.startsWith("/w ")) {
                String[] tokens = inputMessage.split("\\s+", 3);
                server.sendPersonalMessage(this, tokens[1], tokens[2]);
            }
            return true;
        }
        server.broadCastMessage(userName + ": " + inputMessage); //отображение сообщения всем юзерам
        return true;
    }
    private boolean consumeAuthorizeMessage(String message) { // Цикл авторизации
        if (message.startsWith("/auth ")) {
            String[] tokens = message.split("\\s+");//определение никнейма при авторизации
            if (tokens.length == 1) {
                sendMessage("Server : Не указано имя пользователя и пароль.");
                return false;
            } else if (tokens.length == 2) {
                sendMessage("Server : Не указано имя пользователя или пароль.");
                return false;
            } else if (tokens.length > 3) {
                sendMessage("Server : Имя пользователя или пароль не может состоять из нескольких слов");
                return false;
            }
            String selectedUsername = tokens[1];
            String selectedPassword = tokens[2];
            if (server.isUsernameUsed(DbConnection.selectedUsername(selectedUsername, selectedPassword))) {
                sendMessage("Server : Данное имя уже используется.");
                return false;
            }

            userName = DbConnection.selectedUsername(selectedUsername, selectedPassword);
            if (userName == null) {
                sendMessage("Server : что то пошло не так");
                return false;
            } else {
                sendMessage("/authok "); //подверждение авторизации
                sendMessage("/TextNickName " + userName);
                server.subscribe(this); //добавление в список подписчиков
                return true;
            }
        } else {
            sendMessage("Server : Необходима авторизация");
            return false;
        }
    }

    public String getUserName() { //геттер для вызова имени в сервере
        return userName;
    }

    private void closeConnection() {
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
