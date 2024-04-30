package com.migration.dto;

import com.migration.enums.MigrationObjectStatus;
import lombok.Data;

@Data
public class MigrationStatistics {
    private int created;
    private int captured;
    private int warnings;
    private int failed;
    private int done;
    private int total;
    private String sourcePath;

    public void step(MigrationObjectStatus from, MigrationObjectStatus to, int value, String sourcePath){
        if (from == null) {
            total+=value;
            created+=value;
        } else {
            switch (from) {
                case NEW -> created-=value;
                case CAPTURED -> captured-=value;
                case WARNING -> warnings-=value;
                case FAILED -> failed-=value;
                case DONE -> done-=value;
            }

            switch (to) {
                case NEW -> created+=value;
                case CAPTURED -> captured+=value;
                case WARNING -> warnings+=value;
                case FAILED -> failed+=value;
                case DONE -> done+=value;
            }

            if (sourcePath != null) {
                this.sourcePath = sourcePath;
            }
        }
    }

    public int getProcessed(){
        return warnings+failed+done;
    }
}
