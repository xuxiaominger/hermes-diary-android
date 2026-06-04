# Default ProGuard rules
-keepattributes *Annotation*
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keep class com.hermesdiary.app.data.** { *; }
-dontwarn com.hermesdiary.app.**
