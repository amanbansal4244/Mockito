package com.ptc.projectmanagement.plan.processors;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.FormResult;
import com.ptc.core.components.forms.ObjectFormProcessorDelegate;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.util.beans.NmClipboardBean;
import com.ptc.netmarkets.util.beans.NmClipboardItem;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.netmarkets.util.misc.NmContext;
import com.ptc.netmarkets.util.misc.NmContextItem;
import com.ptc.projectmanagement.assignment.AssignmentHelper;
import com.ptc.projectmanagement.assignment.ResourceAssignment;
import com.ptc.projectmanagement.assignment.ResourceAssignmentType;
import com.ptc.projectmanagement.assignment.SharedAssignmentDetails;
import com.ptc.projectmanagement.assignment.resource.PlanResource;
import com.ptc.projectmanagement.assignment.resource.ResourceHelper;
import com.ptc.projectmanagement.assignment.resource.Resourceable;
import com.ptc.projectmanagement.deliverable.TrackingIntentType;
import com.ptc.projectmanagement.plan.DateConstraint;
import com.ptc.projectmanagement.plan.Duration;
import com.ptc.projectmanagement.plan.HealthStatusType;
import com.ptc.projectmanagement.plan.Plan;
import com.ptc.projectmanagement.plan.PlanActivity;
import com.ptc.projectmanagement.plan.PlanIXTestUtil;
import com.ptc.projectmanagement.plan.Plannable;
import com.ptc.projectmanagement.plan.RiskType;
import com.ptc.projectmanagement.plan.TaskType;
import com.ptc.projectmanagement.plan.ilog.PlanWorkingHourHandler;
import com.ptc.projectmanagement.plannable.PlannableHelper;
import com.ptc.projectmanagement.plannable.PrecedenceConstraint;
import com.ptc.projectmanagement.plannable.PrecedenceType;
import com.ptc.projectmanagement.plannable.ScheduleUtils;
import com.ptc.projectmanagement.plannable.StandardPlannableService;
import com.ptc.projectmanagement.testUtils.ProjectManagementTestUtil;
import com.ptc.projectmanagement.util.DurationUtils;
import com.ptc.projectmanagement.util.PasteActionUtil;
import com.ptc.projectmanagement.util.PlannableUtils;
import com.ptc.projectmanagement.util.ProcessorUtils;
import com.ptc.test.remote.LoginUtils;
import com.ptc.test.remote.Server;
import com.ptc.windchill.gantt2.common.WorkingHourHandler;

import wt.access.SecurityLabelsTestHelper;
import wt.fc.ObjectNoLongerExistsException;
import wt.fc.ObjectReference;
import wt.fc.PersistenceHelper;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTCollection;
import wt.inf.container.OrgContainer;
import wt.inf.container.WTContainerRef;
import wt.org.TimeZoneHelper;
import wt.ownership.Ownership;
import wt.projmgmt.admin.Project2;
import wt.services.ac.impl.ACUtils;
import wt.session.SessionHelper;
import wt.type.ClientTypedUtility;
import wt.type.TypeDefinitionReference;
import wt.util.UniquenessHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

@RunWith(Server.class)
public class PlanPasteProcessorIntegrationTest {

    
    public PlanPasteProcessor classToTest=null;
    public static NmCommandBean clientData = null;
    public List<ObjectBean> objectBeans = null;
    private UniquenessHelper uniqueHelper = new UniquenessHelper();
    static final List<String> DONOT_PERSIST_PLANACTIVITY_LEVEL_ATTRIBUTES = Arrays.asList("healthStatusDescription", "healthStatusType", "percentWorkComplete",
            "riskType");
    
    static final List<String> DONOT_PERSIST_RESOURCE_ASSIGNMENT_ATTRIBUTES = Arrays.asList("percentWorkComplete", "DoneEffort", "remainingEffort", "healthStatusType", 
            "healthStatusDescription",  "riskType", "riskDescription");
    
    static final List<String> PERSIST_RESOURCE_ASSIGNMENT_ATTRIBUTES_CUT_FOR_SAME_PLAN = Arrays.asList("percentWorkComplete", "DoneEffort", "remainingEffort", "healthStatusType", 
            "healthStatusDescription",  "riskType", "riskDescription");
    
    ArrayList<NmClipboardItem> clipBoardItems;
    NmClipboardItem clipBoardItem;
    NmClipboardBean clipBean;
    PlanActivity activity1=null ,activity2 = null,activity3 = null,activity4 = null, activity1Source = null; 
    PlanActivity act1 = null, act2 = null, act3 = null, actSNETType = null, activitySNETSource = null;
    PlanActivity activityR1 = null, activityR2 = null, activityR3 = null, activityR4 = null , resAssignmentActSource = null;
    PlanActivity activityM1 = null, activityM2 = null, activityM3 = null, activityMFO4 = null , activityMFOSource = null;
    PlanActivity activityA = null, activityB = null, activityC = null,  activitySucc = null , activityPred = null, activityPredSource = null, activitySuccSource = null;
    NmOid targetPlanOid = null, targetSNETPlanOid = null,targetMFOTypePlanOid = null, targetSuccPredTypePlanOid = null, targetResAssignmentPlanOid = null; 
    NmOid activity1Oid = null;
    Plan targetPlan =  null , sourcePlan = null, targetSNETPlan = null, sourceSuccPredTypePlan =  null,sourceSNETPlan =null, sourcePlanWithMFOActivity = null, targetPlanWithMFOActivity = null, targetSuccPredTypePlan = null, targetResAssignmentPlan = null, targetResAssignmentsSourcePlan = null;
    ArrayList<NmOid> clippedObjects = new ArrayList<NmOid>();
    Project2 project = null;
    WTContainerRef projectRef = null;
    private static final String orgName = "Demo Organization";
    private static final boolean CUT = true;
    private static final boolean SNET = true;
    private static final boolean SAME_PLAN = true;
    private static final boolean targetPlanStartDateBeforeCurrentDay = true;
    public void createPlanAndActivities(){
        try{
            targetPlan = Plan.newPlan(projectRef);
            targetPlan.setName("TargetPlan");
            targetPlan = (Plan)PersistenceHelper.manager.store(targetPlan);

            activity1 = PlanActivity.newPlanActivity(projectRef);
            activity1.setLineNumber(1);activity1.setName("activity1");
            activity1 = (PlanActivity)PlannableHelper.service.addPlannable(activity1, targetPlan);

            targetPlan = (Plan)PersistenceHelper.manager.refresh(targetPlan);
            activity2 = PlanActivity.newPlanActivity(projectRef);
            activity2.setLineNumber(2); activity2.setName("activity2");
            activity2 = (PlanActivity)PlannableHelper.service.addPlannable(activity2, targetPlan);

            targetPlan = (Plan)PersistenceHelper.manager.refresh(targetPlan);
            activity3 = PlanActivity.newPlanActivity(projectRef);
            activity3.setLineNumber(3); activity3.setName("activity3");
            activity3 = (PlanActivity)PlannableHelper.service.addPlannable(activity3, targetPlan);

            targetPlan = (Plan)PersistenceHelper.manager.refresh(targetPlan);
            targetPlanOid = NmOid.newNmOid(targetPlan.getPersistInfo().getObjectIdentifier());

            sourcePlan = Plan.newPlan(projectRef);
            sourcePlan.setName("SourcePlan");
            sourcePlan = (Plan)PersistenceHelper.manager.store(sourcePlan);

            sourcePlan = (Plan)PersistenceHelper.manager.refresh(sourcePlan);
            activity1Source = PlanActivity.newPlanActivity(projectRef);
            activity1Source.setLineNumber(1);activity1Source.setName("activity1Source");
            activity1Source = (PlanActivity)PlannableHelper.service.addPlannable(activity1Source, sourcePlan);
        }catch(Exception ae){
            ae.printStackTrace(); 
        }
    }
    
    private void createSNETTypeActivity() throws Exception{
        targetSNETPlan = Plan.newPlan(projectRef);
        targetSNETPlan.setName("targetSNETPlan");
        
        targetSNETPlan = (Plan)PersistenceHelper.manager.store(targetSNETPlan);
        
        /**** set time as 08:00:00:0 to target plan start date ****/
        targetSNETPlan = setTimeToStartDayTime(targetSNETPlan);
        
        /**** create constraint date of more than 10 days of target plan start date ****/
        Timestamp constraintDateForTargetPlanActivity = calculateConstraintDateOfActivity(targetSNETPlan, 80, SNET);
       
        act1 = PlanActivity.newPlanActivity(projectRef);
        act1.setLineNumber(1);act1.setName("act1");
        act1 = (PlanActivity)PlannableHelper.service.addPlannable(act1, targetSNETPlan);   

        targetSNETPlan = (Plan)PersistenceHelper.manager.refresh(targetSNETPlan);
        act2 = PlanActivity.newPlanActivity(projectRef);
        act2.setLineNumber(2); act2.setName("act2");
        act2 = (PlanActivity)PlannableHelper.service.addPlannable(act2, targetSNETPlan);

        targetSNETPlan = (Plan)PersistenceHelper.manager.refresh(targetSNETPlan);
        act3 = PlanActivity.newPlanActivity(projectRef);
        act3.setLineNumber(3); act3.setName("act3");
        act3 = (PlanActivity)PlannableHelper.service.addPlannable(act3, targetSNETPlan);

        targetSNETPlan = (Plan)PersistenceHelper.manager.refresh(targetSNETPlan);
        actSNETType = ProjectManagementTestUtil.CreateNewActivity(project, targetSNETPlan, 4, "actSNETType", 4,DateConstraint.SNET, null,constraintDateForTargetPlanActivity);
        actSNETType = (PlanActivity)PlannableHelper.service.addPlannable(actSNETType, targetSNETPlan);

        targetSNETPlan = (Plan)PersistenceHelper.manager.refresh(targetSNETPlan);

        targetSNETPlanOid = NmOid.newNmOid(targetSNETPlan.getPersistInfo().getObjectIdentifier());
        
        sourceSNETPlan = Plan.newPlan(projectRef);
        sourceSNETPlan.setName("sourceSNETPlan");
        sourceSNETPlan = (Plan)PersistenceHelper.manager.store(sourceSNETPlan);
        
        /**** set time as 08:00:00:0 to source plan start date ****/
        sourceSNETPlan = setTimeToStartDayTime(sourceSNETPlan);
        
        /**** create constraint date of more than 4 days of source plan start date ****/
        Timestamp constraintDateForSourcePlanActivity = calculateConstraintDateOfActivity(sourceSNETPlan, 32, SNET);
        
        activitySNETSource = PlanActivity.newPlanActivity(projectRef);
        activitySNETSource = ProjectManagementTestUtil.CreateNewActivity(project, sourceSNETPlan, 1, "activitySNETSource", 4,DateConstraint.SNET, null,constraintDateForSourcePlanActivity);
        activitySNETSource = (PlanActivity)PlannableHelper.service.addPlannable(activitySNETSource, sourceSNETPlan);
        PlannableHelper.service.propagateSchedule(sourceSNETPlan);
        PlannableHelper.service.propagateSchedule(targetSNETPlan);
    }

