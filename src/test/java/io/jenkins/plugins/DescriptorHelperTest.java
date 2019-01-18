package io.jenkins.plugins;

import io.jenkins.plugins.api.APIFactory;
import io.jenkins.plugins.api.app.App;
import io.jenkins.plugins.api.app.AppApi;
import io.jenkins.plugins.api.scanconfig.ScanConfig;
import io.jenkins.plugins.api.search.SearchApi;
import io.jenkins.plugins.api.search.SearchRequest;
import io.jenkins.plugins.credentials.InsightAPICredentials;
import io.jenkins.plugins.credentials.InsightAPICredentialsImpl;
import io.jenkins.plugins.credentials.InsightCredentialsHelper;
import io.jenkins.plugins.exception.APIException;
import io.jenkins.plugins.mock.MockHttpResponse;
import hudson.model.Item;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.junit.Assert;
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

import static io.jenkins.plugins.api.app.AppModels.aCompleteApp;
import static io.jenkins.plugins.api.scanconfig.ScanConfigModels.aCompleteScanConfig;
import static io.jenkins.plugins.api.search.SearchRequestModels.aScanConfigSearchRequest;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Jenkins.class) // must use powermock to mock Jenkins final method calls
public class DescriptorHelperTest {

    @Mock
    private APIFactory apiFactory;

    @Mock
    private AppApi appApi;

    @Mock
    private SearchApi searchApi;

    @Mock
    private Jenkins context;

    @Mock
    private InsightCredentialsHelper credentialsHelper;

    @Mock
    private DurationStringParser durationStringParser;

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
        mockRefreshAppApi();

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
        mockRefreshAppApi();

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
        mockRefreshAppApi();

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
        Assert.assertEquals(apps.get(0).getId(), apps.get(0).getId(), items.get(1).value);
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
        mockRefreshSearchApi();

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
        mockRefreshSearchApi();

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
        mockRefreshSearchApi();

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
        Assert.assertEquals(scanConfigs.get(0).getId(), scanConfigs.get(0).getId(), items.get(1).value);
    }

    // BUILD ADVANCE INDICATORS

    @Test
    public void getBuildAdvanceIndicatorItems() {
        // when
        ListBoxModel items = descriptorHelper.getBuildAdvanceIndicatorItems();

        // then
        assertEquals(items.size(), BuildAdvanceIndicator.values().length);
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

    // ENABLE SCAN RESULTS

    @Test
    public void doCheckEnableScanResults() {
        // when
        FormValidation validation = descriptorHelper.doCheckEnableScanResults();

        // then
        assertEquals("Ignored if 'Scan has been submitted' or 'Scan has been started' has been selected",
                validation.getMessage());
    }

    // MAX SCAN PENDING DURATION

    @Test
    public void doCheckMaxScanPendingDuration_invalid() {
        // given
        String invalid = "invalid";
        given(durationStringParser.parseDurationString(invalid)).willThrow(new IllegalArgumentException());

        // when
        FormValidation validation = descriptorHelper.doCheckMaxScanPendingDuration(invalid);

        // then
        assertEquals("Duration provided is invalid. Example format: 0d 0h 30m",
                     validation.getMessage());
    }

    @Test
    public void doCheckMaxScanPendingDuration_valid() {
        // given
        String invalid = "valid";
        given(durationStringParser.parseDurationString(invalid)).willReturn(1L);

        // when
        FormValidation validation = descriptorHelper.doCheckMaxScanPendingDuration(invalid);

        // then
        assertEquals("Ignored if 'Scan has been submitted' has been selected",
                     validation.getMessage());
    }

    // MAX SCAN EXECUTION DURATION

    @Test
    public void doCheckMaxScanExecutionDuration_invalid() {
        // given
        String invalid = "invalid";
        given(durationStringParser.parseDurationString(invalid)).willThrow(new IllegalArgumentException());

        // when
        FormValidation validation = descriptorHelper.doCheckMaxScanExecutionDuration(invalid);

        // then
        assertEquals("Duration provided is invalid. Example format: 0d 0h 30m",
                     validation.getMessage());
    }

    @Test
    public void doCheckMaxScanExecutionDuration_valid() {
        // given
        String valid = "valid";
        given(durationStringParser.parseDurationString(valid)).willReturn(1L);

        // when
        FormValidation validation = descriptorHelper.doCheckMaxScanExecutionDuration(valid);

        // then
        assertEquals("Ignored if 'Scan has been submitted' or 'Scan has been started' has been selected",
                     validation.getMessage());
    }

    @Test
    public void doCheckRequiredField_valid() {
        // given
        String field = "some text";

        // when
        FormValidation validation = descriptorHelper.doCheckRequiredField(field);

        // then
        assertEquals(null, validation.getMessage());
    }

    @Test
    public void doCheckRequiredField_invalid() {
        // given
        String empty = "";
        String nulled = null;

        // when
        FormValidation validationEmpty = descriptorHelper.doCheckRequiredField(empty);
        FormValidation validationNull = descriptorHelper.doCheckRequiredField(nulled);

        // then
        assertEquals("Required", validationEmpty.getMessage());
        assertEquals("Required", validationNull.getMessage());
    }

    // TEST HELPERS

    private void mockGetCredentials() {
        given(credentialsHelper.lookupInsightCredentialsById(CREDENTIALS_ID)).willReturn(mockCredentials());
    }

    private InsightAPICredentials mockCredentials() {
        return new InsightAPICredentialsImpl(CREDENTIALS_ID, "some_api_key");
    }

    private void mockRefreshAppApi() {
        given(apiFactory.newAppApi(REGION, CREDENTIALS_ID)).willReturn(appApi);
    }

    private void mockRefreshSearchApi() {
        given(apiFactory.newSearchApi(REGION, CREDENTIALS_ID)).willReturn(searchApi);
    }
}