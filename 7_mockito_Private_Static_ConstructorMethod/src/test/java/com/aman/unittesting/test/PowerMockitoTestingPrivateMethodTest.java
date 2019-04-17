package com.aman.unittesting.test;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.example.demo.powermock.Dependency;
import com.example.demo.powermock.SystemUnderTest;

@RunWith(PowerMockRunner.class)
public class PowerMockitoTestingPrivateMethodTest {

	@Mock
	Dependency dependencyMock;

	// Using @InjectMocks, we don't need create the object of SystemUnderTest.
	@InjectMocks
	SystemUnderTest systemUnderTest;

	@Test
	public void powerMockito_CallingAPrivateMethod() throws Exception {
		when(dependencyMock.retrieveAllStats()).thenReturn(
				Arrays.asList(1, 2, 3));
		
		long value = (Long) Whitebox.invokeMethod(systemUnderTest,
				"privateMethodUnderTest");
		
		assertEquals(6, value);
	}
}