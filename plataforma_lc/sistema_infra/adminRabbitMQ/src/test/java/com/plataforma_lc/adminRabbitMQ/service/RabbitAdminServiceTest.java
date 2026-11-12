package com.plataforma_lc.adminRabbitMQ.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import com.plataforma_lc.adminRabbitMQ.dto.BindingRequestDTO;
import com.plataforma_lc.adminRabbitMQ.dto.ExchangeRequestDTO;
import com.plataforma_lc.adminRabbitMQ.dto.QueueRequestDTO;

@ExtendWith(MockitoExtension.class)
class RabbitAdminServiceTest {

    @Mock
    private AmqpAdmin amqpAdmin;

    @Mock
    private RabbitAdmin rabbitAdmin;

    private RabbitAdminService service;

    @BeforeEach
    void setUp() {
        service = new RabbitAdminService(amqpAdmin, rabbitAdmin);
    }

    @Test
    void createQueue_declaraColaConPropiedadesDelDto() {
        QueueRequestDTO dto = new QueueRequestDTO();
        dto.setName("cola.anotaciones");
        dto.setDurable(true);
        dto.setExclusive(false);
        dto.setAutoDelete(true);

        final Queue[] captured = new Queue[1];
        doAnswer(inv -> {
            captured[0] = inv.getArgument(0);
            return dto.getName();
        }).when(amqpAdmin).declareQueue(any(Queue.class));

        service.createQueue(dto);

        assertNotNull(captured[0]);
        assertEquals("cola.anotaciones", captured[0].getName());
        assertTrue(captured[0].isDurable());
        assertFalse(captured[0].isExclusive());
        assertTrue(captured[0].isAutoDelete());
    }

    @Test
    void deleteQueue_delegaEnAmqpAdmin() {
        when(amqpAdmin.deleteQueue("cola.test")).thenReturn(true);

        assertTrue(service.deleteQueue("cola.test"));
        verify(amqpAdmin).deleteQueue("cola.test");
    }

    @Test
    void getQueueInfo_retornaInformacionONull() {
        QueueInformation info = new QueueInformation("cola.test", 4, 1);
        when(rabbitAdmin.getQueueInfo("cola.test")).thenReturn(info);
        when(rabbitAdmin.getQueueInfo("inexistente")).thenReturn(null);

        QueueInformation resultado = service.getQueueInfo("cola.test");
        assertNotNull(resultado);
        assertEquals("cola.test", resultado.getName());
        assertEquals(4, resultado.getMessageCount());
        assertNull(service.getQueueInfo("inexistente"));
    }

    @Test
    void purgeQueue_retornaCantidadPurgada() {
        when(rabbitAdmin.purgeQueue("cola.test")).thenReturn(12);

        int purgados = service.purgeQueue("cola.test");

        assertEquals(12, purgados);
        verify(rabbitAdmin).purgeQueue("cola.test");
    }

    @Test
    void createExchange_soportaTopicFanoutYDirect() {
        final Exchange[] captured = new Exchange[1];
        doAnswer(inv -> {
            captured[0] = inv.getArgument(0);
            return null;
        }).when(amqpAdmin).declareExchange(any(Exchange.class));

        ExchangeRequestDTO topicDto = new ExchangeRequestDTO();
        topicDto.setName("ex.topic");
        topicDto.setType("TOPIC");
        service.createExchange(topicDto);
        assertInstanceOf(TopicExchange.class, captured[0]);
        assertEquals("ex.topic", captured[0].getName());

        ExchangeRequestDTO fanoutDto = new ExchangeRequestDTO();
        fanoutDto.setName("ex.fanout");
        fanoutDto.setType("fanout");
        service.createExchange(fanoutDto);
        assertInstanceOf(FanoutExchange.class, captured[0]);

        ExchangeRequestDTO directDto = new ExchangeRequestDTO();
        directDto.setName("ex.direct");
        directDto.setType("direct");
        service.createExchange(directDto);
        assertInstanceOf(DirectExchange.class, captured[0]);
    }

    @Test
    void deleteExchange_delegaEnAmqpAdmin() {
        when(amqpAdmin.deleteExchange("ex.test")).thenReturn(true);

        assertTrue(service.deleteExchange("ex.test"));
        verify(amqpAdmin).deleteExchange("ex.test");
    }

    @Test
    void createYRemoveBinding_declaranYEliminanBindingCorrectamente() {
        BindingRequestDTO dto = new BindingRequestDTO();
        dto.setQueueName("cola.anotaciones");
        dto.setExchangeName("ex.anotaciones");
        dto.setRoutingKey("anotaciones.negativa");

        final Binding[] captured = new Binding[1];
        doAnswer(inv -> {
            captured[0] = inv.getArgument(0);
            return null;
        }).when(amqpAdmin).declareBinding(any(Binding.class));

        service.createBinding(dto);

        assertNotNull(captured[0]);
        assertEquals("cola.anotaciones", captured[0].getDestination());
        assertEquals("ex.anotaciones", captured[0].getExchange());
        assertEquals("anotaciones.negativa", captured[0].getRoutingKey());

        service.removeBinding(dto);
        verify(amqpAdmin).removeBinding(any(Binding.class));
    }
}