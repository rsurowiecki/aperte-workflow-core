package pl.net.bluesoft.lot.casemanagement.dao;

import pl.net.bluesoft.lot.casemanagement.model.CaseStage;

/**
 * Created by pkuciapski on 2014-04-22.
 */
public interface CaseStageDAO {
    CaseStage createStage(long caseId, long caseStateDefinitionId, String name);
}