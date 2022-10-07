package net.veldor.pdf_parser;

import net.veldor.pdf_parser.model.exception.ArgumentNotFoundException;
import net.veldor.pdf_parser.model.handler.Handler;
import net.veldor.pdf_parser.model.selection.Conclusion;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

public class Main {
    public static void main(String[] args) throws ParseException {
        LogManager.getRootLogger().atLevel(Level.OFF);
        if (args.length == 1) {
            File file = new File(args[0]);
            if (file.exists()) {
                try {
                    Conclusion conclusion = (new Handler(file)).parse();
                    System.out.println(conclusion.patientName);
                    System.out.println(conclusion.patientSex);
                    System.out.println(conclusion.patientBirthdate);
                    System.out.println(conclusion.executionArea);
                    System.out.println(conclusion.contrastInfo);
                    System.out.println(conclusion.executionNumber);
                    System.out.println(conclusion.executionDate);
                    System.out.println(conclusion.diagnostician);
                } catch (IOException e) {
                    System.out.println("Can't handle file, it has error: " + e.getMessage());
                } catch (ArgumentNotFoundException e) {
                    throw new RuntimeException(e);
                }
            } else {
                System.out.println("File not found");
            }
        } else {
            System.out.println("required path to file");
        }
    }
}
