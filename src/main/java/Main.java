import app.AppFactory;
import bot.BotRunner;
import bot.ConsoleBot;
import bot.RateMessageHandler;
import bot.TelegramRunner;

public class Main {

    public static void main(String[] args) {
        String mode = args.length > 0 ? args[0].trim().toLowerCase() : "telegram";
        RateMessageHandler handler = AppFactory.createHandler();

        BotRunner runner;
        switch (mode) {
            case "console" -> runner = new ConsoleBot(handler);
            case "telegram" -> runner = new TelegramRunner(handler);
            default -> {
                System.err.println("Неизвестный режим: " + mode);
                System.err.println("Используй console или telegram");
                return;
            }
        }
        runner.run();
    }
}