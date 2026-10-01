# Proguard rules for Focus Now
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
