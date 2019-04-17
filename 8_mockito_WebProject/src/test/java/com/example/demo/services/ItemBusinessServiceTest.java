package com.example.demo.services;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.example.demo.model.Item;
import com.example.demo.repo.ItemRepository;
import com.example.demo.service.ItemBusinessService;

@RunWith(MockitoJUnitRunner.class)
public class ItemBusinessServiceTest {

	/****
	 * @Mock creates a mock. @InjectMocks creates an instance of the class and 
	 * injects the mocks that are created with the @Mock (or @Spy) annotations into this instance.
	 * 
	 * This tells Mockito which class to inject mocks into:
	 * 
	 * Note that you must use @RunWith(MockitoJUnitRunner.class) or Mockito.initMocks(this) to initialize these mocks and inject them.
	 */
	@InjectMocks
	private ItemBusinessService business;

	@Mock
	private ItemRepository repository;


	/****
	 * Testing method of 'retrieveAllItems' of ItemBusinessService.java
	 */
	@Test
	public void retrieveAllItems_basic() {
		//We have to Mock 'findAll()' method of JPA to get the data. We don't want to be dependent on outside of test
		when(repository.findAll()).thenReturn(Arrays.asList(new Item(2,"Item2",10,10),
				new Item(3,"Item3",20,20)));
		List<Item> items = business.retrieveAllItems();
		assertEquals(100, items.get(0).getValue());
		assertEquals(400, items.get(1).getValue());
	} 
}