package com.test.wordcounter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.test.wordcounter.service.WordCounter;

@RestController
@RequestMapping("/wordcounter")
public class WordCounterController {

	@Autowired
	private WordCounter wordCounter;
	
	public WordCounterController(WordCounter wordCounter) {
		this.wordCounter = wordCounter;
	}
	
	@PostMapping("/addWord")
	public void addWords(@RequestBody String ... words) {
		wordCounter.addWords(words);
	}
	
	@GetMapping("/getWordCount/{word}")
	public int getWordCount(@PathVariable String word) {
		return wordCounter.getWordCount(word);
	}
	
}
