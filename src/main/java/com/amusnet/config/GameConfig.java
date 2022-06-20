package com.amusnet.config;

import com.amusnet.exception.ConfigurationInitializationException;
import com.amusnet.util.ErrorMessages;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class that loads XML configuration for the game.
 *
 * @since 1.0
 */
@Data
@NoArgsConstructor
@Slf4j
public class GameConfig {

    private int screenRowCount;
    private int screenColumnCount;

    private DecimalFormat currencyFormat = new DecimalFormat();

    private double startingBalance;
    private int lineCount;
    private double betLimit;

    private String exitCommand;

    private List<List<Integer>> reels;
    private List<List<Integer>> lines;

    private Set<Integer> scatters;

    private final MultipliersTable table = new MultipliersTable();

    /**
     * Initializes a GameConfig object via XML configuration file.
     * @param xmlConfig A Path object that points to the XML file containing the configuration properties.
     * @throws ParserConfigurationException If a DocumentBuilder cannot be created which satisfies the configuration requested.
     * @throws IOException If any IO errors occur.
     * @throws SAXException If any parse errors occur.
     */
    @SuppressWarnings("unused")
    public GameConfig(Path xmlConfig) throws ParserConfigurationException, IOException, SAXException, ConfigurationInitializationException {
        initialize(xmlConfig, null);
    }

    /**
     * Initializes a GameConfig object via XML configuration file.
     * @param xmlConfig A Path object that points to the XML file containing the configuration properties.
     * @param xsdValidation A Path object that points to the XSD file containing validation data for xmlConfig's file.
     * @throws ParserConfigurationException If a DocumentBuilder cannot be created which satisfies the configuration requested.
     * @throws IOException If any IO errors occur.
     * @throws SAXException If any parse errors occur.
     */
    @SuppressWarnings("unused")
    public GameConfig(Path xmlConfig, Path xsdValidation) throws ParserConfigurationException, IOException, SAXException, ConfigurationInitializationException {
        initialize(xmlConfig, xsdValidation);
    }

