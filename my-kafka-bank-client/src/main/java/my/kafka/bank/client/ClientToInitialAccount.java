package my.kafka.bank.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ClientToInitialAccount {

    public static final Logger logger = LoggerFactory.getLogger(ClientToInitialAccount.class);

    private static final Double amount = 100D;
    private static final int accountNumber = 100;


    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(9);

        List<Callable<String>> callableTasks = generateCallableTasks();
        List<Future<String>> futures = executorService.invokeAll(callableTasks);

        awaitTerminationAfterShutdown(executorService);
    }

    private static List<Callable<String>> generateCallableTasks() {
        List<Callable<String>> callableList = new ArrayList<>();

        for (int i = 0; i < accountNumber; i++) {
            String account = String.valueOf(100001 + i);
            Callable<String> callableTask = generateCallableTask(account, amount);
            callableList.add(callableTask);
        }
        return callableList;
    }

    private static Callable<String> generateCallableTask(String account, Double amount) {
        return () -> {
            return AlphaBankRestClient.moveMoney("external", account, amount);
        };
    }

    public static void awaitTerminationAfterShutdown(ExecutorService threadPool) {
        threadPool.shutdown();
        try {
            //threadPool.awaitTermination blocks until all tasks have completed
            if (!threadPool.awaitTermination(180, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
            System.out.println("threadPool.awaitTermination returns true, all threads are done");
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}
