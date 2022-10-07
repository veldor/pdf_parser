package net.veldor.pdf_parser.model.utils;

import net.veldor.pdf_parser.model.handler.TimeHandler;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Grammar {
    public static String normalizeDateForDb(String text) throws ParseException {
        String originalText = text;
        // произведу манипуляции, сначала обрежу текст
        text = text.trim();
        // теперь заменю всё, что не является цифрами на точки
        text = text.replaceAll("[^0-9]+", "-");
        // теперь, если поставлено несколько разделителей подряд, схлопну их
        text = text.replaceAll("-{2,}", "-");
        // разделители схлопнули, теперь надо проверить, что строка верно составлена
        String[] textArray = text.split("-");
        if (textArray.length == 3) {
            StringBuilder sb = new StringBuilder();
            // если первое или второе значение длиной в один символ-добавлю ведущий ноль
            if (textArray[2].length() < 4) {
                // год введён не полностью
                if (textArray[2].length() == 1) {
                    sb.append("200");
                    sb.append(textArray[2]);
                } else if (textArray[2].length() == 2) {
                    // если введено значение больше текущего года-добавлю 19, иначе- 20
                    if (TimeHandler.shotYearMoreThenCurrent(textArray[2])) {
                        sb.append("19");
                    } else {
                        sb.append("20");
                    }
                    sb.append(textArray[2]);
                } else if (textArray[2].length() == 3) {
                    if (Integer.parseInt(textArray[2]) < 100) {
                        sb.append("2");
                    } else {
                        sb.append("1");
                    }
                    sb.append(textArray[2]);
                }
            } else {
                sb.append(textArray[2]);
            }
            sb.append("-");
            if (textArray[1].length() == 1) {
                sb.append("0");
            }
            sb.append(textArray[1]);
            sb.append("-");
            if (textArray[0].length() == 1) {
                sb.append("0");
            }
            sb.append(textArray[0]);
            text = sb.toString();
            // фу, теперь главное-проверю текст на соответствие шаблону, если он в формате dd.mm.yyyy
            // - то мы почти обработали строку)))
            String patternString = "\\d{4}-\\d{2}-\\d{2}";
            Pattern pattern = Pattern.compile(patternString);
            Matcher matcher = pattern.matcher(text);
            if (matcher.matches() && TimeHandler.isValidDate(text)) {
                return text;
            }
        }
        throw new IllegalArgumentException("Неверно введена дата: " + originalText);
    }
}