    private void initialize(Path xmlConfig, Path xsdValidation) throws ParserConfigurationException, SAXException, IOException, ConfigurationInitializationException {

        // DOM API
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document document = builder.parse(xmlConfig.toFile());

        // if .xsd validation file is provided
        if (xsdValidation != null) {
            Schema schema;
            try {
                String language = XMLConstants.W3C_XML_SCHEMA_NS_URI;
                SchemaFactory schemaFactory = SchemaFactory.newInstance(language);
                schema = schemaFactory.newSchema(xsdValidation.toFile());
            } catch (Exception e) {
                log.error("Schema validation error");
                throw new RuntimeException(e);
            }
            Validator validator = schema.newValidator();
            validator.validate(new DOMSource(document));
        }

        // for error tracking
        ErrorMessages errorMessages = ErrorMessages.getInstance();

        document.getDocumentElement().normalize();
        Element root = document.getDocumentElement();

        /*
        Column size, reel arrays and multipliers table are read first
        because dependencies exist between them and if one is violated,
        there's no need to continue the initialization.
         */

        // set column size
        {
            NodeList nlColumns = root.getElementsByTagName("columns");
            this.screenColumnCount = Integer.parseInt(nlColumns.item(0).getChildNodes().item(0).getNodeValue());
        }

        // set up reel arrays (size of columns)
        {
            NodeList nlReelArrays = root.getElementsByTagName("reelArray");
            // there should be an equal amount of reel arrays and screen columns
            if (nlReelArrays.getLength() != this.screenColumnCount) {
                log.error("Number of reel arrays are not the same as number of screen reels (screen columns):" +
                                System.lineSeparator() + "# reel arrays: {}{}# screen reels (screen columns): {}",
                        nlReelArrays.getLength(), System.lineSeparator(), this.screenColumnCount);
                throw new ConfigurationInitializationException(errorMessages.message(
                        "Reels discrepancy", "Number of reel arrays not equal to number of screen reels"
                ));
            }
            this.reels = new ArrayList<>();
            for (int i = 0; i < nlReelArrays.getLength(); i++) {
                String strReelArray = nlReelArrays.item(i).getChildNodes().item(0).getNodeValue();
                String[] reelArrayValues = strReelArray.split(",");
                List<Integer> reelList = new ArrayList<>();
                for (String v : reelArrayValues)
                    reelList.add(Integer.parseInt(v));
                this.reels.add(reelList);
            }
        }

        // set up multipliers table
        {
            NodeList nlCardColumn = root.getElementsByTagName("card");
            NodeList nlMultipliers = root.getElementsByTagName("multiplier");
            Map<String, Integer> finalOccurrenceCounts = new LinkedHashMap<>();
            Map<Integer, Map<Integer, Integer>> data = new LinkedHashMap<>();
            List<Integer> cards = new ArrayList<>();     // to keep track of the cards in the table
            int j = 0, multipliersPerCard = 0;
            for (int i = 0; i < nlCardColumn.getLength(); i++) {

                // fetch current card value ("face")
                Node card = nlCardColumn.item(i);
                String strFace = ((Element) card).getAttribute("face");
                int cardValue = Integer.parseInt(strFace);

                // table should not have duplicate cards
                if (!cards.contains(cardValue))
                    cards.add(cardValue);
                else {
                    log.error("Duplicate card {} in multipliers table", cardValue);
                    throw new ConfigurationInitializationException(errorMessages.message(
                            "Duplicate card(s)", "Duplicate card(s) found in multipliers table"
                    ));
                }

                // iterate through current card's multipliers
                Map<Integer, Integer> rightColumns = new LinkedHashMap<>();
                List<Integer> occurrenceCountTrack = new ArrayList<>();     // to keep track of potential duplicated or unwanted extras
                var multiplier = nlMultipliers.item(j);
                int multipliersForThisCard = 0;
                while (multiplier != null && multiplier.getParentNode().equals(card)) {

                    // fetch occurrence count for current multiplier
                    String strOccurrences = ((Element) multiplier).getAttribute("occurrences");
                    int occurrencesValue = Integer.parseInt(strOccurrences);

                    // make sure value isn't duplicated
                    if (occurrenceCountTrack.contains(occurrencesValue)) {
                        log.error("Card occurrence duplication. Duplicated value: {}", occurrencesValue);
                        throw new ConfigurationInitializationException(errorMessages.message(
                                "Card occurrence duplication", "Duplicated card occurrence value " +
                                        "in multipliers table"
                        ));
                    }
                    else
                        occurrenceCountTrack.add(occurrencesValue);

                    // put value in final map to be passed
                    finalOccurrenceCounts.put("x" + strOccurrences, occurrencesValue);

                    // fetch multiplication amount for current multiplier
                    String strAmount = ((Element) multiplier).getAttribute("amount");
                    int amountValue = Integer.parseInt(strAmount);
                    rightColumns.put(occurrencesValue, amountValue);

                    // check if there is a discrepancy with previous amounts of multipliers
                    if (++multipliersForThisCard > multipliersPerCard)
                        if (i > 0) {
                            log.error("Card multipliers until now were {} but encountered a card with {} multipliers",
                                    multipliersPerCard, multipliersForThisCard);
                            throw new ConfigurationInitializationException(errorMessages.message(
                                    "Card multipliers discrepancy", "Cards in multipliers table should " +
                                            "have consistent amount of multipliers"
                            ));
                        }
                        else
                            multipliersPerCard = multipliersForThisCard;

                    multiplier = nlMultipliers.item(++j);
                }

                // make sure occurrence counts (i.e. x3, x4, x5... etc.) are uniform across cards
                if (i > 0) {
                    if (!occurrenceCountTrack.equals(finalOccurrenceCounts.values().stream().toList())) {
//                        var previous = finalOccurrenceCounts.values().stream()
//                                .limit(finalOccurrenceCounts.values().size() - 1);
                        Deque<Integer> prev = new ArrayDeque<>(finalOccurrenceCounts.values());
                        var culprit = prev.pollLast();
                        log.error("Card occurrence counts until now were {} but encountered an extra one: {}",
                                prev, culprit);
                        throw new ConfigurationInitializationException(errorMessages.message(
                                "Card occurrences discrepancy", "Card occurrences in multipliers table " +
                                        "should have consistent amount of occurrence counts"
                        ));
                    }
                }
                data.put(cardValue, rightColumns);
            }

            // compare unique cards from reel arrays and those described in table - should be the same
            Set<Integer> playingCards = new HashSet<>();
            for (List<Integer> r : this.reels)
                playingCards.addAll(new HashSet<>(r));
            if (!data.keySet().containsAll(playingCards)) {
                var soreThumbs = playingCards.stream()
                        .filter(c -> !data.containsKey(c))
                        .collect(Collectors.toSet());
                log.error("Cards {} are missing from multipliers table", soreThumbs);
                throw new ConfigurationInitializationException(errorMessages.message(
                        "Missing cards in multipliers table", "Discrepancy between playing cards " +
                                "in reel arrays and those in multipliers table. Card(s) probably missing from table."
                ));
            }

            // all is good
            this.table.setOccurrenceCounts(finalOccurrenceCounts.values().stream().toList());
            this.table.setData(data);
        }

        // set row size
        {
            NodeList nlRows = root.getElementsByTagName("rows");
            this.screenRowCount = Integer.parseInt(nlRows.item(0).getChildNodes().item(0).getNodeValue());
        }

        // set currency format
        {
            NodeList nlCurrencyFormat = root.getElementsByTagName("currency");
            this.currencyFormat = new DecimalFormat();
            switch (((Element) nlCurrencyFormat.item(0)).getAttribute("format")) {
                case "normal" -> this.currencyFormat.applyPattern("#.##");
                case "round" -> this.currencyFormat.applyPattern("#");
                default -> {
                    log.error("Illegal value for property 'currency': should be either 'normal' or 'round'");
                    throw new ConfigurationInitializationException(errorMessages.message(
                            "Invalid Currency Format", "Unknown value for property 'currency': should be 'normal' or 'round'"
                    ));
                }
            }
        }

        // set starting balance
        {
            NodeList nlStartingBalance = root.getElementsByTagName("balance");
            this.startingBalance = Double.parseDouble(nlStartingBalance.item(0).getChildNodes().item(0).getNodeValue());
        }

        // set number of lines
        {
            NodeList nlLineArrays = root.getElementsByTagName("lineArray");
            this.lineCount = nlLineArrays.getLength();
        }

        // set bet limit
        {
            NodeList nlBetLimit = root.getElementsByTagName("betlimit");
            this.betLimit = Double.parseDouble(nlBetLimit.item(0).getChildNodes().item(0).getNodeValue());
        }

        // set exit command
        {
            NodeList nlExitCommand = root.getElementsByTagName("exit");
            this.exitCommand = nlExitCommand.item(0).getChildNodes().item(0).getNodeValue();
        }

        // set up line arrays
        {
            NodeList nlLineArrays = root.getElementsByTagName("lineArray");
            this.lines = new ArrayList<>();
            for (int i = 0; i < nlLineArrays.getLength(); i++) {
                String strLineArray = nlLineArrays.item(i).getChildNodes().item(0).getNodeValue();
                String[] lineValues = strLineArray.split(",");
                List<Integer> lineList = new ArrayList<>();
                for (String v : lineValues)
                    lineList.add(Integer.parseInt(v));
                this.lines.add(lineList);
            }
        }

        // set up scatter cards
        {
            NodeList nlScatterCards = root.getElementsByTagName("scatters");
            String strScatterValues = nlScatterCards.item(0).getChildNodes().item(0).getNodeValue();
            String[] scatterValues = strScatterValues.split(",");
            this.scatters = new LinkedHashSet<>();
            for (String v : scatterValues)
                this.scatters.add(Integer.parseInt(v));
        }

    }

