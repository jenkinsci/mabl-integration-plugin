package com.mabl.integration.jenkins;

import hudson.model.AbstractItem;
import hudson.model.Item;
import hudson.model.Job;
import hudson.security.Permission;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.Collection;

import static com.cloudbees.plugins.credentials.CredentialsProvider.USE_ITEM;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;
import static org.junit.Assert.assertEquals;

public class MablStepDescriptorTest {

    private MablStepBuilder.MablStepDescriptor mablStepDescriptor;

    @Before
    public void setup() {
        mablStepDescriptor = mock(MablStepBuilder.MablStepDescriptor.class);
        doNothing().when(mablStepDescriptor).load();
    }

    @Test
    public void testCheckRestApiKeyIds_Blank() {
        Item item = new AbstractItem(null, "testItem") {
            @Override
            public Collection<? extends Job> getAllJobs() {
                return null;
            }

            @Override
            public boolean hasPermission(Permission permission) {
                return EXTENDED_READ.equals(permission);
            }
        };
        when(mablStepDescriptor.doCheckRestApiKeyIds(item, null)).thenCallRealMethod();
        assertNotEquals(FormValidation.ok(), mablStepDescriptor.doCheckRestApiKeyIds(item, null));
        when(mablStepDescriptor.doCheckRestApiKeyIds(item, "")).thenCallRealMethod();
        assertNotEquals(FormValidation.ok(), mablStepDescriptor.doCheckRestApiKeyIds(item, ""));
    }

    @Test
    public void testCheckRestApiKeyIds_ExtendedRead() {
        Item item = new AbstractItem(null, "testItem") {
            @Override
            public Collection<? extends Job> getAllJobs() {
                return null;
            }

            @Override
            public boolean hasPermission(Permission permission) {
                return EXTENDED_READ.equals(permission);
            }
        };
        when(mablStepDescriptor.doCheckRestApiKeyIds(item, "an-api-key")).thenCallRealMethod();
        assertEquals(FormValidation.ok(), mablStepDescriptor.doCheckRestApiKeyIds(item, "an-api-key"));
    }

    @Test
    public void testCheckRestApiKeyIds_UseItem() {
        Item item = new AbstractItem(null, "testItem") {
            @Override
            public Collection<? extends Job> getAllJobs() {
                return null;
            }

            @Override
            public boolean hasPermission(Permission permission) {
                return USE_ITEM.equals(permission);
            }
        };
        when(mablStepDescriptor.doCheckRestApiKeyIds(item, "an-api-key")).thenCallRealMethod();
        assertEquals(FormValidation.ok(), mablStepDescriptor.doCheckRestApiKeyIds(item, "an-api-key"));
    }

    @Test
    public void testCheckRestApiKeyIds_InsufficentPermissions() {
        Item item = new AbstractItem(null, "testItem") {
            @Override
            public Collection<? extends Job> getAllJobs() {
                return null;
            }

            @Override
            public boolean hasPermission(Permission permission) {
                return false;
            }
        };
        when(mablStepDescriptor.doCheckRestApiKeyIds(item, "an-api-key")).thenCallRealMethod();
        assertNotEquals(FormValidation.ok(),
                mablStepDescriptor.doCheckRestApiKeyIds(item, "an-api-key"));
    }

    @Test
    public void testCheckRestApiKeyIds_ExpressionBasedCreds() {
        Item item = new AbstractItem(null, "testItem") {
            @Override
            public Collection<? extends Job> getAllJobs() {
                return null;
            }

            @Override
            public boolean hasPermission(Permission permission) {
                return true;
            }
        };
        when(mablStepDescriptor.doCheckRestApiKeyIds(item, "${invalidName}")).thenCallRealMethod();
        assertNotEquals(FormValidation.ok(),
                mablStepDescriptor.doCheckRestApiKeyIds(item, "${invalidName}"));

        when(mablStepDescriptor.doCheckRestApiKeyIds(item, "${couldBeValid")).thenCallRealMethod();
        assertEquals(FormValidation.ok(),
                mablStepDescriptor.doCheckRestApiKeyIds(item, "${couldBeValid"));
    }

