package com.test.wordcounter.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import com.test.wordcounter.translator.Translator;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class WordCounterTest {
	
	private static final String NOT_EXISTING = "NOT_EXISTING";
	private static final String THROW_ERROR = "THROW_ERROR";
	private static final String FLOWER = "flower";
	private static final String FLOR = "flor";
	private static final String GLUME = "glume";

	private static final String TEST = "test";
	private static final String RUN = "run";
	private static final String MANY = "many";
	

	@Autowired
	private WordCounter counter;
	
	@Autowired
	@MockBean
	private Translator translator;
	
	@BeforeEach
	private void setup() {
		when(translator.translate(NOT_EXISTING.toLowerCase())).thenReturn(null);
		when(translator.translate(THROW_ERROR.toLowerCase())).thenThrow(new IllegalArgumentException(THROW_ERROR));
		when(translator.translate(FLOR)).thenReturn(FLOWER);
		when(translator.translate(GLUME)).thenReturn(FLOWER);
		when(translator.translate(TEST)).thenReturn(TEST);
		when(translator.translate(RUN)).thenReturn(RUN);
	}
	
	@Test
	public void testHappyCaseString() {
		counter.addWords(TEST, RUN, TEST, TEST, TEST);
		counter.addWords(TEST);
		
		assertEquals(5, counter.getWordCount(TEST));
		assertEquals(1, counter.getWordCount(RUN));
		assertEquals(0, counter.getWordCount(MANY));
	}

	@Test
	public void testTranslatedWordsCase2String() {
		counter.addWords(FLOWER, FLOR, GLUME);
		
		assertEquals(3, counter.getWordCount(FLOWER));
		assertEquals(3, counter.getWordCount(FLOR));
		assertEquals(3, counter.getWordCount(GLUME));
	}

	@Test
	public void testTranslatorThrowsError() {
		counter.addWords(THROW_ERROR, TEST, TEST);
		counter.addWords(TEST);
		
		assertEquals(3, counter.getWordCount(TEST));
		assertEquals(1, counter.getWordCount(THROW_ERROR));
	}

	@Test
	public void testTranslatorReturnsNull() {
		counter.addWords(NOT_EXISTING, TEST, TEST);
		counter.addWords(TEST);
		
		assertEquals(3, counter.getWordCount(TEST));
		assertEquals(1, counter.getWordCount(NOT_EXISTING));
	}

	@Test
	public void testIgnoresWordsWithDigit() {
		String TEST3 = "test3";
		counter.addWords(TEST3, TEST);
		
		assertEquals(1, counter.getWordCount(TEST));
		assertEquals(0, counter.getWordCount(TEST3));
	}

	@Test
	public void testMultiThreadedAddingOfWords() throws InterruptedException {
		final CountDownLatch latch = new CountDownLatch(1);
		final ExecutorService service = Executors.newFixedThreadPool(100);
		for(int i = 0; i < 100; i++) {
			service.submit(() -> {
				try {
					latch.await();
				} catch (InterruptedException e) {
					throw new RuntimeException();
				}
				counter.addWords(TEST, RUN, TEST);
			});
		}
		
		latch.countDown();
		
		service.awaitTermination(10, TimeUnit.SECONDS);
		service.shutdown();
		
		assertEquals(200, counter.getWordCount(TEST));
		assertEquals(100, counter.getWordCount(RUN));
	}

}
