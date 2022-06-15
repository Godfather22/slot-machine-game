package com.amusnet.config;

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
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

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
     * @param xmlConfig A File object that points to the XML file containing the configuration properties.
     * @throws ParserConfigurationException If a DocumentBuilder cannot be created which satisfies the configuration requested.
     * @throws IOException If any IO errors occur.
     * @throws SAXException If any parse errors occur.
     */
    public GameConfig(File xmlConfig) throws ParserConfigurationException, IOException, SAXException {
        initialize(xmlConfig, null);
    }

    /**
     * Initializes a GameConfig object via XML configuration file.
     * @param xmlConfig A File object that points to the XML file containing the configuration properties.
     * @param xsdValidation A File object that points to the XSD file containing validation data for xmlConfig's file.
     * @throws ParserConfigurationException If a DocumentBuilder cannot be created which satisfies the configuration requested.
     * @throws IOException If any IO errors occur.
     * @throws SAXException If any parse errors occur.
     */
    public GameConfig(File xmlConfig, File xsdValidation) throws ParserConfigurationException, IOException, SAXException {
        initialize(xmlConfig, xsdValidation);
    }

    private void initialize(File xmlConfig, File xsdValidation) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document document = builder.parse(xmlConfig);

        if (xsdValidation != null) {
            Schema schema;
            try {
                String language = XMLConstants.W3C_XML_SCHEMA_NS_URI;
                SchemaFactory schemaFactory = SchemaFactory.newInstance(language);
                schema = schemaFactory.newSchema(xsdValidation);
            } catch (Exception e) {
                log.error("Schema validation error");
                throw new RuntimeException(e);
            }
            Validator validator = schema.newValidator();
            // TODO troubleshoot
            //validator.validate(new DOMSource(document));
        }

        document.getDocumentElement().normalize();
        Element root = document.getDocumentElement();

        NodeList nlRows = root.getElementsByTagName("rows");
        this.screenRowCount = Integer.parseInt(nlRows.item(0).getChildNodes().item(0).getNodeValue());
        NodeList nlColumns = root.getElementsByTagName("columns");
        this.screenColumnCount = Integer.parseInt(nlColumns.item(0).getChildNodes().item(0).getNodeValue());

        NodeList nlCurrencyFormat = root.getElementsByTagName("currency");
        this.currencyFormat = new DecimalFormat();
        switch (((Element)nlCurrencyFormat.item(0)).getAttribute("format")) {
            case "normal" -> this.currencyFormat.applyPattern("#.##");
            case "round" -> this.currencyFormat.applyPattern("#");
        }

        NodeList nlStartingBalance = root.getElementsByTagName("balance");
        this.startingBalance = Double.parseDouble(nlStartingBalance.item(0).getChildNodes().item(0).getNodeValue());

        NodeList nlLineArrays = root.getElementsByTagName("lineArray");
        this.lineCount = nlLineArrays.getLength();

        NodeList nlBetLimit = root.getElementsByTagName("betlimit");
        this.betLimit = Double.parseDouble(nlBetLimit.item(0).getChildNodes().item(0).getNodeValue());

        NodeList nlExitCommand = root.getElementsByTagName("exit");
        this.exitCommand = nlExitCommand.item(0).getChildNodes().item(0).getNodeValue();

        NodeList nlReelArrays = root.getElementsByTagName("reelArray");
        this.reels = new ArrayList<>();
        for (int i = 0; i < nlReelArrays.getLength(); i++) {
            String strReelArray = nlReelArrays.item(i).getChildNodes().item(0).getNodeValue();
            String[] reelArrayValues = strReelArray.split(",");
            List<Integer> reelList = new ArrayList<>();
            for (String v : reelArrayValues)
                reelList.add(Integer.parseInt(v));
            this.reels.add(reelList);
        }

        this.lines = new ArrayList<>();
        for (int i = 0; i < nlLineArrays.getLength(); i++) {
            String strLineArray = nlLineArrays.item(i).getChildNodes().item(0).getNodeValue();
            String[] lineValues = strLineArray.split(",");
            List<Integer> lineList = new ArrayList<>();
            for (String v : lineValues)
                lineList.add(Integer.parseInt(v));
            this.lines.add(lineList);
        }

        NodeList nlScatterCards = root.getElementsByTagName("scatters");
        String strScatterValues = nlScatterCards.item(0).getChildNodes().item(0).getNodeValue();
        String[] scatterValues = strScatterValues.split(",");
        this.scatters = new LinkedHashSet<>();
        for (String v : scatterValues)
            this.scatters.add(Integer.parseInt(v));

        NodeList nlCardColumn = root.getElementsByTagName("card");
        NodeList nlMultipliers = root.getElementsByTagName("multiplier");
        Map<String, Integer> occurrenceCounts = new LinkedHashMap<>();
        Map<Integer, Map<Integer, Integer>> data = new LinkedHashMap<>();
        int j = 0;
        for (int i = 0; i < nlCardColumn.getLength(); i++) {
            Node card = nlCardColumn.item(i);
            String strFace = ((Element)card).getAttribute("face");
            int cardValue = Integer.parseInt(strFace);

            Map<Integer, Integer> rightColumns = new LinkedHashMap<>();
            var multiplier = nlMultipliers.item(j);
            while (multiplier != null && multiplier.getParentNode().equals(card)) {
                String strOccurrences = ((Element)multiplier).getAttribute("occurrences");
                int occurrencesValue = Integer.parseInt(strOccurrences);
                occurrenceCounts.put(strOccurrences, occurrencesValue);

                String strAmount = ((Element)multiplier).getAttribute("amount");
                int amountValue = Integer.parseInt(strAmount);
                rightColumns.put(occurrencesValue, amountValue);

                multiplier = nlMultipliers.item(++j);
            }
            data.put(cardValue, rightColumns);
        }
        this.table.setOccurrenceCounts(occurrenceCounts.values().stream().toList());
        this.table.setData(data);
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

            sb.append("\n");

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

    // TODO some elements are missing
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Current configuration:\n\n");

        sb.append("Starting balance: ").append(startingBalance).append("\n");

        sb.append("Max Bet Amount: ").append(betLimit).append("\n");

        sb.append("Reel arrays:\n");
        reels.forEach(ra -> sb.append(ra).append("\n"));

        sb.append("Line arrays:\n");
        lines.forEach(la -> sb.append(la).append("\n"));

        sb.append("Multipliers Table:\n");
        sb.append(table);

        sb.append("Currency format:\n");
        sb.append(this.currencyFormat.toPattern());

        sb.append(System.lineSeparator());

        return sb.toString();
    }

}
