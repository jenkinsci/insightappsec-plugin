package com.rapid7.insightappsec.intg.jenkins;

import com.rapid7.insightappsec.intg.jenkins.api.app.App;
import com.rapid7.insightappsec.intg.jenkins.api.app.AppApi;
import com.rapid7.insightappsec.intg.jenkins.api.scanconfig.ScanConfig;
import com.rapid7.insightappsec.intg.jenkins.api.search.SearchApi;
import com.rapid7.insightappsec.intg.jenkins.api.search.SearchRequest;
import com.rapid7.insightappsec.intg.jenkins.credentials.InsightAPICredentials;
import com.rapid7.insightappsec.intg.jenkins.credentials.InsightAPICredentialsImpl;
import com.rapid7.insightappsec.intg.jenkins.credentials.InsightCredentialsHelper;
import com.rapid7.insightappsec.intg.jenkins.exception.APIException;
import com.rapid7.insightappsec.intg.jenkins.mock.MockHttpResponse;
import hudson.model.Item;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.rapid7.insightappsec.intg.jenkins.api.app.AppModels.aCompleteApp;
import static com.rapid7.insightappsec.intg.jenkins.api.scanconfig.ScanConfigModels.aCompleteScanConfig;
import static com.rapid7.insightappsec.intg.jenkins.api.search.SearchRequestModels.aScanConfigSearchRequest;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Jenkins.class) // must use powermock to mock Jenkins final method calls
public class DescriptorHelperTest {

    @Mock
    private AppApi appApi;

    @Mock
    private SearchApi searchApi;

    @Mock
    private Jenkins context;

    @Mock
    private InsightCredentialsHelper credentialsHelper;

    @Mock
    private WaitTimeParser waitTimeParser;

    @InjectMocks
    private DescriptorHelper descriptorHelper;

    private static final String CREDENTIALS_ID = "some_arbitrary_id";
    private static final String REGION = "US";
    private static final String EMPTY_STRING_VALUE = "";

    // REGION

    @Test
    public void getRegionItems() {
        // given
        String expectedFirstItemName = "- Select region -";

        // when
        ListBoxModel items = descriptorHelper.getRegionItems();

        // then
        assertEquals(items.size(), Region.values().length + 1);

        assertEquals(expectedFirstItemName, items.get(0).name);
        assertEquals(EMPTY_STRING_VALUE, items.get(0).value);
    }

    // CREDENTIALS

    @Test
    public void getInsightCredentialsIdItems_nullContextOrInvalidPermissions() {
        // given
        PowerMockito.when(context.hasPermission(Item.CONFIGURE)).thenReturn(false);

        // when
        ListBoxModel items0 = descriptorHelper.getInsightCredentialsIdItems(context);
        ListBoxModel items1 = descriptorHelper.getInsightCredentialsIdItems(null);

        // then
        assertEquals(items0.size(), 0);
        assertEquals(items1.size(), 0);
    }

    @Test
    public void getInsightCredentialsIdItems() {
        // given
        String expectedFirstItemName = "- Select API key -";

        PowerMockito.when(context.hasPermission(Item.CONFIGURE)).thenReturn(true);

        List<InsightAPICredentials> credentials = Collections.singletonList(mockCredentials());
        given(credentialsHelper.lookupAllInsightCredentials(context)).willReturn(credentials);

        // when
        ListBoxModel items = descriptorHelper.getInsightCredentialsIdItems(context);

        // then
        assertEquals(credentials.size() + 1, items.size());

        assertEquals(expectedFirstItemName, items.get(0).name);
        assertEquals(EMPTY_STRING_VALUE, items.get(0).value);
    }

    // APPS

    @Test
    public void getAppIdItems_regionOrCredentialsEmpty() {
        // given
        String expectedName = "- First select region and API key -";

        // when
        ListBoxModel items0 = descriptorHelper.getAppIdItems("", CREDENTIALS_ID);
        ListBoxModel items1 = descriptorHelper.getAppIdItems(REGION, "");

        // then
        assertEquals(1, items0.size());
        assertEquals(1, items1.size());

        assertEquals(expectedName, items0.get(0).name);
        assertEquals(expectedName, items1.get(0).name);

        assertEquals(EMPTY_STRING_VALUE, items0.get(0).value);
        assertEquals(EMPTY_STRING_VALUE, items1.get(0).value);
    }

