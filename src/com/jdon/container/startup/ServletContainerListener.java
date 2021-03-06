/**
 * Copyright 2003-2006 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jdon.container.startup;

import java.io.Serializable;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionEvent;

import com.jdon.container.access.xml.AppConfigureCollection;
import com.jdon.controller.context.web.ServletContextWrapper;
import com.jdon.util.Debug;
import com.jdon.util.StringUtil;
import com.jdon.util.UtilValidate;

/**
 * Bootstrap listener to start up Jdon Framework it delegates to
 * ContainerSetupScript
 * 
 * setup web.xml <listener>
 * <listener-class>com.jdon.container.startup.ServletContainerListener
 * </listener-class> </listener>
 * 
 * @author banq
 */
public class ServletContainerListener implements ServletContextListener, Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = -3277156967613387119L;

	public final static String module = ServletContainerListener.class.getName();

	private final ContainerSetupScript css = new ContainerSetupScript();

	public void contextInitialized(ServletContextEvent event) {
		ServletContext scontext = event.getServletContext();
		ServletContextWrapper context = new ServletContextWrapper(scontext);
		css.initialized(context);
		Debug.logVerbose("[JdonFramework]contextInitialized", module);

		String app_configFile = context.getInitParameter(AppConfigureCollection.CONFIG_NAME);
		if (UtilValidate.isEmpty(app_configFile)){			
			Debug.logError("[JdonFramework] not locate a configuration in web.xml :" , module);
			Debug.logError("[JdonFramework] in web.xml There should have: <context-param>	<param-name>modelmapping-config</param-name><param-value>jdonframework.xml</param-value></context-param>" , module);
			return;
		}
		String[] configs = StringUtil.split(app_configFile, ",");
		for (int i = 0; i < configs.length; i++) {
			Debug.logVerbose("[JdonFramework] locate a configuration in web.xml :" + configs[i], module);
			css.prepare(configs[i], context);
		}
		Debug.logVerbose("[JdonFramework]ServletContainerListener is preparing...", module);

	}

	public void contextDestroyed(ServletContextEvent event) {
		ServletContext scontext = event.getServletContext();
		ServletContextWrapper context = new ServletContextWrapper(scontext);
		css.destroyed(context);

	}

	public void sessionCreated(HttpSessionEvent event) {

	}

	public void sessionDestroyed(HttpSessionEvent event) {
	}

}
