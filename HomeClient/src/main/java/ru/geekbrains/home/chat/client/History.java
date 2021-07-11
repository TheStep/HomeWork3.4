package ru.geekbrains.home.chat.client;

import javafx.scene.control.TextArea;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class History {
    private static final File history = new File("history.txt");
    private static final int lineMax = 100;

    public static void saveHistory(String message) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(history, true));
            if (!message.startsWith("/")) {
                writer.write(message + "\n");
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadHistory(TextArea textArea) {
        List<String> historyList = new ArrayList<>();
        try {
            BufferedReader read = new BufferedReader(new FileReader(history));
            for (String temp; (temp = read.readLine()) != null; ) {
                historyList.add(temp);
                if (historyList.size() > lineMax) {
                    historyList.remove(0);
                }
            }
            for (String s : historyList) {
                textArea.appendText(s + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        textArea.appendText(" История переписки *чата* \n");
    }
}
