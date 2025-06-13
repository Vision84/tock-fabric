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
            LOGGER.debug("Server is over tick budget, reducing processing");
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
            LOGGER.warn("Tick took {}ms (budget: {}ms)", tickTime, ModConfig.getInstance().maxTickTime);
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

        public RollingAverage(int size) {
            this.size = size;
            this.values = new double[size];
        }

        public void add(double value) {
            sum -= values[index];
            values[index] = value;
            sum += value;
            index = (index + 1) % size;
        }

        public double getAverage() {
            return sum / size;
        }
    }
} 