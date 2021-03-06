package com.jdon.controller;

import com.jdon.aop.interceptor.InterceptorsChain;
import com.jdon.bussinessproxy.TargetMetaDef;
import com.jdon.bussinessproxy.meta.MethodMetaArgs;
import com.jdon.container.ContainerWrapper;
import com.jdon.container.builder.ContainerRegistryBuilder;
import com.jdon.container.finder.ContainerFinderImp;
import com.jdon.container.startup.ContainerSetupScript;
import com.jdon.controller.context.application.Application;
import com.jdon.controller.context.application.DemoSessionWrapper;
import com.jdon.controller.context.application.MockRequest;
import com.jdon.controller.service.Service;
import com.jdon.controller.service.ServiceFacade;
import com.jdon.model.handler.HandlerMethodMetaArgsFactory;
import com.jdon.util.Debug;

public class AppUtil extends Application {
	private final static String module = WebAppUtil.class.getName();

	private final ContainerSetupScript css = new ContainerSetupScript();
	private final static ContainerFinderImp scf = new ContainerFinderImp();

	public AppUtil(String fileName) {
		css.prepare(fileName, this);
	}

	public AppUtil() {
		css.prepare("", this);
	}

	public MockRequest createDemoRequest() {
		DemoSessionWrapper ds = new DemoSessionWrapper();
		return new MockRequest(this, ds);
	}

	public Object getService(String name) {
		ServiceFacade serviceFacade = new ServiceFacade();
		return serviceFacade.getServiceFactory(this).getService(name, createDemoRequest());
	}

	public Object getService(TargetMetaDef targetMetaDef) {
		ServiceFacade serviceFacade = new ServiceFacade();
		return serviceFacade.getServiceFactory(this).getService(targetMetaDef, createDemoRequest());
	}

	public Object getComponentInstance(String name) {
		ContainerWrapper containerWrapper = scf.findContainer(this);
		return containerWrapper.lookup(name);
	}

	public Object getEJBService(String name) throws Exception {
		return getService(name);
	}

	public Object callService(String serviceName, String methodName, Object[] methodParams) throws Exception {
		Debug.logVerbose("[JdonFramework] call the method: " + methodName + " for the service: " + serviceName, module);
		Object result = null;
		try {
			HandlerMethodMetaArgsFactory maFactory = new HandlerMethodMetaArgsFactory();
			MethodMetaArgs methodMetaArgs = maFactory.createDirectMethod(methodName, methodParams);

			ServiceFacade serviceFacade = new ServiceFacade();
			Service service = serviceFacade.getService(this);
			result = service.execute(serviceName, methodMetaArgs, createDemoRequest());
		} catch (Exception ex) {
			Debug.logError("[JdonFramework] serviceAction Error: " + ex, module);
			throw new Exception(" serviceAction Error:" + ex);
		}
		return result;
	}

	public String getContainerKey() {
		return ContainerRegistryBuilder.APPLICATION_CONTEXT_ATTRIBUTE_NAME;
	}

	public String getInterceptorKey() {
		return InterceptorsChain.NAME;
	}

	public ContainerWrapper getContainer() throws Exception {
		ContainerFinderImp scf = new ContainerFinderImp();
		return scf.findContainer(this);
	}

}
