/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.guvnor.server.repository;

import org.drools.guvnor.client.common.AssetFormats;
import org.drools.repository.*;
import org.jbpm.compiler.xml.processes.RuleFlowMigrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;


/**
 * This class is used to migrate version 4 ruleflow assets in a version 4
 * or 5 drools repository into version 5 ruleflow assets.
 * IMPORTANT: the current code only performs the transformations if the
 * Drools system property drools.ruleflow.port is true, just as the
 * drools compiler only transforms version 4 ruleflows to 5 if this
 * property is set.
 * <p/>
 * If a ruleflow is migrated, it is checked in as new version so that
 * the previous version is preserved. The current code checks-in
 * the new version as the admin user with an appropriate comment indicating
 * that the flow has been migrated. Hover, the code could also be changed
 * to check-in each migrated ruleflow using the credentials of the last
 * person to check-in the ruleflow, however, in future there may be a
 * danger that the person who last checked in the file may have lost
 * privileges to check-in the file, so we use the admin user
 * for now.
 */
public class MigrateRepository {

    private static final Logger log = LoggerFactory.getLogger(MigrateRepository.class);


    /**
     * **********************************************************************
     * Returns true if the drools system property drools.ruleflow.port is true
     * indicating that ruleflow migration should be performed.
     *
     * @param repo
     * @return true if the drools system property drools.ruleflow.port is true
     *         indicating that ruleflow migration should be performed.
     * @throws RepositoryException **********************************************************************
     */
    public static boolean needsRuleflowMigration(RulesRepository repo) throws RepositoryException {
        String portRuleFlow = System.getProperty("drools.ruleflow.port", "false");
        return portRuleFlow.equalsIgnoreCase("true");
    }


    /**
     * **********************************************************************
     * Iterates through all the packages in the given repository
     * migrating all drools 4 .rfm and .rf ruleflow assets that need to be
     * migrated to 5.
     * Note that archived assets, and assets in snapshots are also migrated.
     *
     * @param repo
     * @throws RepositoryException **********************************************************************
     */
    public static void migrateRuleflows(RulesRepository repo) throws RepositoryException {
        log.debug("AUTO MIGRATION: Performing drools ruleflow migration...");

        ModuleIterator pkgs = repo.listModules();
        boolean performed = false;
        while (pkgs.hasNext()) {
            performed = true;
            ModuleItem pkg = pkgs.next();
            migrateRuleflows(pkg);

            String[] snaps = repo.listModuleSnapshots(pkg.getName());
            if (snaps != null) {
                for (String snap1 : snaps) {
                    ModuleItem snap = repo.loadModuleSnapshot(pkg.getName(), snap1);
                    migrateRuleflows(snap);
                }
            }
        }

        if (performed) {
            log.debug("AUTO MIGRATION: Drools rulesflow migration completed.");
        }
    }


    /**
     * **********************************************************************
     * migrate all ruleflows in the package, including archived ones.
     * The migrated ruleflow is checked in as a new version and previous
     * versions are not migrated.
     * NOTE that we always try to migrate if the drools.ruleflow.port
     * property is true, even if the repository has been migrated before.
     * This is needed as the drools.ruleflow.port property may have been
     * false when the repository was first migrated (i.e. the
     * HEADER_PROPERTY_NAME above may have been migrated, but not the
     * ruleflows).
     * Also, all snapshot packages are updated as well.
     *
     * @param pkg **********************************************************************
     */
    private static void migrateRuleflows(ModuleItem pkg) {
        String portRuleFlow = System.getProperty("drools.ruleflow.port", "false");
        if (portRuleFlow.equalsIgnoreCase("true")) {
            AssetItemIterator it = listAssetsByFormatIncludingArchived(pkg,
                    new String[]{AssetFormats.RULE_FLOW_RF});

            while (it.hasNext()) {
                AssetItem item = it.next();
                String rf = item.getContent();
                try {
                    if (RuleFlowMigrator.needToMigrateRFM(rf)) {
                        log.debug("Migrating v4 RFM to v5: " + item.getName());
                        rf = RuleFlowMigrator.portRFMToCurrentVersion(rf);
                        item.updateContent(rf);
                        item.checkin("Auto migration from ruleflow RFM version 4 to 5");
                    } else if (RuleFlowMigrator.needToMigrateRF(rf)) {
                        log.debug("Migrating v4 RF to v5: " + item.getName());
                        rf = RuleFlowMigrator.portRFToCurrentVersion(rf);
                        item.updateContent(rf);
                        item.checkin("Auto migration from ruleflow RF version 4 to 5");
                    }
                } catch (Exception ex) {
                    log.error("Ruleflow migration failed for item: "
                            + item.getName() + " due to " + ex);
                    ex.printStackTrace(System.out);
                }

            }
        }
    }


    /**
     * **********************************************************************
     * This will load an iterator for assets in the given package of the
     * given format type, including archived assets.
     *
     * @param pkg     The package to check
     * @param formats an array of the format types to find.
     * @return an iterator for assets of the given format type, including
     *         archived assets.
     *         **********************************************************************
     */
    private static AssetItemIterator listAssetsByFormatIncludingArchived(ModuleItem pkg,
                                                                         String[] formats) {
        if (formats.length == 1) {
            return pkg.queryAssets("drools:format='" + formats[0] + "'", true);
        } else {
            String predicate = " ( ";
            for (int i = 0; i < formats.length; i++) {
                predicate = predicate + "drools:format='" + formats[i] + "'";
                if (!(i == formats.length - 1)) {
                    predicate = predicate + " OR ";
                }
            }
            predicate = predicate + " ) ";
            return pkg.queryAssets(predicate, true);
        }
    }

}
