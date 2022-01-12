package org.xgame.comm.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义计时器
 */
public final class MyTimer {
    /**
     * 单例对象
     */
    private static final MyTimer INSTANCE = new MyTimer();

    /**
     * 索引生成器
     */
    static private final AtomicInteger _nextIndexGen = new AtomicInteger(0);

    /**
     * 线程池
     */
    private final ScheduledExecutorService[] _esArray;

    /**
     * 私有化类默认构造器
     */
    private MyTimer() {
        int threadNum = Runtime.getRuntime().availableProcessors() * 2;
        _esArray = new ScheduledExecutorService[threadNum];

        for (int i = 0; i < threadNum; i++) {
            // 线程名称
            final String threadName = "xgame_MyTimer[" + i + "]";

            _esArray[i] = new ScheduledThreadPoolExecutor(1, (r) -> {
                Thread t = new Thread(r);
                t.setName(threadName);
                return t;
            });
        }
    }

    /**
     * 获取单例对象
     *
     * @return 单例对象
     */
    public static MyTimer getInstance() {
        return INSTANCE;
    }

    /**
     * 执行单次任务
     *
     * @param task  任务
     * @param delay 延迟时间
     * @param tu    时间单位
     * @param <T>   模板参数 -- 返回值类型
     * @return 定时任务预期
     */
    public <T> ScheduledFuture<T> schedule(Callable<T> task, int delay, TimeUnit tu) {
        if (null == task) {
            return null;
        }

        int bindId = _nextIndexGen.getAndIncrement();
        return schedule(bindId, task, delay, tu);
    }

    /**
     * 执行单次任务
     *
     * @param bindId 绑定 Id, 让定时任务在相同线程里被执行
     * @param task   任务
     * @param delay  延迟时间
     * @param tu     时间单位
     * @param <T>    模板参数 -- 返回值类型
     * @return 定时任务预期
     */
    public <T> ScheduledFuture<T> schedule(int bindId, Callable<T> task, int delay, TimeUnit tu) {
        if (null == task) {
            return null;
        }

        int index = Math.abs(bindId % _esArray.length);

        return _esArray[index].schedule(
            new SafeCaller<>(task), delay, tu
        );
    }

    /**
     * 执行定时任务
     *
     * @param task         任务
     * @param initialDelay 第一次执行的延迟时间
     * @param delay        第一次之后每次执行间隔时间
     * @param tu           时间单位
     * @return 定时任务预期
     */
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, int initialDelay, int delay, TimeUnit tu) {
        if (null == task) {
            return null;
        }

        int bindId = _nextIndexGen.getAndIncrement();
        return scheduleWithFixedDelay(bindId, task, initialDelay, delay, tu);
    }

    /**
     * 执行定时任务
     *
     * @param bindId       绑定 Id, 让定时任务在相同线程里被执行
     * @param task         任务
     * @param initialDelay 第一次执行的延迟时间
     * @param delay        第一次之后每次执行间隔时间
     * @param tu           时间单位
     * @return 定时任务预期
     */
    public ScheduledFuture<?> scheduleWithFixedDelay(int bindId, Runnable task, int initialDelay, int delay, TimeUnit tu) {
        if (null == task) {
            return null;
        }

        int index = Math.abs(bindId % _esArray.length);

        return _esArray[index].scheduleWithFixedDelay(
            new SafeRunner(task), initialDelay, delay, tu
        );
    }
}
