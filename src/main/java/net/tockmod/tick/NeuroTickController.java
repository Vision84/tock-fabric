package net.tockmod.tick;

import net.minecraft.server.MinecraftServer;
import net.tockmod.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

public class NeuroTickController {
    private static final Logger LOGGER = LoggerFactory.getLogger("Tock/NeuroTick");
    private final AtomicLong lastTickTime = new AtomicLong(0);
    private final AtomicLong currentTickStart = new AtomicLong(0);
    private final RollingAverage tickTimeAverage = new RollingAverage(100);
    private boolean isOverBudget = false;

    public void onServerTickStart(MinecraftServer server) {
        if (!ModConfig.getInstance().neurotickEnabled) {
            return;
        }

        currentTickStart.set(System.nanoTime());
        
        // If we're over budget, reduce processing
        if (isOverBudget) {
            LOGGER.warn("Server is over tick budget (avg: {:.2f}ms), reducing processing", getAverageTickTime());
            // TODO: Implement tick reduction logic
        }
    }

    public void onServerTickEnd(MinecraftServer server) {
        if (!ModConfig.getInstance().neurotickEnabled) {
            return;
        }

        long tickTime = (System.nanoTime() - currentTickStart.get()) / 1_000_000; // Convert to milliseconds
        tickTimeAverage.add(tickTime);
        lastTickTime.set(tickTime);

        // Check if we're over budget
        isOverBudget = tickTime > ModConfig.getInstance().maxTickTime;
        
        if (isOverBudget) {
            LOGGER.warn("Tick took {}ms (budget: {}ms, avg: {:.2f}ms)", 
                tickTime, 
                ModConfig.getInstance().maxTickTime,
                getAverageTickTime());
        } else if (tickTime > ModConfig.getInstance().maxTickTime * 0.8) {
            LOGGER.info("Tick approaching budget limit: {}ms (budget: {}ms)", 
                tickTime, 
                ModConfig.getInstance().maxTickTime);
        }

        // Log tick timing summary every 100 ticks
        if (tickTimeAverage.getCount() % 100 == 0) {
            LOGGER.info("=== Tick Timing Summary ===");
            LOGGER.info("Last tick: {}ms", tickTime);
            LOGGER.info("Average tick: {:.2f}ms", getAverageTickTime());
            LOGGER.info("Min tick: {:.2f}ms", tickTimeAverage.getMin());
            LOGGER.info("Max tick: {:.2f}ms", tickTimeAverage.getMax());
            LOGGER.info("Over budget: {}", isOverBudget);
        }
    }

    public long getLastTickTime() {
        return lastTickTime.get();
    }

    public double getAverageTickTime() {
        return tickTimeAverage.getAverage();
    }

    public boolean isOverBudget() {
        return isOverBudget;
    }

    private static class RollingAverage {
        private final int size;
        private final double[] values;
        private int index = 0;
        private double sum = 0;
        private int count = 0;
        private double min = Double.MAX_VALUE;
        private double max = Double.MIN_VALUE;

        public RollingAverage(int size) {
            this.size = size;
            this.values = new double[size];
        }

        public void add(double value) {
            sum -= values[index];
            values[index] = value;
            sum += value;
            index = (index + 1) % size;
            count = Math.min(count + 1, size);
            
            // Update min/max
            min = Math.min(min, value);
            max = Math.max(max, value);
        }

        public double getAverage() {
            return count > 0 ? sum / count : 0;
        }

        public int getCount() {
            return count;
        }

        public double getMin() {
            return min;
        }

        public double getMax() {
            return max;
        }
    }
} 