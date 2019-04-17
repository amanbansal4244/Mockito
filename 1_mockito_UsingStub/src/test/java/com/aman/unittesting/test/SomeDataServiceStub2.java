package com.aman.unittesting.test;

import com.aman.unittesting.data.SomeDataService;

public class SomeDataServiceStub2 implements SomeDataService{

	@Override
	public int[] retriveAllData() {
		return new int[] {};
	}

}