    private void createrSuccessorPredessorTypeActivity() throws WTPropertyVetoException, WTException{
        targetSuccPredTypePlan = Plan.newPlan(projectRef);
        targetSuccPredTypePlan.setName("targetSuccPredTypePlan");
        targetSuccPredTypePlan = (Plan)PersistenceHelper.manager.store(targetSuccPredTypePlan);
       
        activityA = PlanActivity.newPlanActivity(projectRef);
        activityA.setLineNumber(1);activityA.setName("activityA");
        activityA = (PlanActivity)PlannableHelper.service.addPlannable(activityA, targetSuccPredTypePlan);   
        
        targetSuccPredTypePlan = (Plan)PersistenceHelper.manager.refresh(targetSuccPredTypePlan);
        activityB = PlanActivity.newPlanActivity(projectRef);
        activityB.setLineNumber(2); activityB.setName("activityB");
        activityB = (PlanActivity)PlannableHelper.service.addPlannable(activityB, targetSuccPredTypePlan);

        targetSuccPredTypePlan = (Plan)PersistenceHelper.manager.refresh(targetSuccPredTypePlan);
        activityC = PlanActivity.newPlanActivity(projectRef);
        activityC.setLineNumber(3); activityC.setName("activityC");
        activityC = (PlanActivity)PlannableHelper.service.addPlannable(activityC, targetSuccPredTypePlan);
        
        targetSuccPredTypePlan = (Plan)PersistenceHelper.manager.refresh(targetSuccPredTypePlan);
        activityPred = PlanActivity.newPlanActivity(projectRef);
        activityPred.setLineNumber(4); activityPred.setName("activityPred");
        activityPred = (PlanActivity)PlannableHelper.service.addPlannable(activityPred, targetSuccPredTypePlan);

        targetSuccPredTypePlan = (Plan)PersistenceHelper.manager.refresh(targetSuccPredTypePlan);
        activitySucc = PlanActivity.newPlanActivity(projectRef);
        activitySucc.setLineNumber(5); activitySucc.setName("activitySucc");
        activitySucc = (PlanActivity)PlannableHelper.service.addPlannable(activitySucc, targetSuccPredTypePlan);
        
        targetSuccPredTypePlan = (Plan)PersistenceHelper.manager.refresh(targetSuccPredTypePlan);
        PrecedenceType preceConstraint=PrecedenceType.toPrecedenceType("FS");
        PrecedenceConstraint cons1 = PlannableHelper.service.addPrecedenceConstraint(activitySucc, activityPred, preceConstraint);
        
        targetSuccPredTypePlanOid = NmOid.newNmOid(targetSuccPredTypePlan.getPersistInfo().getObjectIdentifier());
        
        sourceSuccPredTypePlan = Plan.newPlan(projectRef);
        sourceSuccPredTypePlan.setName("sourceSuccPredTypePlan");
        sourceSuccPredTypePlan = (Plan)PersistenceHelper.manager.store(sourceSuccPredTypePlan);

        sourceSuccPredTypePlan = (Plan)PersistenceHelper.manager.refresh(sourceSuccPredTypePlan);
        activityPredSource = PlanActivity.newPlanActivity(projectRef);
        activityPredSource.setLineNumber(1);activityPredSource.setName("activityPredSource");
        activityPredSource = (PlanActivity)PlannableHelper.service.addPlannable(activityPredSource, sourceSuccPredTypePlan);

        sourceSuccPredTypePlan = (Plan)PersistenceHelper.manager.refresh(sourceSuccPredTypePlan);
        activitySuccSource = PlanActivity.newPlanActivity(projectRef);
        activitySuccSource.setLineNumber(2);activitySuccSource.setName("activitySuccSource");
        activitySuccSource = (PlanActivity)PlannableHelper.service.addPlannable(activitySuccSource, sourceSuccPredTypePlan);

        PrecedenceType preceConstraintSource=PrecedenceType.toPrecedenceType("FS");
        PrecedenceConstraint cons2 = PlannableHelper.service.addPrecedenceConstraint(activitySuccSource, activityPredSource, preceConstraintSource);;
    }
    
    private void createrMFOTypeActivity() throws Exception{
        targetPlanWithMFOActivity = ProjectManagementTestUtil.getPlan(project,"2014-07-14");
        targetPlanWithMFOActivity.setName("Target Plan");
  
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        formatter.setTimeZone(TimeZoneHelper.getTimeZone());
        Timestamp MFODateForTargetPlan = new Timestamp(formatter.parse("2014-07-25 17:00").getTime());
        
        activityM1 = ProjectManagementTestUtil.CreateNewActivity(project, targetPlanWithMFOActivity, 1,         "activityM1",         2,     DateConstraint.ASAP, null);
        activityM2 = ProjectManagementTestUtil.CreateNewActivity(project, targetPlanWithMFOActivity, 2,         "activityM2",         1,     DateConstraint.ASAP, null);
        activityM3 = ProjectManagementTestUtil.CreateNewActivity(project, targetPlanWithMFOActivity, 3,         "activityM2",         4,     DateConstraint.ASAP, null);
        activityMFO4 = ProjectManagementTestUtil.CreateNewActivity(project, targetPlanWithMFOActivity, 4,       "activityMFO4",       5,   DateConstraint.MFO, null,MFODateForTargetPlan);
        
        PlannableHelper.service.propagateSchedule(targetPlanWithMFOActivity);

        targetPlanWithMFOActivity = (Plan)PersistenceHelper.manager.refresh(targetPlanWithMFOActivity);

        targetMFOTypePlanOid = NmOid.newNmOid(targetPlanWithMFOActivity.getPersistInfo().getObjectIdentifier());

        sourcePlanWithMFOActivity = ProjectManagementTestUtil.getPlan(project,"2014-07-14");
        sourcePlanWithMFOActivity.setName("Source Plan");

        Timestamp MFODateForSourcePlan = new Timestamp(formatter.parse(("2014-07-17 17:00")).getTime());
        
        activityM1 = ProjectManagementTestUtil.CreateNewActivity(project, sourcePlanWithMFOActivity, 1,         "activity1",         1,     DateConstraint.ASAP, null);
        activityMFOSource = ProjectManagementTestUtil.CreateNewActivity(project, sourcePlanWithMFOActivity, 2, "activityMFOSource", 1.0,DateConstraint.MFO, null,MFODateForSourcePlan);
        PlannableHelper.service.propagateSchedule(sourcePlanWithMFOActivity);
        sourcePlanWithMFOActivity = (Plan)PersistenceHelper.manager.refresh(sourcePlanWithMFOActivity);
    }
    
    private void createrResAssignmentTypeActivity() throws WTPropertyVetoException, WTException{
        targetResAssignmentPlan = Plan.newPlan(projectRef);
        targetResAssignmentPlan.setName("targetResAssignmentsPlan");
        targetResAssignmentPlan = (Plan)PersistenceHelper.manager.store(targetResAssignmentPlan);
       
        activityR1 = PlanActivity.newPlanActivity(projectRef);
        activityR1.setLineNumber(1);activityR1.setName("activityR1");
        activityR1 = (PlanActivity)PlannableHelper.service.addPlannable(activityR1, targetResAssignmentPlan);   
        
        targetResAssignmentPlan = (Plan)PersistenceHelper.manager.refresh(targetResAssignmentPlan);
        activityR2 = PlanActivity.newPlanActivity(projectRef);
        activityR2.setLineNumber(2); activityR2.setName("activityR2");
        activityR2 = (PlanActivity)PlannableHelper.service.addPlannable(activityR2, targetResAssignmentPlan);

        targetResAssignmentPlan = (Plan)PersistenceHelper.manager.refresh(targetResAssignmentPlan);
        activityR3 = PlanActivity.newPlanActivity(projectRef);
        activityR3.setLineNumber(3); activityR3.setName("activityR3");
        activityR3 = (PlanActivity)PlannableHelper.service.addPlannable(activityR3, targetResAssignmentPlan);
        
        targetResAssignmentPlan = (Plan)PersistenceHelper.manager.refresh(targetResAssignmentPlan);
        activityR4 = PlanActivity.newPlanActivity(projectRef);
        activityR4.setLineNumber(4); activityR4.setName("activityR4");
        activityR4 = (PlanActivity)PlannableHelper.service.addPlannable(activityR4, targetResAssignmentPlan);
        
        targetResAssignmentPlan = (Plan)PersistenceHelper.manager.refresh(targetResAssignmentPlan);
        targetResAssignmentPlanOid = NmOid.newNmOid(targetResAssignmentPlan.getPersistInfo().getObjectIdentifier());
        
        targetResAssignmentsSourcePlan = Plan.newPlan(projectRef);
        targetResAssignmentsSourcePlan.setName("targetResAssignmentsSourcePlan");
        targetResAssignmentsSourcePlan = (Plan)PersistenceHelper.manager.store(targetResAssignmentsSourcePlan);

        resAssignmentActSource = PlanActivity.newPlanActivity(projectRef);
        resAssignmentActSource.setLineNumber(1);resAssignmentActSource.setName("resAssignmentActSource");
        resAssignmentActSource = (PlanActivity)PlannableHelper.service.addPlannable(resAssignmentActSource, targetResAssignmentsSourcePlan);
    }
    
    @Before
    public void setUp() throws Exception {
        LoginUtils.becomeUser("demo");;
        if(projectRef == null){
            UniquenessHelper uniqueHelper = new UniquenessHelper();
            String projectName = uniqueHelper.qualifyName("pjlTestProject_01");
            project = ProjectManagementTestUtil.lookupProject(projectName);
            if (project == null) {
                OrgContainer orgContainer = ProjectManagementTestUtil.lookupOrgContainer(orgName);
                project = ProjectManagementTestUtil.createNewProject(orgContainer, projectName);
            }
            projectRef = WTContainerRef.newWTContainerRef(project);
        }
        
        initialize();
        
    }
    
    private void initialize(){
        classToTest = new PlanPasteProcessor();
        List<ObjectFormProcessorDelegate> delegates = new ArrayList<ObjectFormProcessorDelegate>();
        classToTest.setDelegates(delegates);
        clientData = new NmCommandBean();
        clientData.addRequestDataParam("actionName", PlannableUtils.PPPASTE, false);
        objectBeans = new ArrayList<ObjectBean>();
        
        clipBoardItems = new ArrayList<NmClipboardItem>();
        clipBean = new NmClipboardBean();
    }
    
