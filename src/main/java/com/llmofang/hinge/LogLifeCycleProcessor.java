package com.llmofang.hinge;

import com.github.stephanenicolas.afterburner.AfterBurner;
import com.github.stephanenicolas.afterburner.exception.AfterBurnerImpossibleException;
import com.github.stephanenicolas.afterburner.inserts.InsertableMethod;
import com.github.stephanenicolas.afterburner.InsertableMethodBuilder;

import org.apache.http.impl.client.AbstractHttpClient;

import java.util.HashSet;
import java.util.Set;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.build.IClassTransformer;
import javassist.build.JavassistBuildException;
import javassist.bytecode.AccessFlag;
import lombok.extern.slf4j.Slf4j;

import static com.github.stephanenicolas.morpheus.commons.JavassistUtils.isActivity;
import static com.github.stephanenicolas.morpheus.commons.JavassistUtils.isApplication;
import static com.github.stephanenicolas.morpheus.commons.JavassistUtils.isBroadCastReceiver;
import static com.github.stephanenicolas.morpheus.commons.JavassistUtils.isContentProvider;
import static com.github.stephanenicolas.morpheus.commons.JavassistUtils.isFragment;
import static com.github.stephanenicolas.morpheus.commons.JavassistUtils.isService;
import static com.github.stephanenicolas.morpheus.commons.JavassistUtils.isSupportFragment;
import static com.github.stephanenicolas.morpheus.commons.JavassistUtils.isView;

/**
 * A class transformer to inject logging byte code for all life cycle methods.
 *
 * @author SNI
 */

public class LogLifeCycleProcessor implements IClassTransformer {

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogLifeCycleProcessor.class);
  private AfterBurner afterBurner = new AfterBurner();
  private boolean debug;

  public LogLifeCycleProcessor(boolean debug) {
    this.debug = debug;
  }

  @Override
//  public boolean shouldTransform(CtClass candidateClass) throws JavassistBuildException {
//    try {
//      //boolean isSupported = isSupported(candidateClass);
//      //return candidateClass.hasAnnotation(LogLifeCycle.class) && isSupported;
//      return candidateClass.getClass().equals(AbstractHttpClient.class);
//    } catch (Exception e) {
//      logMoreIfDebug("Should transform filter failed for class " + candidateClass.getName(), e);
//      throw new JavassistBuildException(e);
//    }
//  }


    public boolean shouldTransform(CtClass candidateClass) throws JavassistBuildException {
        try {
            boolean isSupported = isSupported(candidateClass);
            return candidateClass.hasAnnotation(LogLifeCycle.class) && isSupported;
        } catch (Exception e) {
            logMoreIfDebug("Should transform filter failed for class " + candidateClass.getName(), e);
            throw new JavassistBuildException(e);
        }
    }
  private boolean isSupported(CtClass candidateClass) throws NotFoundException {
    return isActivity(candidateClass)
        || isFragment(candidateClass)
        || isSupportFragment(candidateClass)
        || isView(candidateClass)
        || isService(candidateClass)
        || isBroadCastReceiver(candidateClass)
        || isContentProvider(candidateClass)
        || isApplication(candidateClass);
  }

  @Override
  public void applyTransformations(CtClass classToTransform) throws JavassistBuildException {

    /**
    InsertableMethod.Builder builder = new InsertableMethod.Builder( new AfterBurner() );

    CtClass classToInsertInto = ClassPool.getDefaultPool().get(A.class.getName());
    String targetMethod = "foo";
    String insertionAfterMethod = "bar";
    String fullMethod = "public void foo() { this.foo = 2; }";
    String body = "this.foo = 2;";
    builder.insertIntoClass(classToInsertInto)
           .inMethodIfExists(targetMethod)
           .afterACallTo(insertionAfterMethod)
           .withBody(body)
           .elseCreateMethodIfNotExists(fullMethod)
           .doIt();
    */


    String classToTransformName = classToTransform.getName();
    try {
      log.info("Transforming " + classToTransformName);
      ClassPool pool = classToTransform.getClassPool();
      Set<CtMethod> methodSet = getAllLifeCycleMethods(pool, classToTransform.getName());
      debugLifeCycleMethods(classToTransform, methodSet.toArray(new CtMethod[methodSet.size()]));
    } catch (Exception e) {
      logMoreIfDebug("Transformation failed for class " + classToTransformName, e);
      throw new JavassistBuildException(e);
    }
    log.info("Transformation successful for " + classToTransformName);
  }

