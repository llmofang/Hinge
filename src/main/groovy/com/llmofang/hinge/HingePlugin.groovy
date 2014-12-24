package com.llmofang.hinge

import javassist.build.IClassTransformer;
import org.gradle.api.Project;

/**
 * @author SNI
 */
public class HingePlugin extends AbstractHingePlugin {

  @Override
  public IClassTransformer[] getTransformers(Project project) {
    return new LogLifeCycleProcessor(project.loglifecycle.debug);
  }

  @Override
  protected void configure(Project project) {
    project.dependencies {
      provided 'com.github.stephanenicolas.loglifecycle:loglifecycle-annotations:1.0.1'
    }
  }

  @Override
  protected Class getPluginExtension() {
    LogLifeCyclePluginExtension
  }

  @Override
  protected String getExtension() {
    "loglifecycle"
  }

  @Override
  public boolean skipVariant(def variant) {
    return variant.name.contains('release')
  }
}