    @After
    public void tearDown() throws Exception {
        classToTest=null;
    }
    
  
    @Server.Rollback
    @Test
    public void testCutCopyWithInSamePlan() throws Exception{
        try{
            //Cut Functionality Testing
            createPlanAndActivities();
            clientData = setClipBoardAndSelectedItem(NmOid.newNmOid(this.activity1.getPersistInfo().getObjectIdentifier()) ,setSelectedActivity(activity3), CUT, targetPlanOid);

            FormResult result = classToTest.doOperation(clientData, objectBeans);
            activity1 = (PlanActivity)PersistenceHelper.manager.refresh(activity1);
            Assert.assertTrue("activity1 Expected at lineNumber 2 after cut instead found at= "+ activity1.getLineNumber(),
                    activity1.getLineNumber() == (activity3.getLineNumber() - 1));

            //Copy Functionality Testing
            int sizeBeforeCopy = PlannableHelper.service.getAllPlannables(targetPlan, true).size();
            clientData = setClipBoardAndSelectedItem(NmOid.newNmOid(this.activity1.getPersistInfo().getObjectIdentifier()) , setSelectedActivity(activity3),!CUT, targetPlanOid);
            activity1 = (PlanActivity)PersistenceHelper.manager.refresh(activity1);

            result = classToTest.doOperation(clientData, objectBeans);

            targetPlan = (Plan)PersistenceHelper.manager.refresh(targetPlan);
            WTCollection plannables = PlannableHelper.service.getAllPlannables(targetPlan, true);
            Assert.assertTrue("plannables size expected as "+(sizeBeforeCopy + 1 )+ " instead found = "+plannables.size(),
                    plannables.size() == sizeBeforeCopy + 1);

        }finally{
            ACUtils.revokeTestServices();
        }   
    }
    
    
    @Server.Rollback
    @Test
    public void testCutCopyWithInDifferentPlan() throws Exception{
        try{
            createPlanAndActivities();
            //Copy Functionality Testing
            clientData = setClipBoardAndSelectedItem(NmOid.newNmOid(this.activity1Source.getPersistInfo().getObjectIdentifier()) , setSelectedActivity(activity3),!CUT, targetPlanOid);
            activity1Source = (PlanActivity)PersistenceHelper.manager.refresh(activity1Source);
           
            int taregtSizeBeforeCopy = PlannableHelper.service.getAllPlannables(targetPlan, true).size();

            activity1Source = initializeValues(activity1Source);
            activity1Source = (PlanActivity) PersistenceHelper.manager.save(activity1Source);

            FormResult result = classToTest.doOperation(clientData, objectBeans);

            targetPlan = (Plan)PersistenceHelper.manager.refresh(targetPlan);
            WTCollection plannables = PlannableHelper.service.getAllPlannables(targetPlan, true);
            Assert.assertTrue("plannables size expected as "+(taregtSizeBeforeCopy + 1 )+ " instead found = "+plannables.size(),
                    plannables.size() == taregtSizeBeforeCopy + 1);

            activity3 = (PlanActivity)PersistenceHelper.manager.refresh(activity3);
            PlanActivity newActivity = (PlanActivity)PlannableHelper.service.getPlannableByLineNumber(targetPlan, 
                    (activity3.getLineNumber() - 1));
            ArrayList<String> alFailedCompareList = ProjectManagementTestUtil.compareAttributes(StandardPlannableService.PERSIST_PLANACTIVITY_LEVEL_ATTRIBUTES,
                    activity1Source,newActivity);   

            Assert.assertTrue(StandardPlannableService.PERSIST_PLANACTIVITY_LEVEL_ATTRIBUTES +" attributes not copied correctly "
                    ,alFailedCompareList.size() == 0);

            alFailedCompareList = ProjectManagementTestUtil.compareAttributes(DONOT_PERSIST_PLANACTIVITY_LEVEL_ATTRIBUTES,
                    activity1Source,newActivity); 

            Assert.assertTrue(DONOT_PERSIST_PLANACTIVITY_LEVEL_ATTRIBUTES +" attributes should not be copied "
                    ,alFailedCompareList.size() == DONOT_PERSIST_PLANACTIVITY_LEVEL_ATTRIBUTES.size()); 

            //Cut Functionality Testing
            clientData = setClipBoardAndSelectedItem(NmOid.newNmOid(this.activity1Source.getPersistInfo().getObjectIdentifier()) , setSelectedActivity(activity3), CUT, targetPlanOid);
            activity1Source = (PlanActivity)PersistenceHelper.manager.refresh(activity1Source);
            int sourceSizeBeforeCopy = PlannableHelper.service.getAllPlannables(sourcePlan, true).size();
            result = classToTest.doOperation(clientData, objectBeans);

            //Cut will Increase the size by 1 + 1(from Copy Functionality above) 
            targetPlan = (Plan)PersistenceHelper.manager.refresh(targetPlan);
            plannables = PlannableHelper.service.getAllPlannables(targetPlan, true);
            Assert.assertTrue("plannables size expected as "+(taregtSizeBeforeCopy + 2) + " instead found = "+plannables.size(),
                    plannables.size() == taregtSizeBeforeCopy + 2);

            sourcePlan = (Plan)PersistenceHelper.manager.refresh(sourcePlan);
            plannables = PlannableHelper.service.getAllPlannables(sourcePlan, true);
            Assert.assertTrue("plannables size expected as "+(sourceSizeBeforeCopy - 1) + " instead found = "+plannables.size(),
                    plannables.size() == (sourceSizeBeforeCopy - 1));
        }finally{
            ACUtils.revokeTestServices();
        }
    }
    
    
    @Server.Rollback
    @Test
    public void testCutCopySNET() throws Exception{
       try{
           testCutCopySNETTypeActivityWithInBeforeCurrentDateDiffTargetPlan();
           testCutCopySNETTypeActivityWithInSamePlan();
           testCutCopySNETTypeActivityWithInAfterCurrentDateDiffTargetPlan();
           
       }finally{
           ACUtils.revokeTestServices();
       }
        
    }
    
    @Server.Rollback
    public void testCutCopySNETTypeActivityWithInSamePlan() throws Exception{
        try{
            /**** Creating Plans and Activities ****/
            createSNETTypeActivity();
            initialize();
            /*********************************** Cut Functionality Testing ******************************************/
            clientData = setClipBoardAndSelectedItem(NmOid.newNmOid(this.actSNETType.getPersistInfo().getObjectIdentifier()) ,setSelectedActivity(act3), CUT, targetSNETPlanOid);

            FormResult result = classToTest.doOperation(clientData, objectBeans);
            act3 = (PlanActivity)PersistenceHelper.manager.refresh(act3);
            Plan SNETPlan = (Plan)PersistenceHelper.manager.refresh(targetSNETPlan);
            actSNETType = (PlanActivity)PersistenceHelper.manager.refresh(actSNETType);
            Assert.assertTrue("actSNETType Expected at lineNumber 3 after cut instead found at= "+ actSNETType.getLineNumber(),
                    actSNETType.getLineNumber() == (act3.getLineNumber() - 1));

            /*********************************** Copy Functionality Testing ******************************************/
            int sizeBeforeCopy = PlannableHelper.service.getAllPlannables(SNETPlan, false).size();
            clientData = setClipBoardAndSelectedItem(NmOid.newNmOid(this.actSNETType.getPersistInfo().getObjectIdentifier()) ,setSelectedActivity(act2), !CUT, targetSNETPlanOid);
            result = classToTest.doOperation(clientData, objectBeans);

            actSNETType = (PlanActivity)PersistenceHelper.manager.refresh(actSNETType);
            act2 = (PlanActivity)PersistenceHelper.manager.refresh(act2);
            SNETPlan = (Plan)PersistenceHelper.manager.refresh(SNETPlan);

            WTCollection plannables = PlannableHelper.service.getAllPlannables(SNETPlan, false);
            PlanActivity targetActivity = (PlanActivity)PlannableHelper.service.getPlannableByLineNumber(SNETPlan, (act2.getLineNumber() - 1));
            PlanActivity sourceActivity = (PlanActivity)PlannableHelper.service.getPlannableByLineNumber(SNETPlan, (4));

            Assert.assertTrue("plannables size expected as "+(sizeBeforeCopy + 1 )+ " instead found = "+plannables.size(),
                    plannables.size() == sizeBeforeCopy + 1);
            Assert.assertTrue("targetActivity's expected Constraint Type is "+sourceActivity.getConstraintType()+ " instead found = "+targetActivity.getConstraintType(),
                    targetActivity.getConstraintType().equals(sourceActivity.getConstraintType()));
            Assert.assertTrue("targetActivity's expected Constraint Date is "+sourceActivity.getConstraintDate()+ " instead found = "+targetActivity.getConstraintDate(),
                    targetActivity.getConstraintDate().equals(sourceActivity.getConstraintDate()));
        }finally{
            ACUtils.revokeTestServices();
        }
    }
    
    @Server.Rollback
    public void testCutCopySNETTypeActivityWithInAfterCurrentDateDiffTargetPlan() throws Exception{
        try{
            /**** Creating Plans and Activities ****/
            createSNETTypeActivity(); 
            initialize();
            verifySNETConstaintDateForDiffPlan(!targetPlanStartDateBeforeCurrentDay);
        }finally{
            ACUtils.revokeTestServices();
        }
    }

    @Server.Rollback
    public void testCutCopySNETTypeActivityWithInBeforeCurrentDateDiffTargetPlan() throws Exception{
        try{
            /**** Creating Plans and Activities ****/
            createSNETTypeActivity();
            initialize();
            verifySNETConstaintDateForDiffPlan(targetPlanStartDateBeforeCurrentDay);
        }finally{
            ACUtils.revokeTestServices();
        }
    }
    
    private Calendar addDurationAndAdjustDate(Date dStartDate, long lDuration, boolean SNETConstraintType) throws WTException {
        Timestamp timeStamp = ScheduleUtils.addWorkingDuration(dStartDate, lDuration);
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(PlanWorkingHourHandler.getTimeZone());
        cal.setTime(timeStamp);
        if(SNETConstraintType){
            ScheduleUtils.adjustStartDate(cal, PlanWorkingHourHandler.getTimeZone());
        }
        return cal;
    }

    @Server.Rollback
    @Test
    public void testCutCopyMFO() throws Exception{
       try{
           testCutCopyMFOTypeActivityWithInBeforeCurrentDateDiffTargetPlan();
           testCutCopyMFOWithInSamePlan();
           testCutCopyMFOTypeActivityWithInAfterCurrentDateDiffTargetPlan();
       }finally{
           ACUtils.revokeTestServices();
       }
        
    }
    
    @Server.Rollback
    public void testCutCopyMFOWithInSamePlan() throws Exception{
        try{
            createrMFOTypeActivity();
            //Cut Functionality Testing
            initialize();
            PlanActivity oldActivityBeforCut = (PlanActivity)PlannableHelper.service.getPlannableByLineNumber(targetPlanWithMFOActivity, 
                    (4));
            clientData = setClipBoardAndSelectedItem(NmOid.newNmOid(this.activityMFO4.getPersistInfo().getObjectIdentifier()) ,setSelectedActivity(activityM3), CUT, targetMFOTypePlanOid);

            FormResult result = classToTest.doOperation(clientData, objectBeans);
            activityM3 = (PlanActivity)PersistenceHelper.manager.refresh(activityM3);
            activityMFO4 = (PlanActivity)PersistenceHelper.manager.refresh(activityMFO4);
            Assert.assertTrue("activityMFO4 Expected at lineNumber 3 after cut instead found at= "+ activityMFO4.getLineNumber(),
                    activityMFO4.getLineNumber() == (activityM3.getLineNumber() - 1));
            Assert.assertTrue("New activity's expected Constraint Type is MFO instead found = "+activityMFO4.getConstraintType(),
                    activityMFO4.getConstraintType().equals(oldActivityBeforCut.getConstraintType()));
            Assert.assertTrue("New activity's expected Constraint Date is "+oldActivityBeforCut.getConstraintDate()+ " instead found = "+activityMFO4.getConstraintDate(),
                    activityMFO4.getConstraintDate().equals(oldActivityBeforCut.getConstraintDate()));
            
            //Copy Functionality Testing
            int sizeBeforeCopy = PlannableHelper.service.getAllPlannables(targetPlanWithMFOActivity, true).size();
            clientData = setClipBoardAndSelectedItem(NmOid.newNmOid(this.activityMFO4.getPersistInfo().getObjectIdentifier()) ,setSelectedActivity(activityM2), !CUT, targetMFOTypePlanOid);
            result = classToTest.doOperation(clientData, objectBeans);
            targetPlanWithMFOActivity = (Plan)PersistenceHelper.manager.refresh(targetPlanWithMFOActivity);
            activityM2 = (PlanActivity)PersistenceHelper.manager.refresh(activityM2);
            activityMFO4 = (PlanActivity)PersistenceHelper.manager.refresh(activityMFO4);
            activityM3 = (PlanActivity)PersistenceHelper.manager.refresh(activityM3);

            WTCollection plannables = PlannableHelper.service.getAllPlannables(targetPlanWithMFOActivity, true);
            Assert.assertTrue("plannables size expected as "+(sizeBeforeCopy + 1 )+ " instead found = "+plannables.size(),
                    plannables.size() == sizeBeforeCopy + 1);

            PlanActivity newActivity = (PlanActivity)PlannableHelper.service.getPlannableByLineNumber(targetPlanWithMFOActivity, 
                    (activityM2.getLineNumber() - 1));
            PlanActivity oldActivity = (PlanActivity)PlannableHelper.service.getPlannableByLineNumber(targetPlanWithMFOActivity, 
                    (4));
            Assert.assertTrue("New activity's expected Constraint Type is "+oldActivity.getConstraintType()+ " instead found = "+newActivity.getConstraintType(),
                    newActivity.getConstraintType().equals(oldActivity.getConstraintType()));
            Timestamp expectedContraintDateForTargetActivity = calculateExpectedConstraintDateOfTargetActivity(activityMFO4,targetPlanWithMFOActivity, targetPlanWithMFOActivity);
            Assert.assertTrue("New activity's expected Constraint Date is "+expectedContraintDateForTargetActivity+ " instead found = "+newActivity.getConstraintDate(),
                    newActivity.getConstraintDate().equals(expectedContraintDateForTargetActivity));
        }finally{
            ACUtils.revokeTestServices();
        }
    }

