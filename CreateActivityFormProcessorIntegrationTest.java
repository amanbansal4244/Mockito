/* bcwti
 *
 * Copyright (c) 2010 Parametric Technology Corporation (PTC). All Rights Reserved.
 *
 * This software is the confidential and proprietary information of PTC
 * and is subject to the terms of a software license agreement. You shall
 * not disclose such confidential information and shall use it only in accordance
 * with the terms of the license agreement.
 *
 * ecwti
 */
package com.ptc.projectmanagement.plan.processors;
import static com.ptc.projectmanagement.testUtils.ProjectManagementTestUtil.CreateNewActivity;
import static com.ptc.projectmanagement.testUtils.ProjectManagementTestUtil.getPlan;
import static com.ptc.projectmanagement.testUtils.ProjectManagementTestUtil.getProject;
import static org.junit.Assert.assertEquals;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.core.components.forms.ObjectFormProcessorDelegate;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.util.beans.HTTPRequestData;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.netmarkets.util.beans.NmContextBean;
import com.ptc.netmarkets.util.misc.NmContext;
import com.ptc.netmarkets.util.misc.NmContextItem;
import com.ptc.projectmanagement.deliverable.DeliverableHelper;
import com.ptc.projectmanagement.deliverable.TrackingIntentType;
import com.ptc.projectmanagement.plan.DateConstraint;
import com.ptc.projectmanagement.plan.DeadlineBasedOnStartDateHandler;
import com.ptc.projectmanagement.plan.DurationFormat;
import com.ptc.projectmanagement.plan.EPPDeadlineHandler;
import com.ptc.projectmanagement.plan.HealthStatusBasedOnDurationHandler;
import com.ptc.projectmanagement.plan.HealthStatusHandler;
import com.ptc.projectmanagement.plan.HealthStatusType;
import com.ptc.projectmanagement.plan.Plan;
import com.ptc.projectmanagement.plan.PlanActivity;
import com.ptc.projectmanagement.plannable.PlannableHelper;
import com.ptc.projectmanagement.plannable.ScheduleUtils;
import com.ptc.projectmanagement.testUtils.ProjectManagementTestUtil;
import com.ptc.projectmanagement.util.ActivityDeliverablesUtils;
import com.ptc.projectmanagement.util.DurationUtils;
import com.ptc.projectmanagement.util.PlannableUtils;
import com.ptc.projectmanagement.util.ProcessorUtils;
import com.ptc.projectmanagement.util.ProjectManagementActionsUtils;
import com.ptc.test.remote.LoginUtils;
import com.ptc.test.remote.Server;

import wt.fc.PersistenceHelper;
import wt.fc.collections.WTCollection;
import wt.fc.collections.WTHashSet;
import wt.fc.collections.WTSet;
import wt.folder.Cabinet;
import wt.inf.container.OrgContainer;
import wt.inf.container.WTContainerRef;
import wt.inf.team.ContainerTeam;
import wt.inf.team.ContainerTeamReference;
import wt.log4j.LogR;
import wt.method.RemoteMethodServer;
import wt.part.PartHelper;
import wt.part.WTPartMaster;
import wt.projmgmt.admin.Project2;
import wt.projmgmt.deliverable.SubjectOfDeliverable;
import wt.projmgmt.deliverable.VersionedSubjectOfDeliverable;
import wt.services.ac.impl.ACUtils;
import wt.type.ClientTypedUtility;
import wt.type.TypeDefinitionReference;
import wt.util.PrintHelper;
import wt.util.UniquenessHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

@RunWith(Server.class)
public class CreateActivityFormProcessorIntegrationTest {

    private static CreateActivityFormProcessor createActvityFormProcessorToTest;
    private  Plan plan;
    private  PlanActivity planActivity1, planActivity2, planActivity_ASAP_1, planActivity_ASAP_2;
    private  Project2 project;
    private WTContainerRef projectRef;
    private static final String ACT_FIELD_NAME = "activity_name";
    private static final String DU_FIELD_NAME = "duration_unit";
    private static final String DN_FIELD_NAME = "duration_number";
    private static final String TLN_FIELD_NAME = "predecessor";
    private static final String PT_FIELD_NAME = "precedence_type";
    private static final String SO_FIELD_NAME = "schedule_offset";
    private static final String SOT_FIELD_NAME = "schedule_offset_type";
    private static final String RN_FIELD_NAME = "resource_name";
    private static final String RU_FIELD_NAME = "resource_unit";
    private static final String EF_FIELD_NAME = "effort";
    private static final String EFT_FIELD_NAME = "effort_type";
    private static final String FIXED_COST = "fixed_cost";
    private static final String MS_FIELD_NAME = "is_milestone";
    private static final String ED_FIELD_NAME = "effort_driven";
    private static final String TT_FIELD_NAME = "task_type";
    private static final String PLAN_FIELD_NAME = "select_plan";
    private static final String DEL_ACT_VALUE = "deliverableActivityValue";
    private static final String ACTIVITY_DURATION = "activity_duration";
    private static final String REL_POSITION = "select_relative_position";
    private static final String ACTIVITY_DEADLINE_COMPONENT_NAME = "deadline_0_col_deadline_0";
    private static final String RESOURCES = "resources";
    private static final String HEALTH_STATUS_DESCRIPTION = "health_status_description_col_health_status_description";
    private static final String RISK_DESCRIPTION = "risk_desc_col_risk_desc";
    private static final String ACTIVITY_OWNER ="containerInfo.ownerRef_col_containerInfo.ownerRef";
    private static final String ACTIVITY_DESCRIPTION = "activity_description";
    protected static final String ESTIMATED_START_DATE = "startDate_0_col_startDate_0";
    protected static final String ESTIMATED_START_DATE_HOURID = "startDate_0_col_startDate_0_qual_Hours";
    protected static final String ESTIMATED_START_DATE_MINUTEID = "startDate_0_col_startDate_0_qual_Mins";
    protected static final String UTIL_RESOURCE ="wt.util.utilResource";
    private static final String INSERT_BELOW = "insertBelow";
    private static final String CREATE_ACTIVITY_ABOVE = "create_activity_above";
    private static final String CREATE_ACTIVITY_BELOW = "create_activity_below";
    private static final Logger log;
    protected static UniquenessHelper uniqueHelper;