    @Test
    public void getAppIdItems_APIException_401() {
        // given
        String expectedFirstItemName = "- Invalid API key: Forbidden -";

        mockGetCredentials();

        given(appApi.getApps()).willThrow(new APIException("message", MockHttpResponse.create(401)));

        // when
        ListBoxModel items = descriptorHelper.getAppIdItems(REGION, CREDENTIALS_ID);

        // then
        assertEquals(1, items.size());

        assertEquals(expectedFirstItemName, items.get(0).name);
        assertEquals(EMPTY_STRING_VALUE, items.get(0).value);
    }

    @Test
    public void getAppIdItems_Error() {
        // given
        String expectedFirstItemName = "- Error loading apps -";

        mockGetCredentials();

        given(appApi.getApps()).willThrow(new RuntimeException());

        // when
        ListBoxModel items = descriptorHelper.getAppIdItems(REGION, CREDENTIALS_ID);

        // then
        assertEquals(1, items.size());

        assertEquals(expectedFirstItemName, items.get(0).name);
        assertEquals(EMPTY_STRING_VALUE, items.get(0).value);
    }

    @Test
    public void getAppIdItems() {
        // given
        String expectedFirstItemName = "- Select app -";

        mockGetCredentials();

        List<App> apps = Stream.generate(() -> aCompleteApp().build()).limit(10)
                                                                      .sorted(Comparator.comparing(App::getName))
                                                                      .collect(Collectors.toList());
        given(appApi.getApps()).willReturn(apps);

        // when
        ListBoxModel items = descriptorHelper.getAppIdItems(REGION, CREDENTIALS_ID);

        // then
        assertEquals(apps.size() + 1, items.size());

        assertEquals(expectedFirstItemName, items.get(0).name);
        assertEquals(EMPTY_STRING_VALUE, items.get(0).value);

        assertEquals(String.format("%s (%s)", apps.get(0).getName(), apps.get(0).getId()), items.get(1).name);
        assertEquals(apps.get(0).getId(), apps.get(0).getId(), items.get(1).value);
    }

    // SCAN CONFIGS

    @Test
    public void getScanConfigIdItems_regionOrCredentialsOrAppIdEmpty() {
        // given
        String appId = UUID.randomUUID().toString();
        String expectedName = "- First select app -";

        // when
        ListBoxModel items0 = descriptorHelper.getScanConfigIdItems("", CREDENTIALS_ID, appId);
        ListBoxModel items1 = descriptorHelper.getScanConfigIdItems(REGION, CREDENTIALS_ID, "");
        ListBoxModel items2 = descriptorHelper.getScanConfigIdItems(REGION, "", CREDENTIALS_ID);

        // then
        assertEquals(1, items0.size());
        assertEquals(1, items1.size());
        assertEquals(1, items2.size());

        assertEquals(expectedName, items0.get(0).name);
        assertEquals(expectedName, items1.get(0).name);
        assertEquals(expectedName, items2.get(0).name);

        assertEquals(EMPTY_STRING_VALUE, items0.get(0).value);
        assertEquals(EMPTY_STRING_VALUE, items1.get(0).value);
        assertEquals(EMPTY_STRING_VALUE, items2.get(0).value);
    }

    @Test
    public void getScanConfigIdItems_APIException_401() {
        // given
        String appId = UUID.randomUUID().toString();
        String expectedFirstItemName = "- Invalid API key: Forbidden -";

        mockGetCredentials();

        given(searchApi.searchAll(any(SearchRequest.class), any())).willThrow(new APIException("message", MockHttpResponse.create(401)));

        // when
        ListBoxModel items = descriptorHelper.getScanConfigIdItems(REGION, CREDENTIALS_ID, appId);

        // then
        assertEquals(1, items.size());

        assertEquals(expectedFirstItemName, items.get(0).name);
        assertEquals(EMPTY_STRING_VALUE, items.get(0).value);
    }

    @Test
    public void getScanConfigIdItems_Error() {
        // given
        String appId = UUID.randomUUID().toString();

        String expectedFirstItemName = "- Error loading scan configs -";

        mockGetCredentials();

        given(searchApi.searchAll(any(SearchRequest.class), any())).willThrow(new RuntimeException());

        // when
        ListBoxModel items = descriptorHelper.getScanConfigIdItems(REGION, CREDENTIALS_ID, appId);

        // then
        assertEquals(1, items.size());

        assertEquals(expectedFirstItemName, items.get(0).name);
        assertEquals(EMPTY_STRING_VALUE, items.get(0).value);
    }

