package com.migration.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.migration.enums.MigrationObjectStatus;
import com.migration.utils.Utils;
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

    @JsonIgnore
    private boolean changed;

    public synchronized String step(MigrationObjectStatus from, MigrationObjectStatus to, int value, int cacheSize, String executorValue, String sourcePath) {
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
            String result = String.format(Utils.LOG_PATTERN
                    , DateFormatUtils.format(new Date(), Utils.DATE_FORMAT)
                    , Thread.currentThread().getName()
                    , cacheSize
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
