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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class SmartScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger("Tock/Scheduler");
    private final Queue<ScheduledTask> taskQueue = new PriorityQueue<>(Comparator.comparingInt(ScheduledTask::getPriority));
    private final Map<BlockPos, ScheduledTask> pendingTasks = new ConcurrentHashMap<>();
    private final AtomicLong lastTaskId = new AtomicLong(0);

    public void onServerTickStart(MinecraftServer server) {
        if (!ModConfig.getInstance().schedulerEnabled) {
            return;
        }

        // Process high priority tasks
        processTasks(server);
    }

    public void onServerTickEnd(MinecraftServer server) {
        // No-op for now
    }

    public void scheduleTask(ServerWorld world, BlockPos pos, Runnable task, int priority) {
        if (!ModConfig.getInstance().schedulerEnabled) {
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
                LOGGER.debug("Cancelling redundant task at {}", pos);
                return;
            }
        }

        pendingTasks.put(pos, scheduledTask);
        taskQueue.offer(scheduledTask);
    }

    private void processTasks(MinecraftServer server) {
        int processedTasks = 0;
        while (!taskQueue.isEmpty() && processedTasks < 1000) { // Limit tasks per tick
            ScheduledTask task = taskQueue.poll();
            if (task != null) {
                task.run();
                pendingTasks.remove(task.pos);
                processedTasks++;
            }
        }
    }

    private static class ScheduledTask implements Runnable {
        private final long id;
        private final ServerWorld world;
        private final BlockPos pos;
        private final Runnable task;
        private final int priority;

        public ScheduledTask(long id, ServerWorld world, BlockPos pos, Runnable task, int priority) {
            this.id = id;
            this.world = world;
            this.pos = pos;
            this.task = task;
            this.priority = priority;
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