    @Server.Rollback
    public void testCutCopyMFOTypeActivityWithInAfterCurrentDateDiffTargetPlan() throws Exception{
        try{
            createrMFOTypeActivity();
            initialize();
            verifyMFOConstaintDateForDiffPlan(!targetPlanStartDateBeforeCurrentDay);
        }finally{
            ACUtils.revokeTestServices();
        }
    }

    @Server.Rollback
    public void testCutCopyMFOTypeActivityWithInBeforeCurrentDateDiffTargetPlan() throws Exception{
        try{
            createrMFOTypeActivity();
            initialize();
            verifyMFOConstaintDateForDiffPlan(targetPlanStartDateBeforeCurrentDay);
        }finally{
            ACUtils.revokeTestServices();
        }
    }
    
    @Server.Rollback
   @Test
    public void testCutCopySummaryActivities() throws Exception{
        
        String typeName = uniqueHelper.getUniqueName("org.rnd.VW_AUTO_");
        createPlanAndActivities();
        
        sourcePlan = (Plan)PersistenceHelper.manager.refresh(sourcePlan);
        LoginUtils.becomeAdminUser();
        ProjectManagementTestUtil.createSubType(PlanActivity.class.getName(), typeName, new String[]{});
        LoginUtils.becomeUser("Demo");
        TypeDefinitionReference definitionReference = ClientTypedUtility.getTypeDefinitionReference(PlanActivity.class.getName() + "|" + typeName);
        PlanActivity summary1= ProjectManagementTestUtil.createScheduledDeliverableForType(sourcePlan, "Summary1",
                                  TrackingIntentType.FIXED_REVISION,sourcePlan, definitionReference,sourcePlan.getContainerReference(),2, true,true);

        PlanActivity childSummary1Act1= ProjectManagementTestUtil.createScheduledDeliverableForType(sourcePlan, "Child1", 
                TrackingIntentType.FIXED_REVISION,summary1, definitionReference,projectRef,3, true,false);
        

        //Copy Functionality Testing
        clientData = setClipBoardAndSelectedItem(NmOid.newNmOid(summary1.getPersistInfo().getObjectIdentifier()) , setSelectedActivity(activity3), !CUT, targetPlanOid);
        summary1 = (PlanActivity)PersistenceHelper.manager.refresh(summary1);
        int taregtSizeBeforeCopy = PlannableHelper.service.getAllPlannables(targetPlan, true).size();
        FormResult result = classToTest.doOperation(clientData, objectBeans);
        
        //Copy will Increase the size by 2 (Summary + Child)
        targetPlan = (Plan)PersistenceHelper.manager.refresh(targetPlan);
        WTCollection plannables = PlannableHelper.service.getAllPlannables(targetPlan, true);
        Assert.assertTrue("plannables size expected as "+(taregtSizeBeforeCopy + 2) + " instead found = "+plannables.size(),
                plannables.size() == taregtSizeBeforeCopy + 2);
        
        
        //Cut Functionality Testing
        
        clientData = setClipBoardAndSelectedItem(NmOid.newNmOid(summary1.getPersistInfo().getObjectIdentifier()) ,setSelectedActivity(activity3), CUT, targetPlanOid);
        summary1 = (PlanActivity)PersistenceHelper.manager.refresh(summary1);
        int sourceSizeBeforeCopy = PlannableHelper.service.getAllPlannables(sourcePlan, true).size();
        result = classToTest.doOperation(clientData, objectBeans);
        
        //Cut will Increase the size by 2 (Summary + Child) + 2(From Copy Functionality above)
        targetPlan = (Plan)PersistenceHelper.manager.refresh(targetPlan);
        plannables = PlannableHelper.service.getAllPlannables(targetPlan, true);
        Assert.assertTrue("plannables size expected as "+(taregtSizeBeforeCopy + 4) + " instead found = "+plannables.size(),
                plannables.size() == taregtSizeBeforeCopy + 4);
        
        sourcePlan = (Plan)PersistenceHelper.manager.refresh(sourcePlan);
        plannables = PlannableHelper.service.getAllPlannables(sourcePlan, true);
        Assert.assertTrue("plannables size expected as "+(sourceSizeBeforeCopy - 2) + " instead found = "+plannables.size(),
                plannables.size() == (sourceSizeBeforeCopy - 2));
    }
    
    @Server.Rollback
    @Test
    public void testCutCopyPredecessorSuccessorWithInSamePlan() throws Exception{
        try{
            createrSuccessorPredessorTypeActivity();
            //Cut Functionality Testing
            clientData = setClipBoardAndSelectedItem(NmOid.newNmOid(this.activitySucc.getPersistInfo().getObjectIdentifier()) , setSelectedActivity(activityC), CUT, targetSuccPredTypePlanOid);
            clientData = setClipBoardAndSelectedItem(NmOid.newNmOid(this.activityPred.getPersistInfo().getObjectIdentifier()) , setSelectedActivity(activityC), CUT, targetSuccPredTypePlanOid);
            FormResult result = classToTest.doOperation(clientData, objectBeans);
            activitySucc = (PlanActivity)PersistenceHelper.manager.refresh(activitySucc);
            activityPred = (PlanActivity)PersistenceHelper.manager.refresh(activityPred);
            WTCollection  predecessorList1= PasteActionUtil.getPredecessors(activitySucc);
            Assert.assertTrue("The Predecessor list should not be empty",predecessorList1.size() >0);
            Assert.assertTrue("The Predecessor of activitySucc should be activityPred",predecessorList1.contains(activityPred));

            //Copy Functionality Testing
            activityC = (PlanActivity)PersistenceHelper.manager.refresh(activityC);
            clientData = setClipBoardAndSelectedItem(NmOid.newNmOid(this.activitySucc.getPersistInfo().getObjectIdentifier()) , setSelectedActivity(activityB), !CUT, targetSuccPredTypePlanOid);
            clientData = setClipBoardAndSelectedItem(NmOid.newNmOid(this.activityPred.getPersistInfo().getObjectIdentifier()) , setSelectedActivity(activityB), !CUT, targetSuccPredTypePlanOid);

            result = classToTest.doOperation(clientData, objectBeans);

            activitySucc = (PlanActivity)PersistenceHelper.manager.refresh(activitySucc);
            activityPred = (PlanActivity)PersistenceHelper.manager.refresh(activityPred);
            activityB = (PlanActivity)PersistenceHelper.manager.refresh(activityB);
            targetSuccPredTypePlan = (Plan)PersistenceHelper.manager.refresh(targetSuccPredTypePlan);
            WTCollection  predecessorList2 = null;
            PlanActivity newSuccActivity = (PlanActivity)PlannableHelper.service.getPlannableByLineNumber(targetSuccPredTypePlan,(activityB.getLineNumber() - 1));     
            PlanActivity newPredActivity = (PlanActivity)PlannableHelper.service.getPlannableByLineNumber(targetSuccPredTypePlan,(activityB.getLineNumber() - 2)); 
            predecessorList2 = PasteActionUtil.getPredecessors(newSuccActivity);
            Assert.assertTrue("The Predecessor list should not be empty",predecessorList2.size() >0);
            Assert.assertTrue("The Predecessor of activitySuccSource should be activityPredSource",predecessorList2.contains(newPredActivity));

        }finally{
            ACUtils.revokeTestServices();
        }
    }
    
    @Server.Rollback
    @Test
    public void testCutCopyOnlySuccessorWithInSamePlan() throws Exception{
        try{
            createrSuccessorPredessorTypeActivity();
            //Cut Functionality Testing
            clientData = setClipBoardAndSelectedItem(NmOid.newNmOid(this.activitySucc.getPersistInfo().getObjectIdentifier()) , setSelectedActivity(activityC), CUT, targetSuccPredTypePlanOid);
            FormResult result = classToTest.doOperation(clientData, objectBeans);
            activitySucc = (PlanActivity)PersistenceHelper.manager.refresh(activitySucc);
            WTCollection  predecessorList1= PasteActionUtil.getPredecessors(activitySucc);
            Assert.assertTrue("Thel Predecessor list should not be empty",predecessorList1.size() > 0);
            Assert.assertTrue("The Predecessor of activitySucc should be activityPred",predecessorList1.contains(activityPred));

            //Copy Functionality Testing
            activitySucc = (PlanActivity)PersistenceHelper.manager.refresh(activitySucc);
            activityC = (PlanActivity)PersistenceHelper.manager.refresh(activityC);
            clientData = setClipBoardAndSelectedItem(NmOid.newNmOid(this.activitySucc.getPersistInfo().getObjectIdentifier()) , setSelectedActivity(activityB), !CUT, targetSuccPredTypePlanOid);
            activitySucc = (PlanActivity)PersistenceHelper.manager.refresh(activitySucc);

            result = classToTest.doOperation(clientData, objectBeans);

            activitySucc = (PlanActivity)PersistenceHelper.manager.refresh(activitySucc);
            activityB = (PlanActivity)PersistenceHelper.manager.refresh(activityB);
            targetSuccPredTypePlan = (Plan)PersistenceHelper.manager.refresh(targetSuccPredTypePlan);
            WTCollection  predecessorList2 = null;
            PlanActivity newSuccActivity = (PlanActivity)PlannableHelper.service.getPlannableByLineNumber(targetSuccPredTypePlan,(activityB.getLineNumber() - 1));      
            predecessorList2 = PasteActionUtil.getPredecessors(newSuccActivity);
            Assert.assertTrue("The Predecessor list should be empty",predecessorList2.size() == 0);

        }finally{
            ACUtils.revokeTestServices();
        } 
    }
    
    @Server.Rollback
    @Test
    public void testCutCopyPredecessorSuccessorWithInDifferentPlan() throws Exception{
        try{
            createrSuccessorPredessorTypeActivity();
            //Copy Functionality Testing
            clientData = setClipBoardAndSelectedItem(NmOid.newNmOid(this.activitySuccSource.getPersistInfo().getObjectIdentifier()) , setSelectedActivity(activityC), !CUT, targetSuccPredTypePlanOid);
            clientData = setClipBoardAndSelectedItem(NmOid.newNmOid(this.activityPredSource.getPersistInfo().getObjectIdentifier()) , setSelectedActivity(activityC), !CUT, targetSuccPredTypePlanOid);
            activitySuccSource = (PlanActivity)PersistenceHelper.manager.refresh(activitySuccSource);
            activityPredSource = (PlanActivity)PersistenceHelper.manager.refresh(activityPredSource);
            FormResult result = classToTest.doOperation(clientData, objectBeans);
            activityC = (PlanActivity)PersistenceHelper.manager.refresh(activityC);
            targetSuccPredTypePlan = (Plan)PersistenceHelper.manager.refresh(targetSuccPredTypePlan);
            PlanActivity newSuccActivity = (PlanActivity)PlannableHelper.service.getPlannableByLineNumber(targetSuccPredTypePlan,(activityC.getLineNumber() - 1));
            PlanActivity newPredActivity = (PlanActivity)PlannableHelper.service.getPlannableByLineNumber(targetSuccPredTypePlan,(activityC.getLineNumber() - 2));
            WTCollection  predecessorList1= PasteActionUtil.getPredecessors(newSuccActivity);
            Assert.assertTrue("The Predecessor list should not be empty",predecessorList1.size() >  0);
            Assert.assertTrue("The Predecessor of activitySuccSource should be activityPredSource",predecessorList1.contains(newPredActivity));

            //Cut Functionality Testing
            clientData = setClipBoardAndSelectedItem(NmOid.newNmOid(this.activitySuccSource.getPersistInfo().getObjectIdentifier()) , setSelectedActivity(activityB), CUT, targetSuccPredTypePlanOid);
            clientData = setClipBoardAndSelectedItem(NmOid.newNmOid(this.activityPredSource.getPersistInfo().getObjectIdentifier()) , setSelectedActivity(activityB), CUT, targetSuccPredTypePlanOid);
            result = classToTest.doOperation(clientData, objectBeans);
            activityB = (PlanActivity)PersistenceHelper.manager.refresh(activityB); 
            targetSuccPredTypePlan = (Plan)PersistenceHelper.manager.refresh(targetSuccPredTypePlan);
            PlanActivity newSuccActivity2 = (PlanActivity)PlannableHelper.service.getPlannableByLineNumber(targetSuccPredTypePlan,(activityB.getLineNumber() - 1));
            PlanActivity newPredActivity2 = (PlanActivity)PlannableHelper.service.getPlannableByLineNumber(targetSuccPredTypePlan,(activityB.getLineNumber() - 2));
            WTCollection  predecessorList2= PasteActionUtil.getPredecessors(newSuccActivity2);
            Assert.assertTrue("The Predecessor list should not be empty",predecessorList2.size() > 0);
            Assert.assertTrue("The Predecessor of activitySuccSource should be activityPredSource",predecessorList2.contains(newPredActivity2));

        }finally{
            ACUtils.revokeTestServices();
        }
    }
    
