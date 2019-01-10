package com.rapid7.insightappsec.intg.jenkins;

import com.rapid7.insightappsec.intg.jenkins.exception.UnrecognizedRegionException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static junit.framework.TestCase.assertEquals;

public class RegionTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void fromString_legit() {
        Region us = Region.fromString("us");

        assertEquals(us, Region.US);
        assertEquals(us.getAPIHost(), "us.api.insight.rapid7.com");
    }

    @Test
    public void fromString_notLegit() {
        exception.expect(UnrecognizedRegionException.class);
        exception.expectMessage("The region provided [bogus] is not recognized");

        Region.fromString("bogus");
    }

}