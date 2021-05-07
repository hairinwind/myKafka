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

import static my.kafka.bank.client.AlphaBankRestClient.producerHost1;
import static my.kafka.bank.client.AlphaBankRestClient.producerHost2;
import static my.kafka.bank.client.AlphaBankRestClient.producerHost3;

/**
 * set the balance of 100001 is 500
 * create 510 concurrent transactions, and each tx moves $1 to account 100002
 * the result is the balance of 100001 shall be 0, it cannot be negative
 * the balance of 100002 shall be 500
 * there shall be 10 dead letters in DLT
 * ===
 * my first implementation is in my-kafka-bank-limit
 * It is branching the data by comparing with the state store balance
 * when balance > amount, decrease the money. Otherwise send to retry topic.
 * but the real test result is that the balance of 100002 is 510
 * and the balance of 100001 is -10
 * which means the state store of balance has some delay
 * ===
 * my second implementation is in my-kafka-bank-limit-advanced-1
 * It compares the amount with the subtotal in aggregator. The subtotal there is actually the realtime balance.
 * If the balance is enough, do the "debit" on the "from account" first, then the "credit" on "to account".
 * If the balance is not enough, retry it
 */
public class ClientToTestLimitConcurrent {

    public static final Logger logger = LoggerFactory.getLogger(ClientToTestLimitConcurrent.class);

    private static final int testCount = 510;

    public static void main(String[] args) throws InterruptedException {
        ClientToResetBalance.resetAllBalances(0D);
        //initial 100010 balance to $10
        AlphaBankRestClient.moveMoney("external", "100001", 500D);

        ExecutorService executorService = Executors.newFixedThreadPool(3);

        List<Callable<String>> callableTasks = generateCallableTasks();

        StopWatch stopWatch = new StopWatch("stopWatch_transactions");
        stopWatch.start();
        List<Future<String>> futures = executorService.invokeAll(callableTasks);
        awaitTerminationAfterShutdown(executorService);
        stopWatch.stop();
        logger.info("total callable tasks: {}", callableTasks.size());
        logger.info("all messages were sent in {} seconds", stopWatch.getTotalTimeSeconds());

        //verify the balance
        Thread.sleep(5000);
        ClientToVerifyBalance.main(new String[]{});
    }

    private static List<Callable<String>> generateCallableTasks() {
        List<Callable<String>> callableList = new ArrayList<>();

        for (int i = 0; i < testCount; i++) {
            String host = getHost(i);
            String fromAccount = String.valueOf(100001);
            String toAccount = String.valueOf(100002);
            Double amount = 1D;
            Callable<String> callableTask = generateCallableTask(host, fromAccount, toAccount, amount);
            callableList.add(callableTask);
        }
        return callableList;
    }

    private static String getHost(int j) {
        List<String> hosts = Arrays.asList(producerHost1, producerHost2, producerHost3);
        return hosts.get(j % 3);
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
