/*
 * Copyright (c) 2014-2018 T-Systems Multimedia Solutions GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.tsystems.mms.apm.performancesignature.ui;

import de.tsystems.mms.apm.performancesignature.dynatrace.model.ChartDashlet;
import de.tsystems.mms.apm.performancesignature.dynatrace.model.DashboardReport;
import de.tsystems.mms.apm.performancesignature.dynatrace.model.Measure;
import de.tsystems.mms.apm.performancesignature.dynatrace.model.Measurement;
import hudson.Plugin;
import hudson.init.Initializer;
import hudson.model.Run;

import static hudson.init.InitMilestone.PLUGINS_STARTED;

@SuppressWarnings({"unused", "deprecation"})
public class PerfSigUIPlugin extends Plugin {

    @Initializer(before = PLUGINS_STARTED)
    public static void addAliases() {
        // Moved in 2.2.0
        Run.XSTREAM2.addCompatibilityAlias("de.tsystems.mms.apm.performancesignature.dynatrace.model.ChartDashlet", ChartDashlet.class);
        Run.XSTREAM2.addCompatibilityAlias("de.tsystems.mms.apm.performancesignature.dynatrace.model.DashboardReport", DashboardReport.class);
        Run.XSTREAM2.addCompatibilityAlias("de.tsystems.mms.apm.performancesignature.dynatrace.model.Measure", Measure.class);
        Run.XSTREAM2.addCompatibilityAlias("de.tsystems.mms.apm.performancesignature.dynatrace.model.Measurement", Measurement.class);

        Run.XSTREAM2.addCompatibilityAlias("de.tsystems.mms.apm.performancesignature.PerfSigBuildAction", PerfSigBuildAction.class);
    }
}
