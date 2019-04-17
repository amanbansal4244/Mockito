package com.aman.unittesting.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.aman.unittesting.business.SomeBusinessImpl;

public class SomeBusinessStubTest {

	@Test
	public void calculateSumUsingDataService_Basic() {
		SomeBusinessImpl businessImpl = new SomeBusinessImpl();
		/***** We need some data to test. so we need to set the data. 
		 	* Typically we get the data from DB, but I don't want my test should not be dependent on DB or outside the test.
		 	* So to set the data, we can create the STUB implementation of this.
		*****/
		businessImpl.setSomeDataService(new SomeDataServiceStub());
		int actualResult = businessImpl.calculateSumUsingDataService();

		int expectedResult = 6;
		assertEquals(expectedResult, actualResult);
	}

	@Test
	public void calculateSumUsingDataService_EmptyArray() {
		SomeBusinessImpl businessImpl = new SomeBusinessImpl();
		businessImpl.setSomeDataService(new SomeDataServiceStub2());
		int actualResult = businessImpl.calculateSumUsingDataService();

		int expectedResult = 0;
		assertEquals(expectedResult, actualResult);
	}
	
	@Test
	public void calculateSumUsingDataService_OneValue() {
		SomeBusinessImpl businessImpl = new SomeBusinessImpl();
		businessImpl.setSomeDataService(new SomeDataServiceStub3());
		int actualResult = businessImpl.calculateSumUsingDataService();

		int expectedResult = 1;
		assertEquals(expectedResult, actualResult);
	}
}


