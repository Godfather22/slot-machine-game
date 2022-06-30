package com.amusnet;

import com.amusnet.config.DatabaseConnectionJdbi;
import com.amusnet.game.Game;
import com.amusnet.util.ErrorMessages;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

import static com.amusnet.util.ErrorMessages.DefaultMessageTitles.*;

public class Application {

    public static void main(String[] args) {

        final Game game = new Game();
        final Logger log = LoggerFactory.getLogger(Application.class);

        log.debug(System.lineSeparator() + game.getConfiguration().toString());

        ErrorMessages errorMessages = ErrorMessages.getInstance();
        DatabaseConnectionJdbi dbc = DatabaseConnectionJdbi.getInstance();

        LocalDateTime now = LocalDateTime.now();
        String gameName = "game" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSS"));
        try (Handle handle = dbc.jdbi().open()) {
            handle.execute("""
                    CREATE TABLE IF NOT EXISTS `games` (
                      `id` int NOT NULL AUTO_INCREMENT,
                      `started` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      `name` varchar(100) NOT NULL,
                      PRIMARY KEY (`id`),
                      UNIQUE KEY `id_UNIQUE` (`id`),
                      UNIQUE KEY `name_UNIQUE` (`name`)
                    )""");

            handle.execute("""
                    CREATE TABLE\040""" + gameName + """ 
                       (
                      `turn` int NOT NULL AUTO_INCREMENT,
                      `lines_played` int NOT NULL,
                      `bet_amount` decimal(10,0) NOT NULL,
                      `total_win` decimal(10,0) NOT NULL,
                      PRIMARY KEY (`turn`),
                      UNIQUE KEY `turn_UNIQUE` (`turn`)
                    )""");

            Update recordGame = handle.createUpdate("INSERT INTO `games` (started, name) VALUES (:timestamp, :name); ");
            recordGame.bind("timestamp", now).bind("name", gameName);
            recordGame.execute();
        }

        Scanner sc = new Scanner(System.in);
        final int maxLines = game.getConfiguration().getLineCount();
        final double betLimit = game.getConfiguration().getBetLimit();

        final String exitCommand = game.getConfiguration().getExitCommand();

        // main game loop
        while (game.getGameState().getCurrentBalance() >= 0.0) {

            game.prompt();

            String firstInput = sc.next();
            if (firstInput.equalsIgnoreCase(exitCommand))
                break;

            int linesInput = -1;
            double betInput = -1.0;

            // valid input checks
            {
                try {
                    linesInput = Integer.parseInt(firstInput);
                } catch (NumberFormatException e) {
                    System.err.println(errorMessages.message
                            (TITLE_EMSG_INVALID_LINES_INPUT, "Invalid input for number of lines played!"));
                    log.error("Invalid user input for number of lines played: {}", linesInput);
                    continue;
                }

                try {
                    betInput = Double.parseDouble(sc.next());
                } catch (NumberFormatException e) {
                    System.err.println(errorMessages.message
                            (TITLE_EMSG_INVALID_BET_INPUT, "Invalid input for bet amount!"));
                    log.error("Invalid user input for bet amount: {}", betInput);
                    continue;
                }
            }

            // bound checks
            {
                if (linesInput < 1 || linesInput > maxLines) {
                    System.err.println(errorMessages.message
                            (TITLE_EMSG_INCORRECT_LINES_INPUT, "Incorrect number of lines chosen!"));
                    log.error("Number of lines input {} out of bounds for available values: 1-{}",
                            linesInput, maxLines);
                    continue;
                }
                if (betInput < 1 || betInput > betLimit) {
                    System.err.println(errorMessages.message
                            (TITLE_EMSG_INCORRECT_BET_INPUT, "Incorrect bet amount placed!"));
                    log.error("Bet amount input {} out of bounds for available values: 1-{}",
                            betInput, betLimit);
                    continue;
                }
            }

            game.setupNextRound(linesInput, betInput);
            game.getGameState().subtractFromBalance(betInput * linesInput);

            // feedback
            System.out.printf("%s\t%s%nBalance: %s%n%n%s%n",
                    game.getGameRound().getLinesPlayed(),
                    game.getConfiguration().getCurrencyFormat().format(game.getGameRound().getBetAmount()),
                    game.getConfiguration().getCurrencyFormat().format(game.getGameState().getCurrentBalance()),
                    game.getGameRound().getReelScreen()
            );

            // side effect - prints necessary info to screen
            double totalWin = game.playNextRound();

            try (Handle handle = dbc.jdbi().open()) {
                Update update = handle.createUpdate("INSERT INTO " + gameName + " " +
                        "(`lines_played`, `bet_amount`, `total_win`)" +
                        "VALUES " +
                        "(:lines, :bet, :win); ");
                update.bind("lines", game.getGameRound().getLinesPlayed())
                        .bind("bet", game.getGameRound().getBetAmount())
                        .bind("win", totalWin);
                update.execute();
            }

            System.out.println();
        }
        System.out.println("game over");
    }

}
