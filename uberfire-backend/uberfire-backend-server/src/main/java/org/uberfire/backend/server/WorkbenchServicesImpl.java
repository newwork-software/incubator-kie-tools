/*
 * Copyright 2012 JBoss Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.uberfire.backend.server;

import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.thoughtworks.xstream.XStream;
import org.jboss.errai.bus.server.annotations.Service;
import org.kie.commons.io.IOService;
import org.kie.commons.java.nio.file.NoSuchFileException;
import org.kie.commons.java.nio.file.Path;
import org.uberfire.workbench.model.PerspectiveDefinition;
import org.uberfire.workbench.services.WorkbenchServices;

/**
 * Workbench services
 */
@Service
@ApplicationScoped
public class WorkbenchServicesImpl
        implements
        WorkbenchServices {

    @Inject
    @Named("ioStrategy")
    private IOService ioService;

    @Inject
    private UserServicesImpl userServices;

    private XStream xs = new XStream();

    @Override
    public void save( final PerspectiveDefinition perspective ) {
        final String xml = xs.toXML( perspective );
        final Path perspectivePath = userServices.buildPath( "perspectives",
                                                             perspective.getName() + ".perspective" );
        ioService.write( perspectivePath, xml );
    }

    @Override
    public PerspectiveDefinition load( final String perspectiveName ) {
        final Path perspectivePath = userServices.buildPath( "perspectives",
                                                             perspectiveName + ".perspective" );
        if ( ioService.exists( perspectivePath ) ) {
            final String xml = ioService.readAllString( perspectivePath );
            return (PerspectiveDefinition) xs.fromXML( xml );
        }

        return null;
    }

    @Override
    public Map<String, String> loadDefaultEditorsMap() {
        final Map<String, String> map = new HashMap<String, String>();
        try {
            final Path path = getPathToDefaultEditors();
            if ( ioService.exists( path ) ) {
                for ( String line : ioService.readAllLines( path ) ) {
                    if ( !line.trim().startsWith( "#" ) ) {
                        String[] split = line.split( "=" );
                        map.put( split[ 0 ], split[ 1 ] );
                    }
                }
            }

            return map;

        } catch ( final NoSuchFileException e ) {
            e.printStackTrace();
            return map;
        }
    }

    @Override
    public void saveDefaultEditors( final Map<String, String> properties ) {
        final StringBuilder text = new StringBuilder();
        for ( String key : properties.keySet() ) {
            text.append( String.format( "%s=%s", key, properties.get( key ) ) );
        }
        ioService.write( getPathToDefaultEditors(),
                         text.toString() );
    }

    private Path getPathToDefaultEditors() {
        return userServices.buildPath( "defaultEditors",
                                       null );
    }
}
