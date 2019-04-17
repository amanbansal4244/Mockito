package com.aman.unittesting.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.aman.unittesting.business.SomeBusinessImpl;
import com.aman.unittesting.data.SomeDataService;

public class SomeBusinessMockTest {

	SomeBusinessImpl businessImpl = new SomeBusinessImpl();
	/***** Creating the mock of 'SomeDataService' class *****/
	SomeDataService someDataService = mock(SomeDataService.class);
	
	@Before
	public void before() {
		businessImpl.setSomeDataService(someDataService);
	}
	
	@Test
	public void calculateSumUsingDataService_Basic() {
		/**** When 'retriveAllData()' of 'SomeDataService' is called then return "new int[] {1,2,3}" ****/
		when(someDataService.retriveAllData()).thenReturn(new int[] {1,2,3});
		int actualResult = businessImpl.calculateSumUsingDataService();
		assertEquals(6, actualResult);
	}

	@Test
	public void calculateSumUsingDataService_EmptyArray() {
		when(someDataService.retriveAllData()).thenReturn(new int[] {});
		int actualResult = businessImpl.calculateSumUsingDataService();
		assertEquals(0, actualResult);
	}
	
	@Test
	public void calculateSumUsingDataService_OneValue() {
		when(someDataService.retriveAllData()).thenReturn(new int[] {1});
		int actualResult = businessImpl.calculateSumUsingDataService();
		assertEquals(1, actualResult);
	}
}


