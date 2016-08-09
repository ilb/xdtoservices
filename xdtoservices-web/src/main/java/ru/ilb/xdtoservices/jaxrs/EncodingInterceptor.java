/*
 * Copyright 2016 slavb.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.ilb.xdtoservices.jaxrs;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

/**
 *
 * @author slavb
 */
public class EncodingInterceptor extends AbstractPhaseInterceptor<Message> {

        public EncodingInterceptor() {
                super(Phase.RECEIVE);
        }

        @Override
        public void handleMessage(Message message) throws Fault {
                String encoding = (String) message.get(Message.ENCODING);
               
                if (encoding == null || !encoding.equals("UTF-8")) {
                        message.put(Message.ENCODING, "UTF-8");
                }
        }

} 