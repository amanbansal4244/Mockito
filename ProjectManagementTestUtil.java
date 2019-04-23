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
package com.ptc.projectmanagement.testUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.descriptor.ModelContext;
import com.ptc.core.components.factory.DefaultComponentDescriptor;
import com.ptc.core.components.factory.DefaultJCAObjectSource;
import com.ptc.core.components.factory.DefaultModelContext;
import com.ptc.core.components.forms.NamePropertyProcessor;
import com.ptc.core.components.forms.ObjectFormProcessorDelegate;
import com.ptc.core.components.util.AttributeHelper;
import com.ptc.core.lwc.common.BaseDefinitionService;
import com.ptc.core.lwc.common.TypeDefinitionService;
import com.ptc.core.lwc.common.view.PropertyDefinitionReadView;
import com.ptc.core.lwc.common.view.PropertyDefinitionWriteView;
import com.ptc.core.lwc.common.view.PropertyValueReadView;
import com.ptc.core.lwc.common.view.PropertyValueWriteView;
import com.ptc.core.lwc.common.view.TypeDefinitionReadView;
import com.ptc.core.lwc.server.LWCTypeDefinition;
import com.ptc.core.meta.common.AttributeTypeIdentifier;
import com.ptc.core.meta.common.CreateOperationIdentifier;
import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.common.TypeInstanceIdentifier;
import com.ptc.core.meta.common.impl.WCTypeIdentifier;
import com.ptc.core.meta.server.TypeIdentifierUtility;
import com.ptc.core.meta.type.common.TypeInstance;
import com.ptc.core.ui.resources.ComponentMode;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.project.processor.AutoUpdateProjectFormDelegate;
import com.ptc.netmarkets.project.processor.AutoUpdateUserFormDelegate;
import com.ptc.netmarkets.project.processor.CreatePROPLProjectFormProcessor;
import com.ptc.netmarkets.util.beans.HTTPRequestData;
import com.ptc.netmarkets.util.beans.NmClipboardBean;
import com.ptc.netmarkets.util.beans.NmClipboardItem;
import com.ptc.netmarkets.util.beans.NmClipboardUtility;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.netmarkets.util.misc.NmContext;
import com.ptc.netmarkets.util.misc.NmContextItem;
import com.ptc.projectmanagement.assignment.AssignmentHelper;
import com.ptc.projectmanagement.assignment.ResourceAssignment;
import com.ptc.projectmanagement.assignment.ResourceAssignmentType;
import com.ptc.projectmanagement.assignment.resource.PlanResource;
import com.ptc.projectmanagement.assignment.resource.Resourceable;
import com.ptc.projectmanagement.deliverable.PlanDeliverable;
import com.ptc.projectmanagement.deliverable.PlanDeliverableLink;
import com.ptc.projectmanagement.deliverable.TrackingIntentType;
import com.ptc.projectmanagement.msproject.MspExportInfo;
import com.ptc.projectmanagement.msproject.MspExportUtils;
import com.ptc.projectmanagement.msproject.MspHelper;
import com.ptc.projectmanagement.msproject.MspImportInfo;
import com.ptc.projectmanagement.msproject.MspImportMode;
import com.ptc.projectmanagement.msproject.StandardMspServiceIntegrationTest;
import com.ptc.projectmanagement.plan.AbstractPlanActivity;
import com.ptc.projectmanagement.plan.DateConstraint;
import com.ptc.projectmanagement.plan.Duration;
import com.ptc.projectmanagement.plan.DurationFormat;
import com.ptc.projectmanagement.plan.Plan;
import com.ptc.projectmanagement.plan.PlanActivity;
import com.ptc.projectmanagement.plan.PlanHelper;
import com.ptc.projectmanagement.plan.Plannable;
import com.ptc.projectmanagement.plan.PlannableState;
import com.ptc.projectmanagement.plan.processors.EditPlanFormProcessor;
import com.ptc.projectmanagement.plannable.PlannableHelper;
import com.ptc.projectmanagement.plannable.PrecedenceConstraint;
import com.ptc.projectmanagement.plannable.PrecedenceType;
import com.ptc.projectmanagement.plannable.Rootable;
import com.ptc.projectmanagement.plannable.ScheduleUtils;
import com.ptc.projectmanagement.plannable.StandardPlannableService;
import com.ptc.projectmanagement.util.AdminUtils;
import com.ptc.projectmanagement.util.ClassicToEPPUtils;
import com.ptc.projectmanagement.util.DurationUtils;
import com.ptc.projectmanagement.util.PlannableUtils;
import com.ptc.projectmanagement.util.ProcessorUtils;
import com.ptc.test.remote.LoginUtils;

import wt.access.AccessControlServerHelper;
import wt.access.SecurityLabels;
import wt.admin.AdminDomainRef;
import wt.admin.AdministrativeDomain;
import wt.admin.DomainAdministeredHelper;
import wt.fc.ObjectIdentifier;
import wt.fc.ObjectNoLongerExistsException;
import wt.fc.ObjectReference;
import wt.fc.PersistInfo;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.collections.CollectionsHelper;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTCollection;
import wt.fc.collections.WTHashSet;
import wt.fc.collections.WTKeyedHashMap;
import wt.fc.collections.WTSet;
import wt.folder.CabinetBased;
import wt.folder.Folder;
import wt.folder.FolderHelper;
import wt.folder.FolderNotFoundException;
import wt.folder.SubFolder;
import wt.inf.container.ContainerTestHelper;
import wt.inf.container.OrgContainer;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerHelper;
import wt.inf.container.WTContainerRef;
import wt.inf.container.WTContainerTemplate;
import wt.inf.container.WTContainerTemplateRef;
import wt.inf.team.ContainerTeam;
import wt.inf.team.ContainerTeamHelper;
import wt.inf.team.ContainerTeamReference;
import wt.inf.template.ContainerTemplateHelper;
import wt.introspection.ClassInfo;
import wt.introspection.WTIntrospectionException;
import wt.introspection.WTIntrospector;
import wt.log4j.LogR;
import wt.method.MethodContext;
import wt.method.RemoteMethodServer;
import wt.org.TestOrgHelper;
import wt.org.WTGroup;
import wt.org.WTPrincipal;
import wt.org.WTUser;
import wt.ownership.Ownership;
import wt.pdmlink.PDMLinkProduct;
import wt.pds.StatementSpec;
import wt.preference.PreferenceHelper;
import wt.project.Role;
import wt.projmgmt.admin.Project2;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.queue.MethodArgument;
import wt.queue.QueueHelper;
import wt.queue.ScheduleQueue;
import wt.queue.ScheduleQueueEntry;
import wt.services.ServiceFactory;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.type.TypeDefinitionReference;
import wt.type.TypedUtility;
import wt.util.UniquenessHelper;
import wt.util.WTContext;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;

public class ProjectManagementTestUtil {

    protected static UniquenessHelper uniqueHelper;
    private static Plan testPlan;
    private static Project2 testProject,project;
    private static final Logger log;
    public static final HashMap<String,PrecedenceType> precConstantsMap;
    public static OrgContainer orgContainer = null;
    private static final TypeDefinitionService TYPE_DEF_SERVICE = ServiceFactory.getService(TypeDefinitionService.class);
    private static final BaseDefinitionService BASE_DEF_SERVICE = ServiceFactory.getService(BaseDefinitionService.class);

    public static final String CHILD_PARENT_MAP = "CHILD_PARENT_MAP";
    public static final String PARENT_CHILD_MAP = "PARENT_CHILD_MAP";
    public static final String CONSTRAINT_DATE = "constraintDate_0_col_constraintDate_0";
    public static final String CONSTRAINT_DATE_HOURS = "constraintDate_0_col_constraintDate_0_qual_Hours";
    public static final String CONSTRAINT_DATE_MINUTES = "constraintDate_0_col_constraintDate_0_qual_Mins";
    private static HashMap childParentAssociationMap = new HashMap();
    public TimeZone originalUserTimeZone;