    /**
     * A nested class within GameConfig which represents a table of multiplication values for
     * the number of occurrences for each card. Used to calculate the player win amounts.
     *
     * @since 1.0
     */
    @Data
    public static class MultipliersTable {

        private List<Integer> occurrenceCounts;  // should always be sorted, need order hence not a Set
        private Map<Integer, Map<Integer, Integer>> data;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append(String.format("%-10s", "card")).append(" ");

            for (var m : occurrenceCounts)
                sb.append(String.format("%5s", m));

            sb.append(System.lineSeparator());

            var keys = data.keySet();
            keys.forEach(key -> {
                sb.append(String.format("%-10s", key)).append(" ");
                var rightCells = data.get(key).values();
                rightCells.forEach(cell -> sb.append(String.format("%5.2s", cell)).append(" "));
                sb.append(System.lineSeparator());
            });
            return sb.toString();
        }

    }

    /**
     * Sets up the table containing multiplication values for the occurrences
     * of each card which in turn is used for the calculation of player win amounts.
     *
     * @param occurrenceCounts The columns headers representing the number of times a card is present on screen.
     * @param data The multiplication values for each card's amount of times present on screen.
     * @see MultipliersTable
     */
    public void setupTable(List<Integer> occurrenceCounts, Map<Integer, Map<Integer, Integer>> data) {
        table.occurrenceCounts = occurrenceCounts;
        table.data = data;
    }

    @Override
    public String toString() {
        
        final String nl = System.lineSeparator();
        
        StringBuilder sb = new StringBuilder(
                """
                        ----------------------
                        Current configuration
                        ----------------------
                        """
        );

        sb.append("Screen row count: ").append(screenRowCount).append(nl);
        sb.append("Screen column count: ").append(screenColumnCount).append(nl);

        sb.append("Currency format:\n");
        sb.append(currencyFormat.toPattern());

        sb.append("Starting balance: ").append(startingBalance).append(nl);
        sb.append("Line count: ").append(startingBalance).append(nl);
        sb.append("Bet limit: ").append(betLimit).append(nl);

        sb.append("Exit command: ").append(exitCommand).append(nl);

        sb.append("Reel arrays:").append(nl);
        reels.forEach(ra -> sb.append(ra).append(nl));

        sb.append("Line arrays:").append(nl);
        lines.forEach(la -> sb.append(la).append(nl));

        sb.append("Scatters:").append(nl);
        sb.append(scatters).append(nl);

        sb.append("Multipliers Table:").append(nl);
        sb.append(table);

        return sb.toString();
    }

}