    @Server.Rollback
    @Test
    public void testCutCopyOnlySuccessorWithInDifferentPlan() throws Exception{
        try{
            createrSuccessorPredessorTypeActivity();
            //Copy Functionality Testing
            clientData = setClipBoardAndSelectedItem(NmOid.newNmOid(this.activitySuccSource.getPersistInfo().getObjectIdentifier()) , setSelectedActivity(activityC), !CUT, targetSuccPredTypePlanOid);
            activitySuccSource = (PlanActivity)PersistenceHelper.manager.refresh(activitySuccSource);
            FormResult result = classToTest.doOperation(clientData, objectBeans);
            activityC = (PlanActivity)PersistenceHelper.manager.refresh(activityC);
            targetSuccPredTypePlan = (Plan)PersistenceHelper.manager.refresh(targetSuccPredTypePlan);
            PlanActivity newSuccActivity = (PlanActivity)PlannableHelper.service.getPlannableByLineNumber(targetSuccPredTypePlan,(activityC.getLineNumber() - 1));
            WTCollection  predecessorList1= PasteActionUtil.getPredecessors(newSuccActivity);
            Assert.assertTrue("The Predecessor list should be empty",predecessorList1.size() == 0);

            //Cut Functionality Testing
            clientData = setClipBoardAndSelectedItem(NmOid.newNmOid(this.activitySuccSource.getPersistInfo().getObjectIdentifier()) , setSelectedActivity(activityB), CUT, targetSuccPredTypePlanOid);
            activitySuccSource = (PlanActivity)PersistenceHelper.manager.refresh(activitySuccSource);
            result = classToTest.doOperation(clientData, objectBeans);
            activityB = (PlanActivity)PersistenceHelper.manager.refresh(activityB); 
            targetSuccPredTypePlan = (Plan)PersistenceHelper.manager.refresh(targetSuccPredTypePlan);
            PlanActivity newSuccActivity2 = (PlanActivity)PlannableHelper.service.getPlannableByLineNumber(targetSuccPredTypePlan,(activityB.getLineNumber() - 1));
            WTCollection  predecessorList2= PasteActionUtil.getPredecessors(newSuccActivity2);
            Assert.assertTrue("The Predecessor list should be empty",predecessorList2.size() == 0);

        }finally{
            ACUtils.revokeTestServices();
        }
    }
    
    @Server.Rollback
    @Test
    public void testCutCopyForResourceAssignmentInSamePlan() throws Exception {
        try {
            /**** Creating Plans and Activities ****/
            createrResAssignmentTypeActivity();

            /*********************************** Cut Functionality Testing ******************************************/
            ArrayList<String> notMatchingAttributesList = createAssignmentDataAndCompare(activityR1, SAME_PLAN, CUT, activityR3, targetResAssignmentPlanOid); 

            Assert.assertTrue(PERSIST_RESOURCE_ASSIGNMENT_ATTRIBUTES_CUT_FOR_SAME_PLAN +" attributes should be copied within plan for cut operation"
                    ,notMatchingAttributesList.size() == 0); 

            /*********************************** Copy Functionality Testing ******************************************/
            notMatchingAttributesList = createAssignmentDataAndCompare(activityR1, SAME_PLAN, !CUT, activityR4, targetResAssignmentPlanOid); 

            Assert.assertTrue(DONOT_PERSIST_RESOURCE_ASSIGNMENT_ATTRIBUTES +" attributes should not be copied within plan for copy operation"
                    ,notMatchingAttributesList.size() == DONOT_PERSIST_RESOURCE_ASSIGNMENT_ATTRIBUTES.size());  
        } finally {
            ACUtils.revokeTestServices();
        }
    }
    
    @Server.Rollback
    @Test
    public void testCutCopyForResourceAssignmentInDifferentPlan() throws Exception{
        try{
            /**** Creating Plans and Activities ****/
            createrResAssignmentTypeActivity();

            /*********************************** Copy Functionality Testing ******************************************/
            ArrayList<String> notMatchingAttributesList = createAssignmentDataAndCompare(resAssignmentActSource, !SAME_PLAN, !CUT, activityR3, targetResAssignmentPlanOid); 

            Assert.assertTrue(DONOT_PERSIST_RESOURCE_ASSIGNMENT_ATTRIBUTES +" attributes should not be copied in differnet plan for copy operation"
                    ,notMatchingAttributesList.size() == DONOT_PERSIST_RESOURCE_ASSIGNMENT_ATTRIBUTES.size());

            /*********************************** Cut Functionality Testing ******************************************/
            notMatchingAttributesList = createAssignmentDataAndCompare(resAssignmentActSource, !SAME_PLAN, CUT, activityR4, targetResAssignmentPlanOid); 

            Assert.assertTrue(DONOT_PERSIST_RESOURCE_ASSIGNMENT_ATTRIBUTES +" attributes should not be copied differnet plan for cut operation"
                    ,notMatchingAttributesList.size() == 0); /**** source activity is not exist any more ****/
        } finally {
            ACUtils.revokeTestServices();
        }
    }

    @Server.Rollback
    @Test
    public void testCutCopyForSharedAssignmentDetailsInDifferentPlan() throws Exception{
        try{
            /**** Creating Plans and Activities ****/
            createrResAssignmentTypeActivity();
            /**** Creating WTgroup and adding WTgroup to the team of the project ****/
            LoginUtils.becomeAdminUser();
            ProjectManagementTestUtil.createGroupAndAddGroupToTeamOfProject(project);
            LoginUtils.becomeUser("demo");

            PlanResource[] resources = PlanIXTestUtil.fetchResourcesForAssignmentCreation(this.targetResAssignmentsSourcePlan);
            PlanResource assignedGroupResource = resources[1];
            ArrayList<ResourceAssignment> assignments = new ArrayList<ResourceAssignment>();
            /**** Creating new resource assignment *****/
            ResourceAssignment assignment = ProjectManagementTestUtil.createNewResourceAssignment(assignedGroupResource, this.resAssignmentActSource, ResourceAssignmentType.SHARED);
            PersistenceHelper.manager.save(assignment);
            assignments.add(assignment);
            /**** Creating new shared assignment details *****/
            WTArrayList wtAssignments = (WTArrayList)AssignmentHelper.service.addResourceAssignments(assignments);
            /**** Making only one shared assignment detail "inactive" so that "inactive" shared assignment detail should not be copied. ****/
            WTArrayList sourceAssignmentDetailsList = AssignmentHelper.service.getSharedAssignmentDetails((ResourceAssignment) ((ObjectReference)wtAssignments.get(0)).getObject());
            SharedAssignmentDetails sharedAssignmentDetail = (SharedAssignmentDetails)((ObjectReference)sourceAssignmentDetailsList.get(0)).getObject();
            sharedAssignmentDetail.setActive(false); 
            PersistenceHelper.manager.save(sharedAssignmentDetail); 

            /*********************************** Copy Functionality Testing ******************************************/
            compareSourceTargetLocationSharedAssignmentDetailsObject(sourceAssignmentDetailsList, false, activityR3);  

            /*********************************** Cut Functionality Testing ******************************************/
            compareSourceTargetLocationSharedAssignmentDetailsObject(sourceAssignmentDetailsList, true, activityR3);   

        } finally {
            ACUtils.revokeTestServices();
        }
    }
    
    @Server.Rollback
    @Test
    public void testCutCopyWithInSamePlan_pasteBelow() throws Exception{
        try{
            //Cut Functionality Testing
            createPlanAndActivities();
            clientData = setClipBoardAndSelectedItem(NmOid.newNmOid(this.activity1.getPersistInfo().getObjectIdentifier()) ,setSelectedActivity(activity3), CUT, targetPlanOid);                                 
            clientData.addRequestDataParam("actionName", PlannableUtils.PPPASTEBELOW, false);
            FormResult result = classToTest.doOperation(clientData, objectBeans);
            activity1 = (PlanActivity)PersistenceHelper.manager.refresh(activity1);
            activity3 = (PlanActivity)PersistenceHelper.manager.refresh(activity3);            
            Assert.assertTrue("activity1 Expected at lineNumber 3 after cut instead found at= "+ activity1.getLineNumber(),
                    activity1.getLineNumber() == (activity3.getLineNumber()+1 ));

            //Copy Functionality Testing
            int sizeBeforeCopy = PlannableHelper.service.getAllPlannables(targetPlan, true).size();
            clientData = setClipBoardAndSelectedItem(NmOid.newNmOid(this.activity1.getPersistInfo().getObjectIdentifier()) , setSelectedActivity(activity3),!CUT, targetPlanOid);            

            result = classToTest.doOperation(clientData, objectBeans);
            activity1 = (PlanActivity)PersistenceHelper.manager.refresh(activity1);
            activity3 = (PlanActivity)PersistenceHelper.manager.refresh(activity3);

            targetPlan = (Plan)PersistenceHelper.manager.refresh(targetPlan);
            WTCollection plannables = PlannableHelper.service.getAllPlannables(targetPlan, true);
            Assert.assertTrue("plannables size expected as "+(sizeBeforeCopy + 1 )+ " instead found = "+plannables.size(),
                    plannables.size() == sizeBeforeCopy + 1);
            
            PlanActivity newActivity = (PlanActivity)PlannableHelper.service.getPlannableByLineNumber(targetPlan, 
                    (activity3.getLineNumber() + 1));
            
            Assert.assertTrue("Duration of a source activity and pasted activity should be same ", newActivity.getDuration().getMillis()== activity1.getDuration().getMillis() );
            Assert.assertTrue("Activity Name of a source activity and pasted activity should be same ", newActivity.getName().equals(activity1.getName()));
            
        }finally{
            ACUtils.revokeTestServices();
        }   
    }
    
    
    @Server.Rollback
    @Test
    public void testCutCopyWithInDifferentPlan_pasteBelow() throws Exception{
        try{
            createPlanAndActivities();
            //Copy Functionality Testing
            clientData = setClipBoardAndSelectedItem(NmOid.newNmOid(this.activity1Source.getPersistInfo().getObjectIdentifier()) , setSelectedActivity(activity3),!CUT, targetPlanOid);
            clientData.addRequestDataParam("actionName", PlannableUtils.PPPASTEBELOW, false);
            activity1Source = (PlanActivity)PersistenceHelper.manager.refresh(activity1Source);
           
            int taregtSizeBeforeCopy = PlannableHelper.service.getAllPlannables(targetPlan, true).size();

            activity1Source = initializeValues(activity1Source);
            activity1Source = (PlanActivity) PersistenceHelper.manager.save(activity1Source);

            FormResult result = classToTest.doOperation(clientData, objectBeans);

            targetPlan = (Plan)PersistenceHelper.manager.refresh(targetPlan);
            WTCollection plannables = PlannableHelper.service.getAllPlannables(targetPlan, true);
            Assert.assertTrue("plannables size expected as "+(taregtSizeBeforeCopy + 1 )+ " instead found = "+plannables.size(),
                    plannables.size() == taregtSizeBeforeCopy + 1);

            activity3 = (PlanActivity)PersistenceHelper.manager.refresh(activity3);
            PlanActivity newActivity = (PlanActivity)PlannableHelper.service.getPlannableByLineNumber(targetPlan, 
                    (activity3.getLineNumber() + 1));
            
            Assert.assertTrue("Duration of a source activity and pasted activity should be same ", newActivity.getDuration().getMillis()== activity1Source.getDuration().getMillis() );
            Assert.assertTrue("Activity Name of a source activity and pasted activity should be same ", newActivity.getName().equals(activity1Source.getName()));
            
            ArrayList<String> alFailedCompareList = ProjectManagementTestUtil.compareAttributes(StandardPlannableService.PERSIST_PLANACTIVITY_LEVEL_ATTRIBUTES,
                    activity1Source,newActivity);   

            Assert.assertTrue(StandardPlannableService.PERSIST_PLANACTIVITY_LEVEL_ATTRIBUTES +" attributes not copied correctly "
                    ,alFailedCompareList.size() == 0);

            alFailedCompareList = ProjectManagementTestUtil.compareAttributes(DONOT_PERSIST_PLANACTIVITY_LEVEL_ATTRIBUTES,
                    activity1Source,newActivity); 

            Assert.assertTrue(DONOT_PERSIST_PLANACTIVITY_LEVEL_ATTRIBUTES +" attributes should not be copied "
                    ,alFailedCompareList.size() == DONOT_PERSIST_PLANACTIVITY_LEVEL_ATTRIBUTES.size()); 

            //Cut Functionality Testing
            clientData = setClipBoardAndSelectedItem(NmOid.newNmOid(this.activity1Source.getPersistInfo().getObjectIdentifier()) , setSelectedActivity(activity3), CUT, targetPlanOid);
            activity1Source = (PlanActivity)PersistenceHelper.manager.refresh(activity1Source);
            int sourceSizeBeforeCopy = PlannableHelper.service.getAllPlannables(sourcePlan, true).size();
            result = classToTest.doOperation(clientData, objectBeans);

            //Cut will Increase the size by 1 + 1(from Copy Functionality above) 
            targetPlan = (Plan)PersistenceHelper.manager.refresh(targetPlan);
            plannables = PlannableHelper.service.getAllPlannables(targetPlan, true);
            Assert.assertTrue("plannables size expected as "+(taregtSizeBeforeCopy + 2) + " instead found = "+plannables.size(),
                    plannables.size() == taregtSizeBeforeCopy + 2);

            sourcePlan = (Plan)PersistenceHelper.manager.refresh(sourcePlan);
            plannables = PlannableHelper.service.getAllPlannables(sourcePlan, true);
            Assert.assertTrue("plannables size expected as "+(sourceSizeBeforeCopy - 1) + " instead found = "+plannables.size(),
                    plannables.size() == (sourceSizeBeforeCopy - 1));
        }finally{
            ACUtils.revokeTestServices();
        }
    }
    