    static {
        try {
            log = LogR.getLogger(CreateActivityFormProcessorIntegrationTest.class.getName());

        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Before
    public void setUp() throws Exception {
        LoginUtils.becomeUser("demo");
        createActvityFormProcessorToTest = new CreateActivityFormProcessor();
        createActvityFormProcessorToTest.setDelegates(new ArrayList<ObjectFormProcessorDelegate>());
        uniqueHelper = new UniquenessHelper();
    }   

    private void createPlanActivityObject() throws WTException, Exception {
        project = getProject("AUTO_ALAP_SIMPLE_TEST_1");
        projectRef = WTContainerRef.newWTContainerRef(project);
        plan = getPlan(project);
        plan = (Plan) PersistenceHelper.manager.refresh(plan);
        plan = (Plan) PersistenceHelper.manager.save(plan);
        log.trace("Plan has been created & shifted to required date, creating activities now..");
        planActivity1 = CreateNewActivity(project, plan, 1, "planActivity1", 11, DateConstraint.ASAP, null);
        planActivity2 = CreateNewActivity(project, plan, 2, "planActivity2", 11, DateConstraint.ASAP, null);
        PlannableHelper.service.propagateSchedule(plan);
    }

    public Object insertActivity(String activityName, PlanActivity selectedActivity, String actionName, String pos) throws WTException, WTPropertyVetoException {

        NmCommandBean clientData = new NmCommandBean();

        NmContextBean contextBean = new NmContextBean();

        NmContext nmContext = NmContext.fromString(new NmOid(selectedActivity).toString());

        Stack contextItems = nmContext.getContextItems();
        if(contextItems.size() > 0){
            NmContextItem contextItem = ( NmContextItem)contextItems.get(0);
            contextItem.setOid(new NmOid(selectedActivity));


            NmContextItem lastContextItem = (NmContextItem) contextItems.lastElement();
            Stack<Object> targetOids = new Stack<>();
            targetOids.add(new NmOid(selectedActivity));
            lastContextItem.getElemAddress().setOids(targetOids);
        }

        contextBean.setContext(nmContext);
        clientData.setContextBean(contextBean);
        clientData.setElementContext(nmContext);
        HashMap<String, Object> textBoxMap = new HashMap<String, Object> ();
        if(CREATE_ACTIVITY_BELOW.equals(actionName)){
            textBoxMap.put(INSERT_BELOW,"true");
        }        
        textBoxMap.put(RESOURCES, null);
        textBoxMap.put(ACT_FIELD_NAME, activityName);
        textBoxMap.put(ACTIVITY_DEADLINE_COMPONENT_NAME, "");
        textBoxMap.put(ESTIMATED_START_DATE, null);
        textBoxMap.put(ACTIVITY_DURATION, "1 day");
        textBoxMap.put(FIXED_COST, ""); 
        textBoxMap.put(ACTIVITY_OWNER, "");        

        clientData.setText(textBoxMap);

        HashMap<String, Object> checkedMap = new HashMap<String, Object> ();
        checkedMap.put(MS_FIELD_NAME, null);
        checkedMap.put(ED_FIELD_NAME, null);
        clientData.setChecked(checkedMap);

        HashMap<Object, Object> textArea = clientData.getTextArea();
        textArea.put(RISK_DESCRIPTION, "");
        textArea.put(HEALTH_STATUS_DESCRIPTION, "");
        textArea.put(ACTIVITY_DESCRIPTION, "");
        clientData.setTextArea(textArea);

        HashMap<String, Object> comboMap  = new HashMap<String, Object> ();
        ArrayList<String> taskTypeList = new ArrayList<String>(1);
        taskTypeList.add("Fixed Units");
        comboMap.put(TT_FIELD_NAME, taskTypeList);

        ArrayList<String> constraintTypeList = new ArrayList<String>(1);
        constraintTypeList.add("ASAP");
        comboMap.put(TT_FIELD_NAME, constraintTypeList);

        ArrayList<String> healthStatusTypeList = new ArrayList<String>(1);
        healthStatusTypeList.add("UNAVAILABLE");
        comboMap.put(TT_FIELD_NAME, healthStatusTypeList);

        ArrayList<String> riskTypeList = new ArrayList<String>(1);
        riskTypeList.add("UNAVAILABLE");
        comboMap.put(TT_FIELD_NAME, riskTypeList);
        
        if(pos!=null){
            ArrayList<String> selectedPlan = new ArrayList<String>(1);
            selectedPlan.add("OR:"+selectedActivity.getParentPlannable().toString());
            comboMap.put(PLAN_FIELD_NAME, selectedPlan);
            
            ArrayList<String> relPos = new ArrayList<String>(1);
            relPos.add(pos);
            comboMap.put(REL_POSITION, relPos);            
        }
        
        

        clientData.setComboBox(comboMap);

        HTTPRequestData a_RequestData =  new HTTPRequestData();
        HashMap<String, Object> requestParameterMap = new HashMap<String, Object>();
        requestParameterMap.put(DU_FIELD_NAME,"DAYS");
        requestParameterMap.put(DN_FIELD_NAME,"1");
        requestParameterMap.put(TLN_FIELD_NAME,selectedActivity.getLineNumber());
        requestParameterMap.put(PT_FIELD_NAME,"FS");
        requestParameterMap.put(SO_FIELD_NAME,"0");
        requestParameterMap.put(SOT_FIELD_NAME,"DAYS");
        requestParameterMap.put(RN_FIELD_NAME,"");
        requestParameterMap.put(RU_FIELD_NAME,"");
        requestParameterMap.put(EFT_FIELD_NAME,"HOURS");
        requestParameterMap.put(EF_FIELD_NAME,"0");
        try {
            a_RequestData.setParameterMap(requestParameterMap);
            clientData.setRequestData(a_RequestData);
        } catch (WTPropertyVetoException e) {           
            e.printStackTrace();
        }

        List<ObjectBean> objectBeans = getObjectBeans();

        String compContext = "project$view_plan$"+new NmOid(project)+"$";
        clientData.setCompContext(compContext);

        if(project !=null){
            clientData.setPrimaryOid(new NmOid(project));
            clientData.getPrimaryOid().setRef(project);
        }
        clientData.getActionOid().setRef(selectedActivity);

        Object result = null;
        try {
            if(CREATE_ACTIVITY_ABOVE.equals(actionName)){
                clientData.addRequestDataParam("actionName", PlannableUtils.CREATE_ACTIVITY_ABOVE, false);
            }            
            else if(ProjectManagementActionsUtils.NEW_PLAN_ACTIVITY.equals(actionName) || ProjectManagementActionsUtils.NEW_PLAN_ACTIVITY_TABLE.equals(actionName)){
                clientData.addRequestDataParam("actionName", actionName, false);                
                clientData.addRequestDataParam(DEL_ACT_VALUE, "OR:"+selectedActivity.getPersistInfo().getObjectIdentifier().getStringValue(), false);
            }
            
            result = createActvityFormProcessorToTest.doOperation(clientData, objectBeans);
        } catch (WTException e) {
            e.printStackTrace();
        }
        return result;
    }


    private List<ObjectBean> getObjectBeans() throws WTException, WTPropertyVetoException {
        NmCommandBean clientDataCmdBean = new NmCommandBean();
        NmCommandBean paramCmdBean = new NmCommandBean();

        clientDataCmdBean.getText().put("activity_name", "activity OLD 1");
        paramCmdBean.getText().put("activity_name", "activity 1");

        ArrayList ttlist = new ArrayList();
        ttlist.add("Fixed Work");
        paramCmdBean.getComboBox().put("task_type", ttlist);
        ArrayList<String> hourList = new ArrayList<String>();
        hourList.add("8 AM");
        ArrayList<String> minList = new ArrayList<String>();
        minList.add("00");
        HashMap<String, Object> comboBox = paramCmdBean.getComboBox();
        comboBox.put(ProjectManagementTestUtil.CONSTRAINT_DATE_HOURS, hourList);
        comboBox.put(ProjectManagementTestUtil.CONSTRAINT_DATE_MINUTES, minList );
        paramCmdBean.setComboBox(comboBox);
        HashMap<String, Object> parameterMap = new HashMap<String, Object>();

        parameterMap.put("duration_unit", new String[] { "DAYS", "DAYS" });
        parameterMap.put("duration_number", new String[] { "1", "2" });
        parameterMap.put("predecessor", new String[] { "1", "2" });
        parameterMap.put("precedence_type", new String[] { "", "" });
        parameterMap.put("schedule_offset", new String[] { "1", "2" });
        parameterMap.put("schedule_offset_type", new String[] { "Hour", "Hour" });
        parameterMap.put("resource_name", new String[] { "R1", "R2" });
        parameterMap.put("resource_unit", new String[] { "100", "100" });
        parameterMap.put("effort_type", new String[] { "Hour", "Hour" });
        parameterMap.put("effort", new String[] { "1", "2" });
        parameterMap.put("is_Row_Updated", new String[] { "true", "true" });
        parameterMap.put("activityOwnerFinalValue", new String[] { "demo", "demo" });
        parameterMap.put(ProjectManagementTestUtil.CONSTRAINT_DATE,new String[] { "", "" });

        ObjectBean[] objectBeans = new ObjectBean[1];
        objectBeans[0] = ObjectBean.newInstance(clientDataCmdBean, paramCmdBean, parameterMap, "1");
        PlanActivity planActInsertedBelow = new PlanActivity();
        planActInsertedBelow.setContainerReference(projectRef);
        objectBeans[0].setObject(planActInsertedBelow);
        List<ObjectBean> objectBeanList = new ArrayList<ObjectBean>();

        objectBeanList.add(objectBeans[0]);
        return objectBeanList;
    }

    @Test
    public void testDoOperation_insertActivityBelow() throws Exception {
        createPlanActivityObject();
        FormResult result = (FormResult)insertActivity("planActivity3", planActivity2, CREATE_ACTIVITY_BELOW, null);
        assertEquals("Insert activity below operation is not successful....", FormProcessingStatus.SUCCESS, result.getStatus());
    }   

    @Test
    public void testDoOperation_insertActivityAbove() throws Exception {
        createPlanActivityObject();
        int sizeBeforeCopy = PlannableHelper.service.getAllPlannables(plan, false).size();
        FormResult result = (FormResult)insertActivity("planActivity4", planActivity2, CREATE_ACTIVITY_ABOVE, null);
        
        plan = (Plan)PersistenceHelper.manager.refresh(plan);
        planActivity2 = (PlanActivity)PersistenceHelper.manager.refresh(planActivity2);
        WTCollection plannables = PlannableHelper.service.getAllPlannables(plan, false);
        PlanActivity newActivity = (PlanActivity)PlannableHelper.service.getPlannableByLineNumber(plan, (planActivity2.getLineNumber() - 1));

        assertEquals("Insert activity above operation is not successful....", FormProcessingStatus.SUCCESS, result.getStatus());
        Assert.assertTrue("plannables size expected as "+(sizeBeforeCopy + 1 )+ " instead found = "+plannables.size(),
                plannables.size() == sizeBeforeCopy + 1);
        Assert.assertTrue("Activity Name of a new activity should be 'planActivity4' instead found = "+newActivity.getName(), newActivity.getName().equals("planActivity4"));
    }
    
    @Test
    public void testDoOperation_newActivityAction() throws Exception{
        createPlanActivityObject();
        plan = (Plan)PersistenceHelper.manager.refresh(plan);
        plan = (Plan)PersistenceHelper.manager.save(plan);
        int sizeBeforeCopy = PlannableHelper.service.getAllPlannables(plan, false).size();
        FormResult result = (FormResult)insertActivity("planActivity4", planActivity2, ProjectManagementActionsUtils.NEW_PLAN_ACTIVITY_TABLE, PlannableUtils.INSERT_ABOVE);
        
        plan = (Plan)PersistenceHelper.manager.refresh(plan);
        planActivity2 = (PlanActivity)PersistenceHelper.manager.refresh(planActivity2);
        WTCollection plannables = PlannableHelper.service.getAllPlannables(plan, false);
        PlanActivity newActivity = (PlanActivity)PlannableHelper.service.getPlannableByLineNumber(plan, (planActivity2.getLineNumber() - 1));

        assertEquals("Insert activity above operation is not successful....", FormProcessingStatus.SUCCESS, result.getStatus());
        Assert.assertTrue("plannables size expected as "+(sizeBeforeCopy + 1 )+ " instead found = "+plannables.size(),
                plannables.size() == sizeBeforeCopy + 1);
        Assert.assertTrue("Activity Name of a new activity should be 'planActivity4' instead found = "+newActivity.getName(), newActivity.getName().equals("planActivity4"));
    }

    /**
     * This is specific to test hasDeliverables and Tracking Policy properties of an activity in DO operation method  
     * @throws Exception 
     */
    @Test
    public void testDoOperation_DeliverablesStep() throws Exception {
        getPlanObject();
        ObjectBean objectBean = ProjectManagementTestUtil.getDeliverabelStepDataObjectBean();

        List<ObjectBean> objectBeanList = new ArrayList<ObjectBean>();
        objectBeanList.add(objectBean);
        NmCommandBean cb = ProjectManagementTestUtil.getDeliverabelStepDataCommandBean(project);

        cb.getChecked().put("is_scheduled_deliverable", true);
        ArrayList<String> trackingPolicy = new ArrayList<String>();
        trackingPolicy.add(TrackingIntentType.FIXED_REVISION.toString());
        cb.getComboBox().put("tracking_policy", trackingPolicy);

        createActvityFormProcessorToTest.doOperation(cb, objectBeanList );

        PlanActivity tempActivity = (PlanActivity) PlannableHelper.service
                .getPlannableByLineNumber(plan, 1);

        Assert.assertTrue("hasDeliverable is not set ",
                tempActivity.isHasDeliverable());
        assertEquals("Tracking policy has not been set",
                TrackingIntentType.FIXED_REVISION,
                tempActivity.getTrackingIntent());

    }


    /**
     * This is specific to test hasDeliverables and Tracking Policy properties of an activity in DO operation method  
     * Test when an activity has not been marked as deliverable
     * @throws Exception 
     */
    @Test
    public void testDoOperation_NoDeliverables() throws Exception {
        getPlanObject();
        ObjectBean objectBean = ProjectManagementTestUtil.getDeliverabelStepDataObjectBean();

        List<ObjectBean> objectBeanList = new ArrayList<ObjectBean>();
        objectBeanList.add(objectBean);
        NmCommandBean cb = ProjectManagementTestUtil.getDeliverabelStepDataCommandBean(project);

        createActvityFormProcessorToTest.doOperation(cb, objectBeanList);

        PlanActivity tempActivity = (PlanActivity) PlannableHelper.service
                .getPlannableByLineNumber(plan, 1);

        Assert.assertFalse("hasDeliverable is not set",
                tempActivity.isHasDeliverable());
        assertEquals("Tracking policy should be null", null,
                tempActivity.getTrackingIntent());

    }

    /**
     * @Usage This Test is used to verify creation of DeliverableActivity with the SubjectOfDeliverable
     * @author acharpe
     * @throws Exception 
     */
    @Test
    public void testDoOperation_withDeliverableSubjects() throws Exception {
        prepareDataForActivityDeliverableSubjectsTest();

    }

    private void prepareDataForActivityDeliverableSubjectsTest()
            throws Exception {
        boolean isSuccess = true;
        getPlanObject();
        final String orgName = "Demo Organization"; 
        ObjectBean objectBean =ProjectManagementTestUtil. getDeliverabelStepDataObjectBean();

        // login as "demo"
        RemoteMethodServer rms = RemoteMethodServer.getDefault();
        rms.setUserName("demo");
        rms.setPassword("demo");
        LoginUtils.becomeUser("demo");

        List<ObjectBean> objectBeanList = new ArrayList<ObjectBean>();
        objectBeanList.add(objectBean);
        NmCommandBean cb = ProjectManagementTestUtil.getDeliverabelStepDataCommandBean(project);

        try {
            OrgContainer orgContainer = ProjectManagementTestUtil.lookupOrgContainer(orgName);
            Cabinet defaultCabinet = orgContainer.getDefaultCabinet();

            String subject1Name = uniqueHelper.qualifyName("Subject1");
            //String subject2Name = uniqueHelper.qualifyName("Subject2");   
            //Create WTParts
            VersionedSubjectOfDeliverable versionedSubject1 = PartHelper.createPart(subject1Name, subject1Name, defaultCabinet, null);
            //VersionedSubjectOfDeliverable versionedSubject2 = PartHelper.createPart(subject2Name, subject2Name, defaultCabinet, null);

            NmOid subject1_Oid = NmOid.newNmOid(versionedSubject1.getPersistInfo().getObjectIdentifier());
            //NmOid subject2_Oid = NmOid.newNmOid(versionedSubject2.getPersistInfo().getObjectIdentifier());
            HashMap subjectItemsMap = new HashMap();
            ArrayList subjectOidList = new ArrayList();
            subjectOidList.add(subject1_Oid);
            //subjectOidList.add(subject2_Oid);
            subjectItemsMap.put(ActivityDeliverablesUtils.DELIVERABLEACTIVITY_SUBJECT_TABLE_ID, subjectOidList);
            cb.setAddedItems(subjectItemsMap);

            cb.getChecked().put("is_scheduled_deliverable", true);
            ArrayList<String> trackingPolicy = new ArrayList<String>();
            trackingPolicy.add(TrackingIntentType.FIXED_REVISION.toString());
            cb.getComboBox().put("tracking_policy", trackingPolicy);

            createActvityFormProcessorToTest.doOperation(cb, objectBeanList );

            PlanActivity tempActivity = (PlanActivity) PlannableHelper.service.getPlannableByLineNumber(plan, 1);

            Assert.assertTrue("hasDeliverable is not set ", tempActivity.isHasDeliverable());
            assertEquals("Tracking policy has not been set", TrackingIntentType.FIXED_REVISION, tempActivity.getTrackingIntent());

            assertActivityDeliverableSubjectsTest(subject1Name);

        } catch (Exception e) {
            e.printStackTrace();
            isSuccess= false;
        }

        Assert.assertTrue("Test Failed because of exception.", isSuccess);
    }

    private void assertActivityDeliverableSubjectsTest(String subject1Name) throws WTException {
        PlanActivity planAct1 = (PlanActivity) PlannableHelper.service.getPlannableByLineNumber(plan, 1);
        planAct1 = (PlanActivity)PersistenceHelper.manager.refresh(planAct1);
        WTCollection actualSubjects = DeliverableHelper.service.getAllDeliverableSubjects(planAct1);

        ArrayList<String> actualSubjectNames = new ArrayList<String>();
        SubjectOfDeliverable subject = null;
        if(actualSubjects != null && !actualSubjects.isEmpty()) {
            for(Iterator<SubjectOfDeliverable> subjIter = actualSubjects.persistableIterator(); subjIter.hasNext();) {
                subject = subjIter.next();
                if(subject instanceof WTPartMaster) {
                    actualSubjectNames.add(((WTPartMaster)subject).getName());
                }
            }
        }

        Assert.assertTrue("Subject is not associated with the activity.", subject1Name.equals(((WTPartMaster)subject).getName()));
    }

    private Plan getPlanObject() throws Exception {
        try {
            project = getProject("Test_Plan1");
            projectRef = WTContainerRef.newWTContainerRef(project);
            plan = getPlan(project);
            plan = (Plan) PersistenceHelper.manager.refresh(plan);
        } catch (WTException we) {
            we.printStackTrace();
        } catch (WTPropertyVetoException wve) {
            wve.printStackTrace();
        }
        return plan;
    }

    @Test
    public void testPostProcess_DispatchEvent() throws Exception{
        uniqueHelper.setDefaultMaxNameLen(40);
        String typeName = uniqueHelper.getUniqueName("org.rnd.VW_AutoHealthStatus");
        String planName = uniqueHelper.getUniqueName("VW_PlanActivity_SubType");
        String actName = uniqueHelper.getUniqueName("VW_PlanActivity_SubType_Act");
        final String orgName = "Demo Organization";
        Project2 testProject = null;
        final String projectName;
        // login as windchill admin

        log.debug("Login as admin.");
        LoginUtils.becomeAdminUser();

        ProjectManagementTestUtil.createSubType(PlanActivity.class.getName(), typeName, new String[]{PlanActivity.HEALTH_STATUS_UPDATE_MODE});

        ACUtils.addTestService(
                HealthStatusHandler.class, // Service class
                typeName, // selector
                null, // requestor
                new HealthStatusBasedOnDurationHandler()); // implementation

        try{           
            NmCommandBean clientData = new NmCommandBean();
            OrgContainer orgContainer = ProjectManagementTestUtil.lookupOrgContainer(orgName);
            ContainerTeam containerTeam = ProjectManagementTestUtil.getContainerTeam(orgContainer);
            projectName = uniqueHelper.qualifyName("TestProject");
            testProject = ProjectManagementTestUtil.createProject(projectName, orgContainer, null, ContainerTeamReference.newContainerTeamReference(containerTeam), false);
            WTContainerRef contextRef = WTContainerRef.newWTContainerRef(testProject);
            Plan plan = Plan.newPlan(contextRef);
            plan.setName(planName);
            plan = (Plan)PersistenceHelper.manager.store(plan);

            log.debug("Create New Activity and set its Duration to 1 Day and dispatch the EPP_CREATE_ACTIVITY, which would invoke the custom handler HealthStatusBasedOnDurationHandler and calculate the health status.");
            PlanActivity activity = PlanActivity.newPlanActivity(contextRef);

            log.debug("set the type definition reference to the activity to set the correct type, so that correct handler gets invoked when EPP_CREATE_ACTIVITY event is invoked.");
            TypeDefinitionReference typeDefinitionReference = ClientTypedUtility.getTypeDefinitionReference(PlanActivity.class.getName() + "|" + typeName);
            activity.setTypeDefinitionReference(typeDefinitionReference);
            activity.setName(actName);
            activity = (PlanActivity)PlannableHelper.service.addPlannable(activity, plan);
            activity = (PlanActivity)PersistenceHelper.manager.refresh(activity);
            activity.setHasDeliverable(true);

            activity.getDuration().setMillis(DurationUtils.toMillis(1, DurationFormat.DAYS));
            activity = (PlanActivity)PersistenceHelper.manager.save(activity);
            ObjectBean[] objectBeans = new ObjectBean[1];

            objectBeans[0] = ObjectBean.newInstance();
            objectBeans[0].setObject(activity);
            List<ObjectBean> objectBeanList = new ArrayList<ObjectBean>();
            objectBeanList.add(objectBeans[0]);    

            createActvityFormProcessorToTest.postProcess(clientData, objectBeanList);
            activity = (PlanActivity)PersistenceHelper.manager.refresh(activity);

            Assert.assertTrue("After dispatching event, the status should be Yellow.Health Status ="+ activity.getHealthStatusType(),HealthStatusType.YELLOW.equals(activity.getHealthStatusType()));
        }
        catch (Exception e) {
            e.printStackTrace();           
        }
    }

    @Test
    public void testPostProcess_DeadlineDispatchEvent() throws Exception{
        uniqueHelper.setDefaultMaxNameLen(40);
        String typeName = uniqueHelper.getUniqueName("org.rnd.VW_AutoDeadline");
        String planName = uniqueHelper.getUniqueName("VW_PlanActivity_SubType");
        String actName = uniqueHelper.getUniqueName("VW_PlanActivity_SubType_Act");
        final String orgName = "Demo Organization";
        Project2 testProject = null;
        final String projectName;
        // login as windchill admin

        log.debug("Login as admin.");
        LoginUtils.becomeAdminUser();

        ProjectManagementTestUtil.createSubType(PlanActivity.class.getName(), typeName, new String[]{PlanActivity.DEADLINE_CALCULATION_MODE});

        ACUtils.addTestService(
                EPPDeadlineHandler.class,// Service class
                typeName, // selector
                null, // requestor
                new DeadlineBasedOnStartDateHandler()); // implementation

        try{           
            NmCommandBean clientData = new NmCommandBean();
            OrgContainer orgContainer = ProjectManagementTestUtil.lookupOrgContainer(orgName);
            ContainerTeam containerTeam = ProjectManagementTestUtil.getContainerTeam(orgContainer);
            projectName = uniqueHelper.qualifyName("TestProject");
            testProject = ProjectManagementTestUtil.createProject(projectName, orgContainer, null, ContainerTeamReference.newContainerTeamReference(containerTeam), false);
            WTContainerRef contextRef = WTContainerRef.newWTContainerRef(testProject);
            Plan plan = Plan.newPlan(contextRef);
            plan.setName(planName);
            plan = (Plan)PersistenceHelper.manager.store(plan);

            log.debug("Create New Activity and set its Duration to 1 Day and dispatch the EPP_CREATE_ACTIVITY, which would invoke the custom handler HealthStatusBasedOnDurationHandler and calculate the health status.");
            PlanActivity activity = PlanActivity.newPlanActivity(contextRef);

            log.debug("set the type definition reference to the activity to set the correct type, so that correct handler gets invoked when EPP_CREATE_ACTIVITY event is invoked.");
            TypeDefinitionReference typeDefinitionReference = ClientTypedUtility.getTypeDefinitionReference(PlanActivity.class.getName() + "|" + typeName);
            activity.setTypeDefinitionReference(typeDefinitionReference);
            activity.setName(actName);
            activity = (PlanActivity)PlannableHelper.service.addPlannable(activity, plan);
            activity = (PlanActivity)PersistenceHelper.manager.refresh(activity);
            activity.setHasDeliverable(true);

            Timestamp currentTimeStamp = new Timestamp(System.currentTimeMillis());
            activity.setStartDate(currentTimeStamp);
            activity = (PlanActivity)PersistenceHelper.manager.save(activity);

            Timestamp expectedDeadline = addDays(10, currentTimeStamp);

            ObjectBean[] objectBeans = new ObjectBean[1];

            objectBeans[0] = ObjectBean.newInstance();
            objectBeans[0].setObject(activity);
            List<ObjectBean> objectBeanList = new ArrayList<ObjectBean>();
            objectBeanList.add(objectBeans[0]);    

            createActvityFormProcessorToTest.postProcess(clientData, objectBeanList);
            activity = (PlanActivity)PersistenceHelper.manager.refresh(activity);

            System.out.println("After dispatching event, the deadline should be CurrentDate + 10 Days. ExpectedDeadline="+ expectedDeadline 
                    + " Actual Deadline ="+activity.getDeadline());

            Assert.assertTrue("After dispatching event, the deadline should be CurrentDate + 10 Days. ExpectedDeadline="+ expectedDeadline 
                    + " Actual Deadline ="+activity.getDeadline(),compare(expectedDeadline,activity.getDeadline()));

        }
        catch (Exception e) {
            e.printStackTrace();           
        }
    }
    
    /**
     * Test when plan is in auto execution mode.
     * This is specific to test newly created ASAP activity's finish date and plan's finish date would be adjusted according to the new ASAP activity's finish date.        
     * @throws Exception 
     */    
    @Server.Rollback
    @Test
    public void test_createActivityInAutoExecutionPlan() throws Exception{
        OrgContainer orgContainer;
        final String orgName = "Demo Organization";
        boolean autoExecution = true;
        StartPlanFormProcessor startPlanFormProcessorToTest = new StartPlanFormProcessor();   
        CreateActivityFormProcessor createActivityFormProcessorTest = new CreateActivityFormProcessor();

        log.debug("Login as admin.");
        LoginUtils.becomeAdminUser();

        try{           
            log.debug("Create a project");
            UniquenessHelper uniqueHelper = new UniquenessHelper();
            String projectName = uniqueHelper.qualifyName("TestProject");
            Project2 project = ProjectManagementTestUtil.lookupProject(projectName);
            if (project == null) {
                orgContainer = ProjectManagementTestUtil.lookupOrgContainer(orgName);
                ContainerTeam containerTeam = ProjectManagementTestUtil.getContainerTeam(orgContainer);
                project = ProjectManagementTestUtil.createProject(projectName, orgContainer, null, ContainerTeamReference.newContainerTeamReference(containerTeam), false);
            }

            log.debug("Create a new plan");
            plan = ProjectManagementTestUtil.createNewPlan(project);

            log.debug("Set plan's exeutiion mode to AUTOEXECUTION.");
            plan.setAutoExecution(autoExecution);
            plan = (Plan) PersistenceHelper.manager.save(plan);

            log.debug("Create an activity");
            planActivity_ASAP_1 = ProjectManagementTestUtil.CreateNewActivity(project, plan, 1, "activity1", 1, DateConstraint.ASAP, null);
            planActivity_ASAP_1.setStartDate(plan.getStartDate());
            planActivity_ASAP_1 = (PlanActivity) PersistenceHelper.manager.save(planActivity_ASAP_1);
            ObjectBean[] objectBeans = new ObjectBean[1];

            objectBeans[0] = ObjectBean.newInstance();
            objectBeans[0].setObject(planActivity_ASAP_1);
            List<ObjectBean> objectBeanList = new ArrayList<ObjectBean>();
            objectBeanList.add(objectBeans[0]);    

            NmCommandBean clientData = new NmCommandBean();
            plan = (Plan) PersistenceHelper.manager.refresh(plan);
            clientData.setPrimaryOid(new NmOid(plan));
            clientData.getPrimaryOid().setRef(plan);

            log.debug("Start the auto execution of the plan.");
            startPlanFormProcessorToTest.doOperation(clientData, objectBeanList);

            planActivity_ASAP_1 = (PlanActivity) PersistenceHelper.manager.refresh(planActivity_ASAP_1);
            plan = (Plan) PersistenceHelper.manager.refresh(plan);

            Thread.sleep(60000);
            Timestamp currentTime = ProcessorUtils.getAdjustedCurrentTime(plan, new Date());

            log.debug("Create another ASAP activity after 1 min.");
            objectBeans[0] = null; 
            objectBeanList = null;
            planActivity_ASAP_2 = ProjectManagementTestUtil.CreateNewActivity(project, plan, 1, "activity2", 1, DateConstraint.ASAP, null);

            objectBeans[0] = ObjectBean.newInstance();
            objectBeans[0].setObject(planActivity_ASAP_2);
            objectBeanList = new ArrayList<ObjectBean>();
            objectBeanList.add(objectBeans[0]);

            HTTPRequestData a_RequestData =  new HTTPRequestData();
            HashMap<String, Object> requestParameterMap = new HashMap<String, Object>();
            requestParameterMap.put(DU_FIELD_NAME,"DAYS");
            requestParameterMap.put(DN_FIELD_NAME,"1");
            requestParameterMap.put(PT_FIELD_NAME,"FS");
            requestParameterMap.put(SO_FIELD_NAME,"0");
            requestParameterMap.put(SOT_FIELD_NAME,"DAYS");
            requestParameterMap.put(RN_FIELD_NAME,"");
            requestParameterMap.put(RU_FIELD_NAME,"");
            requestParameterMap.put(EFT_FIELD_NAME,"HOURS");
            requestParameterMap.put(EF_FIELD_NAME,"0");
            try {
                a_RequestData.setParameterMap(requestParameterMap);
                clientData.setRequestData(a_RequestData);
            } catch (WTPropertyVetoException e) {           
                e.printStackTrace();
            }

            createActivityFormProcessorTest.doOperation(clientData, objectBeanList);
            planActivity_ASAP_2 = (PlanActivity) PersistenceHelper.manager.refresh(planActivity_ASAP_2);
            Timestamp calculatedFinish = ScheduleUtils.addWorkingDuration(currentTime, planActivity_ASAP_2.getDuration().getMillis());
            plan = (Plan) PersistenceHelper.manager.refresh(plan);
            
            log.debug("Verify the new ASAP Activity's finish date.");
            Assert.assertTrue("Activity's finish date would be "+planActivity_ASAP_2.getFinishDate(), planActivity_ASAP_2.getFinishDate().equals(calculatedFinish));
            
            log.debug("Verify the plan's finish date and new ASAP activity's finish date.");
            Assert.assertTrue("After creating another activity plan's finish date will be = "+plan.getFinishDate(), plan.getFinishDate().equals(planActivity_ASAP_2.getFinishDate()));

        }
        catch (Exception e) {
            e.printStackTrace();           
        }
    }

    @After
    public void tearDown() throws Exception {
        /* Removing the temporary created objects*/
        try{

            WTCollection activities = PlannableHelper.service.getAllPlannables(plan, false);
            WTSet activitySet = new WTHashSet(activities);
            PersistenceHelper.manager.delete(activitySet);

            plan = (Plan) PersistenceHelper.manager.refresh(plan);
            PersistenceHelper.manager.delete(plan);

            project = (Project2) PersistenceHelper.manager.refresh(project);
            PersistenceHelper.manager.delete(project);

            plan = null;
            project = null; 
        }catch(Exception e){
            PrintHelper.print("tearDown():Exception :"+e.getMessage()); 
        }

    }

    private Long dayToMiliseconds(int days){
        Long result = Long.valueOf(days * 24 * 60 * 60 * 1000);
        return result;
    }

    private Timestamp addDays(int days, Timestamp t1) throws Exception{
        if(days < 0){
            throw new Exception("Day in wrong format.");
        }
        Long miliseconds = dayToMiliseconds(days);
        return new Timestamp(t1.getTime() + miliseconds);
    }

    private boolean compare(Timestamp t1 , Timestamp t2){

        if(t1 != null && t2 != null){
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(t1);
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(t2);
            if(cal1.get(Calendar.DATE) == cal2.get(Calendar.DATE) 
                    && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) 
                    &&  cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)){
                return true;
            }
        }

        return false;
    }

}