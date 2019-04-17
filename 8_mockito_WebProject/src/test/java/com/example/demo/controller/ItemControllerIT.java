package com.example.demo.controller;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import
org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
/****
 * @SpringBootTest is saying “bootstrap with Spring Boot’s support” (e.g. load application.properties and give me all the Spring Boot goodness)
 * The webEnvironment attribute allows specific “web environments” to be configured for the test. 
 * You can start tests with a MOCK servlet environment or with a real HTTP server running on either a RANDOM_PORT or a DEFINED_PORT.
 *If we want to load a specific configuration, we can use the classes attribute of @SpringBootTest. 
 *In this example, we’ve omitted classes which means that the test will first attempt to load @Configuration from any inner-classes, 
 *and if that fails, it will search for your primary @SpringBootApplication class.
 *
 *By default, @SpringBootTest will not start a server. You can use the webEnvironment attribute of @SpringBootTest to further refine how your tests run:
 *MOCK(Default) : Loads a web ApplicationContext and provides a mock web environment. Embedded servers are not started when using this annotation.
 *If a web environment is not available on your classpath, this mode transparently falls back to creating a regular non-web ApplicationContext.
 *It can be used in conjunction with @AutoConfigureMockMvc or @AutoConfigureWebTestClient for mock-based testing of your web application.
 *
 *RANDOM_PORT: Loads a WebServerApplicationContext and provides a real web environment. Embedded servers are started and listen on a random port.
 *
 *DEFINED_PORT: Loads a WebServerApplicationContext and provides a real web environment. Embedded servers are started and listen on a
 *defined port (from your application.properties or on the default port of 8080).
 *
 *NONE: Loads an ApplicationContext by using SpringApplication but does not provide any web environment (mock or otherwise).
 *
 *@SpringBootTest loads the in-memory DB so that we are not dependent in real DB.
 */ 
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
public class ItemControllerIT {
	
	/****
	 * Convenient alternative of RestTemplate that is suitable for integration tests.
	 * If you are using the @SpringBootTest annotation, a TestRestTemplate is automatically available and can be @Autowired into your test. 
	 * If you need customizations (for example to adding additional message converters) use a RestTemplateBuilder @Bean.
	 * 
	 * TestRestTemplate is already aware which RANDOM_PORT will be assigned.
	 */
	@Autowired
	private TestRestTemplate restTemplate;
	
	/**** When we launch this test, then complete spring boot application will launch up.
	 * What it does that, it sees all the packages and parent packages of naming package "com.example.demo.controller"
	 * classes which has the @SpringBootApplication
	 * If @SpringBootApplication is not found in package "com.example.demo.controller", then it goes to its parent package "com.example.demo."
	 * so on.....
	 */
	@Test
	public void contextLoads() throws JSONException

	{
		String response = this.restTemplate.getForObject("/all-items-from-database",String.class);
		JSONAssert.assertEquals("[{id:10001},{id:10002},{id:10003}]",response, false);;
	} 
}
