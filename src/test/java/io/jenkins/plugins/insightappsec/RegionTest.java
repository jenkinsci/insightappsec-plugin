package io.jenkins.plugins.insightappsec;

import io.jenkins.plugins.insightappsec.exception.UnrecognizedRegionException;
import org.junit.Assert;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class RegionTest {

    @Test
    public void fromString_legit() {
        Region us = Region.fromString("us");

        assertEquals(us, Region.US);
        assertEquals(us.getAPIHost(), "us.api.insight.rapid7.com");
    }

    @Test
    public void fromString_notLegit() {
        // when
        UnrecognizedRegionException thrown = Assert.assertThrows(UnrecognizedRegionException.class, () ->
            Region.fromString("bogus")
        );

        // then
        Assert.assertTrue(thrown.getMessage().contains("The region provided [bogus] is not recognized"));
    }

}