//  @Override
//  public void applyTransformations(CtClass classToTransform) throws JavassistBuildException {
//        String classToTransformName = classToTransform.getName();
//        try {
//            log.info("Transforming " + classToTransformName);
//            ClassPool pool = classToTransform.getClassPool();
//            Set<CtMethod> methodSet = getAllHttpConnMethods(pool, classToTransform.getName());
//            debugHttpClientMethods(classToTransform, methodSet.toArray(new CtMethod[methodSet.size()]));
//        } catch (Exception e) {
//            logMoreIfDebug("Transformation failed for class " + classToTransformName, e);
//            throw new JavassistBuildException(e);
//        }
//        log.info("Transformation successful for " + classToTransformName);
//    }

  private Set<CtMethod> getAllLifeCycleMethods(ClassPool pool, String className)
      throws NotFoundException {
    Set<CtMethod> methodSet = new HashSet<CtMethod>();
    CtMethod[] inheritedMethods = pool.get(className).getMethods();
    CtMethod[] declaredMethods = pool.get(className).getDeclaredMethods();
    for (CtMethod method : inheritedMethods) {
      methodSet.add(method);
    }
    for (CtMethod method : declaredMethods) {
      methodSet.add(method);
    }
    return methodSet;
  }

//  private Set<CtMethod> getAllHttpConnMethods(ClassPool pool, String className)
//      throws NotFoundException {
//        Set<CtMethod> methodSet = new HashSet<CtMethod>();
//        CtMethod[] inheritedMethods = pool.get(className).getMethods();
//        CtMethod[] declaredMethods = pool.get(className).getDeclaredMethods();
//        for (CtMethod method : inheritedMethods) {
//            methodSet.add(method);
//        }
//        for (CtMethod method : declaredMethods) {
//            methodSet.add(method);
//        }
//        return methodSet;
//    }
//    private void debugHttpClientMethods(CtClass classToTransform, CtMethod[] methods)
//            throws CannotCompileException, AfterBurnerImpossibleException, NotFoundException {
//        for (CtMethod execMethod : methods) {
//            String methodName = execMethod.getName();
//            String className = classToTransform.getName();
//
//            int accessFlags = execMethod.getMethodInfo().getAccessFlags();
//            boolean isFinal = (accessFlags & AccessFlag.FINAL) == AccessFlag.FINAL;
//            boolean canOverride = !isFinal && (AccessFlag.isPublic(accessFlags)
//                    || AccessFlag.isProtected(accessFlags)
//                    || AccessFlag.isPackage(accessFlags));
//
//            if (canOverride && methodName.startsWith("exec")) {
//                log.info("Overriding " + methodName);
//                try {
//
//                    String body = "android.util.Log.d(\"httpxxx\", \""
//                            + className
//                            + " [\" + System.identityHashCode(this) + \"] \u27F3 "
//                            + methodName
//                            + "\");";
//                    afterBurner.beforeOverrideMethod(classToTransform, methodName, body);
//                    log.info("Override successful " + methodName);
//                } catch (Exception e) {
//                    logMoreIfDebug("Override didn't work ", e);
//                }
//            } else {
//                log.info(
//                        "Skipping " + methodName + ". Either it is final, private or doesn't start by 'exec...'");
//            }
//        }
//    }

  private void debugLifeCycleMethods(CtClass classToTransform, CtMethod[] methods)
      throws CannotCompileException, AfterBurnerImpossibleException, NotFoundException {
    for (CtMethod lifeCycleHook : methods) {
      String methodName = lifeCycleHook.getName();
      String className = classToTransform.getName();

      int accessFlags = lifeCycleHook.getMethodInfo().getAccessFlags();
      boolean isFinal = (accessFlags & AccessFlag.FINAL) == AccessFlag.FINAL;
      boolean canOverride = !isFinal && (AccessFlag.isPublic(accessFlags)
          || AccessFlag.isProtected(accessFlags)
          || AccessFlag.isPackage(accessFlags));

      if (canOverride && methodName.startsWith("on")) {
        log.info("Overriding " + methodName);
        try {

          String body = "android.util.Log.d(\"LogLifeCycle\", \""
              + className
              + " [\" + System.identityHashCode(this) + \"] \u27F3 "
              + methodName
              + "\");";
          afterBurner.afterOverrideMethod(classToTransform, methodName, body);
          log.info("Override successful " + methodName);
        } catch (Exception e) {
          logMoreIfDebug("Override didn't work ", e);
        }
      } else {
        log.info(
                "Skipping " + methodName + ". Either it is final, private or doesn't start by 'on...'");
      }
    }
  }

  private void logMoreIfDebug(String message, Exception e) {
    if (debug) {
      log.debug(message, e);
    } else {
      log.info(message);
    }
  }
}
