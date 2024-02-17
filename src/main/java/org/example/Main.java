package org.example;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class Main {
    public static void main(String[] args) {
        if (args.length < 6) {
            System.out.println("Usage: myapp -a <algorithm> -h <hash> -l <length>");
            return;
        }

        String algorithm = args[2];
        String hash = args[4];
        int length = Integer.parseInt(args[6]);

        ExecutorService executor = Executors.newWorkStealingPool();

        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i <= 999999; i++) {
            final String candidate = String.format("%0" + length + "d", i);
            Future<String> future = executor.submit(() -> {
                try {
                    MessageDigest md = MessageDigest.getInstance(algorithm);
                    md.update(candidate.getBytes(StandardCharsets.UTF_8));
                    byte[] digest = md.digest();
                    String candidateHash = bytesToHex(digest);
                    if (candidateHash.equals(hash.toLowerCase())) {
                        return candidate;
                    }
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                return null;
            });
            futures.add(future);
        }

        for (Future<String> future : futures) {
            try {
                String result = future.get();
                if (result != null) {
                    System.out.println(result);
                    executor.shutdownNow();
                    break;
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }

        executor.shutdown();
    }

    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}