    @Server.Rollback
    @Test
    public void testCutCopyWithInSamePlan_pasteChild() throws Exception{
        try{
            //Cut Functionality Testing
            createPlanAndActivities();
            clientData = setClipBoardAndSelectedItem(NmOid.newNmOid(this.activity1.getPersistInfo().getObjectIdentifier()) ,setSelectedActivity(activity3), CUT, targetPlanOid);                                 
            clientData.addRequestDataParam("actionName", PlannableUtils.PPPASTECHILD, false);
            FormResult result = classToTest.doOperation(clientData, objectBeans);
            activity1 = (PlanActivity)PersistenceHelper.manager.refresh(activity1);
            activity3 = (PlanActivity)PersistenceHelper.manager.refresh(activity3);            

            Assert.assertTrue("The parent of copied activity should be the selected activity ", activity3.equals((PlanActivity) activity1.getParentPlannable()));


            //Copy Functionality Testing
            int sizeBeforeCopy = PlannableHelper.service.getAllPlannables(targetPlan, true).size();
            clientData = setClipBoardAndSelectedItem(NmOid.newNmOid(this.activity1.getPersistInfo().getObjectIdentifier()) , setSelectedActivity(activity2),!CUT, targetPlanOid);            

            result = classToTest.doOperation(clientData, objectBeans);
            activity1 = (PlanActivity)PersistenceHelper.manager.refresh(activity1);
            activity2 = (PlanActivity)PersistenceHelper.manager.refresh(activity2);

            targetPlan = (Plan)PersistenceHelper.manager.refresh(targetPlan);
            WTCollection plannables = PlannableHelper.service.getAllPlannables(targetPlan, true);
            Assert.assertTrue("plannables size expected as "+(sizeBeforeCopy + 1 )+ " instead found = "+plannables.size(),
                    plannables.size() == sizeBeforeCopy + 1);
            
            PlanActivity newActivity = (PlanActivity)PlannableHelper.service.getPlannableByLineNumber(targetPlan, 
                    (activity2.getLineNumber() + 1));
            
            Assert.assertTrue("The parent of copied activity should be the selected activity ", activity2.equals((PlanActivity) newActivity.getParentPlannable()));
            Assert.assertTrue("Duration of a source activity and pasted activity should be same " + newActivity.getDuration().getMillis(), newActivity.getDuration().getMillis()== activity1.getDuration().getMillis() );
            Assert.assertTrue("Activity Name of a source activity and pasted activity should be same " + newActivity.getName(), newActivity.getName().equals(activity1.getName()));
            
        }finally{
            ACUtils.revokeTestServices();
        }   
    }
    
    @Server.Rollback
    @Test
    public void testCutCopyWithInDifferentPlan_pasteChild() throws Exception{
        try{
            createPlanAndActivities();
            //Copy Functionality Testing
            clientData = setClipBoardAndSelectedItem(NmOid.newNmOid(this.activity1Source.getPersistInfo().getObjectIdentifier()) , setSelectedActivity(activity3),!CUT, targetPlanOid);
            clientData.addRequestDataParam("actionName", PlannableUtils.PPPASTECHILD, false);
            activity1Source = (PlanActivity)PersistenceHelper.manager.refresh(activity1Source);
           
            int taregtSizeBeforeCopy = PlannableHelper.service.getAllPlannables(targetPlan, true).size();

            activity1Source = initializeValues(activity1Source);
            activity1Source = (PlanActivity) PersistenceHelper.manager.save(activity1Source);

            FormResult result = classToTest.doOperation(clientData, objectBeans);

            targetPlan = (Plan)PersistenceHelper.manager.refresh(targetPlan);
            WTCollection plannables = PlannableHelper.service.getAllPlannables(targetPlan, true);
            Assert.assertTrue("plannables size expected as "+(taregtSizeBeforeCopy + 1 )+ " instead found = "+plannables.size(),
                    plannables.size() == taregtSizeBeforeCopy + 1);

            activity3 = (PlanActivity)PersistenceHelper.manager.refresh(activity3);
            PlanActivity newActivity = (PlanActivity)PlannableHelper.service.getPlannableByLineNumber(targetPlan, 
                    (activity3.getLineNumber() + 1));

            //Assert if the activity1Source copied in Plan has activity3 as Parent.
            Assert.assertTrue("The parent of copied activity should be the selected activity ", activity3.equals((PlanActivity) newActivity.getParentPlannable()));
            
            Assert.assertTrue("Duration of a source activity and pasted activity should be same " +newActivity.getDuration().getMillis(), newActivity.getDuration().getMillis()== activity1Source.getDuration().getMillis() );
            Assert.assertTrue("Activity Name of a source activity and pasted activity should be same " + newActivity.getName(), newActivity.getName().equals(activity1Source.getName()));
            
            ArrayList<String> alFailedCompareList = ProjectManagementTestUtil.compareAttributes(StandardPlannableService.PERSIST_PLANACTIVITY_LEVEL_ATTRIBUTES,
                    activity1Source,newActivity);   

            Assert.assertTrue(StandardPlannableService.PERSIST_PLANACTIVITY_LEVEL_ATTRIBUTES +" attributes not copied correctly "
                    ,alFailedCompareList.size() == 0);

            alFailedCompareList = ProjectManagementTestUtil.compareAttributes(DONOT_PERSIST_PLANACTIVITY_LEVEL_ATTRIBUTES,
                    activity1Source,newActivity); 

            Assert.assertTrue(DONOT_PERSIST_PLANACTIVITY_LEVEL_ATTRIBUTES +" attributes should not be copied "
                    ,alFailedCompareList.size() == DONOT_PERSIST_PLANACTIVITY_LEVEL_ATTRIBUTES.size()); 

            //Cut Functionality Testing
            clientData = setClipBoardAndSelectedItem(NmOid.newNmOid(this.activity1Source.getPersistInfo().getObjectIdentifier()) , setSelectedActivity(activity2), CUT, targetPlanOid);
            activity1Source = (PlanActivity)PersistenceHelper.manager.refresh(activity1Source);
            int sourceSizeBeforeCopy = PlannableHelper.service.getAllPlannables(sourcePlan, true).size();
            result = classToTest.doOperation(clientData, objectBeans);
            
            PlanActivity newActivity2 = (PlanActivity)PlannableHelper.service.getPlannableByLineNumber(targetPlan, 
                    (activity2.getLineNumber() + 1));
            Assert.assertTrue("The parent of copied activity should be the selected activity ", activity2.equals((PlanActivity) newActivity2.getParentPlannable()));

            //Cut will Increase the size by 1 + 1(from Copy Functionality above) 
            targetPlan = (Plan)PersistenceHelper.manager.refresh(targetPlan);
            plannables = PlannableHelper.service.getAllPlannables(targetPlan, true);
            Assert.assertTrue("plannables size expected as "+(taregtSizeBeforeCopy + 2) + " instead found = "+plannables.size(),
                    plannables.size() == taregtSizeBeforeCopy + 2);

            sourcePlan = (Plan)PersistenceHelper.manager.refresh(sourcePlan);
            plannables = PlannableHelper.service.getAllPlannables(sourcePlan, true);
            Assert.assertTrue("plannables size expected as "+(sourceSizeBeforeCopy - 1) + " instead found = "+plannables.size(),
                    plannables.size() == (sourceSizeBeforeCopy - 1));
        }finally{
            ACUtils.revokeTestServices();
        }
    }

    private void compareSourceTargetLocationSharedAssignmentDetailsObject(WTArrayList sourceAssignmentDetailsList, boolean cutFlag, PlanActivity targetActivity)
            throws WTException, WTPropertyVetoException, ObjectNoLongerExistsException {
        clientData = setClipBoardAndSelectedItem(NmOid.newNmOid(this.resAssignmentActSource.getPersistInfo().getObjectIdentifier()),setSelectedActivity(activityR3), cutFlag, targetResAssignmentPlanOid);
        this.resAssignmentActSource = (PlanActivity) PersistenceHelper.manager.refresh(this.resAssignmentActSource);
        FormResult result = classToTest.doOperation(clientData, objectBeans);

        PlanActivity newActivity = getActivityPastedAbove(targetActivity);
        WTCollection targetResources = AssignmentHelper.service.getResourceAssignments(newActivity);
        ResourceAssignment targetResourceAssignment = null;
        java.util.Iterator targetCutResourceItr = targetResources.persistableIterator();
        while(targetCutResourceItr.hasNext()){
            targetResourceAssignment  = (ResourceAssignment)targetCutResourceItr.next();
        }
        WTCollection targetAssignmentDetailsList = AssignmentHelper.service.getSharedAssignmentDetails(targetResourceAssignment);
        /**** Comparing no of shared assignment details of source activity with target assignment details of source activity ****/
        Assert.assertFalse("Target activity shared assignements details size :" + targetAssignmentDetailsList.size() + "should not be same as Source activity assignements details size :"
                + sourceAssignmentDetailsList.size(), targetAssignmentDetailsList.size() == sourceAssignmentDetailsList.size());
    }
    
