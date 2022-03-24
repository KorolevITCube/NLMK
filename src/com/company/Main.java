package com.company;

import java.io.*;

public class Main {

    private static byte[] bitSetTable = new byte[256];  // предварительный массив заранее подсчитанного количества единиц в байте
    private static byte[][] ipCheck = new byte[256][];  // массив для хранения всех адресов

    public static void main(String[] args) {
        String path = args[0];
        try(Reader reader = new FileReader(path);
            BufferedReader br = new BufferedReader(reader)){

            Thread[] threads = new Thread[3];
            for(int i = 0; i < 2; i++) {
                Thread thread = new Thread(new ReadThread(br, "Thread-"+i));
                thread.start();;
                threads[i] = thread;
            }

            UtilThread thread = new UtilThread();
            thread.start();
            threads[2] = thread;

            for (Thread th:threads) {
                th.join();
            }

        }catch(IOException | InterruptedException e){
            e.printStackTrace();
        }
        setBitSetTable();
        countIpAndWrite();
    }

    // Поток для параллельного чтения из файла
    private static class ReadThread implements Runnable{
        BufferedReader br;
        String name;
        @Override
        public void run() {
            String currentIp;
            try {
                while ((currentIp = br.readLine()) != null) {
                    parseIpAndSave(currentIp);
                }
            }catch(Exception e){
                System.out.println("thread failed");
            }
        }

        public ReadThread(BufferedReader br, String name){
            this.br = br;
            this.name = name;
        }

    }

    // Поток для просчета предварительной таблицы
    private static class UtilThread extends Thread{
        @Override
        public void run() {
            setBitSetTable();
        }
    }

    // подсчет количества единиц в байте
    public static byte countOnes(int n){
        return (byte)(((long)0x08040201*n & 0x111111111L)%15);
    }

    // возврат количества единиц по заранее сделанному массиву
    public static byte countOnesTable(byte n){
        return bitSetTable[n & 0xFF];
    }

    // просчет всей таблицы
    public static void setBitSetTable(){
        for(int i = 0; i < 256; i++){
            bitSetTable[i] = countOnes(i);
        }
    }

    // установка нужных битов в 1 по заданному ip адресу
    public static void setBits(int ipCheckIndex, int ipNumber){
        int nByte = ipNumber/8;
        int nBit = ipNumber%8;
        if(ipCheck[ipCheckIndex] == null) ipCheck[ipCheckIndex] = new byte[2097152];
        ipCheck[ipCheckIndex][nByte] |= (1 << nBit);
    }

    // парсинг и сохранение ip адреса, тк идет запись в массив - синхронизировал
    public synchronized static void parseIpAndSave(String str){
        int ipCheckIndex;
        int ipNumber = 0;
        String[] parsedIp = str.split("\\.");
        ipCheckIndex = Integer.parseInt(parsedIp[0]);
        for (int i = 2; i >= 0; i--){
            int ip = Integer.parseInt(parsedIp[3-i]);
            ipNumber |= ip << (i * 8);
        }
        setBits(ipCheckIndex,ipNumber);
    }

    // просчет итогового массива и вывод
    public static void countIpAndWrite(){
        System.out.println("Task-2\nFirst byte  |  Unique count");
        int finalCount = 0;
        for(int i = 0; i < 256; i++){
            int currentCount = 0;
            if(ipCheck[i] != null){
                for(int j = 0; j < 2097152; j++){
                    currentCount += countOnesTable(ipCheck[i][j]);
                }
            }
            writeToConsole(i,currentCount);
            finalCount += currentCount;
        }
        System.out.println("Task-1\n Unique count: "+finalCount);
    }

    // вспомогательный метод для форматированного вывода
    public static void writeToConsole(int firstByte, int count){
        System.out.printf("%4d  |  %5d\n",firstByte,count);
    }
}
