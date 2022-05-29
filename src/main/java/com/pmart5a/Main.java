package com.pmart5a;

import com.pmart5a.myclasses.CallCenter;

/* Так как работа колл-центра зависит от быстроты реагирования специалистов на поступающие звонки, то очередь, в
которую помещаются входящие звонки, должна обладать максимальной пропускной способностью и неограниченной ёмкостью.
Поскольку в условии задания не требуется предусмотреть возможность для специалистов возвращать звонки обратно в очередь,
то лучше выбрать коллекцию реализующую интерфейс Queue. Для имитации работы техподдержки через АТС, согласно условию
задания, наиболее подходит потокобезопасная коллекция ConcurrentLinkedQueue. У данной коллекции из-за отсутствия
блокировок хорошеее быстродействие и её емкость ограничена только доступной оперативной памятью (ОП). При большом
количестве звонков и малом количестве специалистов, существует опасность разрастания очереди и нехватки ОП. Разрастание
очереди можно скомпенсировать увеличением количества специалистов и увеличением ОП (если это возможно).
Если количество специалистов фиксировано и ОП недостаточно, и достигнут предел её увеличения, то в этом случае
альтернативой ConcurrentLinkedQueue будет потокобезопасная коллекция LinkedBlockingQueue, также реализующая интерфейс
Queue и имеющая приемлимое быстродействие. У данной коллекции можно задать ёмкость, что при недостаточном количестве
ОП будет плюсом. Так как в задании нет ограничений по ОП, то для его выполнения я использовал ConcurrentLinkedQueue.
Вывод в консоль из потоков реализован для наглядности их работы.
 */

public class Main {

    public static final int NUMBER_OF_TREADS = 31;
    public static final int DELAY_TIME_BEFORE_SHUTDOWN_ATS = 5000;

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
        System.out.printf("Всего сгенерировано звонков АТС: %d\n", callCenter.getAts().getNumberOfCallsForAllTime());
        System.out.printf("Всего обработано звонков: %d\n", callCenter.getProcessedPhoneCalls().size());
        System.out.println("Обработанные звонки:");
        callCenter.getProcessedPhoneCalls().parallelStream()
                .forEach(System.out::println);
    }
}