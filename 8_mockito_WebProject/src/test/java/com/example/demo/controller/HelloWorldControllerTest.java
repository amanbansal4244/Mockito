package com.example.demo.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@RunWith(SpringRunner.class) // This helps to launch the Spring Unit Test.
/**** This tells that this is MVC test and telling this test which controller need to be test ****/
@WebMvcTest(value=HelloWorldController.class)
public class HelloWorldControllerTest {

	/**** TO call the "/hello-world" , we need 'mockMvc' from spring ****/
	@Autowired
    private MockMvc mockMvc;
	
	/**** To test the URL mapping (/hello-world) in HelloWorldController.below class, we need to do below two things:
	 * 	1. //call GET "/hello-world"  application/json
	 * 	2. //verify "Hello World"
	 * 
	 * 
	 * 
	 */
	@Test
	public void helloWorld_basic() throws Exception {
		//call GET "/hello-world"  application/json
		//Building the request which takes the URI "/hello-world" and which accepts the format in JSON.
		RequestBuilder request =
				MockMvcRequestBuilders
				.get("/hello-world")
				.accept(MediaType.APPLICATION_JSON);
		
		//Need to invoke 'perform' the above built request.
		MvcResult result = mockMvc.perform(request)
				.andExpect(status().isOk()) // to expect the status back and checking whether status is OK or not.
				.andExpect(content().string("Hello World")) //to expect the content.
				.andReturn(); // need to use this method to return the result back.
		
		//verify "Hello World"
		//assertEquals("Hello World",
		
		/**** We should use 'getContentAsString()' to convert the response into String in Spring MVC, don't use 'toString' method.
		 * If we use toString' method, we will get exception while testing this test class.
		 */
		result.getResponse().getContentAsString();
	}

}
