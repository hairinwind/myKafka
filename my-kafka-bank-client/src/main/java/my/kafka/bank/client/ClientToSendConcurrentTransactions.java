package my.kafka.bank.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static my.kafka.bank.client.AlphaBankRestClient.getHost;

public class ClientToSendConcurrentTransactions {

    public static final Logger logger = LoggerFactory.getLogger(ClientToSendConcurrentTransactions.class);

    private static final int testAccountNumber = 100;

    public static void main(String[] args) throws InterruptedException {
//        ClientToResetBalance.resetAllBalances(100D);

        ExecutorService executorService = Executors.newFixedThreadPool(10);

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

        Double amount = 1D;
        for (int i = 0; i < testAccountNumber; i++) {
            String fromAccount = String.valueOf(100001 + i);
            // move $1 to any account after current account
            // every account balance is starting from $1000
            for (int j = i + 1; j < testAccountNumber; j++) {
                String host = getHost(j);
                String toAccount = String.valueOf(100001 + j);
                Callable<String> callableTask = generateCallableTask(host, fromAccount, toAccount, amount);
                callableList.add(callableTask);
            }
            final int index = i;
            callableList.add(() -> {
                System.out.println("..." + index);
                return "";
            });
        }
        return callableList;
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
