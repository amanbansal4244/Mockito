/* bcwti
 *
 * Copyright (c) 2017 Parametric Technology Corporation (PTC). All Rights
 * Reserved.
 *
 * This software is the confidential and proprietary information of PTC
 * and is subject to the terms of a software license agreement. You shall
 * not disclose such confidential information and shall use it only in accordance
 * with the terms of the license agreement.
 *
 * ecwti
 */
package com.ptc.windchill.enterprise.part.structure;

import static org.junit.Assert.assertEquals;

import java.util.Comparator;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import mockit.Mock;
import mockit.MockUp;
import wt.fc.Persistable;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.part.WTPartUsageLink;
import wt.part._WTPart;
import wt.part._WTPartMaster;
import wt.part._WTPartUsageLink;
import wt.util.WTException;

/**
 * Test class for the PartStructureFindNumberComparator.
 */
public class PartStructureFindNumberComparatorTest {

    final static int LESS_THAN = -1;
    final static int GREATER_THAN = 1;
    final static int EQUAL = 0;

    private Comparator comparer;
    private PartStructureFindNumberComparator psfnc;
    private WTPart part;
    WTPartMaster partmaster;
    WTPartUsageLink partlink;

    String[] arr = new String[2];
    int pos=0;
    int expected;

    /**
     * Object initialization for tests
     * @throws WTException
     */
    @Before
    public void setUp() throws WTException {
        setupMocks();
        comparer=null;
        psfnc=new PartStructureFindNumberComparator( comparer,Locale.US);
        partmaster = new WTPartMaster();
        part=new WTPart();
        partlink = WTPartUsageLink.newWTPartUsageLink(part, partmaster);
        pos = 0;
    }

    /**
    * Tests {@link com.ptc.windchill.enterprise.part.structure#compare(Object o1, Object o2)}
    * Tests both findnumber1 and findnumber2 are equal
    * Expected Result: The output from compare() gives the integer 0.
    */
    @Test
    public void testEquals(){
       arr[0] = arr[1] = "test";
       expected = EQUAL;
       Object obj1[]={partlink,null};
       Object obj2[]={partlink,null};
        int result = psfnc.compare(obj1, obj2);
        assertEquals("compare method not return 0 for equal",expected,result);
    }

    /**
     * Tests {@link com.ptc.windchill.enterprise.part.structure#compare(Object o1, Object o2)}
     * passing findnumber1 as null
     * Expected Result: The output from compare() gives the integer 1.
     */
    @Test
    public void testFirstNull(){
       arr[0] = null;
       arr[1] = "test2";
       expected = GREATER_THAN;
        Object obj1[]={partlink,null};
        Object obj2[]={partlink,null};
        int result = psfnc.compare(obj1, obj2);
        assertEquals("compare method not return 1 for findnumber1 null",expected,result);
    }

    /**
     * Tests {@link com.ptc.windchill.enterprise.part.structure#compare(Object o1, Object o2)}
     * passing findnumber2 as null
     * Expected Result: The output from compare() gives the integer -1.
     */
    @Test
    public void testSecondNull(){
       arr[0] = "test2";
       arr[1] = null;
       expected = LESS_THAN;
         Object obj1[]={partlink,null};
        Object obj2[]={partlink,null};
        int result = psfnc.compare(obj1, obj2);
        assertEquals("compare method not return -1 for findnumber2 null",expected,result);
    }

    /**
     * Tests {@link com.ptc.windchill.enterprise.part.structure#compare(Object o1, Object o2)}
     * Test findnumber1 is bigger and local is null
     * Expected Result: The output from compare() gives the integer 1.
     */
    @Test
    public void testFirstBig1(){
       arr[0] = "test2";
       arr[1] = "test1";
       expected = GREATER_THAN;
       Locale local = null;
       psfnc=new PartStructureFindNumberComparator( comparer,local);
         Object obj1[]={partlink,null};
        Object obj2[]={partlink,null};
        int result = psfnc.compare(obj1, obj2);
        assertEquals("scompare method not return 1 for findnumber1 is bigger",expected,result);
    }

    /**
     * Tests {@link com.ptc.windchill.enterprise.part.structure#compare(Object o1, Object o2)}
     * passing findnumber1 and findnumber2 as null expect equal
     * Expected Result: The output from compare() gives the integer 0.
     */
    @Test
    public void testEqualsWithNullValues(){
       arr[0] = null;
       arr[1] = null;
       expected = EQUAL;
        Object obj1[]={partlink,null};
        Object obj2[]={partlink,null};
        int result = psfnc.compare(obj1, obj2);
        assertEquals("compare method not return 0 for findnumber1 and findnumber2 null",expected,result);
    }

    /**
     * Tests {@link com.ptc.windchill.enterprise.part.structure#compare(Object o1, Object o2)}
     * with user defined sub comparator
     * Expected Result: compare method returns 2
     */
    @Test
    public void testwithComparator(){
        comparer=new Comparator()
        {
            @Override
            public int compare(Object o1, Object o2) {
                // TODO Auto-generated method stub
                return 2;
            }
        };
        arr[0] = null;
        arr[1] = null;
        expected = 2;
        psfnc=new PartStructureFindNumberComparator(comparer);
        Object obj1[]={partlink,null};
        Object obj2[]={partlink,null};
        int result = psfnc.compare(obj1, obj2);
        assertEquals("subcomparator compare method not get called",expected,result);
    }

    /**
     * Tests {@link com.ptc.windchill.enterprise.part.structure#compare(Object o1, Object o2)}
     * not giving WTPartUsageLink in Object array
     * Expected Result: exception ClassCastException.
     */
    @Test(expected = ClassCastException.class)
    public void testExpectException(){
        Object obj1[]={"STRING"};
        Object obj2[]={"test",null};
        psfnc.compare(obj1, obj2);
    }

    /**
     * Tests {@link com.ptc.windchill.enterprise.part.structure#compare(Object o1, Object o2)}
     * not giving array of object
     * Expected Result: exception ClassCastException.
     */
    @Test(expected = ClassCastException.class)
    public void testExpectException1(){
        Object obj1 = "test";
        Object obj2= "test";
        psfnc.compare(obj1, obj2);
    }

    /**
     *  Mocks method
     */
    public void setupMocks(){
        new MockUp<WTPart>(){
            @Mock
          public void $init(){
              //
          }
        };
        new MockUp<_WTPart>(){
            @Mock
          public void $init(){
                //
          }
        };
        new MockUp<WTPartMaster>(){
            @Mock
          public void $init(){
              //
          }
        };
        new MockUp<_WTPartMaster>(){
            @Mock
          public void $init(){
              //
          }
        };
        new MockUp<WTPartUsageLink>(){
            @Mock
          public void $init(){
              //
          }
            @Mock
            protected void initialize( Persistable aObj, Persistable bObj )
                    throws WTException {
                //
            }
        };
        new MockUp<_WTPartUsageLink>(){
            @Mock
          public void $init(){
             //
          }
            @Mock
            String getFindNumber(){
                return arr[pos++];
            }
       };
    }
}

