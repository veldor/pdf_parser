package net.veldor.pdf_parser.model.handler;

import net.veldor.pdf_parser.model.exception.ArgumentNotFoundException;
import net.veldor.pdf_parser.model.selection.Conclusion;
import net.veldor.pdf_parser.model.utils.Checksum;
import net.veldor.pdf_parser.model.utils.Grammar;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Handler {
    private final File file;
    private String[] stringsArray;
    private String[] stringsReversedArray;

    public Handler(File file) {
        this.file = file;
    }

    public Conclusion parse() throws IOException, ArgumentNotFoundException, ParseException {
        Conclusion conclusion = new Conclusion();
        conclusion.file = file;
        try (PDDocument document = PDDocument.load(file)) {
            if (!document.isEncrypted()) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);
                document.close();
                stringsArray = text.split("\n");
                stringsReversedArray = reverseArray(stringsArray);
                conclusion.conclusionText = text;
                conclusion.patientName = getPatientName();
                conclusion.patientBirthdate = Grammar.normalizeDateForDb(getPatientBirthdate());
                conclusion.executionArea = getExecutionArea();
                conclusion.executionNumber = getExecutionNumber();
                conclusion.executionDate = Grammar.normalizeDateForDb(getExecutionDate());
                // теперь-необязательные параметры
                conclusion.patientSex = getPatientSex();
                conclusion.contrastInfo = getContrastInfo();
                conclusion.diagnostician = getDoctorName();
                conclusion.hash = Checksum.checksum(file);
                return conclusion;
            } else {
                System.out.println("Document encrypted");
            }
        }
        return null;
    }

    private String getContrastInfo() {
        for (String s : stringsArray) {
            if (s.length() != 0) {
                if (s.trim().toLowerCase().startsWith("в/в контрастное усиление")) {
                    return getStringFrom(s, 25);
                } else if (s.trim().toLowerCase().startsWith("в/в контрастирование")) {
                    return getStringFrom(s, 21);
                } else if (s.trim().toLowerCase().startsWith("в/венное контрастное усиление")) {
                    return getStringFrom(s, 30);
                } else if (s.trim().toLowerCase().startsWith("в/в динамическое контрастное усиление")) {
                    return getStringFrom(s, 38);
                } else if (s.trim().toLowerCase().startsWith("контрастирование")) {
                    return getStringFrom(s, 17);
                }
            }
        }
        return null;
    }

    private String getPatientSex() {
        for (String s : stringsArray) {
            if (s.length() != 0) {
                if (s.trim().startsWith("Пол:")) {
                    String sexString = getStringFrom(s, 4).toLowerCase();
                    if (sexString.contains("м")) {
                        return "м";
                    } else if (sexString.contains("ж")) {
                        return "ж";
                    }
                }
            }
        }
        return "-";
    }

    private String getExecutionDate() throws ArgumentNotFoundException {
        Pattern sSignPattern = Pattern.compile(
                "^(Дата исследования:)?\\s*(\\d{1,2}[/|.]\\d{1,2}[/|.]\\d{2,4}).*",
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        for (String s : stringsReversedArray) {
            if (s.length() != 0) {
                Matcher matcher = sSignPattern.matcher(s.trim());
                if (matcher.find()) {
                    // первая найденная группа-дата, тут всё просто, она уже отформатирована
                    return matcher.group(2);
                }
            }
        }
        throw new ArgumentNotFoundException("Execution date not found");
    }

    private String getExecutionNumber() throws ArgumentNotFoundException {
        for (String s : stringsArray) {
            if (s.length() != 0) {
                if (s.trim().startsWith("Номер исследования:")) {
                    return superTrim(getStringFrom(s, 19).toUpperCase().replace("А", "A").replace("Т", "T"));
                }

                if (s.trim().startsWith("ID исследования:")) {
                    return superTrim(getStringFrom(s, 17).toUpperCase().replace("А", "A").replace("Т", "T"));
                }

                if (s.trim().startsWith("Patient ID")) {
                    return superTrim(getStringFrom(s, 11).toUpperCase().replace("А", "A").replace("Т", "T"));
                }
            }
        }
        throw new ArgumentNotFoundException("Execution number not found");
    }

    private String getExecutionArea() throws ArgumentNotFoundException {
        for (String s : stringsArray) {
            if (s.length() != 0 && s.trim().startsWith("Область исследования:")) {
                return getStringFrom(s, 21);
            }
        }
        throw new ArgumentNotFoundException("Patient birthdate not found");
    }

    private String getDoctorName() {
        Pattern sSignPattern = Pattern.compile("^\\s*(\\d{1,2}[/|.]\\d{1,2}[/|.]\\d{2,4}).*(врач)?.+\\s([а-я]\\.)*\\s*([а-я]{5,})", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        Pattern sSignPattern1 = Pattern.compile("^\\s*(врач)?.+\\s([а-я]\\.)*\\s*([а-я]{5,})", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        for (String s : stringsReversedArray) {
            if (s.length() != 0) {
                if (s.startsWith("Данное заключение не является диагнозом")) {
                    continue;
                }
                Matcher matcher = sSignPattern.matcher(s.trim());
                if (matcher.find()) {
                    if (!matcher.group(4).equals("Врач")) {
                        return matcher.group(4);
                    }
                }
                Matcher matcher1 = sSignPattern1.matcher(s.trim());
                if (matcher1.find()) {
                    if (!matcher1.group(3).equals("Врач")) {
                        return matcher1.group(3);
                    }
                }
            }
        }
        return null;
    }

    private String getPatientBirthdate() throws ArgumentNotFoundException {
        for (String s : stringsArray) {
            if (s.length() != 0) {
                if (s.trim().startsWith("Дата рождения:")) {
                    return getStringFrom(s, 14);
                } else if (s.trim().startsWith("Год рождения:")) {
                    return getStringFrom(s, 13);
                } else if (s.trim().startsWith("Дата рождения (возраст):")) {
                    return getStringFrom(s, 24);
                }
            }
        }
        throw new ArgumentNotFoundException("Patient birthdate not found");
    }

    private String getPatientName() throws ArgumentNotFoundException {
        for (String s : stringsArray) {
            if (s.length() != 0) {
                if (s.trim().startsWith("Фамилия, имя, отчество:")) {
                    return getStringFrom(s, 23);
                } else if (s.trim().startsWith("Ф.И.О. пациента:")) {
                    return getStringFrom(s, 16);
                } else if (s.trim().startsWith("ФИО пациента:")) {
                    return getStringFrom(s, 13);
                } else if (s.trim().startsWith("Фамилия, имя отчество:")) {
                    return getStringFrom(s, 22);
                }
            }
        }
        throw new ArgumentNotFoundException("Patient name not found");
    }


    private String[] reverseArray(String[] arr) {
        // Converting Array to List
        List<String> list = Arrays.asList(arr);
        // Reversing the list using Collections.reverse() method
        Collections.reverse(list);
        // Converting list back to Array
        return list.toArray(arr);
    }

    private String getStringFrom(String s, int prefixLength) {
        return s.substring(prefixLength).trim();
    }

    private String superTrim(String s) {
        return s.replaceAll("\\s", "");
    }
}
