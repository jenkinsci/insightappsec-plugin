package com.rapid7.insightappsec.intg.jenkins.api.scan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScanAction {

    private Action action;

    public enum Action {

        STOP,
        CANCEL

    }

}
