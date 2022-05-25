package com.pmart5a.myclasses;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AutomaticTelephoneStation {

    private static final int CALL_RECEIPT_INTERVAL = 50;
    private static final long minPhoneNumber = 8100000001L;
    private static final long maxPhoneNumber = 8999999999L;
    private static final ConcurrentLinkedQueue<PhoneCall> queueOfPhoneCalls = new ConcurrentLinkedQueue<>();
    private volatile boolean generation;

    public AutomaticTelephoneStation() {}

    public PhoneCall getCallFromQueue() {
        return queueOfPhoneCalls.poll();
    }

    public void addCallInQueue() {
        queueOfPhoneCalls.offer(createPhoneCall());
    }

    public boolean isEmptyCallInQueue() {
        return queueOfPhoneCalls.isEmpty();
    }

    public void generateCalls() {
        try {
            enableGenerationCall();
            while (generation) {
                    addCallInQueue();
                System.out.printf("Поток %s: входящий звонок.\n", Thread.currentThread().getName());
                Thread.sleep(CALL_RECEIPT_INTERVAL);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.printf("Поток %s: завершаю работу.\n", Thread.currentThread().getName());
    }

    public void enableGenerationCall() {
        generation = true;
    }

    public void disableGenerationCall() {
        generation = false;
    }

    public boolean getGenerationCall() {
        return generation;
    }

    public PhoneCall createPhoneCall() {
        return new PhoneCall(LocalDateTime.now(), getPhoneNumberRandom());
    }

    public long getPhoneNumberRandom() {
        return new Random().nextLong((maxPhoneNumber - minPhoneNumber) + 1) + minPhoneNumber;
    }
}