/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.laurentius.msh.ws;

import java.io.File;
import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import si.laurentius.commons.SEDValues;

/**
 *
 * @author Jože Rihtaršič
 */
public class TestUtils {

  /**
     *
     */
  protected static final String JNDI_CONNECTION_FACTORY = "ConnectionFactory";

  /**
     *
     */
  protected static final String PERSISTENCE_LAU_UNIT_NAME = "ebMS_MSH_PU";

  /**
     *
     */
  protected static final String PERSISTENCE_UNIT_NAME = "ebMS_PU";

  /**
     *
     */
  protected static final String LAU_HOME = "target/TEST-LAU_HOME";

 

}
