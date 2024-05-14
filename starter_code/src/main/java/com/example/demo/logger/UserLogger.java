package com.example.demo.logger;

import com.example.demo.enumerate.Status;
import com.example.demo.controllers.UserController;
import com.example.demo.exception.ValidationException;
import com.example.demo.model.requests.CreateUserRequest;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UserLogger {
    private final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final String csvFile = "USER_LOG.csv";

    public void writeLog(CreateUserRequest request, String message, Status status) throws ValidationException {
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
            String expectedHeader = "USERNAME,PASSWORD,CONFIRM_PASSWORD,MESSAGE,DATE_TIME,STATUS";
            String actualHeader = headerReader.readLine(); // Read the first line
            if (actualHeader == null || !actualHeader.equals(expectedHeader)) {
                csvPrinter.printRecord("USERNAME", "PASSWORD", "CONFIRM_PASSWORD", "MESSAGE", "DATE_TIME", "STATUS");
            }
            //append log
            LocalDateTime currentDateTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String requestTime = currentDateTime.format(formatter);
            csvPrinter.printRecord(request.getUsername(), request.getPassword(), request.getConfirmPassword(), message, requestTime, status);

            logger.warn(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
