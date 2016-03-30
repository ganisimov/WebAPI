package org.ohdsi.webapi.evidence;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.webapi.helper.ResourceHelper;
import org.ohdsi.webapi.service.EvidenceService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public class PredictiveProbabilityTasklet implements Tasklet {
    private static final Log log = LogFactory.getLog(PredictiveProbabilityTasklet.class);
    
    private final PredictiveProbability task;
       
    private final JdbcTemplate evidenceJdbcTemplate;
    
    private final JdbcTemplate ohdsiJdbcTemplate;
    
    private final TransactionTemplate transactionTemplate;
    
    //private final CohortResultsAnalysisRunner analysisRunner;
    
    public PredictiveProbabilityTasklet(PredictiveProbability task, final JdbcTemplate evidenceJdbcTemplate, final JdbcTemplate ohdsiJdbcTemplate,
        final TransactionTemplate transactionTemplate, 
        String sourceDialect) 
    {
        this.task = task;
        this.evidenceJdbcTemplate = evidenceJdbcTemplate;
        this.ohdsiJdbcTemplate = ohdsiJdbcTemplate;
        this.transactionTemplate = transactionTemplate;
        //this.analysisRunner = new CohortResultsAnalysisRunner(sourceDialect, visualizationDataRepository);
    }
    
    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
        boolean isValid = false;
        try {
            final int[] ret = this.transactionTemplate.execute(new TransactionCallback<int[]>() {
                
                @Override
                public int[] doInTransaction(final TransactionStatus status) {	
                    log.debug("entering tasklet");
                    String predictiveProbabilitySql = EvidenceService.getPredictiveProbabilitySql(task);
                    log.debug("predictive probability sql to execute: " + predictiveProbabilitySql);
                    //PredictiveProbabilityMapper<PredictiveProbabilityRecord>
                    final List<PredictiveProbabilityRecord> recs = PredictiveProbabilityTasklet.this.evidenceJdbcTemplate.query(predictiveProbabilitySql, new PredictiveProbabilityMapper());
                    
                    // Remove any results that exist for the concept set
                    String deleteSql = EvidenceService.getPredictiveProbabilityDeleteStatementSql(task);
                    Object[] params = { task.getConceptSetId() };
                    int[] types = { Types.INTEGER };
                    int rows = PredictiveProbabilityTasklet.this.ohdsiJdbcTemplate.update(deleteSql, params, types);
                    
                    log.debug("rows deleted: " + rows);
                    
                    // Insert the results
                    String insertSql = ResourceHelper.GetResourceAsString("/resources/evidence/sql/insertPredictiveProbability.sql");
                    return PredictiveProbabilityTasklet.this.ohdsiJdbcTemplate.batchUpdate(insertSql, new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i)
                            throws SQLException {

                            PredictiveProbabilityRecord ppr = recs.get(i);
                            ps.setInt(1, ppr.getConceptSetId());
                            ps.setString(2, ppr.getConceptSetName());
                            ps.setInt(3, ppr.getConceptId());
                            ps.setString(4, ppr.getConceptName());
                            ps.setString(5, ppr.getDomainId());
                            ps.setDouble(6, ppr.getMedlineCt());
                            ps.setDouble(7, ppr.getMedlineCase());
                            ps.setDouble(8, ppr.getMedlineOther());
                            ps.setDouble(9, ppr.getSemmeddbCtT());
                            ps.setDouble(10, ppr.getSemmeddbCaseT());
                            ps.setDouble(11, ppr.getSemmeddbOtherT());
                            ps.setDouble(12, ppr.getSemmeddbCtF());
                            ps.setDouble(13, ppr.getSemmeddbCaseF());
                            ps.setDouble(14, ppr.getSemmeddbOtherF());
                            ps.setDouble(15, ppr.getEu_spc());
                            ps.setDouble(16, ppr.getSplADR());
                            ps.setDouble(17, ppr.getAers());
                            ps.setDouble(18, ppr.getAersPRR());
                            ps.setDouble(19, ppr.getMedlineCtScaled());
                            ps.setDouble(20, ppr.getMedlineCaseScaled());
                            ps.setDouble(21, ppr.getMedlineOtherScaled());
                            ps.setDouble(22, ppr.getSemmeddbCtTScaled());
                            ps.setDouble(23, ppr.getSemmeddbCaseTScaled());
                            ps.setDouble(24, ppr.getSemmeddbOtherTScaled());
                            ps.setDouble(25, ppr.getSemmeddbCtFScaled());
                            ps.setDouble(26, ppr.getSemmeddbCaseFScaled());
                            ps.setDouble(27, ppr.getSemmeddbOtherFScaled());
                            ps.setDouble(28, ppr.getEuSPCScaled());
                            ps.setDouble(29, ppr.getSplADRScaled());
                            ps.setDouble(30, ppr.getAersScaled());
                            ps.setDouble(31, ppr.getAersPRRScaled());
                            ps.setDouble(32, ppr.getMedlineCtBeta());
                            ps.setDouble(33, ppr.getMedlineCaseBeta());
                            ps.setDouble(34, ppr.getMedlineOtherBeta());
                            ps.setDouble(35, ppr.getSemmeddbCtTBeta());
                            ps.setDouble(36, ppr.getSemmeddbCaseTBeta());
                            ps.setDouble(37, ppr.getSemmeddbOtherFBeta());
                            ps.setDouble(38, ppr.getSemmeddbCtFBeta());
                            ps.setDouble(39, ppr.getSemmeddbCaseFBeta());
                            ps.setDouble(40, ppr.getSemmeddbOtherFBeta());
                            ps.setDouble(41, ppr.getEuSPCBeta());
                            ps.setDouble(42, ppr.getSplADRBeta());
                            ps.setDouble(43, ppr.getAersBeta());
                            ps.setDouble(44, ppr.getAersPRRBeta());
                            ps.setDouble(45, ppr.getRawPrediction());
                            ps.setDouble(46, ppr.getPrediction());
                        }

                        @Override
                        public int getBatchSize() {
                            return recs.size();
                        }
                    });
                }
            });
            log.debug("Update count: " + ret.length);
            isValid = true;
            /*
            log.debug("warm up visualizations");
            final int count = this.analysisRunner.warmupData(evidenceJdbcTemplate, task);
            log.debug("warmed up " + count + " visualizations");
            */
        } catch (final TransactionException e) {
            log.error(e.getMessage(), e);
            throw e;//FAIL job status
        } finally {
            DefaultTransactionDefinition completeTx = new DefaultTransactionDefinition();
            completeTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            TransactionStatus completeStatus = this.transactionTemplate.getTransactionManager().getTransaction(completeTx);      
            //df = this.cohortDefinitionRepository.findOne(defId);
            //info = findBySourceId(df.getGenerationInfoList(), sourceId);
            Date endTime = Calendar.getInstance().getTime();
            //info.setExecutionDuration(new Integer((int)(endTime.getTime() - startTime.getTime())));
            //info.setIsValid(isValid);
            //info.setStatus(GenerationStatus.COMPLETE);
            //this.cohortDefinitionRepository.save(df);
            this.transactionTemplate.getTransactionManager().commit(completeStatus);            
        }
        return RepeatStatus.FINISHED;
    }    
}
