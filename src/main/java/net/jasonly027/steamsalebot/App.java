package net.jasonly027.steamsalebot;

import io.github.cdimascio.dotenv.Dotenv;

public class App {
    public static final Dotenv config = Dotenv.configure().load();

    public static void main(String[] args) {
        SteamSaleBot.getInstance();
    }
}