package com.example.demo.repo;
import static org.junit.Assert.assertEquals;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import
org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.demo.model.Item;

@RunWith(SpringRunner.class)
@DataJpaTest // To test JPA Repository
public class ItemRepositoryTest {
	@Autowired
	private ItemRepository repository;
	
	@Test
	public void testFindAll() {
		List<Item> items = repository.findAll();
		assertEquals(3,items.size());
	}
	
	@Test
	public void testFindOne() {
		Item item = repository.findById(10001).get();

		assertEquals("Item1",item.getName());	
	} 
}
