package com.llmofang.hinge.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

public class HingeGradlePlugin implements Plugin<Project> {
    public void apply(Project project) {
        final def logger = project.getLogger()
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
}
