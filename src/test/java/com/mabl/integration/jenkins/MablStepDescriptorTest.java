package com.mabl.integration.jenkins;

import hudson.model.AbstractItem;
import hudson.model.Item;
import hudson.model.Job;
import hudson.security.Permission;
import hudson.util.FormValidation;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static com.cloudbees.plugins.credentials.CredentialsProvider.USE_ITEM;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;

public class MablStepDescriptorTest {

    private MablStepBuilder.MablStepDescriptor descriptor;

    @Before
    public void setup() {
        descriptor = mock(MablStepBuilder.MablStepDescriptor.class);
        doNothing().when(descriptor).load();
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
        when(descriptor.doCheckRestApiKeyIds(item, null)).thenCallRealMethod();
        assertNotEquals(FormValidation.ok(), descriptor.doCheckRestApiKeyIds(item, null));
        when(descriptor.doCheckRestApiKeyIds(item, "")).thenCallRealMethod();
        assertNotEquals(FormValidation.ok(), descriptor.doCheckRestApiKeyIds(item, ""));
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
        when(descriptor.doCheckRestApiKeyIds(item, "an-api-key")).thenCallRealMethod();
        assertEquals(FormValidation.ok(), descriptor.doCheckRestApiKeyIds(item, "an-api-key"));
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
        when(descriptor.doCheckRestApiKeyIds(item, "an-api-key")).thenCallRealMethod();
        assertEquals(FormValidation.ok(), descriptor.doCheckRestApiKeyIds(item, "an-api-key"));
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
        when(descriptor.doCheckRestApiKeyIds(item, "an-api-key")).thenCallRealMethod();
        assertNotEquals(FormValidation.ok(),
                descriptor.doCheckRestApiKeyIds(item, "an-api-key"));
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
        when(descriptor.doCheckRestApiKeyIds(item, "${invalidName}")).thenCallRealMethod();
        assertNotEquals(FormValidation.ok(),
                descriptor.doCheckRestApiKeyIds(item, "${invalidName}"));

        when(descriptor.doCheckRestApiKeyIds(item, "${couldBeValid")).thenCallRealMethod();
        assertEquals(FormValidation.ok(),
                descriptor.doCheckRestApiKeyIds(item, "${couldBeValid"));
    }

    @Test
    public void testCheckMablBranch_Valid()
    {
        when(descriptor.doCheckMablBranch(null)).thenCallRealMethod();
        assertEquals(FormValidation.ok(), descriptor.doCheckMablBranch(null));

        when(descriptor.doCheckMablBranch("")).thenCallRealMethod();
        assertEquals(FormValidation.ok(), descriptor.doCheckMablBranch(""));

        when(descriptor.doCheckMablBranch("A_Valid_Branch-Name")).thenCallRealMethod();
        assertEquals(FormValidation.ok(), descriptor.doCheckMablBranch("A_Valid_Branch-Name"));
    }

    @Test
    public void testCheckMablBranch_InValid()
    {
        when(descriptor.doCheckMablBranch("Br@nch1nv@l1d")).thenCallRealMethod();
        assertNotEquals(FormValidation.ok(), descriptor.doCheckMablBranch("Br@nch1nv@l1d"));
    }

}
