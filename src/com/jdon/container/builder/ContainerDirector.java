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

package com.jdon.container.builder;

import com.jdon.container.ContainerWrapper;
import com.jdon.util.Debug;

/**
 * container director register order: 1. register all components in
 * container.xml 2. register all components in aspect.xml 3. startup above all
 * components 4. register user services in jdonframework.xml
 * 
 * 
 * @author banq
 */
public class ContainerDirector {
	public final static String module = ContainerDirector.class.getName();

	private final ContainerRegistryBuilder cb;

	private final static Object initLock = new Object();

	public ContainerDirector(ContainerRegistryBuilder cb) {
		this.cb = cb;
	}

	public void startup() throws StartupException {
		Debug.logVerbose("[JdonFramework] <======== JdonFramework beigin to startup =========>", module);
		if (!cb.isKernelStartup())
			synchronized (initLock) {
				if (!cb.isKernelStartup()) {
					try {
						Debug.logVerbose("[JdonFramework] <------ register the basic components in container.xml ------> ", module);
						cb.registerComponents();

						ContainerWrapper cw = cb.getContainerWrapper();
						cw.start();// start core
						cb.setKernelStartup(true);

						Debug.logVerbose("[JdonFramework] <------ started micro container ------> ", module);

						Debug.logVerbose("[JdonFramework] <------ register the pojo services in application's xml ------> ", module);
						cb.registerUserService();

						Debug.logVerbose("[JdonFramework] <------ register the aspect components in container.xml ------> ", module);
						cb.registerAspectComponents();

						cw.setStart(true);
						Debug.logInfo(" <========  Jdon Framework started successfully! =========>", module);

						cb.setupAfterStarted();

						cb.startApp();
					} catch (Exception ex) {
						Debug.logError("[JdonFramework] startup container error: " + ex, module);
						throw new StartupException();
					}
				}
			}

	}

	/**
	 * prepare the applicaition configure files
	 * 
	 * @param configureFileName
	 */
	public void prepareAppRoot(String configureFileName) throws Exception {
		if (!cb.isKernelStartup())
			cb.registerAppRoot(configureFileName);
	}
}
