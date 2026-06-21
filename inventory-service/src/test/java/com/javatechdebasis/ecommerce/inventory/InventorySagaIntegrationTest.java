package com.javatechdebasis.ecommerce.inventory;

import com.javatechdebasis.ecommerce.common.KafkaTopics;
import com.javatechdebasis.ecommerce.common.event.InventoryReservedEvent;
import com.javatechdebasis.ecommerce.common.event.OrderCreatedEvent;
import com.javatechdebasis.ecommerce.inventory.repository.ProductRepository;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * End-to-end slice of the saga inside inventory-service backed by an embedded
 * Kafka broker: publishing an {@link OrderCreatedEvent} should make the service
 * reserve stock and emit an {@link InventoryReservedEvent}.
 */
@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
@EmbeddedKafka(partitions = 1, topics = {KafkaTopics.ORDER_EVENTS, KafkaTopics.INVENTORY_EVENTS})
class InventorySagaIntegrationTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private ProductRepository productRepository;

    private Consumer<String, Object> consumer;

    @AfterEach
    void tearDown() {
        if (consumer != null) {
            consumer.close();
        }
    }

    @Test
    void orderCreatedEvent_reservesStockAndEmitsInventoryReserved() {
        consumer = inventoryEventsConsumer();
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, KafkaTopics.INVENTORY_EVENTS);

        int reservedBefore = productRepository.findByProductCode("LAPTOP-001")
                .orElseThrow(() -> new AssertionError("seed product missing"))
                .getReservedQuantity();

        try (Producer<String, Object> producer = orderEventsProducer()) {
            producer.send(new ProducerRecord<>(KafkaTopics.ORDER_EVENTS, "1", OrderCreatedEvent.builder()
                    .orderId(1L)
                    .productCode("LAPTOP-001")
                    .quantity(2)
                    .amount(new BigDecimal("1998.00"))
                    .customerEmail("alice@example.com")
                    .build()));
            producer.flush();
        }

        ConsumerRecord<String, Object> record =
                KafkaTestUtils.getSingleRecord(consumer, KafkaTopics.INVENTORY_EVENTS);

        assertThat(record.value()).isInstanceOf(InventoryReservedEvent.class);
        InventoryReservedEvent event = (InventoryReservedEvent) record.value();
        assertThat(event.getOrderId()).isEqualTo(1L);
        assertThat(event.getProductCode()).isEqualTo("LAPTOP-001");
        assertThat(event.getQuantity()).isEqualTo(2);

        await().atMost(10, SECONDS).untilAsserted(() ->
                assertThat(productRepository.findByProductCode("LAPTOP-001").get().getReservedQuantity())
                        .isEqualTo(reservedBefore + 2));
    }

    private Producer<String, Object> orderEventsProducer() {
        Map<String, Object> props = KafkaTestUtils.producerProps(embeddedKafka);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<String, Object>(props).createProducer();
    }

    private Consumer<String, Object> inventoryEventsConsumer() {
        Map<String, Object> props = new HashMap<>(KafkaTestUtils.consumerProps("it-consumer", "true", embeddedKafka));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<String, Object>(props).createConsumer();
    }
}
