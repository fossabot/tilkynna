/**
 * *************************************************
 * Copyright (c) 2019, Grindrod Bank Limited
 * License MIT: https://opensource.org/licenses/MIT
 * **************************************************
 */
package org.tilkynna.report.generate.model.db;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GeneratedReportEntityRepository extends JpaRepository<GeneratedReportEntity, UUID>, JpaSpecificationExecutor<GeneratedReportEntity> {

    /**
     * Finds awaiting GenerateReportEntity(report_request)'s that are in 'PENDING' status: Ordered By Priority, RetryCount, Requested date. <br/>
     * Such that the highest priority requests are dealt with first.
     * 
     * downloadable 'are essentially streamed reports for UI' therefore higher priority <br/>
     * highest retryCount first so that those already failed, can be enqueued first <br/>
     * and oldest requested_at so that earliest requests are dealt with first
     * 
     * @return GenerateReportEntity(report_request) of hightest priority to be enqueued for processing next
     */
    @Query(value = " SELECT *, CASE  d.downloadable  WHEN true THEN 1 END as priority " + // downloadable reports have higher priority (they are essentially streamed reports for UI)
            " FROM _reports.generated_report r " + //
            " JOIN _reports.destination d ON r.destination_id = d.destination_id " + // destinations gives us priority
            " WHERE cast(report_status AS varchar) = 'PENDING' " + //
            " ORDER BY priority, retry_count DESC, requested_at ASC " + " LIMIT 1 FOR UPDATE SKIP LOCKED", nativeQuery = true) // ensure locking row, and skip any that are locked already
    public GeneratedReportEntity findReportRequestsToEnqueue();

    /**
     * Finds GenerateReportEntity(report_request)'s that are in 'FAILED' status for more than x milliseconds: Ordered By Priority, RetryCount, Requested date. <br/>
     * Such that the highest priority requests are dealt with first.
     * 
     * downloadable 'are essentially streamed reports for UI' therefore higher priority <br/>
     * highest retryCount first so that those already failed, can be moved to PENDING again first <br/>
     * and oldest requested_at so that earliest requests are dealt with first
     * 
     * @param backOfIntervalInMilliseconds
     *            time in milliseconds to wait until next retry
     * @return GeneratedReportEntity(report_request) that is currently in FAILED status and needs to be retried
     */
    @Query(value = " SELECT *, CASE  d.downloadable  WHEN true THEN 1 END as priority " + // downloadable reports have higher priority (they are essentially streamed reports for UI)
            " FROM _reports.generated_report r " + //
            " JOIN _reports.destination d ON r.destination_id = d.destination_id " + // destinations gives us priority
            " WHERE cast(report_status AS varchar) = 'FAILED'  " + //
            " AND now() - cast(CONCAT(:backOfIntervalInMilliseconds, 'milliseconds') AS interval) >= generated_at" + // actually just WHERE now() - '120000 milliseconds'::interval >= generated_at in postgres SQL (but needing to get
                                                                                                                     // :backOfIntervalInMilliseconds as dynamic via hibernate into the query)
            " ORDER BY priority, retry_count DESC, requested_at ASC " + " LIMIT 1 FOR UPDATE SKIP LOCKED", nativeQuery = true) // ensure locking row, and skip any that are locked already
    public GeneratedReportEntity findFailedReportRequestsToRetry(@Param("backOfIntervalInMilliseconds") int backOfIntervalInMilliseconds);
}
