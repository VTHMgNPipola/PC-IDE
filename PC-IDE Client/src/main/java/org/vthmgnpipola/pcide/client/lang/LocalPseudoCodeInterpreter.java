package org.vthmgnpipola.pcide.client.lang;

import java.util.ArrayList;
import java.util.List;

public class LocalPseudoCodeInterpreter implements PseudoCodeInterpreter {
    private List<Language> languages;

    public LocalPseudoCodeInterpreter() {
        languages = new ArrayList<>();

        // Debug code
        Language language = new Language();
        language.setName("PseudoCode");
        language.setVersion("1.0.0");
        languages.add(language);
    }

    @Override
    public int interpret(String code) {
        return 0;
    }

    @Override
    public List<Language> getAvailableLanguages() {
        // TODO: Ask interpreter what are the languages instead of keeping a list
        return languages;
    }
}
