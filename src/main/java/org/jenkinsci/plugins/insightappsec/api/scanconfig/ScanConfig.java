package org.jenkinsci.plugins.insightappsec.api.scanconfig;

import org.jenkinsci.plugins.insightappsec.api.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ScanConfig extends Identifiable {

    private String name;

    @Builder
    public ScanConfig(String id,
                      String name) {
        super(id);
        this.name = name;
    }
}
