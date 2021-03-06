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
package com.jdon.async.disruptor;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import com.jdon.container.ContainerWrapper;
import com.jdon.container.finder.ContainerCallback;
import com.jdon.domain.message.DomainEventHandler;
import com.jdon.util.Debug;
import com.lmax.disruptor.AbstractEvent;
import com.lmax.disruptor.ClaimStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.wizard.DisruptorWizard;
import com.lmax.disruptor.wizard.EventHandlerGroup;

public class DisruptorFactory implements EventFactory {
	public final static String module = DisruptorFactory.class.getName();
	public final static String TOPICNAME = "TOPIC";

	protected final Map<String, TreeSet<DomainEventHandler>> handlesMap;
	private String RingBufferSize;

	private final ContainerWrapper containerWrapper;

	public DisruptorFactory(DisruptorParams disruptorParams, ContainerCallback containerCallback) {
		this.RingBufferSize = disruptorParams.getRingBufferSize();
		this.containerWrapper = containerCallback.getContainerWrapper();
		this.handlesMap = new ConcurrentHashMap<String, TreeSet<DomainEventHandler>>();
	}

	private DisruptorWizard createDw() {
		return new DisruptorWizard<EventDisruptor>(this, Integer.parseInt(RingBufferSize), Executors.newCachedThreadPool(),
				ClaimStrategy.Option.SINGLE_THREADED, WaitStrategy.Option.YIELDING);
	}

	public DisruptorWizard addEventMessageHandler(String topic, TreeSet<DomainEventHandler> handlers) {
		DisruptorWizard dw = createDw();
		EventHandlerGroup eh = null;
		for (DomainEventHandler handler : handlers) {
			if (eh == null) {
				eh = dw.handleEventsWith(handler);
			} else {
				eh = eh.handleEventsWith(handler);
			}
		}
		return dw;
	}

	public EventDisruptor getEventDisruptor(String topic) {
		TreeSet handlers = handlesMap.get(topic);
		if (handlers == null)// not inited
		{
			handlers = loadEvenHandler(topic);
			handlesMap.put(topic, handlers);
		}
		DisruptorWizard disruptorWizard = addEventMessageHandler(topic, handlers);
		RingBuffer ringBuffer = disruptorWizard.start();
		EventDisruptor eventDisruptor = (EventDisruptor) ringBuffer.nextEvent();
		eventDisruptor.setRingBuffer(ringBuffer);
		return eventDisruptor;
	}

	/**
	 * if there are many consumers, execution order will be alphabetical list by
	 * Name of @Consumer class.
	 * 
	 * @param topic
	 * @return
	 */
	protected TreeSet<DomainEventHandler> loadEvenHandler(String topic) {
		TreeSet<DomainEventHandler> ehs = new TreeSet(new Comparator() {
			public int compare(Object num1, Object num2) {
				String inum1, inum2;
				inum1 = num1.getClass().getName();
				inum2 = num2.getClass().getName();
				if (inum1.compareTo(inum2) < 1) {
					return -1; // returning the first object
				} else {

					return 1;
				}
			}

		});
		Collection consumers = (Collection) containerWrapper.lookup(DisruptorFactory.TOPICNAME + topic);
		if (consumers == null || consumers.size() == 0) {
			Debug.logWarning("[Jdonframework]there is no any consumer class annotated with @Consumer(" + topic + ") ", module);
			return ehs;
		}
		for (Object o : consumers) {
			String consumerName = (String) o;
			DomainEventHandler eh = (DomainEventHandler) containerWrapper.getComponentNewInstance(consumerName);
			ehs.add(eh);
		}
		return ehs;

	}

	public void fire(String topic, EventDisruptor eventDisruptor) {
		RingBuffer ringBuffer = eventDisruptor.getRingBuffer();
		ringBuffer.publish(eventDisruptor);
	}

	@Override
	public AbstractEvent create() {
		return new EventDisruptor();

	}
}
