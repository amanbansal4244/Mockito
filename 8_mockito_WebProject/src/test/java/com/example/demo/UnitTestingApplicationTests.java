package com.example.demo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
//We can have separate property file and we need to define the path of new property file using @TestPropertySource
@TestPropertySource(locations= {"classpath:test-configuration.properties"})
public class UnitTestingApplicationTests {

	@Test
	public void contextLoads() {
	}
}