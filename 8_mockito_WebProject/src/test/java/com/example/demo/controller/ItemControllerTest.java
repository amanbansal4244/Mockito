package com.example.demo.controller;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.example.demo.model.Item;
import com.example.demo.service.ItemBusinessService;

@RunWith(SpringRunner.class)
@WebMvcTest(ItemController.class)
public class ItemControllerTest {

	/**** This annotation is a shorthand for the Mockito.mock() method. As well, 
	 * we should only use it in a test class. Unlike the mock() method, we need to enable Mockito annotations to use this annotation.
	 * We can do this either by using the MockitoJUnitRunner to run the test or calling the MockitoAnnotations.initMocks() method explicitly.
	 */
	@Autowired
	private MockMvc mockMvc;

	/**** We can use the @MockBean to add mock objects to the Spring application context. 
	 * The mock will replace any existing bean of the same type in the application context.
	 * If no bean of the same type is defined, a new one will be added. 
	 * This annotation is useful in integration tests where a particular bean – for example, an external service – needs to be mocked.
	 */
	@MockBean
	private ItemBusinessService businessService;


	@Test
	public void dummyItem_basic() throws Exception {
		RequestBuilder request =
				MockMvcRequestBuilders
				.get("/dummy-item")
				.accept(MediaType.APPLICATION_JSON);

		MvcResult result = mockMvc.perform(request)
				.andExpect(status().isOk())
				.andExpect(content().json("{\"id\":1,\"name\":\"Ball\",\"price\":10,\"quantity\":100}"))
				.andReturn();

		//Under the hoodm this below line is getting invoked.
		//JSONAssert.assertEquals(expected,actual, false);
		result.getResponse().getContentAsString();
	}


	/**** Testing the URI "/item-from-business-service" which is dependent on ItemBusinessService
	 * but we are writing the test which is independent of ItemBusinessService using mock.
	 */
	@Test
	public void itemFromBusinessService_basic() throws Exception {
		when(businessService.retreiveHardcodedItem()).thenReturn(
				new Item(2,"Item2",10,10));
		RequestBuilder request =
				MockMvcRequestBuilders
				.get("/item-from-business-service")
				.accept(MediaType.APPLICATION_JSON);
		MvcResult result = mockMvc.perform(request)
				.andExpect(status().isOk())
				.andExpect(content().json("{id:2,name:Item2,price:10}"))
				.andReturn();

		result.getResponse().getContentAsString();
	}

	/**** Testing the URI "/all-items-from-database" which is dependent on ItemBusinessService
	 * but we are writing the test which is independent of ItemBusinessService using mock.
	 */
	@Test
	public void retrieveAllItems_basic() throws Exception {

		when(businessService.retrieveAllItems()).thenReturn(
				Arrays.asList(new Item(2,"Item2",10,10),
						new Item(3,"Item3",20,20)));

		RequestBuilder request = MockMvcRequestBuilders.get("/all-items-from-database")
				.accept(MediaType.APPLICATION_JSON);

		MvcResult result = mockMvc.perform(request)
				.andExpect(status().isOk())
				//In JSON, array is represent in square bracket like below line:
				.andExpect(content().json("[{id:3,name:Item3,price:20}, {id:2,name:Item2,price:10}]"))
				.andReturn();

		result.getResponse().getContentAsString();
	}

	@Test
	public void retrieveAllItems_noitems() throws Exception {
		when(businessService.retrieveAllItems()).thenReturn(
				Arrays.asList()
				);

		RequestBuilder request =
				MockMvcRequestBuilders
				.get("/all-items-from-database")
				.accept(MediaType.APPLICATION_JSON);

		MvcResult result =	mockMvc.perform(request)
				.andExpect(status().isOk())
				.andExpect(content().json(" "))
				.andReturn();

		result.getResponse().getContentAsString();
	} 
}