    private ArrayList<String> createAssignmentDataAndCompare( PlanActivity sourceActivity, boolean samePlanFlag, boolean cutFlag, PlanActivity targetActivity,  NmOid planOid) throws WTException, ObjectNoLongerExistsException, WTPropertyVetoException {

        /**** Creating Resource Assignment Objects ****/
        ResourceAssignment resourceAssignment = createResourceAssignmentWithActualValues(sourceActivity);
        ArrayList<String> notMatchingAttributesList = new ArrayList<String>();
        clientData = setClipBoardAndSelectedItem(NmOid.newNmOid(sourceActivity.getPersistInfo().getObjectIdentifier()),setSelectedActivity(targetActivity), cutFlag, planOid);
        sourceActivity = (PlanActivity) PersistenceHelper.manager.refresh(sourceActivity);
        FormResult result = classToTest.doOperation(clientData, objectBeans);
        PlanActivity newActivity = getActivityPastedAbove(targetActivity);

        WTCollection sourceResourceAssignments = AssignmentHelper.service.getResourceAssignments(sourceActivity);
        ResourceAssignment sourceResourceAssignment = null;
        java.util.Iterator sourceResourceAssignmentItr = sourceResourceAssignments.persistableIterator();
        while(sourceResourceAssignmentItr.hasNext()){
            sourceResourceAssignment  = (ResourceAssignment)sourceResourceAssignmentItr.next();
        }

        WTCollection targetResourceAssignments = AssignmentHelper.service.getResourceAssignments(newActivity);
        ResourceAssignment targetResourceAssignment = null;
        java.util.Iterator targetCutResourceItr = targetResourceAssignments.persistableIterator();
        while(targetCutResourceItr.hasNext()){
            targetResourceAssignment  = (ResourceAssignment)targetCutResourceItr.next();
        }

        if(!cutFlag  /**** When copy-paste operation is performed with in same and across the plan ****/
                || (cutFlag && samePlanFlag)) /**** When cut-paste operation is performed with in same plan only ****/
        {
            Assert.assertTrue("Source Resources size expected as " + targetResourceAssignments.size() + " instead found = "
                    + sourceResourceAssignments.size(), sourceResourceAssignments.size() == targetResourceAssignments.size());
        }else if (cutFlag && !samePlanFlag) /**** When cut-paste operation is performed with in across the plan ****/
        {
            Assert.assertTrue("Source Resources size expected as " + 0 + " instead found = "
                    + sourceResourceAssignments.size(), sourceResourceAssignments.size() == 0);
        }

        if(sourceResourceAssignment != null && targetResourceAssignment != null){
            compareAssignmentAttributes(sourceResourceAssignment, targetResourceAssignment);
            /**** Comparing value of each field of List 'DONOT_PERSIST_RESOURCE_ASSIGNMENT_ATTRIBUTES'in source and target resource assignments ****/
            notMatchingAttributesList = ProjectManagementTestUtil.compareAttributes(DONOT_PERSIST_RESOURCE_ASSIGNMENT_ATTRIBUTES,
                    sourceResourceAssignment,targetResourceAssignment);
        }
        return notMatchingAttributesList;
    }

    private void compareAssignmentAttributes(ResourceAssignment sourceResourceAssignment, ResourceAssignment targetResourceAssignment)
            throws WTException, WTPropertyVetoException {
        /**** Comparing value of each field of List 'PERSIST_ASSIGNMENT_DETAIL_ATTRIBUTES source and target resource assignments ****/
        ArrayList<String> notMatchingAttributesList = new ArrayList<String>();
        if(sourceResourceAssignment != null && targetResourceAssignment != null){
            notMatchingAttributesList = ProjectManagementTestUtil.compareAttributes(StandardPlannableService.PERSIST_ASSIGNMENT_DETAIL_ATTRIBUTES,
                    sourceResourceAssignment,targetResourceAssignment);
        }

        Assert.assertTrue(StandardPlannableService.PERSIST_ASSIGNMENT_DETAIL_ATTRIBUTES +" attributes should be copied in differnet plan for copy operation"
                ,notMatchingAttributesList.size() == 0);

        /**** Comparing value of each field of List 'PERSIST_ASSIGNMENT_ATTRIBUTES source and target resource assignments ****/
        if(sourceResourceAssignment != null && targetResourceAssignment != null){
            notMatchingAttributesList = ProjectManagementTestUtil.compareAttributes(StandardPlannableService.PERSIST_ASSIGNMENT_ATTRIBUTES,
                    sourceResourceAssignment,targetResourceAssignment);
        }
        Assert.assertTrue(StandardPlannableService.PERSIST_ASSIGNMENT_ATTRIBUTES +" attributes should be copied differnet plan for cut operation"
                ,notMatchingAttributesList.size() == 0);
    }
    
    private PlanActivity getActivityPastedAbove(PlanActivity targetActivity) throws WTException, ObjectNoLongerExistsException {
        targetActivity = (PlanActivity)PersistenceHelper.manager.refresh(targetActivity);
        PlanActivity newActivity = (PlanActivity)PlannableHelper.service.getPlannableByLineNumber(targetResAssignmentPlan, 
                (targetActivity.getLineNumber() - 1));
        return newActivity;
    }
    
    private ResourceAssignment createResourceAssignmentWithActualValues(PlanActivity activity) throws WTPropertyVetoException, WTException {
        String recName = "planResource";
        PlanResource planResource = null;
        Duration activityDuration = new Duration();
        activityDuration.setMillis(ScheduleUtils.ONE_HOUR * 8);
        Duration totalEffort = new Duration();
        totalEffort.setMillis(ScheduleUtils.ONE_HOUR * 8);
        Duration doneEffort = new Duration();
        doneEffort.setMillis(ScheduleUtils.ONE_HOUR * 2);
        Duration remainingEffort = new Duration();
        remainingEffort.setMillis(ScheduleUtils.ONE_HOUR * 6);
        double allocationPercentage = AssignmentHelper.service.calculateAllocationPercentage (activityDuration, remainingEffort);
        int percentWorkComplete = 25;
        com.ptc.projectmanagement.plan.HealthStatusType healthStatusType = HealthStatusType.GREEN;
        String healthStatusDescription = "High Risk";
        com.ptc.projectmanagement.plan.RiskType riskType = RiskType.HIGH;
        String riskDescription = "High Risk";

        planResource = ResourceHelper.service.createResource(recName, projectRef);
        ResourceAssignment resourceAssignment = addResourceAssignment(activity, planResource, totalEffort, doneEffort, 
                remainingEffort, allocationPercentage, percentWorkComplete, healthStatusType, healthStatusDescription, riskType, riskDescription);

        clientData.setPageOid(targetResAssignmentPlanOid);

        return resourceAssignment;
    }
    
    private static ResourceAssignment addResourceAssignment( Plannable plannable, Resourceable resource, Duration totalEffort, Duration doneEffort, Duration remainingEffort,
            double allocationPercentage,  int percentWorkComplete, com.ptc.projectmanagement.plan.HealthStatusType healthStatusType, String healthStatusDescription,  com.ptc.projectmanagement.plan.RiskType riskType, String riskDescription )
                    throws WTException {
        try{
            ResourceAssignment assignment = ResourceAssignment.newResourceAssignment(resource, plannable);
            assignment.setTotalEffort(totalEffort);
            assignment.setDoneEffort(doneEffort);
            assignment.setRemainingEffort(remainingEffort);
            assignment.setAllocationPercentage(allocationPercentage);
            assignment.setPercentWorkComplete(percentWorkComplete);
            assignment.setHealthStatusType(healthStatusType);
            assignment.setHealthStatusDescription(healthStatusDescription);
            assignment.setRiskType(riskType);
            assignment.setRiskDescription(riskDescription);

            assignment = (ResourceAssignment)PersistenceHelper.manager.save(assignment);

            return assignment;
        } catch (WTPropertyVetoException e) {
            throw new WTException(e);
        }
    }
    private NmCommandBean setClipBoardAndSelectedItem(NmOid oid,ArrayList a,boolean cutFlag, NmOid targetPlanOid) throws WTException, WTPropertyVetoException{
        clipBoardItem = new NmClipboardItem();
        clipBoardItem.setObject(oid);
        clipBoardItems.add(clipBoardItem);
        clipBean.add(clipBoardItems);
        clientData.setClipboardBean(clipBean);

        clientData.getClipboardBean().setClippedToCut(cutFlag);
        clientData.setSelected(a);
        clientData.setPageOid(targetPlanOid);
        return clientData;
    }
    
    private ArrayList setSelectedActivity(PlanActivity a) throws WTException{
        NmContext activityContext = null;
        NmContextItem ci = null;
        NmOid activityOid = null;
        ArrayList alSelectedOid = new ArrayList();
        activityOid = NmOid.newNmOid(a.getPersistInfo().getObjectIdentifier());
        activityContext = new NmContext();
        ci = new NmContextItem();
        ci.getElemAddress().getOids().push(activityOid);
        activityContext.getContextItems().push(ci);
        alSelectedOid.add(activityContext);
        return alSelectedOid;
    }
    
    private PlanActivity initializeValues(PlanActivity activity) throws WTPropertyVetoException, WTException{
        //Attributes which should be copied
        activity.setFixedCost(1.0);
        activity.setDescription("TestDescription");
        activity.setDuration(DurationUtils.getDefaultDurationForDisplay());
        activity.setEffortDriven(true);
        activity.setTotalEffort(DurationUtils.getDefaultDurationForDisplay());
        activity.setTotalCost(1.0);
        activity.setMilestone(true);
        activity.setInheritedDomain(true);
        activity.setTaskType(TaskType.FIXED_DURATION);
        activity.setConstraintType(DateConstraint.ASAP);
        activity.setHasDeliverable(true);
        activity.setTrackingIntent(TrackingIntentType.FIXED_REVISION);
        activity.setOwnership(Ownership.newOwnership(SessionHelper.getPrincipal()));
        Map<String, String> labelValueMap = new HashMap<String, String>(1);
        labelValueMap.put("Invalid_Label", "Secret");
        SecurityLabelsTestHelper.setSecurityLabels(labelValueMap, activity);

        //Attributes that need not be copied
        activity.setHealthStatusDescription("TestDescription");
        activity.setHealthStatusType(HealthStatusType.GREEN);
        activity.setPercentWorkComplete(10);
        activity.setRiskDescription("TestDescription");
        activity.setRiskType(RiskType.HIGH);

        return activity;
    }
    
    private Plan adjustDayAndTimeToTargetPlanStartDate(Plan targetPlan, int hours, boolean constraintTypeFlag, boolean targetPlanStartDateBeforeCurrentToday) throws WTException, WTPropertyVetoException {
        long daysInmilliseconds = TimeUnit.SECONDS.toMillis(TimeUnit.HOURS.toSeconds(hours));
        Timestamp newTargetPlanStartDate = null;
        if(targetPlanStartDateBeforeCurrentToday){
            /**** Subtracting days to target plan start date ****/
            WorkingHourHandler wh = new PlanWorkingHourHandler();
            Date newDate =  wh.subtractWorkingDuration(targetPlan.getStartDate(), daysInmilliseconds,  ScheduleUtils.getWorkingCalendarConfig(act3));
            newTargetPlanStartDate = new Timestamp(newDate.getTime());
        }else {
            /**** Adding days to target plan start date ****/
            Calendar calForTargetPlan = addDurationAndAdjustDate(targetPlan.getStartDate(), daysInmilliseconds, constraintTypeFlag);
            newTargetPlanStartDate = new Timestamp(calForTargetPlan.getTimeInMillis());
        }

        targetPlan.setStartDate(newTargetPlanStartDate);
        targetPlan.setDefaultStart(newTargetPlanStartDate);
        targetPlan = (Plan)PersistenceHelper.manager.save(targetPlan);
        
        ProcessorUtils.resetDummyPrecedenceForAllActivities(targetPlan);
        
        PlannableHelper.service.propagateSchedule(targetPlan);
        
        return targetPlan;
    }

