package com.test.wordcounter.translator;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

/*
 * Test implementation of Translator. This should return String which was passed.
 * 
 */
@Component
public class TranslatorImpl implements Translator {

	private final Map<String, String> meaningMap = new HashMap<>();
	
	public TranslatorImpl() {
		meaningMap.put("flor", "flower");
		meaningMap.put("blume", "flower");
	}
	
	@Override
	public String translate(String word) {
		String meaning = meaningMap.get(word);
		return  meaning == null ? word : meaning;
	}

}
