package si.laurentius.ejb.utils;

import jakarta.jms.*;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

public class MockJMS {
    ConnectionFactory connectionFactory = Mockito.mock(ConnectionFactory.class);
    Connection connection = Mockito.mock(Connection.class);
    Session session = Mockito.mock(Session.class);
    Message message = Mockito.mock(Message.class);

    MessageProducer messageProducer = Mockito.mock(MessageProducer.class);

    public MockJMS(String jndiFactoryName) throws JMSException {
        Mockito.doReturn(connection).when(connectionFactory).createConnection();
        Mockito.doReturn(session).when(connection).createSession(ArgumentMatchers.anyBoolean(), ArgumentMatchers.anyInt());
        Mockito.doReturn(message).when(session).createMessage();
        Mockito.doReturn(messageProducer).when(session).createProducer(ArgumentMatchers.any());


        InitialContextFactoryForTest.bind(jndiFactoryName, connectionFactory);
    }

    public void assertJMSSendMessageCallCount(int call) throws JMSException {
        Mockito.verify(messageProducer, Mockito.times(call)).send(ArgumentMatchers.any(Message.class));
    }

    public void jmsResetMessageCount(){
        Mockito.reset(messageProducer);



    }
}