    private Timestamp calculateExpectedConstraintDateOfTargetActivity(PlanActivity activity, Plan targetPlan, Plan sourcePlan) throws WTException, WTPropertyVetoException {
        ProcessorUtils processorutils = new ProcessorUtils(sourcePlan);
        Timestamp currentDate = processorutils.getAdjustedCurrentTime();
        currentDate = ScheduleUtils.getWorkDayFloor(currentDate, null);
        Timestamp dStartDate = null;
        if(currentDate.after(targetPlan.getStartDate())){
            dStartDate = currentDate;
        }else{
            dStartDate = targetPlan.getStartDate();
        }
        Timestamp timeStamp = null;
        TimeZone timeZone = TimeZoneHelper.getTimeZone();
        Calendar targetActivityConstraintDate = Calendar.getInstance(timeZone);
        Duration dummyOffset = new Duration();

        if(activity.isMFO()){         
            Long durationInMillis = ScheduleUtils.calculateWorkingDuration(sourcePlan.getStartDate(), activity.getConstraintDate(), TimeZoneHelper.getTimeZone(), ScheduleUtils.getWorkingCalendarConfig(activity));
            dummyOffset.setMillis(durationInMillis);
        }else if(activity.isSNET()){            
           dummyOffset = (PlannableHelper.service.getDummyPrecedenceConstraintForActivity(activity).getScheduleOffset());
        }
        
        timeStamp = ScheduleUtils.addWorkingDuration(dStartDate, dummyOffset.getMillis());
        targetActivityConstraintDate.setTime(timeStamp);
        if(activity.isSNET()){
            ScheduleUtils.adjustStartDate(targetActivityConstraintDate, timeZone);
        }
        Timestamp expectedContraintDateForTargetActivity = new Timestamp(targetActivityConstraintDate.getTimeInMillis());
        return expectedContraintDateForTargetActivity;
    }
    
    private Timestamp calculateConstraintDateOfActivity(Plan plan, int hours, boolean SNETConstraintType) throws WTException {
        long daysInmilliseconds = TimeUnit.SECONDS.toMillis(TimeUnit.HOURS.toSeconds(hours));
        Calendar calForTargetAct = addDurationAndAdjustDate(plan.getStartDate(), daysInmilliseconds, SNETConstraintType);
        Timestamp constraintDateForActivity = new Timestamp(calForTargetAct.getTimeInMillis());
        return constraintDateForActivity;
    }
    
    private Plan setTimeToStartDayTime(Plan plan) throws WTPropertyVetoException, WTException {
        ProcessorUtils processorutils = new ProcessorUtils(plan);
        Timestamp currentDate = processorutils.getAdjustedCurrentTime();
        currentDate = ScheduleUtils.getWorkDayFloor(currentDate, null);        
        plan.setStartDate(currentDate);
        plan.setDefaultStart(currentDate);
        plan = (Plan)PersistenceHelper.manager.save(plan);
        new StandardPlannableService().createDummyMilestone(plan);
        return plan;
    }
    
    private void verifySNETConstaintDateForDiffPlan(boolean targetPlanStartDateBeforeCurrentToday)
            throws WTException, WTPropertyVetoException, ObjectNoLongerExistsException {
        int hours = 40;
        targetSNETPlan = (Plan)PersistenceHelper.manager.refresh(targetSNETPlan);
        targetSNETPlan = adjustDayAndTimeToTargetPlanStartDate(targetSNETPlan, hours, SNET, targetPlanStartDateBeforeCurrentToday);
        
        /*********************************** Copy Functionality Testing ******************************************/
        clientData = setClipBoardAndSelectedItem(NmOid.newNmOid(this.activitySNETSource.getPersistInfo().getObjectIdentifier()) ,setSelectedActivity(act3), !CUT, targetSNETPlanOid);
        activitySNETSource = (PlanActivity)PersistenceHelper.manager.refresh(activitySNETSource);
        PlanActivity sourceActivity = (PlanActivity)PlannableHelper.service.getPlannableByLineNumber(sourceSNETPlan, (1));
        int taregtSizeBeforeCopy = PlannableHelper.service.getAllPlannables(targetSNETPlan, false).size();
        FormResult result = classToTest.doOperation(clientData, objectBeans);

        targetSNETPlan = (Plan)PersistenceHelper.manager.refresh(targetSNETPlan);
        WTCollection plannables = PlannableHelper.service.getAllPlannables(targetSNETPlan, false);
        Assert.assertTrue("plannables size expected as "+(taregtSizeBeforeCopy + 1 )+ " instead found = "+plannables.size(),
                plannables.size() == taregtSizeBeforeCopy + 1);
        activitySNETSource = (PlanActivity)PersistenceHelper.manager.refresh(activitySNETSource);
        act3 = (PlanActivity)PersistenceHelper.manager.refresh(act3);
        actSNETType = (PlanActivity)PersistenceHelper.manager.refresh(actSNETType);
        PlanActivity targetActivity = (PlanActivity)PlannableHelper.service.getPlannableByLineNumber(targetSNETPlan, (act3.getLineNumber()-1));
        
        /**** Calculating expected constraint date of target activity****/
        Timestamp expectedContraintDateForTargetActivity = calculateExpectedConstraintDateOfTargetActivity(activitySNETSource,targetSNETPlan, sourceSNETPlan);
 
        Assert.assertTrue("targetActivity's expected Constraint Type is "+ sourceActivity.getConstraintType()+ " instead found = "+targetActivity.getConstraintType(),
                targetActivity.getConstraintType().equals(sourceActivity.getConstraintType()));
        Assert.assertTrue("targetActivity's expected Constraint Date is "+ expectedContraintDateForTargetActivity+ " instead found = "+targetActivity.getConstraintDate(),
                targetActivity.getConstraintDate().equals(expectedContraintDateForTargetActivity));
        /*Assert.assertTrue("targetActivity's expected estimated start Date is "+ expectedContraintDateForTargetActivity+ " instead found = "+targetActivity.getStartDate(),
                targetActivity.getConstraintDate().equals(targetActivity.getStartDate()));*/
        
        /*********************************** Cut Functionality Testing ******************************************/
        clientData = setClipBoardAndSelectedItem(NmOid.newNmOid(this.activitySNETSource.getPersistInfo().getObjectIdentifier()) ,setSelectedActivity(act2), CUT, targetSNETPlanOid);
        activitySNETSource = (PlanActivity)PersistenceHelper.manager.refresh(activitySNETSource);
        int sourceSizeBeforeCut = PlannableHelper.service.getAllPlannables(sourceSNETPlan, false).size();
        result = classToTest.doOperation(clientData, objectBeans);

        //Cut will Increase the size by 1 + 1(from Copy Functionality above) 
        targetSNETPlan = (Plan)PersistenceHelper.manager.refresh(targetSNETPlan);
        plannables = PlannableHelper.service.getAllPlannables(targetSNETPlan, false);
        Assert.assertTrue("plannables size expected as "+(taregtSizeBeforeCopy + 2) + " instead found = "+plannables.size(),
                plannables.size() == taregtSizeBeforeCopy + 2);
        
        sourceSNETPlan = (Plan)PersistenceHelper.manager.refresh(sourceSNETPlan);
        plannables = PlannableHelper.service.getAllPlannables(sourceSNETPlan, false);
                    
        Assert.assertTrue("plannables size expected as "+(sourceSizeBeforeCut - 1) + " instead found = "+plannables.size(),
                plannables.size() == (sourceSizeBeforeCut - 1));
        /*Assert.assertTrue("targetActivity's expected estimated start Date is "+ expectedContraintDateForTargetActivity+ " instead found = "+targetActivity.getStartDate(),
                targetActivity.getConstraintDate().equals(targetActivity.getStartDate()));*/
    }
    
    private void verifyMFOConstaintDateForDiffPlan(boolean targetPlanStartDateBeforeCurrentToday)
            throws WTException, WTPropertyVetoException, ObjectNoLongerExistsException {
        int hours = 40;
        targetPlanWithMFOActivity = (Plan)PersistenceHelper.manager.refresh(targetPlanWithMFOActivity);
        targetPlanWithMFOActivity = adjustDayAndTimeToTargetPlanStartDate(targetPlanWithMFOActivity, hours, !SNET, targetPlanStartDateBeforeCurrentToday);
        
        //Copy Functionality Testing
        clientData = setClipBoardAndSelectedItem(NmOid.newNmOid(this.activityMFOSource.getPersistInfo().getObjectIdentifier()) ,setSelectedActivity(activityM3), !CUT, targetMFOTypePlanOid);
        activityMFOSource = (PlanActivity)PersistenceHelper.manager.refresh(activityMFOSource);
        PlanActivity oldActivity = (PlanActivity)PlannableHelper.service.getPlannableByLineNumber(sourcePlanWithMFOActivity, (2));
        int taregtSizeBeforeCopy = PlannableHelper.service.getAllPlannables(targetPlanWithMFOActivity, true).size();
        FormResult result = classToTest.doOperation(clientData, objectBeans);

        targetPlanWithMFOActivity = (Plan)PersistenceHelper.manager.refresh(targetPlanWithMFOActivity);
        WTCollection plannables = PlannableHelper.service.getAllPlannables(targetPlanWithMFOActivity, true);
        Assert.assertTrue("plannables size expected as "+(taregtSizeBeforeCopy + 1 )+ " instead found = "+plannables.size(),
                plannables.size() == taregtSizeBeforeCopy + 1);
        activityMFOSource = (PlanActivity)PersistenceHelper.manager.refresh(activityMFOSource);
        activityM3 = (PlanActivity)PersistenceHelper.manager.refresh(activityM3);
        activityMFO4 = (PlanActivity)PersistenceHelper.manager.refresh(activityMFO4);
        PlanActivity newActivity = (PlanActivity)PlannableHelper.service.getPlannableByLineNumber(targetPlanWithMFOActivity, 
                (activityM3.getLineNumber() - 1));
        
         /**** Calculating expected constraint date of target activity****/
        Timestamp expectedContraintDateForTargetActivity = calculateExpectedConstraintDateOfTargetActivity(activityMFOSource,targetPlanWithMFOActivity, sourcePlanWithMFOActivity);
        
        Assert.assertTrue("New activity's expected Constraint Type is "+oldActivity.getConstraintType()+ " instead found = "+newActivity.getConstraintType(),
                newActivity.getConstraintType().equals(oldActivity.getConstraintType()));
        Assert.assertTrue("New activity's expected Constraint Date is "+expectedContraintDateForTargetActivity+ " instead found = "+newActivity.getConstraintDate(),
                newActivity.getConstraintDate().equals(expectedContraintDateForTargetActivity));
        Assert.assertTrue("targetActivity's expected estimated finish Date is "+ expectedContraintDateForTargetActivity+ " instead found = "+newActivity.getFinishDate(),
                newActivity.getConstraintDate().equals(newActivity.getFinishDate()));
        //Cut Functionality Testing
        clientData = setClipBoardAndSelectedItem(NmOid.newNmOid(this.activityMFOSource.getPersistInfo().getObjectIdentifier()) ,setSelectedActivity(activityM2), CUT, targetMFOTypePlanOid);
        activityMFOSource = (PlanActivity)PersistenceHelper.manager.refresh(activityMFOSource);
        int sourceSizeBeforeCopy = PlannableHelper.service.getAllPlannables(sourcePlanWithMFOActivity, true).size();
        result = classToTest.doOperation(clientData, objectBeans);

        //Cut will Increase the size by 1 + 1(from Copy Functionality above) 
        targetPlanWithMFOActivity = (Plan)PersistenceHelper.manager.refresh(targetPlanWithMFOActivity);
        plannables = PlannableHelper.service.getAllPlannables(targetPlanWithMFOActivity, true);
        Assert.assertTrue("plannables size expected as "+(taregtSizeBeforeCopy + 2) + " instead found = "+plannables.size(),
                plannables.size() == taregtSizeBeforeCopy + 2);
        sourcePlanWithMFOActivity = (Plan)PersistenceHelper.manager.refresh(sourcePlanWithMFOActivity);
        plannables = PlannableHelper.service.getAllPlannables(sourcePlanWithMFOActivity, true);
        Assert.assertTrue("plannables size expected as "+(sourceSizeBeforeCopy - 1) + " instead found = "+plannables.size(),
                plannables.size() == (sourceSizeBeforeCopy - 1));
        Assert.assertTrue("targetActivity's expected estimated finish Date is "+ expectedContraintDateForTargetActivity+ " instead found = "+newActivity.getFinishDate(),
                newActivity.getConstraintDate().equals(newActivity.getFinishDate()));
    }
}
