package com.mabl.integration.jenkins;

import hudson.model.AbstractItem;
import hudson.model.Item;
import hudson.model.Job;
import hudson.security.Permission;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Collection;

import static com.cloudbees.plugins.credentials.CredentialsProvider.USE_ITEM;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;

public class MablStepDescriptorTest {

    private MablStepBuilder.MablStepDescriptor mablStepDescriptor;

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Before
    public void setup() {
        mablStepDescriptor = mock(MablStepBuilder.MablStepDescriptor.class);
        doNothing().when(mablStepDescriptor).load();
        // create a dummy security realm
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
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

        when(mablStepDescriptor.doFillApplicationIdItems(
                null, false, item)).thenCallRealMethod();
        ListBoxModel model = mablStepDescriptor.doFillApplicationIdItems(null, false, item);
        assertEquals(1, model.size());
        assertEquals("Select a valid API key", model.iterator().next().value);

        when(mablStepDescriptor.doFillApplicationIdItems(
                "", false, item)).thenCallRealMethod();
        model = mablStepDescriptor.doFillApplicationIdItems("", false, item);
        assertEquals(1, model.size());
        assertEquals("Select a valid API key", model.iterator().next().value);
    }

    @Test
    public void testDoFillApplicationIdItems_NoSecret()
    {
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

        when(mablStepDescriptor.doFillApplicationIdItems(
                "invalid-key", false, item)).thenCallRealMethod();

        assertThrows(
            IllegalStateException.class,
            () -> mablStepDescriptor.doFillApplicationIdItems("invalid-key", false, item));
    }

    @Test
    public void testDoFillEnvironmentIdItems_NoApiKey() {
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

        when(mablStepDescriptor.doFillEnvironmentIdItems(
                null, false, item)).thenCallRealMethod();
        ListBoxModel model = mablStepDescriptor.doFillEnvironmentIdItems(null, false, item);
        assertEquals(1, model.size());
        assertEquals("Select a valid API key", model.iterator().next().value);

        when(mablStepDescriptor.doFillEnvironmentIdItems(
                "", false, item)).thenCallRealMethod();
        model = mablStepDescriptor.doFillEnvironmentIdItems("", false, item);
        assertEquals(1, model.size());
        assertEquals("Select a valid API key", model.iterator().next().value);
    }

    @Test
    public void testDoFillEnvironmentIdItems_NoSecret()
    {
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

        when(mablStepDescriptor.doFillEnvironmentIdItems(
                "invalid-key", false, item)).thenCallRealMethod();

        assertThrows(
            IllegalStateException.class,
            () -> mablStepDescriptor.doFillEnvironmentIdItems("invalid-key", false, item));
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

}
