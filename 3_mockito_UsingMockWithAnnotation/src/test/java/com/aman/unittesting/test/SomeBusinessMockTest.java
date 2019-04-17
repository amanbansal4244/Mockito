package com.aman.unittesting.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.aman.unittesting.business.SomeBusinessImpl;
import com.aman.unittesting.data.SomeDataService;

@RunWith(MockitoJUnitRunner.class)
public class SomeBusinessMockTest {

	// Using @InjectMocks, we don't need create the object of SomeBusinessImpl.
	@InjectMocks
	SomeBusinessImpl businessImpl;
	
	/***** Creating the mock of 'SomeDataService' class *****/
	@Mock
	SomeDataService someDataService;
	
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


