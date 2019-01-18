package io.jenkins.plugins.api.scan;

import io.jenkins.plugins.MappingConfiguration;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.TestCase.assertEquals;

public class ScanTest {

    @Test
    public void scanStatusFromString_valid() throws IOException {
        // given
        String value = "\"PENDING\"";

        // when
        Scan.ScanStatus status = MappingConfiguration.OBJECT_MAPPER_INSTANCE.readValue(value, Scan.ScanStatus.class);

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