package com.pmart5a;

import com.pmart5a.myclasses.CallCenter;

/* Для имитации работы техподдержки через АТС наиболее подходят, на мой взгляд, две коллекции:
ConcurrentLinkedQueue и LinkedBlockingQueue. Так как поступающие звонки требуют быстрых ответов, то необходимо
наибольшее быстродействие и здесь лучшей будет ConcurrentLinkedQueue, так как у неё отсутствуют блокировки.
Но поскольку звонки поступают быстрее, чем разбираются из очереди операторами, то очередь будет расти, что может
привести (а в нашем случае обязательно приведёт, так как звонки генерируются постоянно) к исчерпанию ОП. Разрастание
очереди можно скомпенсировать увеличением количества операторов. Если количество операторов фиксировано и очередь
быстро растёт, то можно блокировать АТС до момента разгрузки очереди, или использовать LinkedBlockingQueue с
ограниченной ёмкостью (или ArrayBlockingQueue), что при заполенной очереди, в принципе, равнозначно блокированию АТС.
Только при блокировании АТС пользователь не сможет дозвониться, а при заполненной очереди он дозвониться и будет очень
долго ждать ответа, что тоже никому не понравиться, тем более, если канал платный. Мой выбор (я за клиент-ориентированный
подход) - ConcurrentLinkedQueue с увеличением количества операторов при проблемах (физическое увеличение ОП
не расматриваю, считаю что предел уже достигнут). А дальше как скажет бизнес (дополнительные рабочие места, зарплата
дополнительным операторам и п. д., и т. п.).
Вывод в консоль из потоков реализован для наглядности их работы.
 */

public class Main {

    public static final int NUMBER_OF_TREADS = 11;
    public static final int DELAY_TIME_BEFORE_SHUTDOWN_ATS = 7000;

    public static void main(String[] args) {
        ThreadGroup handlerGroup = new ThreadGroup("Группа специалистов");
        final CallCenter callCenter = new CallCenter();
        for (int i = 0; i < NUMBER_OF_TREADS; i++) {
            if (i == 0) {
                new Thread(null, callCenter::receiveIncomingCall, "АТС").start();
            } else {
                new Thread(handlerGroup, callCenter::processIncomingCall, i + " специалист").start();
            }
        }
        try {
            Thread.sleep(DELAY_TIME_BEFORE_SHUTDOWN_ATS);
            System.out.printf("Поток %s: выключаю АТС.\n", Thread.currentThread().getName());
            callCenter.getAts().disableGenerationCall();
            Thread[] handlerThreads = new Thread[NUMBER_OF_TREADS - 1];
            handlerGroup.enumerate(handlerThreads);
            for (Thread thread : handlerThreads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Обработанные звонки:");
        callCenter.getProcessedPhoneCalls().parallelStream()
                .forEach(System.out::println);
    }
}