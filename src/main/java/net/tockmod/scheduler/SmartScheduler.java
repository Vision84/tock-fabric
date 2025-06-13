package net.tockmod.scheduler;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.tockmod.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.time.Instant;

public class SmartScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger("Tock/Scheduler");
    private final Queue<ScheduledTask> taskQueue = new PriorityQueue<>(Comparator.comparingInt(ScheduledTask::getPriority));
    private final Map<BlockPos, ScheduledTask> pendingTasks = new ConcurrentHashMap<>();
    private final AtomicLong lastTaskId = new AtomicLong(0);
    private final AtomicLong lastSummaryTime = new AtomicLong(0);
    private static final long SUMMARY_INTERVAL = 5000; // 5 seconds

    public void onServerTickStart(MinecraftServer server) {
        if (!ModConfig.getInstance().schedulerEnabled) {
            return;
        }

        // Process high priority tasks
        int processedTasks = processTasks(server);
        if (processedTasks > 0) {
            LOGGER.info("Processed {} tasks in this tick ({} remaining)", processedTasks, taskQueue.size());
        }

        // Log task summary every 5 seconds
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSummaryTime.get() > SUMMARY_INTERVAL) {
            logTaskSummary();
            lastSummaryTime.set(currentTime);
        }
    }

    public void onServerTickEnd(MinecraftServer server) {
        // No-op for now
    }

    public void scheduleTask(ServerWorld world, BlockPos pos, Runnable task, int priority) {
        if (!ModConfig.getInstance().schedulerEnabled) {
            LOGGER.debug("Scheduler disabled, executing task immediately at {}", pos);
            task.run();
            return;
        }

        ScheduledTask scheduledTask = new ScheduledTask(
            lastTaskId.incrementAndGet(),
            world,
            pos,
            task,
            priority
        );

        // Check for redundant tasks
        if (ModConfig.getInstance().enablePreemptiveCancellation) {
            ScheduledTask existingTask = pendingTasks.get(pos);
            if (existingTask != null && existingTask.priority <= priority) {
                LOGGER.warn("Cancelling redundant task at {} (priority: {}, existing: {})", 
                    pos, 
                    priority, 
                    existingTask.priority);
                return;
            }
        }

        pendingTasks.put(pos, scheduledTask);
        taskQueue.offer(scheduledTask);
        LOGGER.info("Scheduled task at {} with priority {} (queue size: {})", 
            pos, 
            priority, 
            taskQueue.size());
    }

    private int processTasks(MinecraftServer server) {
        int processedTasks = 0;
        while (!taskQueue.isEmpty() && processedTasks < 1000) { // Limit tasks per tick
            ScheduledTask task = taskQueue.poll();
            if (task != null) {
                try {
                    LOGGER.debug("Executing task at {} with priority {} (world: {})", 
                        task.pos, 
                        task.priority,
                        task.world.getRegistryKey().getValue());
                    task.run();
                    pendingTasks.remove(task.pos);
                    processedTasks++;
                } catch (Exception e) {
                    LOGGER.error("Task at {} failed: {}", task.pos, e.getMessage(), e);
                }
            }
        }
        return processedTasks;
    }

    private void logTaskSummary() {
        LOGGER.info("=== Task Summary ===");
        LOGGER.info("Total pending tasks: {}", taskQueue.size());
        LOGGER.info("Tasks by world:");
        taskQueue.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                task -> task.world.getRegistryKey().getValue(),
                java.util.stream.Collectors.counting()))
            .forEach((world, count) -> 
                LOGGER.info("  {}: {} tasks", world, count));
        
        LOGGER.info("Tasks by priority:");
        taskQueue.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                task -> task.priority,
                java.util.stream.Collectors.counting()))
            .forEach((priority, count) -> 
                LOGGER.info("  Priority {}: {} tasks", priority, count));
    }

    private static class ScheduledTask implements Runnable {
        private final long id;
        private final ServerWorld world;
        private final BlockPos pos;
        private final Runnable task;
        private final int priority;
        private final Instant scheduledTime;

        public ScheduledTask(long id, ServerWorld world, BlockPos pos, Runnable task, int priority) {
            this.id = id;
            this.world = world;
            this.pos = pos;
            this.task = task;
            this.priority = priority;
            this.scheduledTime = Instant.now();
        }

        @Override
        public void run() {
            task.run();
        }

        public int getPriority() {
            return priority;
        }
    }
} 