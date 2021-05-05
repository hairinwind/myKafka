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
 * This is based on the ClientToTestLimitConcurrent
 * Initial account 100001 balance to 500 and account 100002 balance to 0
 * Create 800 transactions to move $1 from 100001 to 100002
 * Create 400 transactions to move $1 from 100002 to 100001
 * 100001 does not have enough balance, but 100002 inject money back
 * eventually balance of 100001 shall be 100, and balance of 100002 shall be 400
 */
public class ClientToTestLimitConcurrentBidirection {

    public static final Logger logger = LoggerFactory.getLogger(ClientToTestLimitConcurrentBidirection.class);

    private static final int testCount = 800;

    public static void main(String[] args) throws InterruptedException {
        ClientToResetBalance.resetAllBalances(0D);
        //initial 100010 balance to $10
        AlphaBankRestClient.moveMoney("external", "100001", 500D);

        ExecutorService executorService = Executors.newFixedThreadPool(9);

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
        ClientToVerifyBalance.verify(100002);
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
        for (int i = 0; i < testCount/2; i++) {
            String host = getHost(i);
            String fromAccount = String.valueOf(100002);
            String toAccount = String.valueOf(100001);
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
