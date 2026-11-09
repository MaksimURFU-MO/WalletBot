package bot;

import java.util.Scanner;

public class ConsoleBot {

    private final RateMessageHandler handler;

    public ConsoleBot(RateMessageHandler handler) {
        this.handler = handler;
    }

    public void run() {
        System.out.println("Консольный бот курсов валют.");
        System.out.println("Введи 3-буквенный код валюты: USD, EUR, CNY. Для выхода: exit");

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                if (!scanner.hasNextLine()) {
                    break;                                   // ввод закрыт
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