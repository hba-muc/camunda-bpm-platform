/* Licensed under the Apache License, Version 2.0 (the "License");
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
package org.camunda.bpm.application.impl;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;

import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.ProcessApplication;

/**
 * <p>This class is an implementation of {@link ServletContainerInitializer} and 
 * is notified whenever a subclass of {@link ServletProcessApplication} annotated 
 * with the {@link ProcessApplication} annotation is deployed. In such an event, 
 * we automatically add the class as {@link ServletContextListener} to the 
 * {@link ServletContext}.</p>
 * 
 * <p><strong>NOTE:</strong> Only works with Servlet 3.0 or better.</p>
 * 
 * @author Daniel Meyer
 *
 */
@HandlesTypes(ProcessApplication.class)
public class ServletProcessApplicationDeployer implements ServletContainerInitializer {
  
  private final static Logger LOGGER = Logger.getLogger(ServletProcessApplicationDeployer.class.getName());
  
  public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {    
    if(c == null || c.isEmpty()) {
      // skip deployments that do not carry a PA
      return;
      
    } else if(c.size() > 1) {
      // a deployment must only contain a single PA
      String msg = getLogMultiplePas(c, ctx);      
      LOGGER.log(Level.SEVERE, msg);
      throw new ServletException(msg);
      
    } else {
      Class<?> paClass = c.iterator().next();
      
      // validate whether it is a legal Process Application
      if(!AbstractProcessApplication.class.isAssignableFrom(paClass)) {
        String msg = getLogWrongType(paClass);
        LOGGER.log(Level.SEVERE, msg);
        throw new ServletException(msg);
      }
      
      // add it as listener if it's a ServletProcessApplication
      if(ServletProcessApplication.class.isAssignableFrom(paClass)) {
        LOGGER.info("Detected @ProcessApplication class "+paClass.getName());
        ctx.addListener(paClass.getName());
      } 
    }
    
  }

  protected String getLogWrongType(Class<?> paClass) {
    String msg = "Class '"+paClass+"' is annotated with @"+ProcessApplication.class.getName()+" " +
    		"but is not a subclass of "+AbstractProcessApplication.class.getName();
    return msg;
  }

  protected String getLogMultiplePas(Set<Class<?>> c, ServletContext ctx) {
    StringBuilder builder = new StringBuilder();
    builder.append("An application must not contain more than one class annotated with @ProcessApplication.\n Application '");
    builder.append(ctx.getContextPath());
    builder.append("' contains the following @ProcessApplication classes:\n");
    for (Class<?> clazz : c) {
      builder.append("  ");
      builder.append(clazz.getName());
      builder.append("\n");                
    }
    String msg = builder.toString();
    return msg;
  }

}
