package com.aman.unittesting.test;

import com.aman.unittesting.data.SomeDataService;

public class SomeDataServiceStub implements SomeDataService{

	@Override
	public int[] retriveAllData() {
		return new int[] {1,2,3};
	}

}
