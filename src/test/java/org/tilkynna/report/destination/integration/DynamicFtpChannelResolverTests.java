/**
 * *************************************************
 * Copyright (c) 2019, Grindrod Bank Limited
 * License MIT: https://opensource.org/licenses/MIT
 * **************************************************
 */
package org.tilkynna.report.destination.integration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.tilkynna.report.destination.assembler.DestinationAssembler;
import org.tilkynna.report.destination.mockdata.SFTPDestinationMockDataGenerator;
import org.tilkynna.report.destination.model.db.SFTPDestinationEntity;

// based on https://github.com/spring-projects/spring-integration-samples/blob/master/advanced/dynamic-ftp/src/test/java/org/springframework/integration/samples/ftp/DynamicFtpChannelResolverTests.java
/**
 * DynamicFtpChannelResolverTests is a simple JUnit test to check that we can get a channel for a SFTPDestinationEntity. Does not check that getting same SFTPDestinationEntity return the same channel as no caching has been implemented in the
 * DynamicFtpChannelResolver. As ... user/password could be changed on DB.
 * 
 * @author melissap
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { DestinationAssembler.class })
public class DynamicFtpChannelResolverTests {

    private SFTPConfigSettings extractSftpConfig(SFTPDestinationEntity sftp) {
        SFTPConfigSettings sftpConfig = new SFTPConfigSettings();
        sftpConfig.setHost(sftp.getHost());
        sftpConfig.setPort(sftp.getPort());
        sftpConfig.setUsername(sftp.getUsername());
        sftpConfig.setPassword(sftp.getPassword());

        return sftpConfig;
    }

    /**
     * Test method for {@link org.tilkynna.report.destination.integration.DynamicFtpChannelResolver#resolve(SFTPDestinationEntity)}.
     */
    @Test
    public void testResolve() {
        SFTPDestinationEntity sftp1 = SFTPDestinationMockDataGenerator.setupSFTPDestinationEntity("SFTP_1");
        SFTPConfigSettings sftpConfig1 = extractSftpConfig(sftp1);
        SFTPDestinationEntity sftp2 = SFTPDestinationMockDataGenerator.setupSFTPDestinationEntity("SFTP_2");
        SFTPConfigSettings sftpConfig2 = extractSftpConfig(sftp2);

        DynamicFtpChannelResolver dynamicFtpChannelResolver = new DynamicFtpChannelResolver();

        MessageChannel channel1 = dynamicFtpChannelResolver.resolve(sftpConfig1);
        assertNotNull(channel1);
        MessageChannel channel2 = dynamicFtpChannelResolver.resolve(sftpConfig2);
        assertNotNull(channel2);

        assertNotSame("assertNotSame: ", channel1, channel2);

    }
}
