package my.kafka.spring.stream.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerOrder {

    private Order order;
    private Customer customer;

    public String productId() {
        return this.order.getProductId();
    }
}
