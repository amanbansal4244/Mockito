/* bcwti
 *
 * Copyright (c) 2017 PTC, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of PTC
 * and is subject to the terms of a software license agreement.
 * You shall not disclose such confidential information and shall use it
 * only in accordance with the terms of the license agreement.
 *
 * ecwti
 */
package com.ptc.windchill.enterprise.part.reports.dataUtilities;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import com.ptc.core.components.descriptor.ComponentDescriptor;
import com.ptc.core.components.descriptor.ModelContext;
import com.ptc.core.components.factory.DefaultComponentDescriptor;
import com.ptc.core.components.factory.DefaultModelContext;
import com.ptc.core.components.util.OidHelper;
import com.ptc.netmarkets.model.NmOid;

import mockit.Mock;
import mockit.MockUp;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.part.WTPart;
import wt.part._WTPart;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
/*
 * Tests for {@link PartReportBackingVersionOidDataUtility}
 */
public class PartReportBackingVersionOidDataUtilityTest{
  PartReportBackingVersionOidDataUtility prt;
    public MockObjectReference mkobj;
    /*
     * It initialize all required object
     */
    @Before
    public void setUP()throws Exception
    {
        prt=new PartReportBackingVersionOidDataUtility();
        new MockOidHelper();
        setupMocks();
    }
    /*
     * Here fake is created to create ModelContext Object
     * @return ModelContext object
     */
    private static ModelContext createModelContext () throws WTException {
        ModelContext mc = new DefaultModelContext();
        ComponentDescriptor cd = new DefaultComponentDescriptor("VIEW");
        mc.setDescriptor(cd);
        return mc;
    }
    /*
     * setupMockes() setup all required mock classes
     */
    public void setupMocks() {
        new MockUp<WTPart>() {
            @Mock
            public void $init() {
                //
            }
        };
        new MockUp<_WTPart>() {
            @Mock
            public void $init() {
                //
            }
        };
        new MockUp<NmOid>() {
            @Mock
            public void $init() {
                //
            }
        };
    }
    /*
     * It Mock objectReference class to get persistable object
     * It @return Persistable Object
     */
    private class MockObjectReference extends MockUp<ObjectReference>
    {
       @Mock
       public  Persistable getObject() throws WTPropertyVetoException
       {
           WTPart sf=new WTPart();
           return sf ;
           }
       }
    /*
     * It Mock OidHelper class to get NmOid object
     * It @return NmOid Object
     */
    public static class MockOidHelper extends MockUp<OidHelper>{
        @Mock
        public static NmOid getNmOid(Object o) throws WTException {
            NmOid result = null;
            result = new NmOid();
            return result;
        }
    }
   /*
    * This test check getValue() and check object is not null
    */
    @Test
    public void testGetValue() throws WTException, WTPropertyVetoException {
        mkobj=new MockObjectReference();
        Persistable p= mkobj.getObject();
        ModelContext mc=createModelContext();
        Object str=prt.getDataValue("name",p,mc);
        assertNotNull("return object is empty",str);
    }
    /*
     * This test check getValue() and check object is null
     */
    @Test
    public void testNullValue() throws WTException
    {
        ModelContext mc=createModelContext();
        Object str=prt.getDataValue("name",12,mc);
        assertNull("object return not null value",str);
    }
}

