package com.llmofang.hinge

import org.gradle.api.Plugin
import org.gradle.api.Project

public abstract class HingeGradlePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        final def logger = project.getLogger
        logger.info("[llmofang] Hinge plugin loaded.")

        configure(project)
    }
    protected void configure(Project project) {
        project.task(type: NewRelicInstrumentTask, "newRelicInstrumentTask")
        project.task(type: NewRelicDeinstrumentTask, "newRelicDeinstrumentTask")

        android.applicationVariants.all { variant ->
            variant.dex.dependsOn("newRelicInstrumentTask")
            variant.dex.finalizedBy("newRelicDeinstrumentTask")
            logger.info("[llmofang] Added instrumentation tasks to " + variant.name + " variant.")
        }

        android.testVariants.all { variant ->
            variant.dex.dependsOn("newRelicInstrumentTask")
            variant.dex.finalizedBy("newRelicDeinstrumentTask")
            logger.info("[llmofang] Added instrumentation tasks to " + variant.name + " variant.")
        }
    }
}
