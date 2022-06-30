package com.amusnet;

import com.amusnet.config.Jdbi;
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

        // TODO migrate to com.amusnet.game

        final Game game = new Game();
        final Logger log = LoggerFactory.getLogger(Application.class);

        log.debug(System.lineSeparator() + game.getConfiguration().toString());

        ErrorMessages errorMessages = ErrorMessages.getInstance();
        Jdbi j = Jdbi.getInstance();

        LocalDateTime now = LocalDateTime.now();
        String gameName = "game" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSS"));
        try (Handle handle = j.jdbi().open()) {
            handle.execute("CREATE TABLE " + gameName + " (" +
                    "  `turn` INT NOT NULL AUTO_INCREMENT," +
                    "  `lines_played` INT NOT NULL," +
                    "  `bet_amount` DECIMAL NOT NULL," +
                    "  `total_win` DECIMAL NOT NULL," +
                    "  PRIMARY KEY (`turn`)," +
                    "  UNIQUE INDEX `turn_UNIQUE` (`turn` ASC) VISIBLE); ");

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

            try (Handle handle = j.jdbi().open()) {
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
