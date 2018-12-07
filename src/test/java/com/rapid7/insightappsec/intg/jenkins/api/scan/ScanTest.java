package com.rapid7.insightappsec.intg.jenkins.api.scan;

import org.junit.Test;

import java.io.IOException;

import static com.rapid7.insightappsec.intg.jenkins.MappingConfiguration.OBJECT_MAPPER_INSTANCE;
import static junit.framework.TestCase.assertEquals;

public class ScanTest {

    @Test
    public void scanStatusFromString_valid() throws IOException {
        // given
        String value = "\"PENDING\"";

        // when
        Scan.ScanStatus status = OBJECT_MAPPER_INSTANCE.readValue(value, Scan.ScanStatus.class);

        // then
        assertEquals(Scan.ScanStatus.PENDING, status);
    }

    @Test
    public void scanStatusFromString_invalid() {
        // given
        String value = "\"SOMETHING\"";

        // when
        Scan.ScanStatus status = Scan.ScanStatus.fromString(value);

        // then
        assertEquals(Scan.ScanStatus.UNKNOWN, status);
    }

}