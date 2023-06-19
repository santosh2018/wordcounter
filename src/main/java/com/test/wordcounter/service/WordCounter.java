package com.test.wordcounter.service;

public interface WordCounter {

	void addWords(String... words);
	int getWordCount(String word);
}
