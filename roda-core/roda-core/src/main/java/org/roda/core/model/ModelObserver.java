/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.model;

import java.util.List;

import org.roda.core.data.v2.agents.Agent;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.OtherMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;

public interface ModelObserver {

  public void aipCreated(AIP aip);

  public void aipUpdated(AIP aip);

  public void aipActiveFlagUpdated(AIP aip);

  public void aipDeleted(String aipId);

  public void descriptiveMetadataCreated(DescriptiveMetadata descriptiveMetadataBinary);

  public void descriptiveMetadataUpdated(DescriptiveMetadata descriptiveMetadataBinary);

  public void descriptiveMetadataDeleted(String aipId, String descriptiveMetadataBinaryId);

  public void representationCreated(Representation representation);

  public void representationUpdated(Representation representation);

  public void representationDeleted(String aipId, String representationId);

  public void fileCreated(File file);

  public void fileUpdated(File file);

  public void fileDeleted(String aipId, String representationId, List<String> fileDirectoryPath, String fileId);

  public void logEntryCreated(LogEntry entry);

  public void userCreated(User user);

  public void userUpdated(User user);

  public void userDeleted(String userID);

  public void groupCreated(Group group);

  public void groupUpdated(Group group);

  public void groupDeleted(String groupID);

  public void preservationMetadataCreated(PreservationMetadata preservationMetadataBinary);

  public void preservationMetadataUpdated(PreservationMetadata preservationMetadataBinary);

  public void preservationMetadataDeleted(PreservationMetadata preservationMetadataBinary);

  public void otherMetadataCreated(OtherMetadata otherMetadataBinary);

  public void jobCreatedOrUpdated(Job job);

  public void jobDeleted(String jobId);

  public void jobReportCreatedOrUpdated(Report jobReport);

  public void jobReportDeleted(String jobReportId);

  public void aipPermissionsUpdated(AIP aip);

  public void transferredResourceDeleted(String transferredResourceID);

  public void riskCreatedOrUpdated(Risk risk);

  public void riskDeleted(String riskId);

  public void agentCreatedOrUpdated(Agent agent);

  public void agentDeleted(String agentId);

  public void formatCreatedOrUpdated(Format format);

  public void formatDeleted(String formatId);
}
