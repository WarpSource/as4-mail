/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.laurentius.ejb.utils;

import jakarta.jms.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.utils.StorageUtils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import static org.junit.Assert.fail;

/**
 * @author Jože Rihtaršič
 */
public class TestUtils {

    protected static final String PERSISTENCE_UNIT_NAME = "ebMS_PU";

    /**
     *
     */
    protected static final String LAU_HOME = "target/TEST-LAU_HOME";
    public static final String LAU_TEST_DOMAIN = "test.com";

    //public static final String S_JMS_JNDI_CF = "java:/jboss/ConnectionFactory";
    // since wildfly 26
    public static final String S_JMS_JNDI_CF = "java:/ConnectionFactory";
    public static final String S_JMS_QUEUE = "queue/MSHQueue";

    static EntityManagerFactory memfMSHFactory = null;

    static {
        System.setProperty("derby.system.home", "target");

        if (!Paths.get(LAU_HOME).toFile().exists()) {
            try {
                Files.createDirectory(Paths.get(LAU_HOME));
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(TestUtils.class.getName()).
                        log(java.util.logging.Level.SEVERE, null, ex);
            }
            System.setProperty(SEDSystemProperties.SYS_PROP_HOME_DIR, LAU_HOME);
            System.setProperty(SEDSystemProperties.SYS_PROP_LAU_DOMAIN,
                    LAU_TEST_DOMAIN);
        }
    }

    public static EntityManager createEntityManager() {
        if (memfMSHFactory == null) {
            memfMSHFactory = Persistence.createEntityManagerFactory(
                    PERSISTENCE_UNIT_NAME);
        }
        return memfMSHFactory.createEntityManager();
    }

    public static void setUpStorage(String folder)
            throws IOException {
        System.setProperty(SEDSystemProperties.SYS_PROP_HOME_DIR, folder);

        Path directory = StorageUtils.getStorageFolder().toPath();
        if (Files.exists(directory)) {
            Path p = Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                        throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }

            });
            if (p == null) {
                fail();
            }
        }
    }

    /**
     * Generate Mock JMS instance for JNDI
     * @param jndiFactoryName jndi factory in Application context
     * @return Mock jndi
     * @throws JMSException
     */
    public static MockJMS setupJMS(String jndiFactoryName) throws JMSException {
        return new MockJMS(jndiFactoryName);
    }

    public void assertJMS(int call, String queueName){

    }
}
