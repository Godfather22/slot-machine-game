package com.amusnet.config;

import com.amusnet.game.Card;
import com.amusnet.game.impl.NumberCard;
import lombok.Data;
import lombok.NoArgsConstructor;
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

@Data
@NoArgsConstructor
@Slf4j
public class GameConfig {

    private int screenRowCount;
    private int screenColumnCount;

    private DecimalFormat currencyFormat = new DecimalFormat();

    private double startingBalance;
    private int maxLines;
    private double betLimit;

    private String exitCommand;

    private List<List<Integer>> reels;
    private List<List<Integer>> lines;

    private Set<Card> scatters;

    private MultipliersTable table = new MultipliersTable();

    public GameConfig(File xmlConfig, File xsdValidation) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document document = builder.parse(xmlConfig);

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

        document.getDocumentElement().normalize();
        Element root = document.getDocumentElement();

        NodeList xmlProperties = root.getChildNodes();

        NodeList nlRows = root.getElementsByTagName("rows");
        this.screenRowCount = Integer.parseInt(nlRows.item(0).getChildNodes().item(0).getNodeValue());
        NodeList nlColumns = root.getElementsByTagName("columns");
        this.screenColumnCount = Integer.parseInt(nlColumns.item(0).getChildNodes().item(0).getNodeValue());

        NodeList nlCurrencyFormat = root.getElementsByTagName("currency");
        this.currencyFormat = new DecimalFormat();
        switch (((Element)nlCurrencyFormat.item(0)).getAttribute("format")) {
            case "normal" -> {
                this.currencyFormat.applyPattern("#.##");
            }
            case "round" -> {
                this.currencyFormat.applyPattern("#");
            }
        }

        NodeList nlStartingBalance = root.getElementsByTagName("balance");
        this.startingBalance = Double.parseDouble(nlStartingBalance.item(0).getChildNodes().item(0).getNodeValue());

        NodeList nlLineArrays = root.getElementsByTagName("lineArray");
        String strLineArray = nlLineArrays.item(0).getChildNodes().item(0).getNodeValue();
        String[] lineArrayValues = strLineArray.split(",");
        this.maxLines = nlLineArrays.getLength();

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
            strLineArray = nlLineArrays.item(i).getChildNodes().item(0).getNodeValue();
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
            this.scatters.add(new NumberCard<>(Integer.parseInt(v), true));

        NodeList nlCardColumn = root.getElementsByTagName("card");
        Map<String, Integer> occurrenceCounts = new LinkedHashMap<>();
        Map<NumberCard<Integer>, Map<Integer, Integer>> data = new LinkedHashMap<>();
        for (int i = 0; i < nlCardColumn.getLength(); i++) {
            Node card = nlCardColumn.item(i);
            String strFace = ((Element)card).getAttribute("face");
            int cardValue = Integer.parseInt(strFace);

            Map<Integer, Integer> rightColumns = new LinkedHashMap<>();
            NodeList nlMultipliers = root.getElementsByTagName("multiplier");
            for (int j = 0; j < nlMultipliers.getLength(); j++) {
                Element multiplier = (Element) nlMultipliers.item(j);

                String strOccurrences = multiplier.getAttribute("occurrences");
                int occurrencesValue = Integer.parseInt(strOccurrences);
                occurrenceCounts.put(strOccurrences, occurrencesValue);

                String strAmount = multiplier.getAttribute("amount");
                int amountValue = Integer.parseInt(strAmount);
                rightColumns.put(occurrencesValue, amountValue);
            }
            data.put(new NumberCard<>(cardValue), rightColumns);
        }
        this.table.setOccurrenceCounts(occurrenceCounts.values().stream().toList());
        this.table.setData(data);
    }

    @Data
    public static class MultipliersTable {

        private List<Integer> occurrenceCounts;  // should always be sorted, need order hence not a Set
        private Map<NumberCard<Integer>, Map<Integer, Integer>> data;

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

    public void setupTable(List<Integer> occurrenceCounts, Map<NumberCard<Integer>, Map<Integer, Integer>> data) {
        table.occurrenceCounts = occurrenceCounts;
        table.data = data;
    }

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
