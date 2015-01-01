package com.llmofang.hinge.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger;

public class HingeGradlePlugin implements Plugin<Project> {
    static Logger logger;
    public void apply(Project project) {
        logger = project.getLogger()
        logger.info("[llmofang] Hinge plugin loaded.")

        project.configure(project) {
            def instrumentTask = "hingeInstrumentTask"
            def deinstrumentTask = "hingeDeinstrumentTask"
            project.task(instrumentTask, type: HingeInstrumentTask)
            project.task(deinstrumentTask, type: HingeDeinstrumentTask)

            android.applicationVariants.all { variant ->
                variant.dex.dependsOn(instrumentTask)
                variant.dex.finalizedBy(deinstrumentTask)
                logger.info("[llmofang] Added instrumentation tasks to " + variant.name + " variant.")
            }

            android.testVariants.all { variant ->
                variant.dex.dependsOn(instrumentTask)
                variant.dex.finalizedBy(deinstrumentTask)
                logger.info("[llmofang] Added instrumentation tasks to " + variant.name + " variant.")
            }
        }
    }

    public static Logger getLogger() {
        return (Logger) logger;
    }

}
