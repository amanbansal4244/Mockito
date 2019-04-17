package com.aman.unittesting.test;

import com.aman.unittesting.data.SomeDataService;

public class SomeDataServiceStub3 implements SomeDataService{

	@Override
	public int[] retriveAllData() {
		return new int[] {1};
	}

}
