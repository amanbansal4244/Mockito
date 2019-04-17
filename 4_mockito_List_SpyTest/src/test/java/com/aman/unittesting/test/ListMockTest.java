package com.aman.unittesting.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMost;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class ListMockTest {

	List listMock = mock(List.class);

	@Test
	public void sizeListTest() {
		//List listMock = mock(List.class);
		when(listMock.size()).thenReturn(5);
		assertEquals(5, listMock.size());
	}

	@Test
	public void returnMultipleValues() {
		//List listMock = mock(List.class);
		/****
		 * When first time 'listMock.size()' gets called, it will return 5 and 
		 * when next time 'listMock.size()' gets called, it will return 10
		 */
		when(listMock.size()).thenReturn(5).thenReturn(10);
		assertEquals(5, listMock.size());
		assertEquals(10, listMock.size());
	}

	@Test
	public void returnWithParameter() {
		//List listMock = mock(List.class);
		/**  When we get 0th index from list it will return "Mocked Indexed Value" 
		 * when we try to get another index except 0 like 4 from list then we will get default value i.e. null in this test
		 **/
		when(listMock.get(0)).thenReturn("Mocked Indexed Value");
		assertEquals("Mocked Indexed Value", listMock.get(0));
		assertEquals(null, listMock.get(4));
	}

	@Test
	public void returnWithGenericParameter() {
		//List listMock = mock(List.class);
		/**  We can use 'anyInt()' method means when we try to get any integer index from list,
		 * we will get "Mocked Indexed Value". 
		 * Similarly like anyInt()' method, we have a lot of methods , like anyLong(), anyDouble() etc. 
		 **/
		when(listMock.get(anyInt())).thenReturn("Mocked Indexed Value");
		assertEquals("Mocked Indexed Value", listMock.get(0));
		assertEquals("Mocked Indexed Value", listMock.get(4));
	}

	/****
	 * In this test, we will learn, how to verify how many whether specific method is called with the specific value on a mock.
	 * This is imp in those scenario where value is not returned back. Lets say we are calling another method(which does not returned anything)
	 *  from some mocked method.
	 *  
	 * In this example, we are taking List class and in real time application it can be our application class and 
	 * we would be checking verify how many whether our specific method is called with the specific value
	 */
	@Test
	public void verificationBasics() {
		//Here we are calling get method of mocked class List
		String value1= (String) listMock.get(0);
		String value2= (String) listMock.get(1);

		//This will verify whether get() method is ever called with data 0
		verify(listMock).get(0);
		//This will verify  get() method is ever called twice with any integer data
		verify(listMock, times(2)).get(anyInt());
		//This will verify  get() method is at-least called once with any integer data
		verify(listMock, atLeast(1)).get(anyInt());
		//This will verify  get() method is at -east once called once with any integer data
		verify(listMock, atLeastOnce()).get(anyInt());
		//This will verify  get() method is at-most called twice with any integer data
		verify(listMock, atMost(2)).get(anyInt());
		//This will verify whether get() method is ever called with data 2
		verify(listMock, never()).get(2);


	}

	/****
	 *How to capture an asingle rgument that is passed to a method call. 
	 */
	@Test
	public void argumentCapturing() {
		//Here we are calling get method of mocked class List and with argument 'SomeString'
		listMock.add("SomeString");

		//verification
		//This is String because we are verifying agrument is 'String' type.
		ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
		//We are trying to capture the argument by "argumentCaptor.capture()" which is passed to call add method on mocked List object.
		verify(listMock).add(argumentCaptor.capture());

		assertEquals("SomeString", argumentCaptor.getValue());

	}

	/****
	 *How to capture an multiple argument that is passed to a method call .
	 */
	@Test
	public void multipleArgumentCapturing() {
		listMock.add("SomeString1");
		listMock.add("SomeString2");

		//verification
		//This is String because we are verifying agrument is 'String' type.
		ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
		//We are trying to capture the argument by "argumentCaptor.capture()" which is passed to call add method on mocked List object.
		verify(listMock, times(2)).add(argumentCaptor.capture());

		List<String> allValues = argumentCaptor.getAllValues();
		assertEquals("SomeString1", allValues.get(0));
		assertEquals("SomeString2", allValues.get(1));

	}
	
	@Test(expected = RuntimeException.class)
	public void letsMockListGetToThrowException() {
		when(listMock.get(anyInt())).thenThrow(
				new RuntimeException("Something went wrong"));
		
		listMock.get(0);
	}

	@Test
	public void mocking() {
		//By Mocking ArrayList means every method in ArrayList is mocked , not the real method.
		ArrayList arrayListMock = mock(ArrayList.class);
		System.out.println(arrayListMock.get(0));// It will return by default value which is null
		System.out.println(arrayListMock.size());// It will return by default value which is  0
		arrayListMock.add("Test");
		arrayListMock.add("Test2");
		/****
		 * After adding two values in array list, still It will return by default value which is 
		 *  because we have added value in mocked ArrayList, not the real ArrayList here.
		 *  So this mocked ArrayList method will return what we ask to ArrayList's method to return.
		 */
		System.out.println(arrayListMock.size());
		when(arrayListMock.size()).thenReturn(5);
		System.out.println(arrayListMock.size());//Now it will return 5
	}

	/****
	 * A spy, by default, retains behavior (code) of the original class!
	 * means in this example, original behavior of ArrayList will be retained and
	 * you can stub specific methods if you want to change the behavior of some specific method as well.
	 */
	@Test
	public void spying() {
		ArrayList arrayListSpy = spy(ArrayList.class);
		/*** In this case, below line would throw an exception because ArrayList does not have any value as of now. **/
		//System.out.println(arrayListMock.get(0));
		
		arrayListSpy.add("Test0");
		System.out.println(arrayListSpy.get(0));//returns Test0
		System.out.println(arrayListSpy.size());//returns 1
		arrayListSpy.add("Test");
		arrayListSpy.add("Test2");
		
		System.out.println(arrayListSpy.size());//returns 3
		//Here, we are changing the behavior if size method of ArrayList.
		when(arrayListSpy.size()).thenReturn(5);
		System.out.println(arrayListSpy.size());//Now it will return 5
		
		/*** We can also verify on spy as well.
		 * When we don't have access to specific class to get the data from it but
		 * you want to check what is going underneath for that specific class and 
		 * what method is getting called. Tn this kind of scenario, we can use 'Spy'
		 */
		verify(arrayListSpy).add("Test");
	} 
	
	
}
