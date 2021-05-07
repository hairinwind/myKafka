package my.kafka.bank.client;

import my.kafka.bank.message.AccountBalance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

public class AlphaBankRestClient {

    public static final Logger logger = LoggerFactory.getLogger(AlphaBankRestClient.class);

    // These are the rest endpoint which receive messages and then send them to kafka
    public static final String producerHost1 = "http://localhost:9999";
    public static final String producerHost2 = "http://localhost:9998";
    public static final String producerHost3 = "http://localhost:9997";
    public static final boolean multipleProducerEnabled = false;

    // stream hosts
    private static final String host1 = "http://localhost:9001";
    private static final String host2 = "http://localhost:9002";
    private static final String host3 = "http://localhost:9003";

    public static String moveMoney(String fromAccount, String toAccount, Double amount, String host) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("fromAccount", fromAccount);
        map.add("toAccount", toAccount);
        map.add("amount", String.valueOf(amount));
        logger.debug("request form: {}", map);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        return restTemplate.postForObject(host + "/send", request, String.class);
    }

    public static String moveMoney(String fromAccount, String toAccount, Double amount) {
        return moveMoney(fromAccount, toAccount, amount, producerHost1);
    }

    public static List<AccountBalance> allAccountBalances() {
        List<AccountBalance> accountBalances = fetchAllAccountBalances(host1);
        try {
            accountBalances.addAll(fetchAllAccountBalances(host2));
            accountBalances.addAll(fetchAllAccountBalances(host3));
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        return accountBalances;
    }

    private static List<AccountBalance> fetchAllAccountBalances(String url) {
        RestTemplate restTemplate = new RestTemplate();
        String endpoint = url + "/fetchAllLocalBalances";
        return restTemplate.exchange(endpoint, HttpMethod.GET, null, new ParameterizedTypeReference<List<AccountBalance>>(){})
                .getBody();
    }

    public static String getHost(int index) {
        if (multipleProducerEnabled) {
            List<String> hosts = Arrays.asList(producerHost1, producerHost2, producerHost3);
            return hosts.get(index % 3);
        }
        return producerHost1;
    }
}
