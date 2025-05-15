package org.example;

import org.example.service.WordReferenceUpdater;

import java.io.IOException;

public class Main {
    private static final String TITLE_NAME_BIBLIOGRAPHY = "СПИСОК ИСПОЛЬЗОВАННЫХ ИСТОЧНИКОВ";
    private static final String NAME_SCIENTIFIC_WORK = "Литература";

    public static void main(String[] args) throws IOException {
        WordReferenceUpdater wordReferenceUpdater = new WordReferenceUpdater(NAME_SCIENTIFIC_WORK, TITLE_NAME_BIBLIOGRAPHY);
        wordReferenceUpdater.sortReferences();
    }
}