    static {
        try {
            log = LogR.getLogger(ProjectManagementTestUtil.class.getName());
            precConstantsMap = new HashMap<String,PrecedenceType>();
            precConstantsMap.put("FS", PrecedenceType.FS);
            precConstantsMap.put("SS", PrecedenceType.SS);
            precConstantsMap.put("FF", PrecedenceType.FF);
            precConstantsMap.put("SF", PrecedenceType.SF);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    public static Plan getPlan() throws Exception {
        if (testPlan == null) {
            try {
                testPlan = createTestPlan();
            } catch (WTException e) {
                e.printStackTrace();
                throw e;
            }
        }
        return testPlan;
    }

    public static void dropAllPlannables() {
        dropAllPlannables(testPlan);
    }

    public static void dropAllPlannables(Plan plan) {
        // Cleanup
        try {
            WTCollection planChildren = PlanHelper.service.getPlannableContents(plan);
            WTSet activities = new WTHashSet(planChildren.persistableCollection());
            activities = (WTSet) CollectionsHelper.manager.refresh(activities);
            PersistenceHelper.manager.delete(activities);
            plan = (Plan) PersistenceHelper.manager.refresh(plan);
            PersistenceHelper.manager.delete(plan);
        } catch (ObjectNoLongerExistsException e) {
            e.printStackTrace();
        } catch (WTException e) {
            e.printStackTrace();
        }
    }

    private static Project2 createProject() throws Exception {
        Project2 testProject = null;
        try {
            RemoteMethodServer.ServerFlag = false;
            WTContainerRef exchangeContainerRef = WTContainerHelper.service.getExchangeRef();
            WTContainer exchangeContainer = exchangeContainerRef.getContainer();
            OrgContainer orgContainer = ContainerTestHelper.findOrgContainer("MyTestOrganization");
            if (orgContainer == null) {
                orgContainer = ContainerTestHelper.createOrgContainer("MyTestOrganization", exchangeContainer);
            }

            testProject = ContainerTestHelper.findProject("TestALAPProject");
            if (testProject == null) {
                ContainerTeam containerTeam = getContainerTeam(orgContainer);
                testProject = createProject("TestALAPProject", orgContainer, null, ContainerTeamReference.newContainerTeamReference(containerTeam), false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return testProject;
    }

    /**
     * @param orgContainer
     * @return
     * @throws WTException
     * @throws Exception
     */
    public static ContainerTeam getContainerTeam(OrgContainer orgContainer) throws WTException, Exception {
        ContainerTeam containerTeam = ContainerTeamHelper.service.getSharedTeamByName(orgContainer, "MyTestSharedTeam");
        if (containerTeam == null) {
            ContainerTeamReference containerTeamRef = ContainerTestHelper.createSharedContainerTeam("MyTestSharedTeam", orgContainer, true, "false", SessionHelper.manager.getPrincipalReference());
            containerTeam = (ContainerTeam) containerTeamRef.getObject();
        }
        return containerTeam;
    }

    public static Project2 createProject(final String name, final WTContainer parent, WTContainerTemplate template, ContainerTeamReference cTeamRef, Boolean extendLocally) throws Exception {
        Project2 project = null;
        WTContainerRef exchangeRef = WTContainerHelper.getExchangeRef();
        project = Project2.newProject2();
        project.setContainer(parent);
        project.setName(name);
        project.setDescription("Created by ContainerTestHelper");
        if (template == null) {
            template = ContainerTemplateHelper.service.getContainerTemplate(exchangeRef, "General", Project2.class);
            project.setContainerTemplate(template);
        } else {
            project.setContainerTemplate(template);
        }
        if (cTeamRef != null) {
            project = (Project2) ContainerTeamHelper.assignSharedTeamToContainer(project, cTeamRef, true);
        }
        if (extendLocally) {
            MethodContext.getContext().put(WTContainerHelper.EXTENDABLE_CONTAINER, Boolean.TRUE);
        }
        project = (Project2) ContainerTeamHelper.setSendInvitations(project, false);

        WTContainerHelper.setPrivateAccess(project, true);
        AdminDomainRef adminDefaultDomainRef = parent.getDefaultDomainReference();
        DomainAdministeredHelper.setAdminDomain(project, AdministrativeDomain.newAdministrativeDomain(project.getName(), adminDefaultDomainRef, exchangeRef));
        WTContainerHelper.setPublicParentDomain(project, adminDefaultDomainRef);

        project = (Project2) WTContainerHelper.service.create(project);
        if (extendLocally) {
            MethodContext.getContext().remove(WTContainerHelper.EXTENDABLE_CONTAINER);
        }
        return project;
    }

    /***
     * 
     * @return
     * @throws WTException
     */
    private static Plan createTestPlan() throws Exception {
        try {
            if (testProject == null) {
                testProject = createProject();
            }
            if (testPlan == null) {
                WTContainerRef contextRef = WTContainerRef.newWTContainerRef(testProject);
                contextRef.setObject(testProject);
                testPlan = Plan.newPlan(contextRef);
                testPlan.setContainer(testProject);
                testPlan.setContainerReference(contextRef);
                testPlan.setName("Test_Plan");
                ProcessorUtils processorutils = new ProcessorUtils(testPlan);
                Timestamp planStart = processorutils.getAdjustedCurrentTime();
                Timestamp planFinish = planStart;
                testPlan.setStartDate(planStart);
                testPlan.setHolder(contextRef);
                testPlan.setFinishDate(planFinish);
                testPlan.setSummary(true);
                testPlan = (Plan) PersistenceHelper.manager.store(testPlan);
            }
        } catch (WTPropertyVetoException e) {
            e.printStackTrace();
        }
        return testPlan;
    }

    /**
     * Creates a new Plan, associates it to the input context object, persists it and returns it to the caller.
     * Delegates the work to an overloaded API if the input context object is a Project. Does nothing if the input
     * object is not a WTContainer instance.
     *
     * @param objRef
     *            ObjectReference of the input context object (e.g., a Project or a Product).
     * @return The newly created Plan, with its attributes set as appropriate.
     * @throws Exception
     *             if any of the invoked APIs threw an exception.
     */
    public static Plan createNewPlan(ObjectReference objRef) throws Exception {
        Plan newPlan = null;

        Object obj = objRef.getObject();
        if (obj instanceof WTContainer) {
            WTContainer wtContainer = (WTContainer) obj;

            if (obj instanceof Project2) {
                newPlan = createNewPlan((Project2) obj);
                return newPlan;
            }

            WTContainerRef contextRef = WTContainerRef.newWTContainerRef(wtContainer);
            contextRef.setObject(wtContainer);
            newPlan = Plan.newPlan(contextRef);
            newPlan.setContainer(wtContainer);
            newPlan.setContainerReference(contextRef);

            // Set the name of the Plan to that of the input context object itself.
            newPlan.setName(wtContainer.getName());

            ProcessorUtils processorutils = new ProcessorUtils(newPlan);
            Timestamp start = processorutils.getAdjustedCurrentTime();
            start = ScheduleUtils.getWorkDayFloor(start, null);
            Timestamp finish = start;
            newPlan.setStartDate(start);
            newPlan.setFinishDate(finish);

            newPlan.setHolder(contextRef);
            newPlan.setSummary(true);
            newPlan.setLineNumber(0);
            newPlan = (Plan) PersistenceHelper.manager.store(newPlan);
        }
        return newPlan;
    }

    public static Plan createNewPlan(WTContainer container) throws Exception {
        Plan newPlan = null;

        WTContainerRef contextRef = WTContainerRef.newWTContainerRef(container);
        contextRef.setObject(container);
        newPlan = Plan.newPlan(contextRef);
        newPlan.setContainer(container);
        newPlan.setContainerReference(contextRef);
        newPlan.setName(container.getName());

        ProcessorUtils processorutils = new ProcessorUtils(newPlan);
        Timestamp start = processorutils.getAdjustedCurrentTime();
        start = ScheduleUtils.getWorkDayFloor(start, null);
        Timestamp finish = start;
        newPlan.setStartDate(start);
        newPlan.setFinishDate(finish);

        newPlan.setHolder(contextRef);
        newPlan.setSummary(true);
        newPlan.setLineNumber(0);
        newPlan = (Plan) PersistenceHelper.manager.store(newPlan);

        return newPlan;
    }
    public static Plan createNewPlan(Project2 project, String planName, boolean isAutoExecution, PlannableState plannableState) throws Exception {
        Plan plan = createNewPlan(project);
        plan.setName(planName);
        plan.setAutoExecution(isAutoExecution);
        plan.setPlannableState(plannableState);
        plan = (Plan) PersistenceHelper.manager.save(plan);
        return plan;
    }
    public static PlanActivity createNewActivity(WTContainer container, Plan plan) throws Exception {
        PlanActivity planActivity = null;
        WTContainerRef contextRef = WTContainerRef.newWTContainerRef(container);
        contextRef.setObject(container);
        planActivity = PlanActivity.newPlanActivity(contextRef);
        planActivity.setPlannableState(PlannableState.SCHEDULED);
        planActivity.setParent(plan);
        planActivity.setRoot(plan);
        return planActivity;
    }

    public static PlanActivity createNewScheduledDeliverable(WTContainer container, Plan plan, String name, TrackingIntentType intentType,PlannableState plannableState, Plannable parent) throws Exception {
        PlanActivity planActivity = createNewActivity(container, plan);
        planActivity.setName(name);;
        planActivity.setHasDeliverable(true);
        planActivity.setTrackingIntent(intentType);
        planActivity.setPlannableState(plannableState);
        planActivity.setParent(parent);
        planActivity = (PlanActivity) PlanHelper.service.addToPlan(planActivity, plan);
        return planActivity;
    }

    public static ModelContext createModelContext(final ComponentMode mode, Persistable object) throws Exception {
        ModelContext mc = new DefaultModelContext();
        // Set the component mode for the model context
        mc.setDescriptor(new DefaultComponentDescriptor() {
            private static final long serialVersionUID = 1L;

            @Override
            public ComponentMode getMode() {
                return mode;
            }

            @Override
            public ComponentMode getDescriptorMode() {
                return mode;
            }
        });

        DefaultComponentDescriptor parentDescriptor = new DefaultComponentDescriptor();
        mc.setParentDescriptor(parentDescriptor);
        mc.getDescriptor().setMode(mode);
        NmCommandBean cb = new NmCommandBean();

        if (object != null) {
            List<NmOid> objectSources = new ArrayList<NmOid>();
            objectSources.add(NmOid.newNmOid(object.getPersistInfo().getObjectIdentifier()));
            DefaultJCAObjectSource jcaObject = new DefaultJCAObjectSource(objectSources, parentDescriptor);
            jcaObject.setTargetObjects(parentDescriptor, objectSources);
            mc.setObjectSource(jcaObject);
            mc.setModelObject(object);

            cb.setPrimaryOid(NmOid.newNmOid(object.getPersistInfo().getObjectIdentifier()));
            cb.getPrimaryOid().setRef(object);
            cb.setPageOid(NmOid.newNmOid(object.getPersistInfo().getObjectIdentifier()));
            cb.getPageOid().setRef(object);

        }
        mc.setNmCommandBean(cb);
        return mc;
    }

    public static ModelContext createModelContextWithTypeInstance(final ComponentMode mode, Persistable object,
            String attributeID, String typeName) throws Exception {
        ModelContext mc = createModelContext(mode, object);
        mc.getJCAObject().setTypeInstance(createTypeInstance(attributeID,typeName));
        return mc;
    }

    public static PDMLinkProduct createTestProduct(OrgContainer org, String productName, WTUser creator) throws WTException {		
        PDMLinkProduct product = null;
        try {
            LoginUtils.becomeUser("demo");
            WTContainerRef exchangeRef = WTContainerHelper.getExchangeRef();
            WTContainerTemplate template = ContainerTemplateHelper.service.getContainerTemplate(exchangeRef, "General Product", PDMLinkProduct.class);

            product = PDMLinkProduct.newPDMLinkProduct();
            product.setContainer(org);
            product.setCreator(creator);
            product.setDescription(productName);
            product.setName(productName);
            WTContainerHelper.setBusinessNamespace(product, true);
            product.setDomainRef(org.getSystemDomainReference());
            product.setInheritedDomain(false);
            product.setSharingEnabled(true);
            product.setContainerTemplate(template);
            product = (PDMLinkProduct) WTContainerHelper.service.create(product);
            createContainerFolder(product, productName, null);

            product = (PDMLinkProduct) PersistenceHelper.manager.save(product);

            return product;
        } catch (WTPropertyVetoException wtpve) {
            throw new WTException(wtpve);
        } catch (Exception e) {
            throw new WTException(e);
        }
    }

    private static Folder createContainerFolder(WTContainer container, String folderName, AdminDomainRef domain) throws WTException {
        String folderPath = null;

        // If no folderName is specified, return the top level cabinet
        if (folderName == null)
            return container.getDefaultCabinet();

        folderPath = FolderHelper.getFolderPath((CabinetBased) container.getDefaultCabinet()) + "/" + folderName;
        WTContainerRef containerRef = WTContainerRef.newWTContainerRef(container);

        Folder folder = null;
        try {
            folder = FolderHelper.service.getFolder(folderPath, containerRef);
            SubFolder subFolder = (SubFolder) folder;
            if (DomainAdministeredHelper.getAdminDomainRef(subFolder).equals(domain) == false)
                folder = null;
        } catch (FolderNotFoundException fnfe) {
            folder = null;
        }

        if (folder == null)
            folder = FolderHelper.service.createSubFolder(folderPath, domain, containerRef);

        return folder;
    }

    public static PDMLinkProduct lookupProduct(String name) throws Exception {
        QuerySpec qs = new QuerySpec(PDMLinkProduct.class);
        qs.appendWhere(new SearchCondition(PDMLinkProduct.class, PDMLinkProduct.NAME, SearchCondition.EQUAL, name), new int[] { 0 });
        QueryResult prods = PersistenceHelper.manager.find((StatementSpec) qs);
        if (prods != null && prods.hasMoreElements()) {
            return (PDMLinkProduct) prods.nextElement();
        }
        return null;
    }

    public static ArrayList<WTPrincipal> createNewUsers(ArrayList<String> userNames) throws Exception {
        ArrayList<WTPrincipal> userList = new ArrayList<WTPrincipal>();
        for (String userName : userNames) {
            WTUser newUser = LoginUtils.createUser(userName, "1");
            userList.add(newUser);
        }
        return userList;
    }

    public static ArrayList<WTPrincipal> createNewGroups(ArrayList<String> groupNames, int noOfMemberUsers) throws Exception {
        ArrayList<WTPrincipal> groupList = new ArrayList<WTPrincipal>();
        for (String groupName : groupNames) {
            WTGroup newGroup = TestOrgHelper.createGroup(groupName, "group for pjl testing", null);
            ArrayList<String> uniqueUserNames = generateUniqueNames("pjlTestUser", noOfMemberUsers);
            ArrayList<WTPrincipal> memberUserList = createNewUsers(uniqueUserNames);
            TestOrgHelper.addMembers(newGroup, (WTPrincipal[]) memberUserList.toArray(new WTPrincipal[] {}));
            groupList.add(newGroup);
        }
        return groupList;
    }

    public static ArrayList<String> generateUniqueNames(String prefix, int num) throws InterruptedException {
        ArrayList<String> uniqueNames = new ArrayList<String>();
        for (int i = 0; i < num; i++) {
            UniquenessHelper uniqueHelper = new UniquenessHelper();
            String uniqueName = uniqueHelper.qualifyName(prefix);
            uniqueNames.add(uniqueName);
            TimeUnit.MILLISECONDS.sleep(10);
        }
        return uniqueNames;
    }

    public static WTCollection createNewPlanActivities(Plan plan, ArrayList<String> activityNames, int startingLineNumber) throws WTException {
        WTCollection activityList = new WTArrayList();
        try {
            Duration actDuration = new Duration();
            actDuration.setDurationFormat(DurationFormat.HOURS);
            // hard code the value of activity duration to be 10 days.
            actDuration.setMillis(10 * 8 * 3600 * 1000);
            for (String actName : activityNames) {
                PlanActivity act = PlanActivity.newPlanActivity(plan.getContainerReference());
                act.setName(actName);
                act.setDuration(actDuration);
                act.setLineNumber(startingLineNumber);
                act = (PlanActivity) PlanHelper.service.addToPlan(act, plan);
                activityList.add(act);
                startingLineNumber++;
            }
        } catch (WTPropertyVetoException ve) {
            throw new WTException(ve);
        }
        activityList = PersistenceHelper.manager.save(activityList);
        return activityList;
    }

    public static WTCollection createNewPlanDeliverables(Plan plan, Map<String, PlanActivity> deliverableName2Activity) throws WTException {
        WTCollection deliverableList = new WTArrayList();
        WTCollection deliverableLinkList = new WTArrayList();
        try {
            Map.Entry<String, PlanActivity> entry = null;
            String delName = null;
            PlanActivity activity = null;
            PlanDeliverable deliverable = null;
            PlanDeliverableLink delLink = null;
            for (Iterator<Map.Entry<String, PlanActivity>> iter = deliverableName2Activity.entrySet().iterator(); iter.hasNext();) {
                entry = iter.next();
                delName = entry.getKey();
                activity = entry.getValue();
                if (activity != null) {
                    deliverable = PlanDeliverable.newPlanDeliverable(activity);
                } else {
                    deliverable = PlanDeliverable.newPlanDeliverable(plan);
                }
                deliverable.setName(delName);
                deliverable.setParent(plan);
                deliverable.setDomainRef(plan.getDomainRef());
                if (activity != null) {
                    delLink = PlanDeliverableLink.newPlanDeliverableLink(activity, deliverable);
                    deliverableLinkList.add(delLink);
                }
                deliverableList.add(deliverable);
            }
        } catch (WTPropertyVetoException ve) {
            throw new WTException(ve);
        }
        deliverableList = PersistenceHelper.manager.save(deliverableList);
        deliverableLinkList = PersistenceHelper.manager.save(deliverableLinkList);
        return deliverableList;
    }

    public static ResourceAssignment createNewResourceAssignment(PlanResource resource, PlanActivity activity, ResourceAssignmentType assignmentType) throws WTException {
        ResourceAssignment assignment = null;
        try {
            // create new duration variable which will be used to set the total effort
            Duration totalEffort = new Duration();
            totalEffort.setDurationFormat(DurationFormat.HOURS);
            // hard code the value for total effort to be 10 days.
            totalEffort.setMillis(10 * 8 * 3600 * 1000);

            // create new duration variable which will be used to set the actual effort
            Duration actualEffort = new Duration();
            actualEffort.setDurationFormat(DurationFormat.HOURS);
            // hard code the value for actual effort to be 0 days.
            actualEffort.setMillis(0);

            Duration remainingEffort = totalEffort;
            Duration duration = activity.getDuration();
            double allocationPercentage = AssignmentHelper.service.calculateAllocationPercentage(duration, remainingEffort);

            assignment = ResourceAssignment.newResourceAssignment(resource, activity, assignmentType);
            assignment.setDoneEffort(actualEffort);
            assignment.setTotalEffort(totalEffort);
            assignment.setRemainingEffort(totalEffort);
            assignment.setAllocationPercentage(allocationPercentage);
        } catch (WTPropertyVetoException ve) {
            throw new WTException(ve);
        }

        return assignment;
    }

    public static OrgContainer lookupOrgContainer(String name) throws Exception {
        OrgContainer orgContainer = null;

        QuerySpec qs = new QuerySpec(OrgContainer.class);
        qs.appendWhere(new SearchCondition(OrgContainer.class, OrgContainer.NAME, SearchCondition.EQUAL, name), new int[] { 0 });
        QueryResult orgs = PersistenceHelper.manager.find((StatementSpec) qs);
        if (orgs.hasMoreElements()) {
            orgContainer = (OrgContainer) orgs.nextElement();
        }

        return orgContainer;
    }

    public static Project2 lookupProject(String name) throws Exception {
        QuerySpec qs = new QuerySpec(Project2.class);
        qs.appendWhere(new SearchCondition(Project2.class, Project2.NAME, SearchCondition.EQUAL, name), new int[] { 0 });
        QueryResult projs = PersistenceHelper.manager.find((StatementSpec) qs);
        if (projs != null && projs.hasMoreElements()) {
            return (Project2) projs.nextElement();
        }
        return null;
    }

    public static Project2 createNewProject(OrgContainer orgContainer, String projectName) throws Exception {
        return createNewProject(orgContainer, projectName, false);
    }

    public static Project2 createNewProject(OrgContainer orgContainer, String projectName, boolean isAutoExecution) throws Exception {
        WTContainerRef exchangeRef = WTContainerHelper.getExchangeRef();
        WTContainerTemplate projectTemplate = ContainerTemplateHelper.service.getContainerTemplate(exchangeRef, "General", Project2.class);
        Project2 newProject = Project2.newProject2();
        newProject.setName(projectName);
        newProject.setContainer(orgContainer);
        newProject.setContainerTemplate(projectTemplate);
        WTContainerHelper.setPrivateAccess(newProject, false);
        newProject = (Project2) WTContainerHelper.service.create(newProject);

        // Save the project
        newProject = (Project2) PersistenceHelper.manager.save(newProject);

        if(isAutoExecution) {
            Plan plan = (Plan) PlanHelper.service.getPlan((ObjectReference.newObjectReference(newProject)));
            plan.setAutoExecution(isAutoExecution);
            plan = (Plan)PersistenceHelper.manager.save(plan);
        }

        return newProject;
    }
    public static void markAllActivitiesInPlanAsDeliverable(Plan plan, TrackingIntentType trackingIntentType) throws Exception {
        WTCollection plannables = PlannableHelper.service.getAllPlannables(plan, false);

        for (Object plannable : plannables) {
            PlanActivity activity = (PlanActivity) ((ObjectReference) plannable).getObject();
            activity.setTrackingIntent(trackingIntentType);
            activity.setHasDeliverable(true);
            activity = (PlanActivity) PersistenceHelper.manager.save(activity);
        }
    }

    /** For all the activities in the given plan, this method sets the available tracking intents one after another.  
     * @param plan
     * @throws Exception
     */
    public static void markActivitiesAsScheduledDeliverableWithVariousTrackingIntents(Plan plan) throws Exception {
        WTCollection plannables = PlannableHelper.service.getAllPlannables(plan, false);
        if(plannables.size()>0){
            TrackingIntentType[] TIArray = TrackingIntentType.getTrackingIntentTypeSet();
            int tiIndex = 0;
            for (Object plannable : plannables) {
                PlanActivity activity = (PlanActivity) ((ObjectReference) plannable).getObject();
                activity.setTrackingIntent(TIArray[tiIndex]);
                activity.setHasDeliverable(true);

                if(tiIndex==TIArray.length-1){
                    tiIndex=0;
                }else{
                    tiIndex++;
                }
            }
            PersistenceHelper.manager.save(plannables);
        }
    }

    public static void setDeadlineDetailsForAllActivitiesInPlan(Plan plan, int beforeDeadlineDays, int afterDeadlineDays, int deadLineOffset) throws Exception {
        WTCollection plannables = PlannableHelper.service.getAllPlannables(plan, false);

        for (Object plannable : plannables) {
            PlanActivity planActivity = (PlanActivity) ((ObjectReference) plannable).getObject();
            planActivity.setOwnership(Ownership.newOwnership(SessionHelper.getPrincipal()));
            planActivity.setBeforeDeadline(beforeDeadlineDays);
            planActivity.setDeadline(new Timestamp(new Date().getTime()+(ClassicToEPPUtils.oneDay*deadLineOffset)));        
            planActivity.setAfterDeadline(afterDeadlineDays);
            planActivity = (PlanActivity) PersistenceHelper.manager.save(planActivity);
        }
    }
    
    public static class FakeNmCommandBean extends NmCommandBean {

        private transient NmOid elementOid = null;
        public void setElementOid(NmOid oid) {

            elementOid=oid;
        }
        public NmOid getElementOid() throws WTException {

            return elementOid;
        }

    }

    /********************************************************************************************************************************************************/
    /**
     * Use this section to author APIs for JUnit test cases
     * 	 
     */

    /**
     * This API is used to create a project in a JUnit test case environment
     * 
     * @return Project2 
     * @throws WTException
     */
    public static Project2 makeProject() throws WTException {
        Project2 result = new Project2();
        assignOid(result);
        return result;
    }

    private static void assignOid(final Persistable p) throws WTException {
        ObjectIdentifier oid = ObjectIdentifier.newObjectIdentifier(p.getClass(), System.currentTimeMillis());
        p.setPersistInfo(PersistInfo.newPersistInfo(oid));
    }

    public static MspExportInfo initializeEditExportInfo() throws WTException {
        MspExportInfo info = new MspExportInfo();
        info.setLocale(wt.session.SessionHelper.getLocale());
        info.setEncoding(MspExportUtils.ENCODING);
        info.setDoNodes(true);
        info.setDoResourceAssignments(true);
        info.setDoAssignedResources(true);
        info.setDoUnassignedResources(true);
        return info;
    }

    public static Enumeration getScheduleQueueEntriesInQueue(String queueName) throws WTException {

        ScheduleQueue eppQueue = null;

        //getting the schedule queue
        boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try {
            // If an queue is not there, creating it.
            eppQueue = (ScheduleQueue)QueueHelper.manager.getQueue(queueName, ScheduleQueue.class);
            if(eppQueue == null)  {
                eppQueue = QueueHelper.manager.createScheduleQueue(queueName, true);
            }
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }

        //getting the queue entries
        Enumeration scheduleQueueEntries = PlannableUtils.getScheduleQueueEntries();
        if(scheduleQueueEntries == null || scheduleQueueEntries.hasMoreElements() == false){
            boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
            try {
                scheduleQueueEntries = QueueHelper.manager.queueEntries(eppQueue);
            } finally {
                SessionServerHelper.manager.setAccessEnforced(accessEnforced);
            }
            PlannableUtils.setScheduleQueueEntries(scheduleQueueEntries);
        }

        return scheduleQueueEntries;
    }

    public static HashMap<String, Boolean> checkIfScheduleEntriesExist(Enumeration scheduleQueueEntries, ArrayList<String> scheduleItemIds){
        HashMap<String, Boolean> scheduleItemsPresenceMap = new HashMap<String, Boolean>(); 
        //Checking against each entry if the entry with given id is present or not
        List<Persistable> scheduleEntries = Collections.list(scheduleQueueEntries);
        for(String scheduleItemEntryId : scheduleItemIds){
            boolean doEntryExists = false;
            for(Persistable obj : scheduleEntries){
                String entry = getScheduleItemEntryId(obj);
                if(scheduleItemsPresenceMap.containsKey(scheduleItemEntryId)){ break;} 
                if(entry != null && entry.equalsIgnoreCase(scheduleItemEntryId)){
                    doEntryExists = true;
                    break;
                }
            }
            scheduleItemsPresenceMap.put(scheduleItemEntryId, doEntryExists);
        }
        return scheduleItemsPresenceMap;
    }

    private static String getScheduleItemEntryId(Persistable obj){
        String scheduleItemEntryId = null;
        if(obj instanceof ScheduleQueueEntry){
            ScheduleQueueEntry scheduleEntry = (ScheduleQueueEntry) obj;
            Vector<MethodArgument> vectMethodArg = scheduleEntry.getArgs();
            //check against if any argument is indeed provided...
            if(vectMethodArg != null && vectMethodArg.size() > 0){
                int vectSize = vectMethodArg.size();
                //last MethodArgument will be the schduleEntryId which will identify the ScheduleQueueEntry..
                MethodArgument methodArg = vectMethodArg.get(vectSize - 1);
                Object objType = methodArg.getObject();
                if(objType instanceof String){
                    scheduleItemEntryId = (String)methodArg.getObject();
                }
            }
        }
        return scheduleItemEntryId;
    }

    public static void deleteScheduleQueueEntries(List<Persistable> scheduleQueueEntries) throws WTException {

        if(scheduleQueueEntries !=null){
            //Logged-in has the access to the Object(e.g. deliverable, activity etc.) however it may not have delete access to schedule entry. 
            //We need to delete the schedule entries on basis of access permissions to the object and NOT on the basis of access permissions to schedule entry.
            AccessControlServerHelper.disableNotAuthorizedAudit();
            boolean oldEnforce = SessionServerHelper.manager.setAccessEnforced (false);
            try{
                QueueHelper.manager.deleteEntries(scheduleQueueEntries);
            }finally{
                SessionServerHelper.manager.setAccessEnforced (oldEnforce);
                AccessControlServerHelper.reenableNotAuthorizedAudit();
            }		
        }
    }

    /**
     * @param plan
     * @param activity
     * @param expected_start_date
     * @param expected_finish_date
     * @param expected_plan_start
     * @param expected_plan_finish
     * @throws WTException 
     * @throws ObjectNoLongerExistsException 
     */
    public static void verifyDates(Plan plan, PlanActivity[] activity, Timestamp[] expected_start_date, Timestamp[] expected_finish_date, Timestamp expected_plan_start, Timestamp expected_plan_finish) throws ObjectNoLongerExistsException, WTException {
        plan = (Plan) PersistenceHelper.manager.refresh(plan);

        assertEquals("Plan estimated Start date mismatch", expected_plan_start,plan.getStartDate());
        assertEquals("Plan estimated Finish date mismatch", expected_plan_finish,plan.getFinishDate());

        int i=0;
        for(PlanActivity act: activity){
            act = (PlanActivity) PersistenceHelper.manager.refresh(act);
            assertEquals("estimated Start date mismatch, activity  :"+act.getName(), expected_start_date[i], act.getStartDate());
            assertEquals("estimated finish date mismatch, activity :"+act.getName(), expected_finish_date[i],act.getFinishDate() );
            i++;
        }
    }
    
    
    /**
     This API is invoked from test classes to set value of User Time zone
     */
    public TimeZone setUserTimeZone(TimeZone timeZone) throws Exception{
        LoginUtils.login(LoginUtils.getAdminNameAndPassword()[0],LoginUtils.getAdminNameAndPassword()[1]);
        originalUserTimeZone = (TimeZone) PreferenceHelper.service.getValue("/ProjectLink/localTimeZone", "WINDCHILL");
        PreferenceHelper.service.setValue( "/ProjectLink/localTimeZone", "WINDCHILL",timeZone , false , "");
        WTUser wcadminUser = TestOrgHelper.getUserByName(LoginUtils.getAdminNameAndPassword()[0], null);
        PreferenceHelper.service.setValue( "/ProjectLink/localTimeZone", "WINDCHILL",timeZone , wcadminUser , "");
        return originalUserTimeZone;
    }

    public static Timestamp getTimestamp(String date) throws ParseException, WTException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm aaa");
        formatter.setTimeZone(WTContext.getContext().getTimeZone());
        Timestamp timeStamp = new Timestamp(formatter.parse(date).getTime());
        return timeStamp;
    }

    public static Timestamp getTimestamp(String date , String format) throws ParseException, WTException {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        formatter.setTimeZone(WTContext.getContext().getTimeZone());
        Timestamp timeStamp = new Timestamp(formatter.parse(date).getTime());
        return timeStamp;
    }

    /**
     * @param container
     * @return
     * @throws Exception
     * @throws WTException
     * @throws ParseException
     */
    public static Plan getPlan(WTContainer container) throws Exception, WTException,
    ParseException {
        Plan plan = ProjectManagementTestUtil.createNewPlan(container);
        new StandardPlannableService().createDummyMilestone(plan);
        NmCommandBean clientData = new NmCommandBean();
        new EditPlanFormProcessor().editPlanStartDate(plan, "2013-05-13",clientData);
        return plan;
    }	  



    /**
     * @param project
     * @return
     * @throws Exception
     * @throws WTException
     * @throws ParseException
     */
    public static Plan getPlan(Project2 project,String planDate) throws Exception, WTException,
    ParseException {
        Plan plan = ProjectManagementTestUtil.createNewPlan(project);
        new StandardPlannableService().createDummyMilestone(plan);
        NmCommandBean clientData = new NmCommandBean();
        new EditPlanFormProcessor().editPlanStartDate(plan, planDate,clientData);
        return plan;
    }

    /**
     * @param projectName
     * @return
     * @throws Exception
     * @throws WTException
     */
    public static Project2 getProject(String projectName) throws Exception,
    WTException {
        OrgContainer orgContainer = ProjectManagementTestUtil.lookupOrgContainer("Demo Organization");

        ContainerTeam containerTeam = ProjectManagementTestUtil.getContainerTeam(orgContainer);

        Project2 project = ContainerTestHelper.findProject(projectName);

        if(project == null){
            log.trace("Creating new project, please wait...");
            project = ProjectManagementTestUtil.createProject(projectName, orgContainer, null, ContainerTeamReference.newContainerTeamReference(containerTeam), false);
            log.trace("Project has been created, creating plan now... ");			  
        }
        log.trace("getting the project");
        WTContainerRef contextRef = WTContainerRef.newWTContainerRef(project);
        contextRef.setObject(project);
        Plan plan = (Plan) PlanHelper.service.getPlan(contextRef);
        if(plan!=null){
            ProjectManagementTestUtil.dropAllPlannables(plan);
        }
        return project;
    }

    /**
     * Performs a lookup for the Organization named "Demo Organization" and for the Project with the input name. If the
     * lookup for the Project succeeded, the fetched Project is simply returned; otherwise, a Project with the input
     * name is created using a ProjectManagementTestUtil API, passing in the fetched Organization among other things.
     *
     * @param projectName
     *            Input name of the Project to be looked up or created.
     * @return The fetched or created Project.
     * @throws Exception if any of the invoked APIs threw an exception.
     * @throws WTException if any of the invoked APIs threw this exception.
     */
    public static Project2 findProject(String projectName) throws Exception, WTException {
        OrgContainer orgContainer = ProjectManagementTestUtil.lookupOrgContainer("Demo Organization");
        ContainerTeam containerTeam = ProjectManagementTestUtil.getContainerTeam(orgContainer);
        Project2 project = ContainerTestHelper.findProject(projectName);
        if (project == null) {
            project = ProjectManagementTestUtil.createProject(projectName, orgContainer, null,
                    ContainerTeamReference.newContainerTeamReference(containerTeam), false);
        }
        return project;
    }

    public static PlanActivity CreateNewActivity(Project2 project, Plan plan,int lineNumber, String activityName, double durationInDays, DateConstraint dateConstraintType,String predecessorListString) throws Exception{	  
        return CreateNewActivity(project, plan, lineNumber, 		  activityName, 		durationInDays,  	dateConstraintType, predecessorListString, null);
    }
    // 
    public static PlanActivity CreateNewActivity(WTContainer container, Plan plan,int lineNumber, String activityName, double durationInDays, DateConstraint dateConstraintType,String predecessorListString,Timestamp constraintDate) throws Exception{
        PlanActivity planActivity;
        planActivity = ProjectManagementTestUtil.createNewActivity(container, plan);
        planActivity.setLineNumber(lineNumber);		  
        planActivity.setName(activityName);
        planActivity.setContainer(container);

        Duration duration = Duration.newDuration();
        duration.setMillis(DurationUtils.toMillis(durationInDays*8, DurationFormat.HOURS));
        planActivity.setDuration(duration);
        planActivity.setConstraintType(dateConstraintType);

        if(DateConstraint.MFO.equals(dateConstraintType) || DateConstraint.SNET.equals(dateConstraintType)){
            planActivity.setConstraintDate(constraintDate);
            if(DateConstraint.MFO.equals(dateConstraintType)){
                planActivity.setFinishDate(constraintDate);
            }
        }

        planActivity = (PlanActivity) PersistenceHelper.manager.save(planActivity);

        if(DateConstraint.SNET.equals(dateConstraintType)){
            ProcessorUtils.createDummyPrecedenceConstraint(plan, planActivity);
        }

        log.trace("Activity "+activityName+" has been saved");
        addPredecessors(planActivity, predecessorListString, plan);
        return planActivity;
    }


    /**
     * @param planActivity
     * @param predecessorListString
     * @param plan
     * @throws WTPropertyVetoException
     * @throws WTException
     */
    public static void addPredecessors(PlanActivity planActivity,String predecessorListString, Plan plan) throws WTPropertyVetoException, WTException {
        if (predecessorListString!=null) {

            ArrayList constraints = new ArrayList();

            String[] predecessorsArray = predecessorListString.split(",");
            String predLineNumber = null;
            for (String predecessorString : predecessorsArray) {

                int offsetIndex = predecessorString.indexOf("+");
                if(offsetIndex==-1){
                    offsetIndex = predecessorString.indexOf("-");
                }
                String offsetString = null;
                if(offsetIndex!=-1){
                    offsetString = predecessorString.substring(offsetIndex);
                }

                int startIndex=0;
                PrecedenceType precedence = null;
                boolean isPredSet = false;
                // check for FS,SS,SF,FF in that order
                for(String predString:precConstantsMap.keySet()){
                    String lineNumber = null;
                    int lineEndIndex = predecessorString.indexOf(predString);
                    if(lineEndIndex!=-1){ //Found the precedence
                        precedence = precConstantsMap.get(predString);
                        lineNumber = predecessorString.substring(startIndex,lineEndIndex);
                        if(lineNumber!=null){
                            isPredSet = true;		
                            predLineNumber = lineNumber;
                        }
                        createPrecedenceConstraint(planActivity, plan, constraints,predLineNumber, offsetString, precedence);
                        break;
                    }
                }// End here
                if(!isPredSet){
                    precedence = precConstantsMap.get("FS");
                    createPrecedenceConstraint(planActivity, plan, constraints,predecessorString, offsetString, precedence);
                }
            }
            PlannableHelper.service.addPrecedenceConstraints(constraints);
        }
    }


    /**
     * @param planActivity
     * @param plan
     * @param constraints
     * @param predLineNumber
     * @param offsetString
     * @param precedence
     * @return
     * @throws WTPropertyVetoException
     */
    private static void createPrecedenceConstraint(
            PlanActivity planActivity, Plan plan, ArrayList constraints,
            String predLineNumber, String offsetString,
            PrecedenceType precedence) throws WTPropertyVetoException {
        int line = new Integer(predLineNumber);
        Plannable predecessor;
        try {
            predecessor = PlannableHelper.service.getPlannableByLineNumber(plan, line);
            PrecedenceConstraint precConstraint = PrecedenceConstraint.newPrecedenceConstraint(planActivity, predecessor);
            precConstraint.setPrecedenceType(precedence);
            if(offsetString!=null){
                Double offset = Double.parseDouble(offsetString);
                Duration scheduleOffset = ProcessorUtils.getScheduleOffset(offset, "DAYS");
                precConstraint.setScheduleOffset(scheduleOffset);
            }
            constraints.add(precConstraint);
        } catch (WTException wte) {
            System.out.println(wte);
        }
    }
    public static void refreshAndDelete(Persistable p) throws WTException {
        if (PersistenceHelper.isPersistent(p)) {
            p = PersistenceHelper.manager.refresh(p);
            PersistenceHelper.manager.delete(p);
        }
    }


    /**This API is used to create activities in given plan
     * @param plan
     * @param plannable states of each activity
     * @param container
     * @param planResource
     * @param totalEffort
     * */
    public static void createActivitiesInPlan(Plan plan, List<PlannableState> list, WTContainer container, Resourceable planResource, Duration totalEffort) throws Exception{
        PlanActivity planAct_FR = ProjectManagementTestUtil.createNewScheduledDeliverable(container,plan,plan.getName() + "_FRAct", TrackingIntentType.FIXED_REVISION,list.get(0),plan);
        AssignmentHelper.service.addResourceAssignment(planAct_FR, planResource, totalEffort, Duration.newDuration(), totalEffort, 100);

        PlanActivity planAct_FS = ProjectManagementTestUtil.createNewScheduledDeliverable(container,plan,plan.getName() + "_FSAct",TrackingIntentType.FIXED_SUBJECT,list.get(1),plan);
        AssignmentHelper.service.addResourceAssignment(planAct_FS, planResource, totalEffort, Duration.newDuration(), totalEffort, 100);

        PlanActivity planAct_LR = ProjectManagementTestUtil.createNewScheduledDeliverable(container,plan,plan.getName() + "_LRAct",TrackingIntentType.LATEST_REVISION,list.get(2),plan);
        AssignmentHelper.service.addResourceAssignment(planAct_LR, planResource, totalEffort, Duration.newDuration(), totalEffort, 100);

        Calendar c = Calendar.getInstance();
        c.setTime(new Date());;
        c.add(Calendar.DATE,3);
        Timestamp SNETDate = new Timestamp(c.getTime().getTime());

        PlanActivity plan_SNETActWithRes = ProjectManagementTestUtil.CreateNewActivity(container, plan, 4, plan.getName() + "_SNET", 8, DateConstraint.SNET, null,SNETDate);
        plan_SNETActWithRes.setPlannableState(list.get(3));
        AssignmentHelper.service.addResourceAssignment(plan_SNETActWithRes, planResource, totalEffort, Duration.newDuration(), totalEffort, 100);

        PlanActivity plan_ActWithoutRes= ProjectManagementTestUtil.CreateNewActivity(container, plan, 5, plan.getName() + "_ASAP", 8, DateConstraint.ASAP, null,null);
        plan_ActWithoutRes.setPlannableState(list.get(3));
    }

    /**
     * Use: This API will be used to create a subtype for given parent type with given set of properties    
     * 
     * @param parentTypeName
     * @param newTypeName
     * @param propertyNames
     * @return
     * @throws WTException
     */
    public static TypeDefinitionReadView createSubType(String parentTypeName, String newTypeName,
            String[] propertyNames) throws WTException {

        TypeDefinitionReadView parentView = TYPE_DEF_SERVICE.getTypeDefView(parentTypeName);

        ObjectIdentifier parentId = parentView.getOid();

        TypeDefinitionReadView childView = TYPE_DEF_SERVICE.getTypeDefView(newTypeName);

        if (childView != null) {
            if (!parentId.equals(childView.getParentId()))
                return childView;
        }

        Collection<PropertyValueReadView> inputProps = new ArrayList<PropertyValueReadView>();
        // set the property
        for(String propertyName : propertyNames){
            PropertyDefinitionReadView propReadView = BASE_DEF_SERVICE.getPropertyDefView(
                    LWCTypeDefinition.class.getName(), propertyName);

            PropertyDefinitionWriteView instantiablePropWriteView = propReadView.getWritableView();
            instantiablePropWriteView.setInheritable(false);
            String value = "AUTOMATIC";
            PropertyValueReadView instantiablePropVal = new PropertyValueWriteView(null, instantiablePropWriteView,
                    value, null, null, false, null, false);

            inputProps.add(instantiablePropVal);
        }
        childView = new TypeDefinitionReadView(newTypeName, false, // modeled
                WTContainerHelper.getExchangeRef(), parentId,
                inputProps, // properties
                null, // attributes
                null, // layouts
                false, // deleted
                false);// isSynchronized

        childView = TYPE_DEF_SERVICE.createTypeDef(childView.getWritableView());
        if (childView == null)
            throw new WTException("Type definition cannot be persisted for - " + newTypeName);

        return childView;
    }
    
    /**
     * Use: This APIis generic and will be used to create a subtype for given parent type.   
     * 
     * @param parentTypeName
     * @param newTypeName
     * @return
     * @throws WTException
     */
    public static TypeDefinitionReadView createSubType(String parentTypeName, String newTypeName) throws WTException {

        TypeDefinitionReadView parentView = TYPE_DEF_SERVICE.getTypeDefView(parentTypeName);

        ObjectIdentifier parentId = parentView.getOid();

        TypeDefinitionReadView childView = TYPE_DEF_SERVICE.getTypeDefView(newTypeName);

        if (childView != null) {
            if (!parentId.equals(childView.getParentId()))
                return childView;
        }

        childView = new TypeDefinitionReadView(newTypeName, false, // modeled
                WTContainerHelper.getExchangeRef(), parentId,
                null, // properties
                null, // attributes
                null, // layouts
                false, // deleted
                false);// isSynchronized

        childView = TYPE_DEF_SERVICE.createTypeDef(childView.getWritableView());
        if (childView == null)
            throw new WTException("Type definition cannot be persisted for - " + newTypeName);

        return childView;
    }
    
    /***
     * Use: To create scheduled deliverables with a parent specified as a parameter of the type given by the param typeDefinitionReference, having particular line number and tracking intent type
     * @param plan
     * @param name
     * @param trackingIntentType
     * @param parent
     * @param typeDefinitionReference
     * @param contextRef
     * @param lineNumber
     * @param isCritical
     * @param isSummary
     * @return PlanActivity
     * @throws WTException
     * @throws WTPropertyVetoException
     */
    public static PlanActivity createScheduledDeliverableForType(Plan plan, String name,
            TrackingIntentType trackingIntentType, Plannable parent, TypeDefinitionReference typeDefinitionReference,
            WTContainerRef contextRef, int lineNumber, boolean isCritical, boolean isSummary) throws WTException, WTPropertyVetoException {
        PlanActivity activity = PlanActivity.newPlanActivity(contextRef);
        activity.setTypeDefinitionReference(typeDefinitionReference);
        activity.setName(name);
        activity.setTrackingIntent(trackingIntentType);
        activity.setHasDeliverable(true);
        activity.setLineNumber(lineNumber);
        activity.setCritical(isCritical);
        activity.setSummary(isSummary);
        activity = (PlanActivity)PlannableHelper.service.addPlannable(activity, plan);
        activity = (PlanActivity)PersistenceHelper.manager.refresh(activity);
        activity.setParent(parent);
        activity = (PlanActivity)PersistenceHelper.manager.save(activity);
        return activity;
    }
    //To Do : We need to assess the need of attribute ID in creating this type instance . In future, type instance will create with type identifier. 
    public static TypeInstance createTypeInstance(String attributeID) throws WTException {
        TypeInstance ti = null;
        ti = createTypeInstance(attributeID,null);
        return ti;
    }
    public static TypeInstance createTypeInstance(String attributeID,String typeInternalName) throws WTException {
        String str = PlanActivity.class.getName();
        StringBuffer typeFullName = new StringBuffer(str);
        if(typeInternalName!=null){
            typeFullName.append("|"+typeInternalName);
        }
        TypeIdentifier tid = new WCTypeIdentifier(typeFullName.toString());   
        AttributeTypeIdentifier ati = AttributeHelper.getATI(attributeID, tid, false);
        TypeInstance ti = TypedUtility.prepare(tid, new AttributeTypeIdentifier[] { ati }, new CreateOperationIdentifier(),SessionHelper.getLocale(), true, true);
        return ti;
    }

    /**
     * Use: This API will be convert days into millisec    
     * 
     * @param long - days
     * @return long 
     */
    private static Long dayToMiliseconds(long days){
        Long result = Long.valueOf(days * 24 * 60 * 60 * 1000);
        return result;
    }

    /**
     * Use: This API will be addDays into timestamp    
     * 
     * @param long - days
     * @param Timestamp
     * @return Timestamp 
     */
    public static Timestamp addDays(long days, Timestamp t1) throws Exception{
        if(days < 0){
            throw new Exception("Day in wrong format.");
        }
        Long miliseconds = dayToMiliseconds(days);
        return new Timestamp(t1.getTime() + miliseconds);
    }

    /**
     * Use: This API will be compare two Timestamp for Date, Month & Year only    
     * 
     * @param Timestamp
     * @param Timestamp
     * @return boolean 
     */
    public static boolean compare(Timestamp t1 , Timestamp t2){

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


    /*
     * Create numOfChidren passed as the child objects of parentPlannable.
     * It populates static childParentAssociationMap which in turn contains two HashMap which can be retrieved as described below:
     *    1. HashMap CHILD_PARENT_MAP :  Key(String Constant): "CHILD_PARENT_MAP"; Value(HashMap) =  WTKeyedHashMap
     *                          WTKeyedHashMap ==> Key:Child Object; Value: Parent Object
     *                          
     *    2. HashMap PARENT_CHILD_MAP: Key(String Constant): "PARENT_CHILD_MAP" Value(HashMap)= WTKeyedHashMap
     *                           WTKeyedHashMap ==> Key:Parent Object; Value= Collection of Child Objects. 
     *  
     */
    public static HashMap createPlanSubStructure(int numOfChildren, Plannable parentPlannable, boolean appendMode) throws WTException {
        log.debug("createPlanSubStructure() : IN ");
        final String Plan_As_Parent_Suffix = "_Parent_Plan";
        final String Summary_As_Parent_Suffix = "_Parent_Summary";
        String childActivityName ="ChildActivity";

        //Holder: KEY --> Child ; VALUE --> PARENT
        WTKeyedHashMap childParentMap = new WTKeyedHashMap();
        //Holder: KEY --> PARENT ; VALUE --> COLLECTION OF CHILD OBJECTS
        WTKeyedHashMap parentChildMap = new WTKeyedHashMap();

        try {
            //Make parentPlannble a Summary...
            parentPlannable.setSummary(true);

            if(parentPlannable instanceof Plan){
                childActivityName += Plan_As_Parent_Suffix;
            }else{
                childActivityName += Summary_As_Parent_Suffix;
            }

            log.debug("parentPlannable : " + parentPlannable + "\t appendMode = " + appendMode);
            log.debug("childActivityName() : " + childActivityName);

            WTCollection collectionToSave = new WTArrayList();
            for(int count = 1; count <= numOfChildren; count++){
                Plannable childPlannable = PlanActivity.newPlanActivity(parentPlannable.getContainerReference());
                childPlannable.setParent(parentPlannable);
                childPlannable.setRoot(parentPlannable instanceof Rootable ? (Rootable)parentPlannable : parentPlannable.getRoot());
                childPlannable.setContainerReference(parentPlannable.getContainerReference());
                childPlannable.setLineNumber(count);
                if (childPlannable instanceof AbstractPlanActivity){
                    ((AbstractPlanActivity)childPlannable).setDomainRef(AdminUtils.getAdministrativeSystemDomainRef(parentPlannable.getContainerReference()));
                }
                childPlannable.setName(childActivityName + "_" + parentPlannable.getPersistInfo().getObjectIdentifier());
                collectionToSave.add(childPlannable);
                log.debug("collectionToSave : " + collectionToSave);
            }

            //Save the modification to the parent. We have marked the parentPlannable as the summary activity here instead of doing it at the caller level...
            collectionToSave.add(parentPlannable);

            PersistenceHelper.manager.save(collectionToSave);
            CollectionsHelper.manager.refresh(collectionToSave);

            collectionToSave.remove(parentPlannable);

            if(parentPlannable instanceof Rootable){
                //Get all Children of Parent. In some cased, plan might have OOTB milestones created when Plan gets created. Hence, retrieve those and populate the correct child list for the plan object.
                collectionToSave = PlannableHelper.service.getImmediateChildren(parentPlannable, true);
                log.debug(" The Total number of Children for plan : " + collectionToSave.size() + "\t Child List = " + collectionToSave);
            }


            java.util.Iterator childIterator = collectionToSave.persistableIterator();
            Plannable childPlannable = null;
            while(childIterator.hasNext()){
                childPlannable = (Plannable)childIterator.next();
                childParentMap.put(childPlannable, parentPlannable);
                log.debug("childPlannable : " + childPlannable + "\t parentPlannable = " + parentPlannable);
            }
            //Store those in collection, to use it further...
            parentChildMap.put(parentPlannable, collectionToSave);
            log.debug("parentPlannable : " + parentPlannable + "\t collectionToSave = " + collectionToSave + "\t collectionToSave.size= " + collectionToSave.size());

            if(appendMode){
                //Add new entries to existing maps.
                WTKeyedHashMap oldParentChildMap = (WTKeyedHashMap)childParentAssociationMap.get(PARENT_CHILD_MAP);
                WTKeyedHashMap oldChildParentMap = (WTKeyedHashMap)childParentAssociationMap.get(CHILD_PARENT_MAP);
                if(oldParentChildMap != null){
                    log.debug("oldParentChildMap : " + oldParentChildMap + "\t size = " + oldParentChildMap.size());
                    parentChildMap.putAll(oldParentChildMap);
                }
                if(oldChildParentMap != null){
                    log.debug("oldChildParentMap : " + oldChildParentMap + "\t size = " + oldChildParentMap.size());
                    childParentMap.putAll(oldChildParentMap);
                }
            }
            childParentAssociationMap.put(PARENT_CHILD_MAP, parentChildMap);
            childParentAssociationMap.put(CHILD_PARENT_MAP, childParentMap);
            log.debug("parentChildMap : " + parentChildMap + "\t size = " + parentChildMap.size());
            log.debug("childParentMap : " + childParentMap + "\t size = " + childParentMap.size());

        }catch (WTPropertyVetoException e) {
            e.printStackTrace();
            throw new WTException (e);
        }
        log.debug("createPlanSubStructure() : OUT ");
        return childParentAssociationMap;
    }

    /*
     * Returns childParentAssociationMap which in turn contains two HashMaps as described below:
     *   1. HashMap CHILD_PARENT_MAP :  Key(String Constant): "CHILD_PARENT_MAP"; Value(HashMap) =  WTKeyedHashMap
     *                          WTKeyedHashMap ==> Key:Child Object; Value: Parent Object
     *                          
     *    2. HashMap PARENT_CHILD_MAP: Key(String Constant): "PARENT_CHILD_MAP" Value(HashMap)= WTKeyedHashMap
     *                           WTKeyedHashMap ==> Key:Parent Object; Value= Collection of Child Objects. 
     */
    public static HashMap getChildParentAssociationMap(){
        log.debug("getChildParentAssociationMap() : In & Out ");
        return childParentAssociationMap;
    }

    public static void compareDateAttributesFromMSP(Timestamp[] expected_start_date , Timestamp[] expected_finish_date , 
            String planName,Plan plan, PlanActivity[] activity ) throws Exception, IOException {
        HashMap hmAttributes = ProjectManagementTestUtil.getDateAttributeFromMSP(planName + ".mpp");

        ProjectManagementTestUtil.setExpectedDates(expected_start_date,expected_finish_date, hmAttributes);

        Timestamp expected_plan_start = (Timestamp) hmAttributes.get("PlanStartDate");
        Timestamp expected_plan_finish = (Timestamp) hmAttributes.get("PlanFinishDate");
        verifyDates(plan, activity, expected_start_date, expected_finish_date, expected_plan_start, expected_plan_finish);
    }

    public static File getFileFromSystem (String resource) throws Exception, IOException {
        return getFileFromSystem (resource, false);
    }

    public static File getFileFromSystem (String resource, boolean defaultPath) throws Exception, IOException {
        if(defaultPath){
            resource = "com/ptc/projectmanagement/msproject/datafiles/"+ resource;
        }

        URL url = StandardMspServiceIntegrationTest.class.getClassLoader().getResource(resource);
        assertNotNull("file resource not found", url);

        File newFile = new File(new URI(url.toString()));
        return newFile;
    }

    public static Plan getPlanObjectFromMSP(String mppFile , WTContainer container) throws IOException, Exception   {
        Plan plan;
        MspImportInfo replaceMode_Mpp_ImportInfo = new MspImportInfo();
        replaceMode_Mpp_ImportInfo.setImportMode(MspImportMode.toMspImportMode("REPLACE"));
        replaceMode_Mpp_ImportInfo.setImportStartDate(true);
        File xmlFile = getFileFromSystem(mppFile,true);

        plan = getPlan(container);

        ObjectReference planRef = ObjectReference.newObjectReference((Persistable) plan);
        plan = (Plan) MspHelper.service.importPlan(planRef, xmlFile, replaceMode_Mpp_ImportInfo);

        return plan;
    }

    public static HashMap getDateAttributeFromMSP(String mppFile) throws IOException, Exception {

        HashMap hmAttributes = new HashMap();
        uniqueHelper = new UniquenessHelper();
        String projectName = uniqueHelper.qualifyName("pjlDummyProject_02");
        Project2 project = getProject(projectName);
        Plan plan = getPlanObjectFromMSP(mppFile,project);


        hmAttributes.put("PlanStartDate", plan.getStartDate());
        hmAttributes.put("PlanFinishDate", plan.getFinishDate());

        WTCollection c = PlannableHelper.service.getAllPlannables(plan, 1);

        Iterator it = c.persistableIterator(Plannable.class, true);
        while (it != null && it.hasNext()) {
            Plannable task = (Plannable) it.next();

            if(task.getName() != null ){
                hmAttributes.put(task.getName() + "_StartDate", task.getStartDate());
                hmAttributes.put(task.getName() + "_FinishDate", task.getFinishDate());
            }

        }

        deleteProjectAndPlan(plan , project);
        return hmAttributes;
    }



    public static void deleteProjectAndPlan(Plan plan,Project2 project) throws WTException{
        WTArrayList allPersistableObjects = new WTArrayList();
        allPersistableObjects.add(plan);allPersistableObjects.add(project);

        CollectionsHelper.manager.refresh(allPersistableObjects);
        plan = PlanHelper.service.deletePlannableContents(plan);
        CollectionsHelper.manager.refresh(allPersistableObjects);
        PersistenceHelper.manager.delete(new WTHashSet(allPersistableObjects));
    }

    public static void setExpectedDates(Timestamp[] expected_start_date,Timestamp[] expected_finish_date, HashMap hmAttributes){

        for (int i=0 ; i < expected_start_date.length ; i++){
            expected_start_date[i] = (Timestamp) hmAttributes.get((i+1)+"_StartDate");
            expected_finish_date[i] = (Timestamp) hmAttributes.get((i+1)+"_FinishDate");

        }
    }

    public static void exportPlan(Plan plan) throws WTException, IOException{
        File exportFile = null;
        WTProperties props = WTProperties.getLocalProperties();
        String WT_HOME = props.getProperty("wt.home", "");
        exportFile = MspHelper.service.exportPlannable(ObjectReference.newObjectReference(plan), ProjectManagementTestUtil.initializeEditExportInfo());
        exportFile.renameTo(new File(WT_HOME+"/tmp/"+plan.getName()+".pppx"));
    }

    public static ArrayList<String> compareAttributes(List<String> attributeNames, Object object1, Object object2) throws WTException {

        Object result1 = null;
        Object result2 = null;
        ArrayList<String> alFailedCompareList = new ArrayList<String>();
        try {
            Method readerObject1 = null;
            Method readerObject2 = null;
            for (String attributeName : attributeNames) {
                PropertyDescriptor objPD1 = getWTPropertyDescriptor(attributeName, object1.getClass());
                if (objPD1 != null) {
                    readerObject1 = objPD1.getReadMethod();
                }
                if (readerObject1 == null) {
                    throw new WTException("Get method for {" + objPD1.getName() + "} attribute not found in "  + object1.getClass() + " class.");
                }

                result1 = readerObject1.invoke(object1, (Object[]) null);
                result1 = result1 instanceof Duration ? ((Duration)result1).getDuration(DurationFormat.HOURS) : result1;
                result1 = result1 instanceof SecurityLabels ? ((SecurityLabels)result1).INTERNAL_VALUE : result1;
                result1 = result1 instanceof Ownership ? ((Ownership)result1).OWNER : result1;
                if (result1 != null) {
                    PropertyDescriptor objPD2 = getWTPropertyDescriptor(attributeName,
                            object2.getClass());
                    if (objPD2 != null) {
                        readerObject2 = objPD2.getReadMethod();
                    }
                    if (readerObject2 == null) {
                        throw new WTException("Get method for {" + objPD2.getName()+ "} attribute not found in " + object2.getClass() + " class");
                    }
                    result2 = readerObject2.invoke(object2, (Object[]) null);
                    result2 = result2 instanceof Duration ? ((Duration)result2).getDuration(DurationFormat.HOURS) : result2;
                    result2 = result2 instanceof SecurityLabels ? ((SecurityLabels)result2).INTERNAL_VALUE : result2;
                    result2 = result2 instanceof Ownership ? ((Ownership)result2).OWNER : result2;
                    if(!result1.equals(result2)){
                        log.debug(objPD2.getName() +"::"+ result1+ "--"+result2);
                        alFailedCompareList.add(objPD2.getName());

                    }
                }

            }


        } catch (Exception e) {
            throw new WTException(e);
        }

        return alFailedCompareList;
    }

    private static PropertyDescriptor getWTPropertyDescriptor(String propertyName, Class<? extends Object> className)
            throws WTException {
        PropertyDescriptor result = null;
        try {
            ClassInfo info = WTIntrospector.getClassInfo(className);
            result = info.getPropertyDescriptor(propertyName);
        } catch (WTIntrospectionException ie) {
            throw new WTException(ie.getLocalizedMessage(SessionHelper.getLocale()));
        }
        return result;
    }

    /**
     * Use: This API will create and return new WTgroup of three WTusers and add created WTgroup to the team of the project.
     * @param Project2
     * 
     * @return  List<WTPrincipal>
     * @throws Exception
     */
    public static  List<WTPrincipal> createGroupAndAddGroupToTeamOfProject(Project2 project) throws Exception {
        // Create Group which will be used as teamMembers as well as resources.
        ArrayList<String> uniqueGroupNames = generateUniqueNames("pjlTestGroup", 1);
        ArrayList<WTPrincipal> groupList =  createNewGroups(uniqueGroupNames,3);

        // Add the newly created group to 'Member' role of the project team.
        // In this way the group will have access to the project and respective resources will be created automatically.
        ContainerTeam team = ContainerTeamHelper.service.getContainerTeam(project, false);
        Role role = Role.toRole("Members");
        ContainerTeamHelper.service.addMembers(team, role, groupList);
        return groupList;
    }

    public static NmCommandBean setClipBoardAndSelectedItem(NmOid oid,ArrayList a,boolean cutFlag, NmOid targetPlanOid, NmCommandBean clientData) throws WTException, WTPropertyVetoException{
        ArrayList<NmClipboardItem> clipBoardItems = new ArrayList<NmClipboardItem>();
        NmClipboardItem clipBoardItem = new NmClipboardItem();
        NmClipboardBean clipBean =  null;

        if(clientData.getClipboardBean() == null){
            clipBean =  new NmClipboardBean();
            clientData.setClipboardBean(clipBean);            
        }
        clipBean = clientData.getClipboardBean();
        clipBoardItem.setObject(oid);
        clipBoardItems.add(clipBoardItem);
        clipBean.add(clipBoardItems);        
        clipBean.setClippedToCut(cutFlag);
        clientData.setSelected(a);
        clientData.setPageOid(targetPlanOid);
        return clientData;
    }

    public static NmCommandBean setClipBoardAndSelectedItem(ArrayList <NmOid> acts, ArrayList selectedAct, boolean cutFlag, NmOid targetPlanOid) throws WTException, WTPropertyVetoException{
        NmCommandBean clientData = new NmCommandBean();
        NmClipboardBean clipboardBean = new NmClipboardBean();       
        List<NmClipboardItem> clipboardItems = NmClipboardUtility.convert(acts,cutFlag);
        clipboardBean.add(clipboardItems);        
        clientData.setClipboardBean(clipboardBean);
        clientData.setSelected(selectedAct);
        clientData.setPageOid(targetPlanOid);
        return clientData;
    }

    public static ArrayList setSelectedActivity(PlanActivity a) throws WTException{
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

    /**
     *  Create a parameter map with predefined data.
     *  Used for setting the request parameters of the command bean.
     * @return
     */
    public static HashMap<String, Object> getParameterMap() {
        HashMap<String, Object> parameterMap = new HashMap<String, Object>();

        parameterMap.put("duration_unit", "DAYS");
        parameterMap.put("duration_number", "2");
        parameterMap.put("effort_type", "Hour");
        parameterMap.put("effort", "8");
        parameterMap.put("is_Row_Updated", "true");
        parameterMap.put("activityOwnerFinalValue", "demo");
        parameterMap.put(CONSTRAINT_DATE, "");
        parameterMap.put("activity_name", "activity 1");
        return parameterMap;
    }

    
    /**
     * get command bean
     * @param WTContainer
     * @return
     * @throws Exception
     */
    public static NmCommandBean getDeliverabelStepDataCommandBean(WTContainer container) throws Exception {
        NmCommandBean cb = new NmCommandBean();

        HashMap<String, Object> parameterMap = getParameterMap();

        HTTPRequestData a_RequestData = new HTTPRequestData();
        HashMap<String, Object> requestParameterMap = new HashMap<String, Object>();
        requestParameterMap.put("isMultiCreate", "false");
        a_RequestData.setParameterMap(requestParameterMap);
        cb.setRequestData(a_RequestData);
        cb.setRequestOverrideParameters(parameterMap);
        if (container != null) {
            cb.setPrimaryOid(new NmOid(container));
            cb.getPrimaryOid().setRef(container);
        }
        cb.getText().put("activity_name", "activity 1");
        return cb;
    }

    /**
     * get object bean
     * @return
     * @throws Exception
     */
    public static ObjectBean getDeliverabelStepDataObjectBean() throws Exception {

        NmCommandBean commonCb1 = new NmCommandBean();
        ArrayList ttlist = new ArrayList();
        ttlist.add("Fixed Work");
        commonCb1.getComboBox().put("task_type", ttlist);
        ArrayList<String> hourList = new ArrayList<String>();
        hourList.add("8 AM");
        ArrayList<String> minList = new ArrayList<String>();
        minList.add("00");
        HashMap<String, Object> comboBox = commonCb1.getComboBox();
        comboBox.put(CONSTRAINT_DATE_HOURS, hourList);
        comboBox.put(CONSTRAINT_DATE_MINUTES, minList);
        commonCb1.setComboBox(comboBox);
        HashMap<String, Object> parameterMap = getParameterMap();
        ObjectBean objectBean = new ObjectBean();

        objectBean = ObjectBean.newInstance(new NmCommandBean(), commonCb1,
                parameterMap, "1");

        return objectBean;
    }
    
    
    /*
     * Use CreatePROPLProjectFormProcessor to create new projects
     */
    public static Project2 createNewProjectThroughFP(String projectName, final WTContainer parent) throws Exception{

        Project2 project = lookupProject(projectName);
        if (project != null) {
            System.out.println("createNewProjectThroughFP: Project with name: "+projectName+" already exists!");
            return project;
        }
        
        NmCommandBean clientData = prepareCommandBean(projectName,parent);
        List<ObjectBean> objList = getObjectBean(clientData,projectName,parent);
        
        List<ObjectFormProcessorDelegate> delegates = new ArrayList<ObjectFormProcessorDelegate>();
        delegates.add(new AutoUpdateProjectFormDelegate());
        delegates.add(new AutoUpdateUserFormDelegate());
        delegates.add(new NamePropertyProcessor());
        
        CreatePROPLProjectFormProcessor projectFP = new CreatePROPLProjectFormProcessor();
        projectFP.setDelegates(delegates);
        projectFP.preProcess(clientData, objList);
        projectFP.doOperation(clientData, objList);
        projectFP.postProcess(clientData, objList);
        
        project = lookupProject(projectName);
        if (project == null) {
            throw new Exception("createNewProjectThroughFP: Project with name: "+projectName+" is not created");
        }
        return project;
    }

    private static List<ObjectBean> getObjectBean(NmCommandBean nmCommandBean, String containerName,WTContainer parent) throws WTException, WTPropertyVetoException{
        List<ObjectBean> objectBeanList = new ArrayList<ObjectBean>();
        HashMap<String, Object> parameterMap = new HashMap<String, Object>();
        
        ObjectBean bean = ObjectBean.newInstance(nmCommandBean,nmCommandBean,parameterMap,"");
        Project2 project = Project2.newProject2();
        project.setName(containerName);
        project.setPseudoType(1);
        project.setContainer(parent);
        
        project.setContainerReference(WTContainerRef.newWTContainerRef(parent));
        project.setDescription("Created by createNewProjectThroughFP");
        project.getContainerInfo().setName(project,containerName);
        
        bean.setObject(project);
        bean.setDelegateClassNames(new ArrayList<>());
        bean.setObjectHandle(null);
        List<String> a_DelegateClassNames = new ArrayList<String>();
        a_DelegateClassNames.add("com.ptc.netmarkets.project.processor.AutoUpdateProjectFormDelegate");
        a_DelegateClassNames.add("com.ptc.netmarkets.project.processor.AutoUpdateUserFormDelegate");
        a_DelegateClassNames.add("com.ptc.core.components.forms.NamePropertyProcessor");
        a_DelegateClassNames.add("com.ptc.windchill.mpml.forms.CategoryFormProcessorDelegate");
        bean.setDelegateClassNames(a_DelegateClassNames);
        bean.setProcessorClassName("com.ptc.netmarkets.project.processor.CreatePROPLProjectFormProcessor");
       
        objectBeanList.add(bean);
        return objectBeanList;
    }
    
    private static NmCommandBean prepareCommandBean(String containerName,WTContainer parent)throws WTException, WTPropertyVetoException{
        
        NmCommandBean nmCommandBean = new NmCommandBean();
        HashMap textMap = new HashMap();
        textMap.put("pseudoType", 1);
        textMap.put("name_col_name",containerName);
        textMap.put("name",containerName);
        textMap.put("containerInfo.ownerRef_col_containerInfo.ownerRef","");
        
        HashMap comboMap = new HashMap();
        ArrayList listAccessgroup = new ArrayList();
        listAccessgroup.add("PRIVATE_PROJECT");
        comboMap.put("ACCESSGROUPS",listAccessgroup);
      
        WTContainerRef exchangeRef = WTContainerHelper.getExchangeRef();
        WTContainerTemplate projectTemplate = ContainerTemplateHelper.service.getContainerTemplate(exchangeRef, "General", Project2.class);
        WTContainerTemplateRef templateRef =  WTContainerTemplateRef.newWTContainerTemplateRef(projectTemplate);
        ArrayList refList = new ArrayList();
        refList.add(templateRef.toString());
        comboMap.put("templateRef",refList);
        
        HashMap radio = new HashMap();
        radio.put("PROJECT_EXECUTION_CONTROL", true);
        
        HashMap checked = new HashMap();
        checked.put("startProjectCheckBox", true);
        
        nmCommandBean.setText(textMap);
        nmCommandBean.setComboBox(comboMap);
        nmCommandBean.setRadio(radio);
        nmCommandBean.setChecked(checked);

        HTTPRequestData reqData = new HTTPRequestData();
        HashMap map = new HashMap();
        String[] typeVal = new String[1];
        TypeInstanceIdentifier tiid=TypeIdentifierUtility.getTypeInstanceIdentifier(Project2.newProject2());
        typeVal[0]=tiid.toString();
        map.put("itemTypeInstanceId", typeVal);
        String[] operationVal = new String[1];
        operationVal[0]="CREATE";
        map.put("operation",operationVal);
        
        String[] containerRefVal = new String[1];
        containerRefVal[0]=WTContainerRef.newWTContainerRef(parent).toString();
        map.put("containerRef",containerRefVal);
        
        reqData.setParameterMap(map);
        nmCommandBean.setRequestData(reqData);
        nmCommandBean.setActionClass("com.ptc.netmarkets.project.processor.CreatePROPLProjectFormProcessor");
        nmCommandBean.setActionMethod("execute");
        
        
        return nmCommandBean;
    }
    
    /**
     * Update non-final private static fields of a class through reflection
     * 
     * @param clazz - class name
     * @param fieldName
     * @param fieldValue
     * @throws Exception
     */
    public static void updatePrivateStaticVariables(Class clazz, String fieldName, Object fieldValue) throws Exception{
        // Get the field instance
        Field field= clazz.getDeclaredField(fieldName);
        field.setAccessible(true);//set Accessible since it is private field
        field.set(null, fieldValue);
    }

}
