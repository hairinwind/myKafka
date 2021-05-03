package my.kafka.bank.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static my.kafka.bank.client.AlphaBankRestClient.multipleProducerEnabled;
import static my.kafka.bank.client.AlphaBankRestClient.producerHost1;
import static my.kafka.bank.client.AlphaBankRestClient.producerHost2;
import static my.kafka.bank.client.AlphaBankRestClient.producerHost3;

public class ClientToTestLimit {

    public static final Logger logger = LoggerFactory.getLogger(ClientToTestLimit.class);

    private static final int testAccountNumber = 10;

    public static void main(String[] args) throws InterruptedException {
        ClientToResetBalance.resetAllBalances(0D);
        //initial 100010 balance to $10
        AlphaBankRestClient.moveMoney("external", "100010", 10D);

        ExecutorService executorService = Executors.newFixedThreadPool(1); // make it sequential, so only the last tx has enough balance

        List<Callable<String>> callableTasks = generateCallableTasks();

        StopWatch stopWatch = new StopWatch("stopWatch_transactions");
        stopWatch.start();
        List<Future<String>> futures = executorService.invokeAll(callableTasks);
        awaitTerminationAfterShutdown(executorService);
        stopWatch.stop();
        logger.info("total callable tasks: {}", callableTasks.size());
        logger.info("all messages were sent in {} seconds", stopWatch.getTotalTimeSeconds());

        //manually run "ClientToVerifyBalance" to verify the balance
    }

    private static List<Callable<String>> generateCallableTasks() {
        List<Callable<String>> callableList = new ArrayList<>();

        /**
         * start balance are all 0 except 100010 balance is $11
         * 100001 -> 100002 $9
         * 100002 -> 100003 $8
         * 100003 -> 100004 $7
         * ...
         * 100009 -> 100010 $1
         * 100010 -> 100001 $10
         */
        for (int i = 0; i < testAccountNumber; i++) {
            String host = getHost(i);
            String fromAccount = String.valueOf(100001 + i);
            String toAccount = String.valueOf(100001 + i + 1);
            Double amount = (double)(testAccountNumber - i - 1);
            if (i+1 == testAccountNumber) {
                toAccount = "100001";
                amount = 10D;
            }
            Callable<String> callableTask = generateCallableTask(host, fromAccount, toAccount, amount);
            callableList.add(callableTask);
        }
        return callableList;
    }

    private static String getHost(int j) {
        if (multipleProducerEnabled) {
            List<String> hosts = Arrays.asList(producerHost1, producerHost2, producerHost3);
            return hosts.get(j % 3);
        } else {
            return producerHost1;
        }
    }

    private static Callable<String> generateCallableTask(String host, String fromAccount, String toAccount, Double amount) {
        return () -> {
            logger.debug("send {} from {} to {}", amount, fromAccount, toAccount);
            return AlphaBankRestClient.moveMoney(fromAccount, toAccount, amount, host);
        };
    }

    public static void awaitTerminationAfterShutdown(ExecutorService threadPool) {
        threadPool.shutdown();
        try {
            //threadPool.awaitTermination blocks until all tasks have completed
            if (!threadPool.awaitTermination(180, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
            logger.info("threadPool.awaitTermination returns true, all threads are done");
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
