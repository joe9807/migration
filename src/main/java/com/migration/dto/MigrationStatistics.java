package com.migration.dto;

import com.migration.enums.MigrationObjectStatus;
import lombok.Data;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Date;

@Data
public class MigrationStatistics {
    private int created;
    private int captured;
    private int warnings;
    private int failed;
    private int done;
    private int total;
    private String logs;
    private boolean changed;

    public String step(MigrationObjectStatus from, MigrationObjectStatus to, int value, String executorValue, String sourcePath) {
        if (from == null) {
            total += value;
            created += value;
        } else {
            switch (from) {
                case NEW -> created -= value;
                case CAPTURED -> captured -= value;
                case WARNING -> warnings -= value;
                case FAILED -> failed -= value;
                case DONE -> done -= value;
            }

            switch (to) {
                case NEW -> created += value;
                case CAPTURED -> captured += value;
                case WARNING -> warnings += value;
                case FAILED -> failed += value;
                case DONE -> done += value;
            }
        }

        changed = true;

        if (sourcePath != null){
            String result = String.format("%-20s :: %-25s :: %6s :: %-15s :: %s"
                    , DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:SS")
                    , Thread.currentThread().getName()
                    , executorValue
                    , (warnings + failed + done) + "/" + total,
                    sourcePath);
            logs = result;
            return result;
        } else {
            return null;
        }
    }
}
