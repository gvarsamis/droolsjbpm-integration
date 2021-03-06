/*
 * Copyright 2010 JBoss Inc
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

package org.drools.runtime.pipeline.impl;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.KnowledgeBase;
import org.kie.KnowledgeBaseFactory;
import org.kie.builder.KnowledgeBuilder;
import org.kie.builder.KnowledgeBuilderFactory;
import org.kie.builder.ResourceType;
import org.kie.builder.help.KnowledgeBuilderHelper;
import org.kie.io.ResourceFactory;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.rule.FactHandle;

import static org.junit.Assert.*;

import org.apache.activemq.broker.BrokerService;
import org.drools.runtime.pipeline.Action;
import org.drools.runtime.pipeline.KnowledgeRuntimeCommand;
import org.drools.runtime.pipeline.Pipeline;
import org.drools.runtime.pipeline.PipelineFactory;
import org.drools.runtime.pipeline.ResultHandler;
import org.drools.runtime.pipeline.ResultHandlerFactory;
import org.drools.runtime.pipeline.Service;
import org.drools.runtime.pipeline.Transformer;
import org.drools.core.util.StringUtils;

import com.sun.tools.xjc.Language;
import com.sun.tools.xjc.Options;

public class JaxbSimpleJmsMessengerTest {

    private SimpleProducer simpleProducer;
    private BrokerService  broker;
    private String         destinationName = "dynamicQueues/FOO.BAR";
    private String         url             = "vm://localhost:61616";

    private Properties     props;

//    @Before
//    public void setUp() {
//        try {
//            this.broker = new BrokerService();
//            // configure the broker
//            this.broker.setBrokerName( "consumer" );
//            this.broker.addConnector( url );
//            this.broker.start();
//
//            props = new Properties();
//            props.setProperty( Context.INITIAL_CONTEXT_FACTORY,
//                               "org.apache.activemq.jndi.ActiveMQInitialContextFactory" );
//            props.setProperty( Context.PROVIDER_URL,
//                               this.url );
//
//            this.simpleProducer = new SimpleProducer( props,
//                                                      this.destinationName );
//            this.simpleProducer.start();
//        } catch ( Exception e ) {
//            throw new RuntimeException( e );
//        }
//    }
//    
//    public void tearDown() throws Exception {
//        this.simpleProducer.stop();
//        this.broker.stop();
//    }

    @Test @Ignore
    public void testJmsWithJaxb() throws Exception {
        Options xjcOpts = new Options();
        xjcOpts.setSchemaLanguage( Language.XMLSCHEMA );
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        String[] classNames = KnowledgeBuilderHelper.addXsdModel( ResourceFactory.newClassPathResource( "order.xsd",
                                                                                                        getClass() ),
                                                                  kbuilder,
                                                                  xjcOpts,
                                                                  "xsd" );

        assertFalse( kbuilder.hasErrors() );

        kbuilder.add( ResourceFactory.newClassPathResource( "test_Jaxb.drl",
                                                            getClass() ),
                      ResourceType.DRL );

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

        Action resultHandlerStage = PipelineFactory.newExecuteResultHandler();

        KnowledgeRuntimeCommand insertStage = PipelineFactory.newStatefulKnowledgeSessionInsert();
        insertStage.setReceiver( resultHandlerStage );

        JAXBContext jaxbCtx = KnowledgeBuilderHelper.newJAXBContext( classNames,
                                                                     kbase );
        Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
        Transformer transformer = PipelineFactory.newJaxbFromXmlTransformer( unmarshaller );
        transformer.setReceiver( insertStage );

        Action unwrapObjectStage = PipelineFactory.newJmsUnwrapMessageObject();
        unwrapObjectStage.setReceiver( transformer );

        Pipeline pipeline = PipelineFactory.newStatefulKnowledgeSessionPipeline( ksession );
        pipeline.setReceiver( unwrapObjectStage );

        ResultHandleFactoryImpl factory = new ResultHandleFactoryImpl();
        Service feeder = PipelineFactory.newJmsMessenger( pipeline,
                                                          props,
                                                          this.destinationName,
                                                          factory );
        feeder.start();

        String xml = StringUtils.readFileAsString( new InputStreamReader( getClass().getResourceAsStream( "order.xml" ) ) );

        this.simpleProducer.sendObject( xml );

        for ( int i = 0; i < 5; i++ ) {
            // iterate and sleep 5 times, to give these messages time to complete.
            if ( factory.list.size() == 1 ) {
                break;
            }
            Thread.sleep( 5000 );
        }

        FactHandle factHandle = (FactHandle) ((Map) ((ResultHandlerImpl) factory.list.get( 0 )).getObject()).keySet().iterator().next();
        assertNotNull( factHandle );

        assertEquals( 1,
                      factory.list.size() );

        Action executeResult = PipelineFactory.newExecuteResultHandler();

        Action assignAsResult = PipelineFactory.newAssignObjectAsResult();
        assignAsResult.setReceiver( executeResult );

        //transformer = PipelineFactory.newXStreamToXmlTransformer( xstream );
        Marshaller marshaller = jaxbCtx.createMarshaller();
        transformer = PipelineFactory.newJaxbToXmlTransformer( marshaller );
        transformer.setReceiver( assignAsResult );

        KnowledgeRuntimeCommand getObject = PipelineFactory.newStatefulKnowledgeSessionGetObject();
        getObject.setReceiver( transformer );

        pipeline = PipelineFactory.newStatefulKnowledgeSessionPipeline( ksession );
        pipeline.setReceiver( getObject );

        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        pipeline.insert( factHandle,
                         resultHandler );

        assertEqualsIgnoreWhitespace( xml,
                                      (String) resultHandler.getObject() );
        feeder.stop();

    }

    public static class ResultHandleFactoryImpl
        implements
        ResultHandlerFactory {
        List list = new ArrayList();

        public ResultHandler newResultHandler() {
            ResultHandler handler = new ResultHandlerImpl();
            list.add( handler );
            return handler;
        }

    }

    public static class ResultHandlerImpl
        implements
        ResultHandler {
        Object object;

        public void handleResult(Object object) {
            this.object = object;
        }

        public Object getObject() {
            return this.object;
        }
    }

    private static void assertEqualsIgnoreWhitespace(final String expected,
                                                     final String actual) {
        final String cleanExpected = expected.replaceAll( "\\s+",
                                                          "" );
        final String cleanActual = actual.replaceAll( "\\s+",
                                                      "" );
        assertEquals( cleanExpected,
                      cleanActual );
    }
}