    @Test
    public void testCheckMablBranch_Valid()
    {
        when(mablStepDescriptor.doCheckMablBranch(null)).thenCallRealMethod();
        assertEquals(FormValidation.ok(), mablStepDescriptor.doCheckMablBranch(null));

        when(mablStepDescriptor.doCheckMablBranch("")).thenCallRealMethod();
        assertEquals(FormValidation.ok(), mablStepDescriptor.doCheckMablBranch(""));

        when(mablStepDescriptor.doCheckMablBranch("A_Valid_Branch-Name")).thenCallRealMethod();
        assertEquals(FormValidation.ok(), mablStepDescriptor.doCheckMablBranch("A_Valid_Branch-Name"));
    }

    @Test
    public void testCheckMablBranch_InValid()
    {
        when(mablStepDescriptor.doCheckMablBranch("Br@nch1nv@l1d")).thenCallRealMethod();
        assertNotEquals(FormValidation.ok(), mablStepDescriptor.doCheckMablBranch("Br@nch1nv@l1d"));
    }

    @Test
    public void testDoFillApplicationIdItems_NoApiKey()
    {
        when(mablStepDescriptor.doFillApplicationIdItems(
                null, false, "https://api.mabl.com", "https://app.mabl.com", null, null)).thenCallRealMethod();
        ListBoxModel model = mablStepDescriptor.doFillApplicationIdItems(null, false, "https://api.mabl.com", "https://app.mabl.com", null, null);
        assertEquals(1, model.size());
        assertEquals("Select a valid API key", model.iterator().next().value);

        when(mablStepDescriptor.doFillApplicationIdItems(
                "", false, "https://api.mabl.com", "https://app.mabl.com", null, null)).thenCallRealMethod();
        model = mablStepDescriptor.doFillApplicationIdItems("", false, "https://api.mabl.com", "https://app.mabl.com", null, null);
        assertEquals(1, model.size());
        assertEquals("Select a valid API key", model.iterator().next().value);
    }

    @Test
    public void testDoFillApplicationIdItems_NoSecret()
    {
        when(mablStepDescriptor.doFillApplicationIdItems(
                "invalid-key", false, "https://api.mabl.com", "https://app.mabl.com", null, null)).thenCallRealMethod();

        try (MockedStatic<MablStepBuilder> mocked = mockStatic(MablStepBuilder.class)) {
            mocked.when(() -> MablStepBuilder.getRestApiSecret("invalid-key")).thenReturn(null);

            ListBoxModel model = mablStepDescriptor.doFillApplicationIdItems("invalid-key", false, "https://api.mabl.com", "https://app.mabl.com", null, null);
            assertEquals(0, model.size());
        }
    }

    @Test
    public void testDoFillEnvironmentIdItems_NoApiKey() {
        when(mablStepDescriptor.doFillEnvironmentIdItems(
                null, false, "https://api.mabl.com", "https://app.mabl.com", null, null)).thenCallRealMethod();
        ListBoxModel model = mablStepDescriptor.doFillEnvironmentIdItems(null, false, "https://api.mabl.com", "https://app.mabl.com", null, null);
        assertEquals(1, model.size());
        assertEquals("Select a valid API key", model.iterator().next().value);

        when(mablStepDescriptor.doFillEnvironmentIdItems(
                "", false, "https://api.mabl.com", "https://app.mabl.com", null, null)).thenCallRealMethod();
        model = mablStepDescriptor.doFillEnvironmentIdItems("", false, "https://api.mabl.com", "https://app.mabl.com", null, null);
        assertEquals(1, model.size());
        assertEquals("Select a valid API key", model.iterator().next().value);
    }

