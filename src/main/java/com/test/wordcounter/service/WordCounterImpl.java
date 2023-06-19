package com.test.wordcounter.service;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.test.wordcounter.translator.Translator;

@Service
public class WordCounterImpl implements WordCounter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(WordCounterImpl.class);

	private final ConcurrentHashMap<String, AtomicLong> wordCountMap = new ConcurrentHashMap<String, AtomicLong>();
	private final ConcurrentHashMap<String, String> transalationMap = new ConcurrentHashMap<String, String>();
	
	@Autowired
	private final Translator translator;
	
	public WordCounterImpl(Translator wordMeaning) {
		this.translator = wordMeaning;
	}
	
	/**
	 * Adds words to the Counter
	 */
	@Override
	public void addWords(String... words) {
		/* 
		 * Assumptions
		 * 1. If one of the String has a digit then ignore that
		 * 2. No assumption on the case of the String. So changing it to lower case before passing it to Translator
		 * 3. Also converting to lower case after translating
		 * 4. Assuming multiple threads could be adding the words and reading the count
		 * 5. If translator returns null or throws error then use the same word
		 */  

		Arrays.stream(words)
			.filter(s -> !s.chars().anyMatch(Character::isDigit))
			.map(String::toLowerCase)
			.map(this::getTransalation)
			.map(String::toLowerCase)
			.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
			.forEach((k, v) -> {
				wordCountMap.computeIfAbsent(k, s -> new AtomicLong()).addAndGet(v);
			});
	}
	
	private String getTransalation(String word) {
		return transalationMap.computeIfAbsent(word, this::getTransalationFromTranslator);
	}
	
	private String getTransalationFromTranslator(String word) {
		String translated = null;
		try {
			
			translated = translator.translate(word);
		} catch(Exception ex) {
			LOGGER.warn("Translator threw error for word " + word, ". Using the word without translation", ex);
		}
		return translated != null ? translated : word;
	}

	@Override
	public int getWordCount(String word) {
		AtomicLong count = wordCountMap.get(getTransalation(word.toLowerCase()));
		return count == null ? 0 : count.intValue();
	}

}
