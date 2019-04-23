/* bcwti
 *
 * Copyright (c) 2013 PTC, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of PTC
 * and is subject to the terms of a software license agreement.
 * You shall not disclose such confidential information and shall use it
 * only in accordance with the terms of the license agreement.
 *
 * ecwti
 */
package com.ptc.projectmanagement.util;

import wt.inf.container.WTContainerRef;

import com.ptc.projectmanagement.plan.Plan;

import mockit.Mock;
import mockit.MockUp;

/**
 * Mock for {@link PlannableUtils}.
 *
 * <BR><BR><B>Supported API: </B>false
 * <BR><BR><B>Extendable: </B>false
 */
public class MockPlannableUtils extends MockUp<PlannableUtils> {

	private static boolean isUpdateWorkForFixedDuration;
	private static boolean resumePlanOnProjectResume;

	public static void setUpdateWorkForFixedDuration(boolean isUpdateWorkForFixedDuration){
		MockPlannableUtils.isUpdateWorkForFixedDuration = isUpdateWorkForFixedDuration;
	}

	private static boolean isCreateDefaultResourceAssignment;

	public static void setCreateDefaultResourceAssignment(boolean isCreateDefaultResourceAssignment){
		MockPlannableUtils.isCreateDefaultResourceAssignment = isCreateDefaultResourceAssignment;
	}

	@Mock
	public void $init() {
	}

	@Mock
	public static boolean isUpdateWorkForFixedDuration(Plan plan){
		return isUpdateWorkForFixedDuration;
	}

	@Mock
	public static boolean isCreateDefaultResourceAssignment(WTContainerRef wtContainerRef){
		return isCreateDefaultResourceAssignment;
	}
	
	@Mock
	public static boolean isResumePlanOnProjectResume(){
		return resumePlanOnProjectResume;
	}
	
	public static void setResumePlanOnProjectResume(boolean resumePlanOnProjectResume){
		MockPlannableUtils.resumePlanOnProjectResume = resumePlanOnProjectResume;
	}	
}
