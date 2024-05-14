package com.example.demo.logger;

import com.example.demo.controllers.OrderController;
import com.example.demo.exception.ValidationException;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.UserOrder;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OrderDetailLogger {
    private final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private final String csvFile = "ORDER_DETAIL_LOG.csv";

    public void writeLog(UserOrder order, Cart cart) throws ValidationException {
        //check if file existed
        Path path = Paths.get(csvFile);
        if (!Files.exists(path)) {
            try (Writer fileWriter = new FileWriter(csvFile);
                 CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT)) {
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try (Reader fileReader = new FileReader(csvFile);
             CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT);
             Writer fileWriter = new FileWriter(csvFile, true);
             CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT)) {
            //check if .csv file has defined headers
            BufferedReader headerReader = new BufferedReader(new FileReader(csvFile));
            String expectedHeader = "ORDER_ID,USERNAME,ITEM";
            String actualHeader = headerReader.readLine(); // Read the first line
            if (actualHeader == null || !actualHeader.equals(expectedHeader)) {
                csvPrinter.printRecord("ORDER_ID", "USERNAME", "ITEM");
            }
            //append log
            cart.getItems().stream().forEach(item -> {
                try {
                    csvPrinter.printRecord(order.getId(), order.getUser().getUsername(), item.getName());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