    @Test
    public void getScanConfigIdItems() {
        // given
        String appId = UUID.randomUUID().toString();

        String expectedFirstItemName = "- Select scan config -";

        mockGetCredentials();

        SearchRequest searchRequest = aScanConfigSearchRequest().query(String.format("scanconfig.app.id='%s'", appId)).build();

        List<ScanConfig> scanConfigs = Stream.generate(() -> aCompleteScanConfig().build()).limit(10)
                                                                                  .sorted(Comparator.comparing(ScanConfig::getName))
                                                                                  .collect(Collectors.toList());

        given(searchApi.searchAll(searchRequest, ScanConfig.class)).willReturn(scanConfigs);

        // when
        ListBoxModel items = descriptorHelper.getScanConfigIdItems(REGION, CREDENTIALS_ID, appId);

        // then
        assertEquals(scanConfigs.size() + 1, items.size());

        assertEquals(expectedFirstItemName, items.get(0).name);
        assertEquals(EMPTY_STRING_VALUE, items.get(0).value);

        assertEquals(String.format("%s (%s)", scanConfigs.get(0).getName(), scanConfigs.get(0).getId()), items.get(1).name);
        assertEquals(scanConfigs.get(0).getId(), scanConfigs.get(0).getId(), items.get(1).value);
    }

    // BUILD ADVANCE INDICATORS

    @Test
    public void getBuildAdvanceIndicatorItems() {
        // when
        ListBoxModel items = descriptorHelper.getBuildAdvanceIndicatorItems();

        // then
        assertEquals(items.size(), Region.values().length);
    }

    // VULNERABILITY QUERY

    @Test
    public void doCheckVulnerabilityQuery() {
        // when
        FormValidation validation = descriptorHelper.doCheckVulnerabilityQuery();

        // then
        assertEquals("Ignored unless 'Vulnerability results query has returned no vulnerabilities' has been selected",
                     validation.getMessage());
    }

    // MAX SCAN START WAIT TIME

    @Test
    public void doCheckMaxScanStartWaitTime_invalid() {
        // given
        String invalid = "invalid";
        given(waitTimeParser.parseWaitTimeString(invalid)).willThrow(new IllegalArgumentException());

        // when
        FormValidation validation = descriptorHelper.doCheckMaxScanStartWaitTime(invalid);

        // then
        assertEquals("Wait time provided is invalid. Example format: 0d 0h 30m",
                     validation.getMessage());
    }

    @Test
    public void doCheckMaxScanStartWaitTime_valid() {
        // given
        String invalid = "valid";
        given(waitTimeParser.parseWaitTimeString(invalid)).willReturn(1L);

        // when
        FormValidation validation = descriptorHelper.doCheckMaxScanStartWaitTime(invalid);

        // then
        assertEquals("Ignored if 'Scan has been submitted' has been selected",
                     validation.getMessage());
    }

    // MAX SCAN RUNTIME

    @Test
    public void doCheckMaxScanRuntime_invalid() {
        // given
        String invalid = "invalid";
        given(waitTimeParser.parseWaitTimeString(invalid)).willThrow(new IllegalArgumentException());

        // when
        FormValidation validation = descriptorHelper.doCheckMaxScanRuntime(invalid);

        // then
        assertEquals("Wait time provided is invalid. Example format: 0d 0h 30m",
                     validation.getMessage());
    }

    @Test
    public void doCheckMaxScanRuntime_valid() {
        // given
        String invalid = "valid";
        given(waitTimeParser.parseWaitTimeString(invalid)).willReturn(1L);

        // when
        FormValidation validation = descriptorHelper.doCheckMaxScanRuntime(invalid);

        // then
        assertEquals("Ignored if 'Scan has been submitted' or 'Scan has been started' has been selected",
                     validation.getMessage());
    }

    // TEST HELPERS

    private void mockGetCredentials() {
        given(credentialsHelper.lookupInsightCredentialsById(CREDENTIALS_ID)).willReturn(mockCredentials());
    }

    private InsightAPICredentials mockCredentials() {
        return new InsightAPICredentialsImpl(CREDENTIALS_ID, "some_api_key");
    }
}