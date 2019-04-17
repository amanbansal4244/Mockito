package com.aman.unittesting.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.aman.unittesting.business.MyDictionary;

public class VoidMethodTest {

	/****
	 * Notice, we configured the getMeaning() method – which returns a value of type String – to throw a NullPointerException when called.
	 */
	@SuppressWarnings("unchecked")
	@Test(expected = NullPointerException.class)
	public void whenConfigNonVoidRetunMethodToThrowEx_thenExIsThrown() {
		MyDictionary dictMock = mock(MyDictionary.class);
		when(dictMock.getMeaning("aman"))
		.thenThrow(NullPointerException.class);

		dictMock.getMeaning("word");
	}

	/****
	 * Here, we configured an add() method – which returns void – to throw IllegalStateException when called.
	 * We can’t use when().thenThrow() with void return type as the compiler doesn’t allow void methods inside brackets.
	 */
	@Test(expected = IllegalStateException.class)
	public void whenConfigVoidRetunMethodToThrowEx_thenExIsThrown() {
		MyDictionary dictMock = mock(MyDictionary.class);
		doThrow(IllegalStateException.class).when(dictMock).add("aman", "bansal");

		dictMock.add("word", "meaning");
	}

	/****
	 * Void methods can be used with Mockito’s doNothing(), doThrow(), and doAnswer() methods, making mocking and verifying intuitive:
	 * However, doNothing() is Mockito’s default behavior for void methods.
	 */
	@Test
	public void whenAddCalledVerfied() {
		MyDictionary dictMock = mock(MyDictionary.class);
		doNothing().when(dictMock).add(isA(Integer.class), isA(String.class));
		dictMock.add(0, "");

		verify(dictMock, times(1)).add(0, "");
	}

	@Test(expected = Exception.class)
	public void givenNull_AddThrows() {
		MyDictionary dictMock = mock(MyDictionary.class);
		doThrow(IllegalStateException.class).when(dictMock).add(any(Integer.class), any(String.class));

		dictMock.add(0, null);
	}
	
	/****
	 * Argument Capture:
	 * 		One reason to override the default behavior with doNothing() is to capture arguments.
	 * 		In the example above verify() is used to check the arguments passed to add().
	 * 
	 * 		However, we may need to capture the arguments and do something more with them.
	 * 		In these cases, we use doNothing() just as we did above, but with an ArgumentCaptor:
	 * 
	 * 
	 * Verifying argument value using ArgumentCaptor
	 * In argumentValuesVerificationWithArgumentCapture(), we use ArgumentCaptor to verify the argument as well as its attributes.
	 * First we need to create an ArgumentCaptor object for the type of argument we are interested in. We will use a factory method to create an instance of the argument captor for the EmpEvent type.
	 * 
	 * ArgumentCaptor<EmpEvent> empEventArgCaptor = ArgumentCaptor.forClass(EmpEvent.class);
	 * We need the ArgumentCaptor object before we start the verifying. We call ArgumentCaptor.capture() to capture the argument. Internally it uses argument matcher and stores the argument value.
	 * 
	 * verify(empManager).recordEvent(empEventArgCaptor.capture());
	 * 
	 * We will retrieve the captured argument value using ArgumentCaptor.getValue().
	 */
	@Test
	public void whenAddCalledValueCaptured() {
		MyDictionary dictMock = mock(MyDictionary.class);
	    ArgumentCaptor valueCapture = ArgumentCaptor.forClass(String.class);
	    doNothing().when(dictMock).add(isA(Integer.class) , valueCapture.capture());
	    dictMock.add(0, "captured");
	  
	    assertEquals("captured", valueCapture.getValue());
	}


	
	/****
	 * Partial Mocking:
	 * 		Partial mocks are an option, too. Mockito’s doCallRealMethod() can be used for void methods:
	 * 		This allows us to call the actual method is called and verify it at the same time.
	 */
	
	@Test
	public void whenAddCalledRealMethodCalled() {
		MyDictionary dictMock = mock(MyDictionary.class);
	    doCallRealMethod().when(dictMock).add(any(Integer.class), any(String.class));
	    dictMock.add(1, "real");
	  
	    verify(dictMock, times(1)).add(1, "real");
	}
	
}