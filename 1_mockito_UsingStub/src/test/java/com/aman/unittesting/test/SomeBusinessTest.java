package com.aman.unittesting.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.aman.unittesting.business.SomeBusinessImpl;

public class SomeBusinessTest {

	@Test
	public void calculateSome_Basic() {
		SomeBusinessImpl businessImpl = new SomeBusinessImpl();
		int actualResult = businessImpl.calculateSum(new int[] {1,2,3});

		int expectedResult = 6;
		assertEquals(expectedResult, actualResult);
	}

	@Test
	public void calculateSome_EmptyArray() {
		SomeBusinessImpl businessImpl = new SomeBusinessImpl();
		int actualResult = businessImpl.calculateSum(new int[] {});

		int expectedResult = 0;
		assertEquals(expectedResult, actualResult);
	}
	
	@Test
	public void calculateSome_OneValue() {
		SomeBusinessImpl businessImpl = new SomeBusinessImpl();
		int actualResult = businessImpl.calculateSum(new int[] {1});

		int expectedResult = 1;
		assertEquals(expectedResult, actualResult);
	}
}


