package co.aikar.timings;

import static co.aikar.timings.TimingsManager.*;

@Deprecated // PulseSpigot
public class FullServerTickHandler extends TimingHandler {
    static final TimingIdentifier IDENTITY = new TimingIdentifier("Minecraft", "Full Server Tick", null, false);
    final TimingData minuteData;
    double avgFreeMemory = -1D;
    double avgUsedMemory = -1D;
    FullServerTickHandler() {
        super(IDENTITY);
        minuteData = new TimingData(id);

        TIMING_MAP.put(IDENTITY, this);
    }

    @Override
    public void startTiming() {
        if (TimingsManager.needsFullReset) {
            TimingsManager.resetTimings();
        } else if (TimingsManager.needsRecheckEnabled) {
            TimingsManager.recheckEnabled();
        }
        super.startTiming();
    }

    @Override
    public void stopTiming() {
        super.stopTiming();
        if (!enabled) {
            return;
        }
        if (TimingHistory.timedTicks % 20 == 0) {
            final Runtime runtime = Runtime.getRuntime();
            double usedMemory = runtime.totalMemory() - runtime.freeMemory();
            double freeMemory = runtime.maxMemory() - usedMemory;
            if (this.avgFreeMemory == -1) {
                this.avgFreeMemory = freeMemory;
            } else {
                this.avgFreeMemory = (this.avgFreeMemory * (59 / 60D)) + (freeMemory * (1 / 60D));
            }

            if (this.avgUsedMemory == -1) {
                this.avgUsedMemory = usedMemory;
            } else {
                this.avgUsedMemory = (this.avgUsedMemory * (59 / 60D)) + (usedMemory * (1 / 60D));
            }
        }

        long start = System.nanoTime();
        TimingsManager.tick();
        long diff = System.nanoTime() - start;
        CURRENT = TIMINGS_TICK;
        TIMINGS_TICK.addDiff(diff);
        // addDiff for TIMINGS_TICK incremented this, bring it back down to 1 per tick.
        record.curTickCount--;
        minuteData.curTickTotal = record.curTickTotal;
        minuteData.curTickCount = 1;
        boolean violated = isViolated();
        minuteData.processTick(violated);
        TIMINGS_TICK.processTick(violated);
        processTick(violated);


        if (TimingHistory.timedTicks % 1200 == 0) {
            MINUTE_REPORTS.add(new TimingHistory.MinuteReport());
            TimingHistory.resetTicks(false);
            minuteData.reset();
        }
        if (TimingHistory.timedTicks % Timings.getHistoryInterval() == 0) {
            TimingsManager.HISTORY.add(new TimingHistory());
            TimingsManager.resetTimings();
        }
    }

    boolean isViolated() {
        return record.curTickTotal > 50000000;
    }
}
