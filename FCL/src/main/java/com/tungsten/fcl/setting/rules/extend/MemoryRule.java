package com.tungsten.fcl.setting.rules.extend;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.tungsten.fcl.setting.VersionSetting;
import com.tungsten.fcl.util.RuleCheckState;
import com.tungsten.fclauncher.utils.FCLPath;
import com.tungsten.fclcore.util.platform.MemoryUtils;

import java.util.Optional;

public class MemoryRule extends RuleBase {
    @SerializedName("minMemory")
    private final int minMemory;
    private transient int totalMemory, setMemory;

    public MemoryRule() {
        super(null);
        this.minMemory = -1;
    }

    public MemoryRule(int minMemory, String tip) {
        super(tip);
        this.minMemory = minMemory;
    }

    public int getMinMemory() {
        return minMemory;
    }

    @Override
    public RuleCheckState setRule(@NonNull VersionSetting setting) {
        super.setRule(setting);

        return Optional.of(minMemory)
                .filter(memory -> canDetectRule() && setMemory < memory)
                .map(memory -> {
                    float availableMemory = totalMemory - 3.5f;
                    if(availableMemory > (memory / 1000f)) {
                        setting.setMaxMemory(memory);
                        return RuleCheckState.SUCCESS;
                    }
                    return RuleCheckState.FAIL;
                })
                .orElse(RuleCheckState.NO_CHANGE);
    }

    @Override
    public boolean canDetectRule() {
        return minMemory > 0;
    }

    @Override
    protected void initPlaceholders(@NonNull VersionSetting setting) {
        this.totalMemory = (int) Math.ceil(MemoryUtils.getTotalDeviceMemory(FCLPath.CONTEXT) / 1000.0);
        this.setMemory = setting.getMaxMemory();
    }
}
