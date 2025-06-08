package org.example.service;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Collator;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordReferenceUpdater {

    private static final String PATH_TO_INPUT_FILES = "src/main/resources/input/";
    private static final String PATH_TO_OUTPUT_FILES = "src/main/resources/output/";
    private static final String WORD_FILE_FORMAT = ".docx";
    private String pathToInputFiles = "src/main/resources/input/";
    private String pathToOutputFiles = "src/main/resources/output/";

    private final String nameScientificWork;
    private final String titleNameBibliography;

    private List<String> bibliography;
    private Map<Integer, Integer> referenceNumbersMapping;

    public WordReferenceUpdater(String nameScientificWork, String titleNameBibliography) {
        bibliography = new ArrayList<>();
        referenceNumbersMapping = new HashMap<>();

        this.nameScientificWork = nameScientificWork;
        this.titleNameBibliography = titleNameBibliography;
    }

    public void setInputPath(String path) {
        this.pathToInputFiles = path.endsWith("/") ? path : path + "/";
    }

    public void setOutputPath(String path) {
        this.pathToOutputFiles = path.endsWith("/") ? path : path + "/";
    }

    /**
     * Обновляет список литературы в документе.
     */
    private static void updateBibliography(XWPFDocument document,
                                           List<String> sortedBibliography, String header) {
        boolean isBibliographySection = false;
        int bibliographyStartIndex = -1;

        for (int i = 0; i < document.getParagraphs().size(); i++) {
            XWPFParagraph paragraph = document.getParagraphs().get(i);
            if (paragraph.getText().trim().equalsIgnoreCase(header)) {
                isBibliographySection = true;
                bibliographyStartIndex = i + 1;
                break;
            }
        }

        if (isBibliographySection) {
            for (int i = 0; i < sortedBibliography.size(); i++) {
                XWPFParagraph paragraph = document.getParagraphs().get(bibliographyStartIndex + i);
                replaceParagraphText(paragraph, sortedBibliography.get(i));
            }
        }
    }

    /**
     * Полностью заменяет текст в параграфе, сохраняя форматирование.
     */
    private static void replaceParagraphText(XWPFParagraph paragraph, String newText) {
        if (paragraph.getRuns().isEmpty()) {
            return;
        }
        // Сохраняем стили из первого run (предполагаем, что стили одинаковы для всех runs)
        XWPFRun firstRun = paragraph.getRuns().get(0);
        String fontFamily = firstRun.getFontFamily();
        int fontSize = firstRun.getFontSize();
        boolean isBold = firstRun.isBold();
        boolean isItalic = firstRun.isItalic();
        String color = firstRun.getColor();

        // Удаляем все runs из параграфа
        for (int i = paragraph.getRuns().size() - 1; i >= 0; i--) {
            paragraph.removeRun(i);
        }

        // Добавляем новый текст как единый run
        XWPFRun newRun = paragraph.createRun();
        newRun.setText(newText);

        // Применяем сохранённые стили
        if (fontFamily != null) {
            newRun.setFontFamily(fontFamily);
        }
        if (fontSize != -1) {
            newRun.setFontSize(fontSize);
        }
        newRun.setBold(isBold);
        newRun.setItalic(isItalic);
        if (color != null) {
            newRun.setColor(color);
        }
    }

    public void sortReferences() {
        String pathToScientificWork = PATH_TO_INPUT_FILES + nameScientificWork + WORD_FILE_FORMAT;
        String pathToUpdatedScientificWork = PATH_TO_OUTPUT_FILES + "new" + nameScientificWork +
                WORD_FILE_FORMAT;

        try (FileInputStream fis = new FileInputStream(pathToScientificWork);
             XWPFDocument document = new XWPFDocument(fis);
             FileOutputStream fos = new FileOutputStream(pathToUpdatedScientificWork)
        ) {
            // Ищем список литературы
            bibliography = findBibliography(document);

            // Сортируем список литературы по алфавиту
            bibliography = sortBibliography(bibliography);

            // Обновляем ссылки в тексте
            updateReferences(document);

            // Обновляем список литературы в документе
            updateBibliography(document, bibliography, titleNameBibliography);

            document.write(fos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Ищет список литературы по заголовку.
     */
    private List<String> findBibliography(XWPFDocument document) {
        List<String> bibliography = new ArrayList<>();
        List<XWPFParagraph> paragraphs = document.getParagraphs();

        int bibliographyPosition = -1;
        for (int i = 0; i < paragraphs.size(); ++i) {
            XWPFParagraph paragraph = paragraphs.get(i);
            String text = paragraph.getText().trim();

            if (text.equalsIgnoreCase(titleNameBibliography)) {
                bibliographyPosition = i + 1;
                break;
            }
        }

        for (int i = bibliographyPosition; i < paragraphs.size(); ++i) {
            XWPFParagraph paragraph = paragraphs.get(i);
            String text = paragraph.getText().trim();

            if (text.isEmpty()) {
                break;
            }
            bibliography.add(text);
        }

        return bibliography;
    }

    /**
     * Сортирует список литературы и обновляет индексы ссылок.
     */
    private List<String> sortBibliography(List<String> bibliography) {
        Pattern numPattern = Pattern.compile("^\\d+\\.\\s*");
        List<String> cleanedEntries = new ArrayList<>();

        for (String entry : bibliography) {
            Matcher matcher = numPattern.matcher(entry);
            cleanedEntries.add(matcher.replaceFirst("").trim());
        }

        Collator collator = Collator.getInstance(new Locale("ru", "RU"));
        collator.setStrength(Collator.PRIMARY);
        cleanedEntries.sort(collator);

        referenceNumbersMapping = new HashMap<>();
        for (int i = 0; i < bibliography.size(); i++) {
            int newIndex = cleanedEntries
                    .indexOf(numPattern.matcher(bibliography.get(i))
                            .replaceFirst("").trim()) + 1;
            referenceNumbersMapping.put(i + 1, newIndex);
        }

        List<String> sortedBibliography = new ArrayList<>();
        for (int i = 0; i < cleanedEntries.size(); i++) {
            sortedBibliography.add((i + 1) + ". " + cleanedEntries.get(i));
        }

        return sortedBibliography;
    }

    /**
     * Обновляет ссылки в тексте.
     */
    private void updateReferences(XWPFDocument document) {
        Pattern referencePattern = Pattern.compile("\\[(\\d+)\\]");

        for (XWPFParagraph paragraph : document.getParagraphs()) {
            String text = paragraph.getText();
            Matcher matcher = referencePattern.matcher(text);

            StringBuilder updatedText = new StringBuilder(text);
            int offset = 0;

            while (matcher.find()) {
                int start = matcher.start() + offset;
                int end = matcher.end() + offset;

                int referenceNumber = Integer.parseInt(matcher.group(1));
                int newReferenceNumber = referenceNumbersMapping.get(referenceNumber);

                String newReference = "[" + newReferenceNumber + "]";
                updatedText.replace(start, end, newReference);

                offset += newReference.length() - (end - start);
            }

            replaceParagraphText(paragraph, updatedText.toString());
        }
    }
}
