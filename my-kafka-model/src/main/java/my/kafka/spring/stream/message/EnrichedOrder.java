package my.kafka.spring.stream.message;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrichedOrder {

    private Product product;
    private Customer customer;
    private Order order;

}
