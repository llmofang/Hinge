-libraryjars <java.home>/lib/rt.jar
-libraryjars <java.home>/lib/jce.jar
-printusage shrinking.outpu
-dontobfuscate
-dontoptimize
-keepattributes *Annotation*,EnclosingMethod
-keep public class com.github.dreamhead.moco.bootstrap.Main {
           public static void main(java.lang.String[]);
}


-keepattributes *Annotation*,EnclosingMethod
-keep class modal.**{void set*(***);*** get*();}
-keepclassmembers class model.** {public <fields>;}
