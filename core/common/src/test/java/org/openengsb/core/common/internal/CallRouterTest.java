/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.common.internal;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.common.OpenEngSBService;
import org.openengsb.core.common.communication.MethodCall;
import org.openengsb.core.common.communication.Port;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class CallRouterTest {

    private TestService serviceMock;

    @Test
    public void testReceiveAnything() throws Exception {
        CallRouterImpl callrouter = new CallRouterImpl();
        Port portMock = createPortMock();
        BundleContext bundleContext = createBundleContextMock();
        callrouter.setBundleContext(bundleContext);
        callrouter.start();
        callrouter.registerPort("jms", portMock);
        Thread.sleep(300);
        callrouter.stop();

        verify(portMock, atLeast(1)).receive();
    }

    @Test
    public void testRecieveMethodCall_shouldCallService() throws Exception {
        CallRouterImpl callrouter = new CallRouterImpl();
        Port portMock = createPortMock();
        BundleContext bundleContext = createBundleContextMock();
        callrouter.setBundleContext(bundleContext);

        callrouter.start();
        callrouter.registerPort("jms", portMock);
        callrouter.stop();
        Thread.sleep(300);

        verify(serviceMock, atLeast(1)).test();
    }

    private BundleContext createBundleContextMock() throws InvalidSyntaxException {
        BundleContext bundleContext = mock(BundleContext.class);
        final ServiceReference serviceRefMock = mock(ServiceReference.class);
        when(bundleContext.getServiceReferences(eq(OpenEngSBService.class.getName()), anyString())).thenReturn(
            new ServiceReference[]{ serviceRefMock, });
        serviceMock = mock(TestService.class);
        when(bundleContext.getService(serviceRefMock)).thenReturn(serviceMock);
        return bundleContext;
    }

    private Port createPortMock() {
        final Port portMock = mock(Port.class);
        when(portMock.receive()).thenAnswer(new Answer<MethodCall>() {
            boolean first = true;

            @Override
            public MethodCall answer(InvocationOnMock invocation) throws Throwable {
                if (!first) {
                    Thread.sleep(10000);
                }
                first = false;
                return new MethodCall("42", "test", new Object[0], null);
            }
        });
        return portMock;
    }
}