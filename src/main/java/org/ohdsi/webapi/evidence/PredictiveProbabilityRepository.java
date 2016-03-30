/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.evidence;

import java.util.Collection;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author asena5
 */
public interface PredictiveProbabilityRepository extends CrudRepository<PredictiveProbabilityRecord, Integer> {
  Collection<PredictiveProbabilityRecord> findAllByConceptSetId(int conceptSetId);
}
