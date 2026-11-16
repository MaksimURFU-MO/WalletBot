package bot;

import java.util.Scanner;

public class ConsoleBot implements BotRunner {

    private final RateMessageHandler handler;

    public ConsoleBot(RateMessageHandler handler) {
        this.handler = handler;
    }

    @Override
    public void run() {
        System.out.println("Консольный бот курсов валют.");
        System.out.println("Введи код валюты (например, USD) или конвертацию (например, 100 USD RUB). Для выхода: exit");

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                if (!scanner.hasNextLine()) {
                    break;
                }
                String line = scanner.nextLine().trim();

                if (line.equalsIgnoreCase("exit")) {
                    System.out.println("Пока!");
                    break;
                }
                System.out.println(handler.handle(line));
            }
        }
    }
}