    @Test
    public void testDoFillEnvironmentIdItems_NoSecret()
    {
        when(mablStepDescriptor.doFillEnvironmentIdItems(
                "invalid-key", false, "https://api.mabl.com", "https://app.mabl.com", null, null)).thenCallRealMethod();

        try (MockedStatic<MablStepBuilder> mocked = mockStatic(MablStepBuilder.class)) {
            mocked.when(() -> MablStepBuilder.getRestApiSecret("invalid-key")).thenReturn(null);

            ListBoxModel model = mablStepDescriptor.doFillEnvironmentIdItems("invalid-key", false, "https://api.mabl.com", "https://app.mabl.com", null, null);
            assertEquals(0, model.size());
        }
    }

    @Test
    public void testProxyCredentialsIds_Blank() {
        Item item = new AbstractItem(null, "testItem") {
            @Override
            public Collection<? extends Job> getAllJobs() {
                return null;
            }

            @Override
            public boolean hasPermission(Permission permission) {
                return EXTENDED_READ.equals(permission);
            }
        };
        when(mablStepDescriptor.doCheckRestApiKeyIds(item, null)).thenCallRealMethod();
        assertNotEquals(FormValidation.ok(), mablStepDescriptor.doCheckRestApiKeyIds(item, null));
        when(mablStepDescriptor.doCheckRestApiKeyIds(item, "")).thenCallRealMethod();
        assertNotEquals(FormValidation.ok(), mablStepDescriptor.doCheckRestApiKeyIds(item, ""));
    }

    @Test
    public void testProxyCredentialsIds_ExtendedRead() {
        Item item = new AbstractItem(null, "testItem") {
            @Override
            public Collection<? extends Job> getAllJobs() {
                return null;
            }

            @Override
            public boolean hasPermission(Permission permission) {
                return EXTENDED_READ.equals(permission);
            }
        };
        when(mablStepDescriptor.doCheckProxyCredentialsIds(item, "an-api-key")).thenCallRealMethod();
        assertEquals(FormValidation.ok(), mablStepDescriptor.doCheckProxyCredentialsIds(item, "an-api-key"));
    }

    @Test
    public void testProxyCredentialsIds_UseItem() {
        Item item = new AbstractItem(null, "testItem") {
            @Override
            public Collection<? extends Job> getAllJobs() {
                return null;
            }

            @Override
            public boolean hasPermission(Permission permission) {
                return USE_ITEM.equals(permission);
            }
        };
        when(mablStepDescriptor.doCheckProxyCredentialsIds(item, "an-api-key")).thenCallRealMethod();
        assertEquals(FormValidation.ok(), mablStepDescriptor.doCheckProxyCredentialsIds(item, "an-api-key"));
    }

    @Test
    public void testProxyCredentialsIds_InsufficentPermissions() {
        Item item = new AbstractItem(null, "testItem") {
            @Override
            public Collection<? extends Job> getAllJobs() {
                return null;
            }

            @Override
            public boolean hasPermission(Permission permission) {
                return false;
            }
        };
        when(mablStepDescriptor.doCheckProxyCredentialsIds(item, "an-api-key")).thenCallRealMethod();
        assertNotEquals(FormValidation.ok(),
                mablStepDescriptor.doCheckProxyCredentialsIds(item, "an-api-key"));
    }

    @Test
    public void testProxyCredentialsIds_ExpressionBasedCreds() {
        Item item = new AbstractItem(null, "testItem") {
            @Override
            public Collection<? extends Job> getAllJobs() {
                return null;
            }

            @Override
            public boolean hasPermission(Permission permission) {
                return true;
            }
        };
        when(mablStepDescriptor.doCheckProxyCredentialsIds(item, "${invalidName}")).thenCallRealMethod();
        assertNotEquals(FormValidation.ok(),
                mablStepDescriptor.doCheckProxyCredentialsIds(item, "${invalidName}"));

        when(mablStepDescriptor.doCheckProxyCredentialsIds(item, "${couldBeValid")).thenCallRealMethod();
        assertEquals(FormValidation.ok(),
                mablStepDescriptor.doCheckProxyCredentialsIds(item, "${couldBeValid"));
    }

}
