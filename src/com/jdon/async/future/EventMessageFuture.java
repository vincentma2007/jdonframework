/*
 * Copyright 2003-2009 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.jdon.async.future;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import com.jdon.async.EventMessage;
import com.jdon.domain.message.DomainMessage;

public class EventMessageFuture implements EventMessage {

	protected String channel;

	protected boolean asyn;

	protected FutureListener messageListener;

	protected FutureTask futureTask;

	protected Object eventResult;

	protected boolean over;

	protected DomainMessage domainMessage;

	public EventMessageFuture(String channel, final FutureListener messageListener, final DomainMessage domainMessage) {
		this.channel = channel;
		this.messageListener = messageListener;
		this.domainMessage = domainMessage;
		this.futureTask = new FutureTask(new Callable<Boolean>() {
			public Boolean call() throws Exception {
				try {
					messageListener.action(domainMessage);
				} catch (Exception e) {
					System.err.println("[JdonFramework]asynExecMessageListener() error" + e + " message=" + getChannel() + " listener="
							+ messageListener.getClass().getName());
					return false;
				}
				return true;
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jdon.async.message.EventMessageHolder#getEventResult()
	 */
	public Object getEventResult() {
		Object result = null;
		if (over)
			return eventResult;
		try {
			boolean runOk = (Boolean) futureTask.get();
			if (runOk) {
				result = this.eventResult;
			}
			over = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public boolean isAsyn() {
		return asyn;
	}

	public void setAsyn(boolean asyn) {
		this.asyn = asyn;
	}

	public FutureListener getMessageListener() {
		return messageListener;
	}

	public void setMessageListener(FutureListener messageListener) {
		this.messageListener = messageListener;
	}

	public void setFutureTask(FutureTask futureTask) {
		this.futureTask = futureTask;
	}

	public void setEventResult(Object result) {
		this.eventResult = result;
	}

	public DomainMessage getDomainMessage() {
		return domainMessage;
	}

	public void setDomainMessage(DomainMessage domainMessage) {
		this.domainMessage = domainMessage;
	}

	public FutureTask getFutureTask() {
		return futureTask;
	}

}